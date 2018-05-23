package com.github.nhirakawa.server.raft;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.immutables.value.Value;

import com.github.nhirakawa.server.config.ClusterMemberModel;
import com.github.nhirakawa.wilson.models.style.WilsonStyle;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;

@Value.Immutable
@WilsonStyle
public interface WilsonStateModel {

  @Value.Default
  default long getCurrentTerm() {
    return 1L;
  }

  @Value.Default
  default Instant getLastHeartbeatReceived() {
    return Instant.now();
  }

  Optional<Instant> getLastElectionStarted();

  Optional<ClusterMemberModel> getCurrentLeader();

  @Value.Default
  default LeaderState getLeaderState() {
    return LeaderState.FOLLOWER;
  }

  List<LogItemModel> getLog();

  Optional<ClusterMemberModel> getLastVotedFor();

  Set<ClusterMemberModel> getVotesReceivedFrom();

  @Value.Lazy
  default long getLastLogTerm() {
    Optional<LogItemModel> maybeLogItem = Optional.ofNullable(Iterables.getLast(getLog(), null));
    return maybeLogItem.map(LogItemModel::getTerm).orElse(1L);
  }

  @Value.Lazy
  default long getLastLogIndex() {
    Optional<LogItemModel> maybeLogItem = Optional.ofNullable(Iterables.getLast(getLog(), null));
    return maybeLogItem.map(LogItemModel::getIndex).orElse(1L);
  }

  @Value.Check
  default void check() {
    Preconditions.checkState(getCurrentTerm() >= 0, "current term must be >= 0");
    Preconditions.checkState(getLastLogTerm() >= 0, "last log term must be >= 0");
    Preconditions.checkState(getLastLogIndex() >= 0, "last log index must be >= 0");

    if (getLeaderState() == LeaderState.FOLLOWER) {

    } else if (getLeaderState() == LeaderState.CANDIDATE) {
      Preconditions.checkState(!getVotesReceivedFrom().isEmpty(), "must have at least one vote if I am candidate");
      Preconditions.checkState(getLastVotedFor().isPresent(), "must have voted for myself if I am candidate");
      Preconditions.checkState(getLastElectionStarted().isPresent(), "election must have started if I am candidate");
    } else if (getLeaderState() == LeaderState.LEADER) {
      Preconditions.checkState(!getLastElectionStarted().isPresent(), "last election cannot be set when leader");
      Preconditions.checkState(!getLastVotedFor().isPresent(), "cannot have outstanding vote when leader");
      Preconditions.checkState(getCurrentLeader().isPresent(), "current leader must be set because I am leader");
    }
  }

}
