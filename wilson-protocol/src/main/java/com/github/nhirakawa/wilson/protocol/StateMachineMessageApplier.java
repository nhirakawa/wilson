package com.github.nhirakawa.wilson.protocol;

import com.github.nhirakawa.wilson.models.AppendEntriesResult;
import com.github.nhirakawa.wilson.models.ClusterMember;
import com.github.nhirakawa.wilson.models.LeaderState;
import com.github.nhirakawa.wilson.models.messages.AppendEntriesRequest;
import com.github.nhirakawa.wilson.models.messages.AppendEntriesResponse;
import com.github.nhirakawa.wilson.models.messages.ElectionTimeoutMessage;
import com.github.nhirakawa.wilson.models.messages.HeartbeatTimeoutMessage;
import com.github.nhirakawa.wilson.models.messages.LeaderTimeoutMessage;
import com.github.nhirakawa.wilson.models.messages.VoteRequest;
import com.github.nhirakawa.wilson.models.messages.VoteResponse;
import com.github.nhirakawa.wilson.models.WilsonState;
import com.google.common.eventbus.EventBus;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;
import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StateMachineMessageApplier {
  private static final Logger LOG = LoggerFactory.getLogger(
    StateMachineMessageApplier.class
  );

  private final AtomicReference<WilsonState> wilsonStateReference;
  private final Set<ClusterMember> clusterMembers;
  private final ClusterMember localMember;
  private final EventBus eventBus;

  @Inject
  StateMachineMessageApplier(
    AtomicReference<WilsonState> wilsonStateReference,
    Set<ClusterMember> clusterMembers,
    @LocalMember ClusterMember localMember,
    EventBus eventBus
  ) {
    this.wilsonStateReference = wilsonStateReference;
    this.clusterMembers = clusterMembers;
    this.localMember = localMember;
    this.eventBus = eventBus;
  }

  public synchronized void apply(LeaderTimeoutMessage leaderTimeoutMessage) {
    WilsonState currentState = wilsonStateReference.get();
    if (currentState.getLeaderState() != LeaderState.FOLLOWER) {
      return;
    }

    WilsonState updatedWilsonState = wilsonStateReference.updateAndGet(
      wilsonState -> applyLeaderTimeout(wilsonState, leaderTimeoutMessage)
    );
    if (updatedWilsonState.getLeaderState() == LeaderState.CANDIDATE) {
      VoteRequest voteRequest = VoteRequest
        .builder()
        .setClusterMember(localMember)
        .setTerm(updatedWilsonState.getCurrentTerm())
        .setLastLogTerm(updatedWilsonState.getLastLogTerm())
        .setLastLogIndex(updatedWilsonState.getLastLogIndex())
        .build();

      LOG.trace("Requesting votes with {}", voteRequest);
      eventBus.post(voteRequest);
    }
  }

  private WilsonState applyLeaderTimeout(
    WilsonState wilsonState,
    LeaderTimeoutMessage leaderTimeoutMessage
  ) {
    // If we're not a follower, no-op
    if (wilsonState.getLeaderState() != LeaderState.FOLLOWER) {
      return wilsonState;
    }
    Instant leaderDeadline = wilsonState
      .getLastHeartbeatReceived()
      .plus(leaderTimeoutMessage.getLeaderTimeout(), ChronoUnit.MILLIS);

    // We received a heartbeat before the last deadline
    Instant messageTimestamp = leaderTimeoutMessage.getTimestamp();
    if (leaderDeadline.isAfter(messageTimestamp)) {
      return wilsonState;
    }

    LOG.debug(
      "Last heartbeat from leader received at {} but leader timeout at {}. Transitioning to candidate",
      leaderDeadline,
      messageTimestamp
    );

    // Leader has timed out, transition to candidate
    return WilsonState
      .builder()
      .from(wilsonState)
      .setCurrentTerm(wilsonState.getCurrentTerm() + 1L)
      .setLeaderState(LeaderState.CANDIDATE)
      .setLastElectionStarted(Instant.now())
      .setLastVotedFor(localMember)
      .addVotesReceivedFrom(localMember)
      .build();
  }

  public synchronized void apply(
    ElectionTimeoutMessage electionTimeoutMessage
  ) {
    WilsonState currentState = wilsonStateReference.get();
    if (currentState.getLeaderState() != LeaderState.CANDIDATE) {
      return;
    }

    wilsonStateReference.updateAndGet(
      wilsonState -> applyElectionTimeout(wilsonState, electionTimeoutMessage)
    );
  }

  private WilsonState applyElectionTimeout(
    WilsonState wilsonState,
    ElectionTimeoutMessage electionTimeoutMessage
  ) {
    Instant timeout = electionTimeoutMessage.getTimestamp();

    // we haven't seen an election
    if (!wilsonState.getLastElectionStarted().isPresent()) {
      return wilsonState;
    }

    Instant electionTimeoutInstant = wilsonState
      .getLastElectionStarted()
      .get()
      .plus(electionTimeoutMessage.getElectionTimeout(), ChronoUnit.MILLIS);

    // election has not timed out yet
    if (electionTimeoutInstant.isAfter(timeout)) {
      return wilsonState;
    }

    LOG.debug(
      "Election started {} but timeout occurred at {}. Transitioning back to follower.",
      electionTimeoutInstant,
      timeout
    );

    // transition back to follower
    return WilsonState
      .builder()
      .from(wilsonState)
      .setLeaderState(LeaderState.FOLLOWER)
      .setLastVotedFor(Optional.empty())
      .setLastElectionStarted(Optional.empty())
      .setVotesReceivedFrom(Collections.emptySet())
      .build();
  }

  public synchronized VoteResponse apply(VoteRequest voteRequest) {
    WilsonState updatedWilsonState = wilsonStateReference.updateAndGet(
      wilsonState -> applyVoteRequest(wilsonState, voteRequest)
    );
    boolean voteGranted = updatedWilsonState.getLastVotedFor().isPresent() &&
    updatedWilsonState
      .getLastVotedFor()
      .get()
      .equals(voteRequest.getClusterMember());
    return VoteResponse
      .builder()
      .setTerm(updatedWilsonState.getCurrentTerm())
      .setVoteGranted(voteGranted)
      .build();
  }

  private WilsonState applyVoteRequest(
    WilsonState wilsonState,
    VoteRequest voteRequest
  ) {
    if (voteRequest.getTerm() < wilsonState.getCurrentTerm()) {
      LOG.debug(
        "Term on vote request ({}) is less than current term ({})",
        voteRequest.getTerm(),
        wilsonState.getCurrentTerm()
      );
      return wilsonState;
    }

    if (voteRequest.getTerm() > wilsonState.getCurrentTerm()) {
      LOG.debug(
        "Term on vote request ({}) is later than current term ({}). Transitioning to follower.",
        voteRequest.getTerm(),
        wilsonState.getCurrentTerm()
      );
      return WilsonState
        .builder()
        .from(wilsonState)
        .setCurrentTerm(voteRequest.getTerm())
        .setLeaderState(LeaderState.FOLLOWER)
        .setLastVotedFor(voteRequest.getClusterMember())
        .setVotesReceivedFrom(Collections.emptySet())
        .build();
    }

    if (
      wilsonState.getLastVotedFor().isPresent() &&
      voteRequest.getTerm() <= wilsonState.getCurrentTerm()
    ) {
      LOG.debug("Already voted for {}", wilsonState.getLastVotedFor().get());
      return wilsonState;
    }

    if (voteRequest.getLastLogTerm() < wilsonState.getLastLogTerm()) {
      LOG.debug(
        "Last log term on vote request ({}) is less than current last log term ({})",
        voteRequest.getLastLogTerm(),
        wilsonState.getLastLogTerm()
      );
      return wilsonState;
    }

    if (voteRequest.getLastLogIndex() < wilsonState.getLastLogIndex()) {
      LOG.debug(
        "Last log index on vote request ({}) is less than current last log index ({})",
        voteRequest.getLastLogIndex(),
        wilsonState.getLastLogIndex()
      );
      return wilsonState;
    }

    LOG.debug("Voting for {}", voteRequest.getClusterMember());
    return wilsonState
      .withCurrentTerm(voteRequest.getTerm())
      .withLastVotedFor(voteRequest.getClusterMember());
  }

  public synchronized void apply(
    VoteResponse voteResponse,
    ClusterMember clusterMember
  ) {
    LOG.debug(
      "Received VoteResponse {} from cluster member {}",
      voteResponse,
      clusterMember
    );
    wilsonStateReference.updateAndGet(
      wilsonState -> applyVoteResponse(wilsonState, voteResponse, clusterMember)
    );
  }

  private WilsonState applyVoteResponse(
    WilsonState wilsonState,
    VoteResponse voteResponse,
    ClusterMember clusterMember
  ) {
    if (!voteResponse.isVoteGranted()) {
      return wilsonState;
    }

    if (wilsonState.getLeaderState() != LeaderState.CANDIDATE) {
      return wilsonState;
    }

    WilsonState updatedWilsonState = WilsonState
      .builder()
      .from(wilsonState)
      .addVotesReceivedFrom(clusterMember)
      .build();

    if (hasQuorum(updatedWilsonState)) {
      LOG.debug("Quorum achieved. Transitioning to leader,");
      updatedWilsonState =
        WilsonState
          .builder()
          .from(updatedWilsonState)
          .setLeaderState(LeaderState.LEADER)
          .setCurrentLeader(localMember)
          .setLastVotedFor(Optional.empty())
          .setVotesReceivedFrom(Collections.emptyList())
          .setLastElectionStarted(Optional.empty())
          .build();
    }

    return updatedWilsonState;
  }

  private boolean hasQuorum(WilsonState immutableWilsonState) {
    int requiredVotesForQuorum = (clusterMembers.size() / 2) + 1;
    return (
      immutableWilsonState.getVotesReceivedFrom().size() >=
      requiredVotesForQuorum
    );
  }

  public synchronized void apply(
    HeartbeatTimeoutMessage heartbeatTimeoutMessage
  ) {
    WilsonState wilsonState = wilsonStateReference.get();

    if (wilsonState.getLeaderState() != LeaderState.LEADER) {
      return;
    }

    LOG.debug("Broadcasting heartbeat");
    AppendEntriesRequest appendEntriesRequest = AppendEntriesRequest
      .builder()
      .setLeader(localMember)
      .setTerm(wilsonState.getCurrentTerm())
      .setLastLogIndex(wilsonState.getLastLogIndex())
      .setLastLogTerm(wilsonState.getLastLogTerm())
      .setLeaderCommitIndex(wilsonState.getCommitIndex())
      .build();

    eventBus.post(appendEntriesRequest);
  }

  public synchronized AppendEntriesResponse apply(
    AppendEntriesRequest appendEntriesRequest
  ) {
    WilsonState updatedWilsonState = wilsonStateReference.updateAndGet(
      wilsonState -> applyAppendEntriesRequest(
        wilsonState,
        appendEntriesRequest
      )
    );
    return AppendEntriesResponse
      .builder()
      .setTerm(updatedWilsonState.getCurrentTerm())
      .setResult(AppendEntriesResult.SUCCESS)
      .build();
  }

  private WilsonState applyAppendEntriesRequest(
    WilsonState wilsonState,
    AppendEntriesRequest appendEntriesRequest
  ) {
    if (appendEntriesRequest.getEntries().isEmpty()) {
      return wilsonState.withLastHeartbeatReceived(Instant.now());
    } else {
      throw new UnsupportedOperationException();
    }
  }
}
