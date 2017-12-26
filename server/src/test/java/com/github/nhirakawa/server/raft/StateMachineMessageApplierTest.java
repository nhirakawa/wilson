package com.github.nhirakawa.server.raft;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.nhirakawa.server.config.Configuration;
import com.github.nhirakawa.server.config.ImmutableClusterMember;
import com.github.nhirakawa.server.config.ImmutableConfiguration;
import com.github.nhirakawa.wilson.models.messages.ElectionTimeoutMessage;
import com.github.nhirakawa.wilson.models.messages.ImmutableElectionTimeoutMessage;
import com.github.nhirakawa.wilson.models.messages.ImmutableLeaderTimeoutMessage;
import com.github.nhirakawa.wilson.models.messages.ImmutableVoteRequest;
import com.github.nhirakawa.wilson.models.messages.ImmutableVoteResponse;
import com.github.nhirakawa.wilson.models.messages.LeaderTimeoutMessage;
import com.github.nhirakawa.wilson.models.messages.VoteRequest;
import com.github.nhirakawa.wilson.models.messages.VoteResponse;
import com.google.common.eventbus.EventBus;

@RunWith(MockitoJUnitRunner.class)
public class StateMachineMessageApplierTest {

  private static final ImmutableClusterMember LOCAL_SERVER = ImmutableClusterMember.builder()
      .setHost("host")
      .setPort(80)
      .build();
  private static final ImmutableClusterMember OTHER_SERVER = ImmutableClusterMember.builder()
      .setHost("other-host")
      .setPort(80)
      .build();

  @Mock
  private EventBus eventBus;
  @Captor
  private ArgumentCaptor<VoteRequest> voteRequestCaptor;

  @Test
  public void itDoesNothingWhenLeaderTimeoutAndLeaderIsValid() throws Exception {
    ImmutableConfiguration configuration = ImmutableConfiguration.builder()
        .setLocalMember(LOCAL_SERVER)
        .setLeaderTimeout(5L)
        .build();
    AtomicReference<ImmutableWilsonState> wilsonStateReference = new AtomicReference<>(
        ImmutableWilsonState.builder()
            .setLastHeartbeatReceived(Instant.ofEpochMilli(100L))
            .build()
    );

    StateMachineMessageApplier stateMachineMessageApplier = buildMessageApplier(configuration, wilsonStateReference);

    LeaderTimeoutMessage leaderTimeoutMessage = ImmutableLeaderTimeoutMessage.builder()
        .setTimestamp(Instant.ofEpochMilli(101L))
        .build();

    stateMachineMessageApplier.apply(leaderTimeoutMessage);

    WilsonState wilsonState = wilsonStateReference.get();
    assertThat(wilsonState.getCurrentTerm()).isEqualTo(1L);
    assertThat(wilsonState.getLeaderState()).isEqualTo(LeaderState.FOLLOWER);
    assertThat(wilsonState.getLastVotedFor()).isNotPresent();
  }

  @Test
  public void itTransitionsToCandidateWhenLeaderTimeoutAndLeaderIsInvalid() throws Exception {
    ImmutableConfiguration configuration = ImmutableConfiguration.builder()
        .setLocalMember(LOCAL_SERVER)
        .setLeaderTimeout(5L)
        .build();
    AtomicReference<ImmutableWilsonState> wilsonStateReference = new AtomicReference<>(
        ImmutableWilsonState.builder()
            .setCurrentTerm(5L)
            .addLog(
                ImmutableLogItem.builder()
                    .setTerm(4L)
                    .setIndex(3L)
                    .build()
            )
            .setLastHeartbeatReceived(Instant.ofEpochMilli(100L))
            .build()
    );

    StateMachineMessageApplier stateMachineMessageApplier = buildMessageApplier(configuration, wilsonStateReference);

    LeaderTimeoutMessage leaderTimeoutMessage = ImmutableLeaderTimeoutMessage.builder()
        .setTimestamp(Instant.ofEpochMilli(120L))
        .build();
    stateMachineMessageApplier.apply(leaderTimeoutMessage);

    WilsonState wilsonState = wilsonStateReference.get();
    assertThat(wilsonState.getCurrentTerm()).isEqualTo(6L);
    assertThat(wilsonState.getLastVotedFor()).isPresent().contains(LOCAL_SERVER);
    assertThat(wilsonState.getVotesReceivedFrom()).containsExactly(LOCAL_SERVER);
    assertThat(wilsonState.getLeaderState()).isEqualTo(LeaderState.CANDIDATE);

    verify(eventBus).post(voteRequestCaptor.capture());
    VoteRequest expectedVoteRequest = ImmutableVoteRequest.builder()
        .setTerm(6L)
        .setLastLogTerm(4L)
        .setLastLogIndex(3L)
        .build();
    assertThat(voteRequestCaptor.getAllValues()).containsExactly(expectedVoteRequest);
  }

  @Test
  public void itDoesNothingWhenLeaderTimeoutWhileCandidate() throws InterruptedException, MalformedURLException, JsonProcessingException, URISyntaxException, ExecutionException {
    ImmutableConfiguration configuration = ImmutableConfiguration.builder()
        .setLocalMember(LOCAL_SERVER)
        .setLeaderTimeout(5L)
        .build();
    ImmutableWilsonState wilsonState = ImmutableWilsonState.builder()
        .setLeaderState(LeaderState.CANDIDATE)
        .setLastHeartbeatReceived(Instant.ofEpochMilli(10L))
        .build();
    AtomicReference<ImmutableWilsonState> wilsonStateReference = new AtomicReference<>(wilsonState);

    StateMachineMessageApplier stateMachineMessageApplier = buildMessageApplier(configuration, wilsonStateReference);

    LeaderTimeoutMessage leaderTimeoutMessage = ImmutableLeaderTimeoutMessage.builder()
        .setTimestamp(Instant.ofEpochMilli(100L))
        .build();
    stateMachineMessageApplier.apply(leaderTimeoutMessage);

    WilsonState updatedWilsonState = wilsonStateReference.get();
    assertThat(updatedWilsonState).isEqualTo(wilsonState);
  }

  @Test
  public void itDoesNothingWhenLeaderTimeoutWhileLeader() throws InterruptedException, MalformedURLException, JsonProcessingException, URISyntaxException, ExecutionException {
    ImmutableConfiguration configuration = ImmutableConfiguration.builder()
        .setLocalMember(LOCAL_SERVER)
        .setLeaderTimeout(5L)
        .build();
    ImmutableWilsonState wilsonState = ImmutableWilsonState.builder()
        .setLastHeartbeatReceived(Instant.ofEpochMilli(10L))
        .setLeaderState(LeaderState.LEADER)
        .build();

    AtomicReference<ImmutableWilsonState> wilsonStateReference = new AtomicReference<>(wilsonState);
    StateMachineMessageApplier stateMachineMessageApplier = buildMessageApplier(configuration, wilsonStateReference);
    LeaderTimeoutMessage leaderTimeoutMessage = ImmutableLeaderTimeoutMessage.builder()
        .setTimestamp(Instant.ofEpochMilli(100L))
        .build();

    stateMachineMessageApplier.apply(leaderTimeoutMessage);

    WilsonState updatedWilsonState = wilsonStateReference.get();
    assertThat(updatedWilsonState).isEqualTo(wilsonState);
  }

  @Test
  public void itNoOpsIfVoteHasAlreadyBeenCast() {
    ImmutableConfiguration configuration = ImmutableConfiguration.builder()
        .setLocalMember(LOCAL_SERVER)
        .setLeaderTimeout(5L)
        .build();
    ImmutableWilsonState wilsonState = ImmutableWilsonState.builder()
        .setLeaderState(LeaderState.CANDIDATE)
        .setCurrentTerm(1L)
        .setLastVotedFor(LOCAL_SERVER)
        .build();

    AtomicReference<ImmutableWilsonState> wilsonStateReference = new AtomicReference<>(wilsonState);
    StateMachineMessageApplier stateMachineMessageApplier = buildMessageApplier(configuration, wilsonStateReference);
    VoteRequest voteRequest = ImmutableVoteRequest.builder()
        .setTerm(2L)
        .setLastLogIndex(1L)
        .setLastLogTerm(1L)
        .build();

    VoteResponse voteResponse = stateMachineMessageApplier.apply(voteRequest, OTHER_SERVER);
    assertThat(voteResponse.getTerm()).isEqualTo(1L);
    assertThat(voteResponse.isVoteGranted()).isFalse();

    WilsonState updatedWilsonState = wilsonStateReference.get();
    assertThat(updatedWilsonState.getLastVotedFor()).isPresent().contains(LOCAL_SERVER);
    assertThat(updatedWilsonState.getCurrentTerm()).isEqualTo(1L);
    assertThat(updatedWilsonState.getLeaderState()).isEqualTo(LeaderState.CANDIDATE);
  }

  @Test
  public void itNoOpsIfVoteRequestHasLowerTerm() {
    Configuration configuration = ImmutableConfiguration.builder()
        .setLocalMember(LOCAL_SERVER)
        .build();
    ImmutableWilsonState wilsonState = ImmutableWilsonState.builder()
        .setLeaderState(LeaderState.FOLLOWER)
        .setCurrentTerm(2L)
        .build();

    AtomicReference<ImmutableWilsonState> wilsonStateReference = new AtomicReference<>(wilsonState);
    StateMachineMessageApplier applier = buildMessageApplier(configuration, wilsonStateReference);
    VoteRequest voteRequest = ImmutableVoteRequest.builder()
        .setTerm(1L)
        .setLastLogTerm(1L)
        .setLastLogIndex(1L)
        .build();

    VoteResponse voteResponse = applier.apply(voteRequest, OTHER_SERVER);
    assertThat(voteResponse.getTerm()).isEqualTo(2L);
    assertThat(voteResponse.isVoteGranted()).isFalse();
  }

  @Test
  public void itNoOpsIfVoteRequestHasLowerLogTerm() {
    Configuration configuration = ImmutableConfiguration.builder()
        .setLocalMember(LOCAL_SERVER)
        .build();
    ImmutableWilsonState wilsonState = ImmutableWilsonState.builder()
        .setLeaderState(LeaderState.FOLLOWER)
        .setCurrentTerm(2L)
        .addLog(ImmutableLogItem.builder().setTerm(2L).setIndex(2L).build())
        .build();

    AtomicReference<ImmutableWilsonState> wilsonStateReference = new AtomicReference<>(wilsonState);
    StateMachineMessageApplier applier = buildMessageApplier(configuration, wilsonStateReference);
    VoteRequest voteRequest = ImmutableVoteRequest.builder()
        .setTerm(2L)
        .setLastLogTerm(1L)
        .setLastLogIndex(1L)
        .build();

    VoteResponse voteResponse = applier.apply(voteRequest, OTHER_SERVER);
    assertThat(voteResponse.getTerm()).isEqualTo(2L);
    assertThat(voteResponse.isVoteGranted()).isFalse();
  }

  @Test
  public void itNoOpsIfVoteRequestHasLowerLogIndex() {
    Configuration configuration = ImmutableConfiguration.builder()
        .setLocalMember(LOCAL_SERVER)
        .build();
    ImmutableWilsonState wilsonState = ImmutableWilsonState.builder()
        .setCurrentTerm(2L)
        .addLog(ImmutableLogItem.builder().setTerm(2L).setIndex(3L).build())
        .build();

    AtomicReference<ImmutableWilsonState> wilsonStateReference = new AtomicReference<>(wilsonState);
    StateMachineMessageApplier applier = buildMessageApplier(configuration, wilsonStateReference);
    VoteRequest voteRequest = ImmutableVoteRequest.builder()
        .setTerm(2L)
        .setLastLogTerm(2L)
        .setLastLogIndex(2L)
        .build();

    VoteResponse voteResponse = applier.apply(voteRequest, OTHER_SERVER);
    assertThat(voteResponse.getTerm()).isEqualTo(2L);
    assertThat(voteResponse.isVoteGranted()).isFalse();
  }

  @Test
  public void itGrantsVoteWhenUpToDateAndVoteNotCast() {
    Configuration configuration = ImmutableConfiguration.builder()
        .setLocalMember(LOCAL_SERVER)
        .setLeaderTimeout(5L)
        .build();
    ImmutableWilsonState wilsonState = ImmutableWilsonState.builder()
        .setCurrentTerm(1L)
        .setLastHeartbeatReceived(Instant.ofEpochMilli(10L))
        .setLeaderState(LeaderState.FOLLOWER)
        .build();

    AtomicReference<ImmutableWilsonState> wilsonStateReference = new AtomicReference<>(wilsonState);
    StateMachineMessageApplier stateMachineMessageApplier = buildMessageApplier(configuration, wilsonStateReference);
    VoteRequest voteRequest = ImmutableVoteRequest.builder()
        .setTerm(2L)
        .setLastLogTerm(1L)
        .setLastLogIndex(1L)
        .build();

    VoteResponse voteResponse = stateMachineMessageApplier.apply(voteRequest, OTHER_SERVER);
    assertThat(voteResponse.isVoteGranted()).isTrue();
    assertThat(voteResponse.getTerm()).isEqualTo(voteRequest.getTerm());

    WilsonState updatedWilsonState = wilsonStateReference.get();
    assertThat(updatedWilsonState.getLeaderState()).isEqualTo(LeaderState.FOLLOWER);
    assertThat(updatedWilsonState.getCurrentTerm()).isEqualTo(voteRequest.getTerm());
    assertThat(updatedWilsonState.getLastVotedFor()).isPresent().contains(OTHER_SERVER);
  }

  @Test
  public void itNoOpsIfElectionTimeoutWhenFollower() {
    Configuration configuration = ImmutableConfiguration.builder()
        .setLocalMember(LOCAL_SERVER)
        .setElectionTimeout(5L)
        .build();
    ImmutableWilsonState wilsonState = ImmutableWilsonState.builder()
        .setLeaderState(LeaderState.FOLLOWER)
        .setCurrentTerm(2L)
        .setLastElectionStarted(Instant.ofEpochMilli(50L))
        .build();

    AtomicReference<ImmutableWilsonState> wilsonStateReference = new AtomicReference<>(wilsonState);
    StateMachineMessageApplier applier = buildMessageApplier(configuration, wilsonStateReference);
    ElectionTimeoutMessage electionTimeoutMessage = ImmutableElectionTimeoutMessage.builder()
        .setTimestamp(Instant.ofEpochMilli(100L))
        .build();

    applier.apply(electionTimeoutMessage);

    assertThat(wilsonStateReference.get()).isEqualTo(wilsonState);
  }

  @Test
  public void itNoOpsIfElectionTimeoutWhenLeader() {
    Configuration configuration = ImmutableConfiguration.builder()
        .setLocalMember(LOCAL_SERVER)
        .setElectionTimeout(5L)
        .build();
    ImmutableWilsonState wilsonState = ImmutableWilsonState.builder()
        .setLeaderState(LeaderState.LEADER)
        .setCurrentTerm(2L)
        .setLastElectionStarted(Instant.ofEpochMilli(50L))
        .build();

    AtomicReference<ImmutableWilsonState> wilsonStateReference = new AtomicReference<>(wilsonState);
    StateMachineMessageApplier applier = buildMessageApplier(configuration, wilsonStateReference);
    ElectionTimeoutMessage electionTimeoutMessage = ImmutableElectionTimeoutMessage.builder()
        .setTimestamp(Instant.ofEpochMilli(100L))
        .build();

    applier.apply(electionTimeoutMessage);

    assertThat(wilsonStateReference.get()).isEqualTo(wilsonState);
  }

  @Test
  public void itNoOpsIfElectionHasNotTimedOutYet() {
    Configuration configuration = ImmutableConfiguration.builder()
        .setLocalMember(LOCAL_SERVER)
        .setElectionTimeout(5L)
        .build();
    ImmutableWilsonState wilsonState = ImmutableWilsonState.builder()
        .setCurrentTerm(2L)
        .setLeaderState(LeaderState.CANDIDATE)
        .setLastElectionStarted(Instant.ofEpochMilli(100L))
        .build();

    AtomicReference<ImmutableWilsonState> wilsonStateReference = new AtomicReference<>(wilsonState);
    StateMachineMessageApplier applier = buildMessageApplier(configuration, wilsonStateReference);
    ElectionTimeoutMessage electionTimeoutMessage = ImmutableElectionTimeoutMessage.builder()
        .setTimestamp(Instant.ofEpochMilli(101L))
        .build();

    applier.apply(electionTimeoutMessage);

    assertThat(wilsonStateReference.get()).isEqualTo(wilsonState);
  }

  @Test
  public void itTransitionsToFollowerWhenElectionTimeoutAsCandidate() {
    Configuration configuration = ImmutableConfiguration.builder()
        .setLocalMember(LOCAL_SERVER)
        .setLeaderTimeout(5L)
        .setElectionTimeout(5L)
        .build();
    ImmutableWilsonState wilsonState = ImmutableWilsonState.builder()
        .setCurrentTerm(1L)
        .setLastElectionStarted(Instant.ofEpochMilli(100L))
        .setLeaderState(LeaderState.CANDIDATE)
        .build();

    AtomicReference<ImmutableWilsonState> wilsonStateReference = new AtomicReference<>(wilsonState);
    StateMachineMessageApplier applier = buildMessageApplier(configuration, wilsonStateReference);

    ElectionTimeoutMessage electionTimeoutMessage = ImmutableElectionTimeoutMessage.builder()
        .setTimestamp(Instant.ofEpochMilli(106L))
        .build();
    applier.apply(electionTimeoutMessage);

    ImmutableWilsonState updatedState = wilsonStateReference.get();
    assertThat(updatedState.getCurrentTerm()).isEqualTo(1L);
    assertThat(updatedState.getLastElectionStarted()).isEmpty();
    assertThat(updatedState.getLastVotedFor()).isEmpty();
    assertThat(updatedState.getLeaderState()).isEqualTo(LeaderState.FOLLOWER);
  }

  @Test
  public void itNoOpsWhenVoteIsNotGranted() {
    Configuration configuration = ImmutableConfiguration.builder()
        .setLocalMember(LOCAL_SERVER)
        .build();
    ImmutableWilsonState immutableWilsonState = ImmutableWilsonState.builder()
        .setLeaderState(LeaderState.CANDIDATE)
        .setCurrentTerm(2L)
        .build();

    AtomicReference<ImmutableWilsonState> wilsonStateReference = new AtomicReference<>(immutableWilsonState);
    StateMachineMessageApplier applier = buildMessageApplier(configuration, wilsonStateReference);

    VoteResponse voteResponse = ImmutableVoteResponse.builder()
        .setTerm(2L)
        .setVoteGranted(false)
        .build();

    applier.apply(voteResponse, OTHER_SERVER);

    assertThat(wilsonStateReference.get()).isEqualTo(immutableWilsonState);
  }

  @Test
  public void itRecordsVoteWhenVoteGrantedButNoQuorum() {
    ImmutableClusterMember thirdMember = ImmutableClusterMember.builder()
        .setHost("third-cluster-member")
        .setPort(9090)
        .build();
    ImmutableClusterMember fourthMember = ImmutableClusterMember.builder()
        .setHost("fourth-cluster-member")
        .setPort(21)
        .build();
    Configuration configuration = ImmutableConfiguration.builder()
        .setLocalMember(LOCAL_SERVER)
        .addClusterMembers(LOCAL_SERVER, OTHER_SERVER, thirdMember, fourthMember)
        .build();
    ImmutableWilsonState wilsonState = ImmutableWilsonState.builder()
        .setLeaderState(LeaderState.CANDIDATE)
        .setCurrentTerm(2L)
        .setLastVotedFor(LOCAL_SERVER)
        .addVotesReceivedFrom(LOCAL_SERVER)
        .build();

    AtomicReference<ImmutableWilsonState> wilsonStateReference = new AtomicReference<>(wilsonState);
    StateMachineMessageApplier applier = buildMessageApplier(configuration, wilsonStateReference);

    VoteResponse voteResponse = ImmutableVoteResponse.builder()
        .setTerm(2L)
        .setVoteGranted(true)
        .build();

    applier.apply(voteResponse, OTHER_SERVER);

    WilsonState updatedWilsonState = wilsonStateReference.get();
    assertThat(updatedWilsonState.getLeaderState()).isEqualTo(LeaderState.CANDIDATE);
    assertThat(updatedWilsonState.getVotesReceivedFrom()).containsExactlyInAnyOrder(LOCAL_SERVER, OTHER_SERVER);
  }

  @Test
  public void itRecordsVoteAndPromotesToLeaderWhenVoteGrantedAndOddQuorum() {
    ImmutableClusterMember thirdMember = ImmutableClusterMember.builder()
        .setHost("third-cluster-member")
        .setPort(22)
        .build();
    Configuration configuration = ImmutableConfiguration.builder()
        .setLocalMember(LOCAL_SERVER)
        .addClusterMembers(LOCAL_SERVER, OTHER_SERVER, thirdMember)
        .build();
    ImmutableWilsonState wilsonState = ImmutableWilsonState.builder()
        .setLeaderState(LeaderState.CANDIDATE)
        .addVotesReceivedFrom(LOCAL_SERVER)
        .setLastVotedFor(LOCAL_SERVER)
        .setCurrentTerm(2L)
        .build();

    AtomicReference<ImmutableWilsonState> wilsonStateReference = new AtomicReference<>(wilsonState);
    StateMachineMessageApplier applier = buildMessageApplier(configuration, wilsonStateReference);

    VoteResponse voteResponse = ImmutableVoteResponse.builder()
        .setTerm(2L)
        .setVoteGranted(true)
        .build();

    applier.apply(voteResponse, OTHER_SERVER);

    WilsonState updatedWilsonState = wilsonStateReference.get();
    assertThat(updatedWilsonState.getLeaderState()).isEqualTo(LeaderState.LEADER);
    assertThat(updatedWilsonState.getCurrentTerm()).isEqualTo(2L);
    assertThat(updatedWilsonState.getLastVotedFor()).isNotPresent();
    assertThat(updatedWilsonState.getLastElectionStarted()).isNotPresent();
  }

  @Test
  public void itRecordsVoteAndPromotesToLeaderWhenVoteGrantedAndEvenQuorum() {
    ImmutableClusterMember thirdMember = ImmutableClusterMember.builder()
        .setHost("third-cluster-member")
        .setPort(22)
        .build();
    ImmutableClusterMember fourthMember = ImmutableClusterMember.builder()
        .setHost("fourth-cluster-member")
        .setPort(443)
        .build();
    Configuration configuration = ImmutableConfiguration.builder()
        .setLocalMember(LOCAL_SERVER)
        .addClusterMembers(LOCAL_SERVER, OTHER_SERVER, thirdMember, fourthMember)
        .build();
    ImmutableWilsonState wilsonState = ImmutableWilsonState.builder()
        .setLeaderState(LeaderState.CANDIDATE)
        .addVotesReceivedFrom(LOCAL_SERVER, OTHER_SERVER)
        .setLastVotedFor(LOCAL_SERVER)
        .setCurrentTerm(2L)
        .build();

    AtomicReference<ImmutableWilsonState> wilsonStateReference = new AtomicReference<>(wilsonState);
    StateMachineMessageApplier applier = buildMessageApplier(configuration, wilsonStateReference);

    VoteResponse voteResponse = ImmutableVoteResponse.builder()
        .setTerm(2L)
        .setVoteGranted(true)
        .build();

    applier.apply(voteResponse, thirdMember);

    WilsonState updatedWilsonState = wilsonStateReference.get();
    assertThat(updatedWilsonState.getLeaderState()).isEqualTo(LeaderState.LEADER);
    assertThat(updatedWilsonState.getCurrentTerm()).isEqualTo(2L);
    assertThat(updatedWilsonState.getLastVotedFor()).isNotPresent();
    assertThat(updatedWilsonState.getLastElectionStarted()).isNotPresent();
  }

  @Test
  public void itMovesToFollowerIfLaterTermSeen() {
    Configuration configuration = ImmutableConfiguration.builder()
        .setLocalMember(LOCAL_SERVER)
        .addClusterMembers(LOCAL_SERVER, OTHER_SERVER)
        .build();
    ImmutableWilsonState wilsonState = ImmutableWilsonState.builder()
        .setLeaderState(LeaderState.CANDIDATE)
        .addVotesReceivedFrom(LOCAL_SERVER)
        .setLastVotedFor(LOCAL_SERVER)
        .setCurrentTerm(2L)
        .build();

    AtomicReference<ImmutableWilsonState> wilsonStateReference = new AtomicReference<>(wilsonState);
    StateMachineMessageApplier applier = buildMessageApplier(configuration, wilsonStateReference);

    VoteRequest voteRequest = ImmutableVoteRequest.builder()
        .setTerm(3L)
        .setLastLogTerm(3L)
        .setLastLogIndex(100L)
        .build();

    applier.apply(voteRequest, OTHER_SERVER);

    WilsonState updatedWilsonState = wilsonStateReference.get();
    assertThat(updatedWilsonState.getLeaderState()).isEqualTo(LeaderState.FOLLOWER);
    assertThat(updatedWilsonState.getCurrentTerm()).isEqualTo(3L);
    assertThat(updatedWilsonState.getLastVotedFor()).contains(OTHER_SERVER);
  }

  private StateMachineMessageApplier buildMessageApplier(Configuration configuration,
                                                         AtomicReference<ImmutableWilsonState> wilsonStateReference) {
    return new StateMachineMessageApplier(wilsonStateReference, configuration, LOCAL_SERVER, eventBus);
  }

}
