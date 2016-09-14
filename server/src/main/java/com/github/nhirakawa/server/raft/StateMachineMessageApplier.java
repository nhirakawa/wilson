package com.github.nhirakawa.server.raft;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nhirakawa.server.config.ClusterMember;
import com.github.nhirakawa.server.config.Configuration;
import com.github.nhirakawa.server.guice.LocalMember;
import com.github.nhirakawa.wilson.models.messages.ElectionTimeoutMessage;
import com.github.nhirakawa.wilson.models.messages.ImmutableHeartbeatRequest;
import com.github.nhirakawa.wilson.models.messages.ImmutableHeartbeatTimeoutMessage;
import com.github.nhirakawa.wilson.models.messages.ImmutableVoteRequest;
import com.github.nhirakawa.wilson.models.messages.ImmutableVoteResponse;
import com.github.nhirakawa.wilson.models.messages.LeaderTimeoutMessage;
import com.github.nhirakawa.wilson.models.messages.VoteRequest;
import com.github.nhirakawa.wilson.models.messages.VoteResponse;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

public class StateMachineMessageApplier {

  private static final Logger LOG = LoggerFactory.getLogger(StateMachineMessageApplier.class);

  private final AtomicReference<ImmutableWilsonState> wilsonStateReference;
  private final Configuration configuration;
  private final ClusterMember localMember;
  private final EventBus eventBus;

  @Inject
  StateMachineMessageApplier(AtomicReference<ImmutableWilsonState> wilsonStateReference,
                             Configuration configuration,
                             @LocalMember ClusterMember localMember,
                             EventBus eventBus) {
    this.wilsonStateReference = wilsonStateReference;
    this.configuration = configuration;
    this.localMember = localMember;
    this.eventBus = eventBus;
  }

  public synchronized void apply(LeaderTimeoutMessage leaderTimeoutMessage) {
    WilsonState currentState = wilsonStateReference.get();
    if (currentState.getLeaderState() != LeaderState.FOLLOWER) {
      return;
    }

    WilsonState updatedWilsonState = wilsonStateReference.updateAndGet(wilsonState -> applyLeaderTimeout(wilsonState, leaderTimeoutMessage));
    if (updatedWilsonState.getLeaderState() == LeaderState.CANDIDATE) {
      VoteRequest voteRequest = ImmutableVoteRequest.builder()
          .setTerm(updatedWilsonState.getCurrentTerm())
          .setLastLogTerm(updatedWilsonState.getLastLogTerm())
          .setLastLogIndex(updatedWilsonState.getLastLogIndex())
          .build();

      LOG.trace("Requesting votes with {}", voteRequest);
      eventBus.post(voteRequest);
    }
  }

  private ImmutableWilsonState applyLeaderTimeout(ImmutableWilsonState wilsonState,
                                                  LeaderTimeoutMessage leaderTimeoutMessage) {
    // If we're not a follower, no-op
    if (wilsonState.getLeaderState() != LeaderState.FOLLOWER) {
      return wilsonState;
    }
    Instant leaderDeadline = wilsonState.getLastHeartbeatReceived().plus(leaderTimeoutMessage.getLeaderTimeout(), ChronoUnit.MILLIS);

    // We received a heartbeat before the last deadline
    Instant messageTimestamp = leaderTimeoutMessage.getTimestamp();
    if (leaderDeadline.isAfter(messageTimestamp)) {
      return wilsonState;
    }

    LOG.debug("Last heartbeat from leader received at {} but leader timeout at {}. Transitioning to candidate", leaderDeadline, messageTimestamp);

    // Leader has timed out, transition to candidate
    return ImmutableWilsonState.builder()
        .from(wilsonState)
        .setCurrentTerm(wilsonState.getCurrentTerm() + 1L)
        .setLeaderState(LeaderState.CANDIDATE)
        .setLastElectionStarted(Instant.now())
        .setLastVotedFor(localMember)
        .addVotesReceivedFrom(localMember)
        .build();
  }

  public synchronized void apply(ElectionTimeoutMessage electionTimeoutMessage) {
    WilsonState currentState = wilsonStateReference.get();
    if (currentState.getLeaderState() != LeaderState.CANDIDATE) {
      return;
    }

    wilsonStateReference.updateAndGet(wilsonState -> applyElectionTimeout(wilsonState, electionTimeoutMessage));
  }

  private ImmutableWilsonState applyElectionTimeout(ImmutableWilsonState wilsonState,
                                                    ElectionTimeoutMessage electionTimeoutMessage) {
    Instant timeout = electionTimeoutMessage.getTimestamp();

    // we haven't seen an election
    if (!wilsonState.getLastElectionStarted().isPresent()) {
      return wilsonState;
    }

    Instant electionTimeoutInstant = wilsonState.getLastElectionStarted().get().plus(electionTimeoutMessage.getElectionTimeout(), ChronoUnit.MILLIS);

    // election has not timed out yet
    if (electionTimeoutInstant.isAfter(timeout)) {
      return wilsonState;
    }

    LOG.debug("Election started {} but timeout occurred at {}. Transitioning back to follower.", electionTimeoutInstant, timeout);

    // transition back to follower
    return wilsonState
        .withLeaderState(LeaderState.FOLLOWER)
        .withLastVotedFor(Optional.empty())
        .withLastElectionStarted(Optional.empty());
  }

  public synchronized VoteResponse apply(VoteRequest voteRequest,
                                         ClusterMember clusterMember) {
    WilsonState updatedWilsonState = wilsonStateReference.updateAndGet(wilsonState -> applyVoteRequest(wilsonState, voteRequest, clusterMember));
    boolean voteGranted = updatedWilsonState.getLastVotedFor().isPresent() && updatedWilsonState.getLastVotedFor().get().equals(clusterMember);
    return ImmutableVoteResponse.builder()
        .setTerm(updatedWilsonState.getCurrentTerm())
        .setVoteGranted(voteGranted)
        .build();
  }

  private ImmutableWilsonState applyVoteRequest(ImmutableWilsonState wilsonState,
                                                VoteRequest voteRequest,
                                                ClusterMember clusterMember) {
    if (voteRequest.getTerm() < wilsonState.getCurrentTerm()) {
      LOG.debug("Term on vote request ({}) is less than current term ({})", voteRequest.getTerm(), wilsonState.getCurrentTerm());
      return wilsonState;
    }

    if (voteRequest.getTerm() > wilsonState.getCurrentTerm()) {
      LOG.debug("Term on vote request ({}) is later than current term ({}). Transitioning to follower.", voteRequest.getTerm(), wilsonState.getCurrentTerm());
      return wilsonState
          .withCurrentTerm(voteRequest.getTerm())
          .withLeaderState(LeaderState.FOLLOWER)
          .withLastVotedFor(clusterMember);
    }

    if (wilsonState.getLastVotedFor().isPresent() && voteRequest.getTerm() <= wilsonState.getCurrentTerm()) {
      LOG.debug("Already voted for {}", wilsonState.getLastVotedFor().get());
      return wilsonState;
    }

    if (voteRequest.getLastLogTerm() < wilsonState.getLastLogTerm()) {
      LOG.debug("Last log term on vote request ({}) is less than current last log term ({})", voteRequest.getLastLogTerm(), wilsonState.getLastLogTerm());
      return wilsonState;
    }

    if (voteRequest.getLastLogIndex() < wilsonState.getLastLogIndex()) {
      LOG.debug("Last log index on vote request ({}) is less than current last log index ({})", voteRequest.getLastLogIndex(), wilsonState.getLastLogIndex());
      return wilsonState;
    }

    LOG.debug("Voting for {}", clusterMember);
    return wilsonState
        .withCurrentTerm(voteRequest.getTerm())
        .withLastVotedFor(clusterMember);
  }

  public synchronized void apply(VoteResponse voteResponse,
                                 ClusterMember clusterMember) {
    LOG.debug("Received VoteResponse {} from cluster member {}", voteResponse, clusterMember);
    wilsonStateReference.updateAndGet(wilsonState -> applyVoteResponse(wilsonState, voteResponse, clusterMember));
  }

  private ImmutableWilsonState applyVoteResponse(ImmutableWilsonState wilsonState,
                                                 VoteResponse voteResponse,
                                                 ClusterMember clusterMember) {
    if (!voteResponse.isVoteGranted()) {
      return wilsonState;
    }

    ImmutableWilsonState updatedWilsonState = ImmutableWilsonState.builder()
        .from(wilsonState)
        .addVotesReceivedFrom(clusterMember)
        .build();

    if (hasQuorum(updatedWilsonState)) {
      LOG.debug("Quorum achieved. Transitioning to leader,");
      updatedWilsonState = ImmutableWilsonState.builder()
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

  private boolean hasQuorum(ImmutableWilsonState immutableWilsonState) {
    int requiredVotesForQuorum = (configuration.getClusterMembers().size() / 2) + 1;
    return immutableWilsonState.getVotesReceivedFrom().size() >= requiredVotesForQuorum;
  }

  public synchronized void apply(ImmutableHeartbeatTimeoutMessage heartbeatTimeoutMessage) {
    WilsonState wilsonState = wilsonStateReference.get();

    if (wilsonState.getLeaderState() != LeaderState.LEADER) {
      return;
    }

    LOG.debug("Broadcasting heartbeat");
    eventBus.post(ImmutableHeartbeatRequest.builder().build());
  }

  public synchronized void apply(ImmutableHeartbeatRequest heartbeatRequest) {
    wilsonStateReference.getAndUpdate(wilsonState -> wilsonState.withLastHeartbeatReceived(Instant.now()));
  }

}
