package com.github.nhirakawa.server.raft;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.nhirakawa.server.config.ClusterMember;
import com.github.nhirakawa.server.config.Configuration;
import com.github.nhirakawa.server.guice.LocalMember;
import com.github.nhirakawa.wilson.models.messages.ElectionTimeoutMessage;
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

  public synchronized void apply(LeaderTimeoutMessage leaderTimeoutMessage) throws InterruptedException, MalformedURLException, URISyntaxException, JsonProcessingException, ExecutionException {
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

      eventBus.post(voteRequest);
    }
  }

  private ImmutableWilsonState applyLeaderTimeout(ImmutableWilsonState wilsonState,
                                                  LeaderTimeoutMessage leaderTimeoutMessage) {
    // If we're not a follower, no-op
    if (wilsonState.getLeaderState() != LeaderState.FOLLOWER) {
      return wilsonState;
    }
    Instant leaderDeadline = getLeaderTimeoutInstant(wilsonState);

    // We received a heartbeat before the last deadline
    if (leaderDeadline.isAfter(leaderTimeoutMessage.getTimestamp())) {
      return wilsonState;
    }

    // Leader has timed out, transition to candidate
    return wilsonState
        .withCurrentTerm(wilsonState.getCurrentTerm() + 1L)
        .withLeaderState(LeaderState.CANDIDATE)
        .withLastVotedFor(localMember)
        .withVotesReceivedFrom(localMember);
  }

  private Instant getLeaderTimeoutInstant(WilsonState wilsonState) {
    return wilsonState.getLastHeartbeatReceived().plus(configuration.getLeaderTimeout(), ChronoUnit.MILLIS);
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

    Instant electionTimeoutInstant = getElectionTimeoutInstant(wilsonState.getLastElectionStarted().get(), configuration.getElectionTimeout());

    // election has not timed out yet
    if (electionTimeoutInstant.isAfter(timeout)) {
      return wilsonState;
    }

    // transition back to follower
    return wilsonState
        .withLeaderState(LeaderState.FOLLOWER)
        .withLastVotedFor(Optional.empty())
        .withLastElectionStarted(Optional.empty());
  }

  private Instant getElectionTimeoutInstant(Instant lastElection,
                                            long electionTimeout) {
    return lastElection.plus(electionTimeout, ChronoUnit.MILLIS);
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
      return wilsonState;
    }

    if (wilsonState.getLastVotedFor().isPresent()) {
      return wilsonState;
    }

    if (voteRequest.getLastLogTerm() < wilsonState.getLastLogTerm()) {
      return wilsonState;
    }

    if (voteRequest.getLastLogIndex() < wilsonState.getLastLogIndex()) {
      return wilsonState;
    }

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
      updatedWilsonState = updatedWilsonState.withLeaderState(LeaderState.LEADER)
          .withLastVotedFor(Optional.empty())
          .withVotesReceivedFrom()
          .withLastElectionStarted(Optional.empty());
    }

    return updatedWilsonState;
  }

  private boolean hasQuorum(ImmutableWilsonState immutableWilsonState) {
    int requiredVotesForQuorum = (configuration.getClusterMembers().size() / 2) + 1;
    return immutableWilsonState.getVotesReceivedFrom().size() >= requiredVotesForQuorum;
  }

}
