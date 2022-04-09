package com.github.nhirakawa.wilson.protocol;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.nhirakawa.wilson.models.ClusterMember;
import com.github.nhirakawa.wilson.models.LeaderState;
import com.github.nhirakawa.wilson.models.LogItem;
import com.github.nhirakawa.wilson.models.messages.ElectionTimeoutMessage;
import com.github.nhirakawa.wilson.models.messages.HeartbeatTimeoutMessage;
import com.github.nhirakawa.wilson.models.messages.LeaderTimeoutMessage;
import com.github.nhirakawa.wilson.models.messages.VoteRequest;
import com.github.nhirakawa.wilson.models.messages.VoteResponse;
import com.github.nhirakawa.wilson.models.messages.VoteResponseModel;
import com.github.nhirakawa.wilson.models.WilsonState;
import com.github.nhirakawa.wilson.models.WilsonStateModel;
import com.google.common.collect.ImmutableSet;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;
import java.util.Set;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class StateMachineMessageApplierTest {
  private static final ClusterMember LOCAL_SERVER = ClusterMember
    .builder()
    .setHost("localhost")
    .setPort(9000)
    .build();
  private static final ClusterMember OTHER_SERVER = ClusterMember
    .builder()
    .setHost("localhost")
    .setPort(9001)
    .build();

  private FakeMessageSender messageSender;

  @Before
  public void setup() {
    messageSender = new FakeMessageSender();
  }

  @Test
  public void itDoesNothingWhenLeaderTimeoutAndLeaderIsValid() {
    long leaderTimeout = 5L;
    AtomicReference<WilsonState> wilsonStateReference = new AtomicReference<>(
      WilsonState
        .builder()
        .setLastHeartbeatReceived(Instant.ofEpochMilli(100L))
        .build()
    );

    StateMachineMessageApplier stateMachineMessageApplier = buildMessageApplier(
      ImmutableSet.of(LOCAL_SERVER, OTHER_SERVER),
      wilsonStateReference
    );

    LeaderTimeoutMessage leaderTimeoutMessageModel = LeaderTimeoutMessage
      .builder()
      .setTimestamp(Instant.ofEpochMilli(101L))
      .setLeaderTimeout(leaderTimeout)
      .build();

    stateMachineMessageApplier.apply(leaderTimeoutMessageModel);

    WilsonStateModel wilsonState = wilsonStateReference.get();
    assertThat(wilsonState.getCurrentTerm()).isEqualTo(1L);
    assertThat(wilsonState.getLeaderState()).isEqualTo(LeaderState.FOLLOWER);
    assertThat(wilsonState.getLastVotedFor()).isNotPresent();
  }

  @Test
  public void itTransitionsToCandidateWhenLeaderTimeoutAndLeaderIsInvalid() {
    long leaderTimeout = 5L;
    AtomicReference<WilsonState> wilsonStateReference = new AtomicReference<>(
      WilsonState
        .builder()
        .setCurrentTerm(5L)
        .addLog(LogItem.builder().setTerm(4L).setIndex(3L).build())
        .setLastHeartbeatReceived(Instant.ofEpochMilli(100L))
        .build()
    );

    StateMachineMessageApplier stateMachineMessageApplier = buildMessageApplier(
      ImmutableSet.of(OTHER_SERVER),
      wilsonStateReference
    );

    LeaderTimeoutMessage leaderTimeoutMessageModel = LeaderTimeoutMessage
      .builder()
      .setTimestamp(Instant.ofEpochMilli(120L))
      .setLeaderTimeout(leaderTimeout)
      .build();
    stateMachineMessageApplier.apply(leaderTimeoutMessageModel);

    WilsonState wilsonState = wilsonStateReference.get();
    assertThat(wilsonState.getCurrentTerm()).isEqualTo(6L);
    assertThat(wilsonState.getLastVotedFor())
      .isPresent()
      .contains(LOCAL_SERVER);
    assertThat(wilsonState.getVotesReceivedFrom())
      .containsExactly(LOCAL_SERVER);
    assertThat(wilsonState.getLeaderState()).isEqualTo(LeaderState.CANDIDATE);

    VoteRequest expectedVoteRequest = VoteRequest
      .builder()
      .setClusterMember(LOCAL_SERVER)
      .setTerm(6L)
      .setLastLogTerm(4L)
      .setLastLogIndex(3L)
      .build();
    assertThat(messageSender.getVoteRequests())
      .containsExactly(expectedVoteRequest);
  }

  @Test
  public void itDoesNothingWhenLeaderTimeoutWhileCandidate() {
    long leaderTimeout = 5L;

    WilsonState wilsonState = WilsonState
      .builder()
      .setLeaderState(LeaderState.CANDIDATE)
      .setLastHeartbeatReceived(Instant.ofEpochMilli(10L))
      .addVotesReceivedFrom(LOCAL_SERVER)
      .setLastVotedFor(LOCAL_SERVER)
      .setLastElectionStarted(Instant.now())
      .build();
    AtomicReference<WilsonState> wilsonStateReference = new AtomicReference<>(
      wilsonState
    );

    StateMachineMessageApplier stateMachineMessageApplier = buildMessageApplier(
      ImmutableSet.of(LOCAL_SERVER, OTHER_SERVER),
      wilsonStateReference
    );

    LeaderTimeoutMessage leaderTimeoutMessageModel = LeaderTimeoutMessage
      .builder()
      .setTimestamp(Instant.ofEpochMilli(100L))
      .setLeaderTimeout(leaderTimeout)
      .build();
    stateMachineMessageApplier.apply(leaderTimeoutMessageModel);

    WilsonState updatedWilsonState = wilsonStateReference.get();
    assertThat(updatedWilsonState).isEqualTo(wilsonState);
  }

  @Test
  public void itDoesNothingWhenLeaderTimeoutWhileLeader() {
    long leaderTimeout = 5L;

    WilsonState wilsonState = WilsonState
      .builder()
      .setLastHeartbeatReceived(Instant.ofEpochMilli(10L))
      .setLeaderState(LeaderState.LEADER)
      .setCurrentLeader(LOCAL_SERVER)
      .build();

    AtomicReference<WilsonState> wilsonStateReference = new AtomicReference<>(
      wilsonState
    );
    StateMachineMessageApplier stateMachineMessageApplier = buildMessageApplier(
      ImmutableSet.of(LOCAL_SERVER, OTHER_SERVER),
      wilsonStateReference
    );
    LeaderTimeoutMessage leaderTimeoutMessageModel = LeaderTimeoutMessage
      .builder()
      .setTimestamp(Instant.ofEpochMilli(100L))
      .setLeaderTimeout(leaderTimeout)
      .build();

    stateMachineMessageApplier.apply(leaderTimeoutMessageModel);

    WilsonState updatedWilsonState = wilsonStateReference.get();
    assertThat(updatedWilsonState).isEqualTo(wilsonState);
  }

  @Test
  public void itNoOpsIfVoteHasAlreadyBeenCast() {
    WilsonState wilsonState = WilsonState
      .builder()
      .setLeaderState(LeaderState.CANDIDATE)
      .setCurrentTerm(2L)
      .setLastVotedFor(LOCAL_SERVER)
      .addVotesReceivedFrom(LOCAL_SERVER)
      .setLastElectionStarted(Instant.now())
      .build();

    AtomicReference<WilsonState> wilsonStateReference = new AtomicReference<>(
      wilsonState
    );
    StateMachineMessageApplier stateMachineMessageApplier = buildMessageApplier(
      ImmutableSet.of(LOCAL_SERVER, OTHER_SERVER),
      wilsonStateReference
    );
    VoteRequest voteRequest = VoteRequest
      .builder()
      .setClusterMember(OTHER_SERVER)
      .setTerm(2L)
      .setLastLogIndex(1L)
      .setLastLogTerm(1L)
      .build();

    VoteResponse voteResponse = stateMachineMessageApplier.apply(voteRequest);
    assertThat(voteResponse.getTerm()).isEqualTo(2L);
    assertThat(voteResponse.isVoteGranted()).isFalse();

    WilsonState updatedWilsonState = wilsonStateReference.get();
    assertThat(updatedWilsonState.getLastVotedFor())
      .isPresent()
      .contains(LOCAL_SERVER);
    assertThat(updatedWilsonState.getCurrentTerm()).isEqualTo(2L);
    assertThat(updatedWilsonState.getLeaderState())
      .isEqualTo(LeaderState.CANDIDATE);
  }

  @Test
  public void itNoOpsIfVoteRequestHasLowerTerm() {
    WilsonState wilsonState = WilsonState
      .builder()
      .setLeaderState(LeaderState.FOLLOWER)
      .setCurrentTerm(2L)
      .build();

    AtomicReference<WilsonState> wilsonStateReference = new AtomicReference<>(
      wilsonState
    );
    StateMachineMessageApplier applier = buildMessageApplier(
      ImmutableSet.of(LOCAL_SERVER, OTHER_SERVER),
      wilsonStateReference
    );
    VoteRequest voteRequest = VoteRequest
      .builder()
      .setClusterMember(OTHER_SERVER)
      .setTerm(1L)
      .setLastLogTerm(1L)
      .setLastLogIndex(1L)
      .build();

    VoteResponse voteResponse = applier.apply(voteRequest);
    assertThat(voteResponse.getTerm()).isEqualTo(2L);
    assertThat(voteResponse.isVoteGranted()).isFalse();
  }

  @Test
  public void itNoOpsIfVoteRequestHasLowerLogTerm() {
    WilsonState wilsonState = WilsonState
      .builder()
      .setLeaderState(LeaderState.FOLLOWER)
      .setCurrentTerm(2L)
      .addLog(LogItem.builder().setTerm(2L).setIndex(2L).build())
      .build();

    AtomicReference<WilsonState> wilsonStateReference = new AtomicReference<>(
      wilsonState
    );
    StateMachineMessageApplier applier = buildMessageApplier(
      ImmutableSet.of(LOCAL_SERVER, OTHER_SERVER),
      wilsonStateReference
    );
    VoteRequest voteRequest = VoteRequest
      .builder()
      .setClusterMember(OTHER_SERVER)
      .setTerm(2L)
      .setLastLogTerm(1L)
      .setLastLogIndex(1L)
      .build();

    VoteResponseModel voteResponse = applier.apply(voteRequest);
    assertThat(voteResponse.getTerm()).isEqualTo(2L);
    assertThat(voteResponse.isVoteGranted()).isFalse();
  }

  @Test
  public void itNoOpsIfVoteRequestHasLowerLogIndex() {
    WilsonState wilsonState = WilsonState
      .builder()
      .setCurrentTerm(2L)
      .addLog(LogItem.builder().setTerm(2L).setIndex(3L).build())
      .build();

    AtomicReference<WilsonState> wilsonStateReference = new AtomicReference<>(
      wilsonState
    );
    StateMachineMessageApplier applier = buildMessageApplier(
      ImmutableSet.of(LOCAL_SERVER, OTHER_SERVER),
      wilsonStateReference
    );
    VoteRequest voteRequest = VoteRequest
      .builder()
      .setClusterMember(OTHER_SERVER)
      .setTerm(2L)
      .setLastLogTerm(2L)
      .setLastLogIndex(2L)
      .build();

    VoteResponseModel voteResponse = applier.apply(voteRequest);
    assertThat(voteResponse.getTerm()).isEqualTo(2L);
    assertThat(voteResponse.isVoteGranted()).isFalse();
  }

  @Test
  public void itGrantsVoteWhenUpToDateAndVoteNotCast() {
    WilsonState wilsonState = WilsonState
      .builder()
      .setCurrentTerm(1L)
      .setLastHeartbeatReceived(Instant.ofEpochMilli(10L))
      .setLeaderState(LeaderState.FOLLOWER)
      .build();

    AtomicReference<WilsonState> wilsonStateReference = new AtomicReference<>(
      wilsonState
    );
    StateMachineMessageApplier stateMachineMessageApplier = buildMessageApplier(
      ImmutableSet.of(LOCAL_SERVER, OTHER_SERVER),
      wilsonStateReference
    );
    VoteRequest voteRequest = VoteRequest
      .builder()
      .setClusterMember(OTHER_SERVER)
      .setTerm(2L)
      .setLastLogTerm(1L)
      .setLastLogIndex(1L)
      .build();

    VoteResponseModel voteResponse = stateMachineMessageApplier.apply(
      voteRequest
    );
    assertThat(voteResponse.isVoteGranted()).isTrue();
    assertThat(voteResponse.getTerm()).isEqualTo(voteRequest.getTerm());

    WilsonStateModel updatedWilsonState = wilsonStateReference.get();
    assertThat(updatedWilsonState.getLeaderState())
      .isEqualTo(LeaderState.FOLLOWER);
    assertThat(updatedWilsonState.getCurrentTerm())
      .isEqualTo(voteRequest.getTerm());
    assertThat(updatedWilsonState.getLastVotedFor())
      .isPresent()
      .contains(OTHER_SERVER);
  }

  @Test
  public void itNoOpsIfElectionTimeoutWhenFollower() {
    long electionTimeout = 5L;

    WilsonState wilsonState = WilsonState
      .builder()
      .setLeaderState(LeaderState.FOLLOWER)
      .setCurrentTerm(2L)
      .setLastElectionStarted(Instant.ofEpochMilli(50L))
      .build();

    AtomicReference<WilsonState> wilsonStateReference = new AtomicReference<>(
      wilsonState
    );
    StateMachineMessageApplier applier = buildMessageApplier(
      ImmutableSet.of(LOCAL_SERVER, OTHER_SERVER),
      wilsonStateReference
    );
    ElectionTimeoutMessage electionTimeoutMessageModel = ElectionTimeoutMessage
      .builder()
      .setTimestamp(Instant.ofEpochMilli(100L))
      .setElectionTimeout(electionTimeout)
      .build();

    applier.apply(electionTimeoutMessageModel);

    assertThat(wilsonStateReference.get()).isEqualTo(wilsonState);
  }

  @Test
  public void itNoOpsIfElectionTimeoutWhenLeader() {
    long electionTimeout = 5L;

    WilsonState wilsonState = WilsonState
      .builder()
      .setLeaderState(LeaderState.LEADER)
      .setCurrentLeader(LOCAL_SERVER)
      .setCurrentTerm(2L)
      .build();

    AtomicReference<WilsonState> wilsonStateReference = new AtomicReference<>(
      wilsonState
    );
    StateMachineMessageApplier applier = buildMessageApplier(
      ImmutableSet.of(LOCAL_SERVER, OTHER_SERVER),
      wilsonStateReference
    );
    ElectionTimeoutMessage electionTimeoutMessageModel = ElectionTimeoutMessage
      .builder()
      .setTimestamp(Instant.ofEpochMilli(100L))
      .setElectionTimeout(electionTimeout)
      .build();

    applier.apply(electionTimeoutMessageModel);

    assertThat(wilsonStateReference.get()).isEqualTo(wilsonState);
  }

  @Test
  public void itNoOpsIfElectionHasNotTimedOutYet() {
    long electionTimeout = 5L;

    WilsonState wilsonState = WilsonState
      .builder()
      .setCurrentTerm(2L)
      .setLeaderState(LeaderState.CANDIDATE)
      .setLastElectionStarted(Instant.ofEpochMilli(100L))
      .addVotesReceivedFrom(LOCAL_SERVER)
      .setLastVotedFor(LOCAL_SERVER)
      .build();

    AtomicReference<WilsonState> wilsonStateReference = new AtomicReference<>(
      wilsonState
    );
    StateMachineMessageApplier applier = buildMessageApplier(
      ImmutableSet.of(LOCAL_SERVER, OTHER_SERVER),
      wilsonStateReference
    );
    ElectionTimeoutMessage electionTimeoutMessageModel = ElectionTimeoutMessage
      .builder()
      .setTimestamp(Instant.ofEpochMilli(101L))
      .setElectionTimeout(electionTimeout)
      .build();

    applier.apply(electionTimeoutMessageModel);

    assertThat(wilsonStateReference.get()).isEqualTo(wilsonState);
  }

  @Test
  public void itTransitionsToFollowerWhenElectionTimeoutAsCandidate() {
    long electionTimeout = 5L;

    WilsonState wilsonState = WilsonState
      .builder()
      .setCurrentTerm(1L)
      .setLastElectionStarted(Instant.ofEpochMilli(100L))
      .setLeaderState(LeaderState.CANDIDATE)
      .addVotesReceivedFrom(LOCAL_SERVER)
      .setLastVotedFor(LOCAL_SERVER)
      .build();

    AtomicReference<WilsonState> wilsonStateReference = new AtomicReference<>(
      wilsonState
    );
    StateMachineMessageApplier applier = buildMessageApplier(
      ImmutableSet.of(LOCAL_SERVER, OTHER_SERVER),
      wilsonStateReference
    );

    ElectionTimeoutMessage electionTimeoutMessageModel = ElectionTimeoutMessage
      .builder()
      .setTimestamp(Instant.ofEpochMilli(106L))
      .setElectionTimeout(electionTimeout)
      .build();
    applier.apply(electionTimeoutMessageModel);

    WilsonState updatedState = wilsonStateReference.get();
    assertThat(updatedState.getCurrentTerm()).isEqualTo(1L);
    assertThat(updatedState.getLastElectionStarted()).isEmpty();
    assertThat(updatedState.getLastVotedFor()).isEmpty();
    assertThat(updatedState.getLeaderState()).isEqualTo(LeaderState.FOLLOWER);
  }

  @Test
  public void itNoOpsWhenVoteIsNotGranted() {
    WilsonState immutableWilsonState = WilsonState
      .builder()
      .setLeaderState(LeaderState.CANDIDATE)
      .addVotesReceivedFrom(LOCAL_SERVER)
      .setLastVotedFor(LOCAL_SERVER)
      .setCurrentTerm(2L)
      .setLastElectionStarted(Instant.now())
      .build();

    AtomicReference<WilsonState> wilsonStateReference = new AtomicReference<>(
      immutableWilsonState
    );
    StateMachineMessageApplier applier = buildMessageApplier(
      ImmutableSet.of(LOCAL_SERVER, OTHER_SERVER),
      wilsonStateReference
    );

    VoteResponse voteResponse = VoteResponse
      .builder()
      .setTerm(2L)
      .setVoteGranted(false)
      .build();

    applier.apply(voteResponse, OTHER_SERVER);

    assertThat(wilsonStateReference.get()).isEqualTo(immutableWilsonState);
  }

  @Test
  public void itRecordsVoteWhenVoteGrantedButNoQuorum() {
    ClusterMember thirdMember = ClusterMember
      .builder()
      .setHost("third-cluster-member")
      .setPort(9090)
      .build();
    ClusterMember fourthMember = ClusterMember
      .builder()
      .setHost("fourth-cluster-member")
      .setPort(21)
      .build();
    WilsonState wilsonState = WilsonState
      .builder()
      .setLeaderState(LeaderState.CANDIDATE)
      .setCurrentTerm(2L)
      .setLastVotedFor(LOCAL_SERVER)
      .addVotesReceivedFrom(LOCAL_SERVER)
      .setLastElectionStarted(Instant.now())
      .build();

    AtomicReference<WilsonState> wilsonStateReference = new AtomicReference<>(
      wilsonState
    );
    StateMachineMessageApplier applier = buildMessageApplier(
      ImmutableSet.of(LOCAL_SERVER, OTHER_SERVER, thirdMember, fourthMember),
      wilsonStateReference
    );

    VoteResponse voteResponse = VoteResponse
      .builder()
      .setTerm(2L)
      .setVoteGranted(true)
      .build();

    applier.apply(voteResponse, OTHER_SERVER);

    WilsonState updatedWilsonState = wilsonStateReference.get();
    assertThat(updatedWilsonState.getLeaderState())
      .isEqualTo(LeaderState.CANDIDATE);
    assertThat(updatedWilsonState.getVotesReceivedFrom())
      .containsExactlyInAnyOrder(LOCAL_SERVER, OTHER_SERVER);
  }

  @Test
  public void itRecordsVoteAndPromotesToLeaderWhenVoteGrantedAndOddQuorum() {
    ClusterMember thirdMember = ClusterMember
      .builder()
      .setHost("third-cluster-member")
      .setPort(22)
      .build();
    WilsonState wilsonState = WilsonState
      .builder()
      .setLeaderState(LeaderState.CANDIDATE)
      .addVotesReceivedFrom(LOCAL_SERVER)
      .setLastVotedFor(LOCAL_SERVER)
      .setCurrentTerm(2L)
      .setLastElectionStarted(Instant.now())
      .build();

    AtomicReference<WilsonState> wilsonStateReference = new AtomicReference<>(
      wilsonState
    );
    StateMachineMessageApplier applier = buildMessageApplier(
      ImmutableSet.of(LOCAL_SERVER, OTHER_SERVER, thirdMember),
      wilsonStateReference
    );

    VoteResponse voteResponse = VoteResponse
      .builder()
      .setTerm(2L)
      .setVoteGranted(true)
      .build();

    applier.apply(voteResponse, OTHER_SERVER);

    WilsonStateModel updatedWilsonState = wilsonStateReference.get();
    assertThat(updatedWilsonState.getLeaderState())
      .isEqualTo(LeaderState.LEADER);
    assertThat(updatedWilsonState.getCurrentTerm()).isEqualTo(2L);
    assertThat(updatedWilsonState.getLastVotedFor()).isNotPresent();
    assertThat(updatedWilsonState.getLastElectionStarted()).isNotPresent();
  }

  @Test
  public void itRecordsVoteAndPromotesToLeaderWhenVoteGrantedAndEvenQuorum() {
    ClusterMember thirdMember = ClusterMember
      .builder()
      .setHost("third-cluster-member")
      .setPort(22)
      .build();
    ClusterMember fourthMember = ClusterMember
      .builder()
      .setHost("fourth-cluster-member")
      .setPort(443)
      .build();
    WilsonState wilsonState = WilsonState
      .builder()
      .setLeaderState(LeaderState.CANDIDATE)
      .addVotesReceivedFrom(LOCAL_SERVER, OTHER_SERVER)
      .setLastVotedFor(LOCAL_SERVER)
      .setCurrentTerm(2L)
      .setLastElectionStarted(Instant.now())
      .build();

    AtomicReference<WilsonState> wilsonStateReference = new AtomicReference<>(
      wilsonState
    );
    StateMachineMessageApplier applier = buildMessageApplier(
      ImmutableSet.of(LOCAL_SERVER, OTHER_SERVER, thirdMember, fourthMember),
      wilsonStateReference
    );

    VoteResponse voteResponse = VoteResponse
      .builder()
      .setTerm(2L)
      .setVoteGranted(true)
      .build();

    applier.apply(voteResponse, thirdMember);

    WilsonState updatedWilsonState = wilsonStateReference.get();
    assertThat(updatedWilsonState.getLeaderState())
      .isEqualTo(LeaderState.LEADER);
    assertThat(updatedWilsonState.getCurrentTerm()).isEqualTo(2L);
    assertThat(updatedWilsonState.getLastVotedFor()).isNotPresent();
    assertThat(updatedWilsonState.getLastElectionStarted()).isNotPresent();
  }

  @Test
  public void itMovesToFollowerIfLaterTermSeen() {
    WilsonState wilsonState = WilsonState
      .builder()
      .setLeaderState(LeaderState.CANDIDATE)
      .addVotesReceivedFrom(LOCAL_SERVER)
      .setLastVotedFor(LOCAL_SERVER)
      .setLastElectionStarted(Instant.now())
      .setCurrentTerm(2L)
      .build();

    AtomicReference<WilsonState> wilsonStateReference = new AtomicReference<>(
      wilsonState
    );
    StateMachineMessageApplier applier = buildMessageApplier(
      ImmutableSet.of(LOCAL_SERVER, OTHER_SERVER),
      wilsonStateReference
    );

    VoteRequest voteRequest = VoteRequest
      .builder()
      .setClusterMember(OTHER_SERVER)
      .setTerm(3L)
      .setLastLogTerm(3L)
      .setLastLogIndex(100L)
      .build();

    applier.apply(voteRequest);

    WilsonStateModel updatedWilsonState = wilsonStateReference.get();
    assertThat(updatedWilsonState.getLeaderState())
      .isEqualTo(LeaderState.FOLLOWER);
    assertThat(updatedWilsonState.getCurrentTerm()).isEqualTo(3L);
    assertThat(updatedWilsonState.getLastVotedFor()).contains(OTHER_SERVER);
  }

  @Test
  public void itSendsHeartbeatRequestAfterTimeoutWhenLeader() {
    WilsonState wilsonState = WilsonState
      .builder()
      .setCurrentLeader(LOCAL_SERVER)
      .setLeaderState(LeaderState.LEADER)
      .build();

    AtomicReference<WilsonState> wilsonStateReference = new AtomicReference<>(
      wilsonState
    );
    StateMachineMessageApplier applier = buildMessageApplier(
      ImmutableSet.of(LOCAL_SERVER, OTHER_SERVER),
      wilsonStateReference
    );

    HeartbeatTimeoutMessage timeoutMessage = HeartbeatTimeoutMessage
      .builder()
      .setHeartbeatTimeout(1L)
      .build();

    applier.apply(timeoutMessage);

    assertThat(messageSender.getAppendEntriesRequests()).isNotEmpty();
  }

  @Test
  public void itNoOpsIfHeartbeatTimeoutAndNotLeader() {
    WilsonState wilsonState = WilsonState
      .builder()
      .setLeaderState(LeaderState.FOLLOWER)
      .build();

    AtomicReference<WilsonState> wilsonStateReference = new AtomicReference<>(
      wilsonState
    );
    StateMachineMessageApplier applier = buildMessageApplier(
      ImmutableSet.of(LOCAL_SERVER, OTHER_SERVER),
      wilsonStateReference
    );

    HeartbeatTimeoutMessage timeoutMessage = HeartbeatTimeoutMessage
      .builder()
      .setHeartbeatTimeout(1L)
      .build();

    applier.apply(timeoutMessage);

    assertThat(messageSender.getVoteRequests()).isEmpty();
    assertThat(messageSender.getAppendEntriesRequests()).isEmpty();
  }

  @Test
  public void itNoOpsIfVoteReceivedAndNotLeader() {
    WilsonState wilsonState = WilsonState
      .builder()
      .setLeaderState(LeaderState.FOLLOWER)
      .build();

    AtomicReference<WilsonState> wilsonStateReference = new AtomicReference<>(
      wilsonState
    );
    StateMachineMessageApplier applier = buildMessageApplier(
      ImmutableSet.of(LOCAL_SERVER, OTHER_SERVER),
      wilsonStateReference
    );

    VoteResponse voteResponse = VoteResponse
      .builder()
      .setTerm(100L)
      .setVoteGranted(true)
      .build();

    applier.apply(voteResponse, OTHER_SERVER);

    assertThat(messageSender.getVoteRequests()).isEmpty();
    assertThat(messageSender.getAppendEntriesRequests()).isEmpty();
  }

  private StateMachineMessageApplier buildMessageApplier(
    Set<ClusterMember> clusterMembers,
    AtomicReference<WilsonState> wilsonStateReference
  ) {
    messageSender.clear();

    return new StateMachineMessageApplier(
      wilsonStateReference,
      clusterMembers,
      LOCAL_SERVER,
      messageSender
    );
  }
}
