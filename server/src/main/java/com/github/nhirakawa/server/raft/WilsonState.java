package com.github.nhirakawa.server.raft;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.immutables.value.Value.Default;
import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Lazy;

import com.github.nhirakawa.server.config.ClusterMember;
import com.github.nhirakawa.wilson.models.style.WilsonStyle;
import com.google.common.collect.Iterables;

@Immutable
@WilsonStyle
public interface WilsonState {

  @Default
  default long getCurrentTerm() {
    return 1L;
  }

  @Default
  default Instant getLastHeartbeatReceived() {
    return Instant.now();
  }

  Optional<Instant> getLastElectionStarted();

  @Default
  default LeaderState getLeaderState() {
    return LeaderState.FOLLOWER;
  }

  List<LogItem> getLog();

  Optional<ClusterMember> getLastVotedFor();

  Set<ClusterMember> getVotesReceivedFrom();

  @Lazy
  default long getLastLogTerm() {
    Optional<LogItem> maybeLogItem = Optional.ofNullable(Iterables.getLast(getLog(), null));
    return maybeLogItem.map(LogItem::getTerm).orElse(1L);
  }

  @Lazy
  default long getLastLogIndex() {
    Optional<LogItem> maybeLogItem = Optional.ofNullable(Iterables.getLast(getLog(), null));
    return maybeLogItem.map(LogItem::getIndex).orElse(1L);
  }

}
