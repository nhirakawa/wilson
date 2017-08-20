package com.github.nhirakawa.server.config;

import java.util.Collections;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.immutables.value.Value;
import org.immutables.value.Value.Auxiliary;
import org.immutables.value.Value.Default;
import org.immutables.value.Value.Lazy;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.nhirakawa.wilson.models.style.WilsonStyle;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

@Value.Immutable
@WilsonStyle
public interface Configuration {

  @Default
  default String getClusterId() {
    return "wilson-default";
  }

  @Default
  @JsonProperty("cluster")
  default Set<ImmutableClusterMember> getClusterMembers() {
    return Collections.singleton(
        ImmutableClusterMember.builder()
            .setHost("localhost")
            .setPort(8080)
            .build()
    );
  }

  Optional<ImmutableClusterMember> getLocalMember();

  @Default
  default long getLeaderTimeout() {
    return 1000L;
  }

  @Default
  default long getElectionTimeout() {
    return 2000L;
  }

  @Default
  default long getHeartbeatTimeout() {
    return 250L;
  }

  @Default
  default int getNumClientThreads() {
    return 10;
  }

  @Lazy
  default Set<String> getClusterServerIds() {
    return getClusterMembers().stream()
        .map(ClusterMember::getServerId)
        .collect(ImmutableSet.toImmutableSet());
  }

  @Auxiliary
  @JsonIgnore
  default Random getRandom() {
    return new Random();
  }

  default long getLeaderTimeoutWithJitter() {
    return getLeaderTimeout() + ThreadLocalRandom.current().nextLong(50, 100);
  }

  default long getElectionTimeoutWithJitter() {
    return getElectionTimeout() + ThreadLocalRandom.current().nextLong(100, 200);
  }

  default long getHeartbeatTimeoutWithJitter() {
    return getHeartbeatTimeout() + ThreadLocalRandom.current().nextLong(25, 50);
  }

  @Auxiliary
  default ClusterMember getClusterMemberFromId(String memberId) {
    return Maps.uniqueIndex(getClusterMembers(), ClusterMember::getServerId).get(memberId);
  }
}
