package com.github.nhirakawa.server.config;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.immutables.value.Value.Auxiliary;
import org.immutables.value.Value.Default;
import org.immutables.value.Value.Derived;
import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Lazy;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.nhirakawa.wilson.models.style.WilsonStyle;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

@Immutable
@WilsonStyle
public interface Configuration {

  @Default
  default String getClusterId() {
    return "wilson-default";
  }

  @Default
  @JsonProperty("cluster")
  default Set<ImmutableClusterMember> getClusterMembers() {
    return Collections.singleton(getLocalMember());
  }

  @Default
  @JsonProperty("local")
  default ImmutableClusterMember getLocalMember() {
    return ImmutableClusterMember.builder()
        .setHost("localhost")
        .setPort(8080)
        .build();
  }

  @Default
  default long getLeaderTimeout() {
    return 100L;
  }

  @Default
  default long getElectionTimeout() {
    return 500L;
  }

  @Default
  default int getNumClientThreads() {
    return 10;
  }

  @Derived
  default Collection<ImmutableClusterMember> getPeers() {
    return getClusterMembers().stream()
        .filter(serverInfo -> !serverInfo.equals(getLocalMember()))
        .collect(Collectors.toSet());
  }

  @Lazy
  default Set<String> getClusterServerIds() {
    return getClusterMembers().stream()
        .map(ClusterMember::getServerId)
        .collect(ImmutableSet.toImmutableSet());
  }

  @Auxiliary
  default ClusterMember getClusterMemberFromId(String memberId) {
    return Maps.uniqueIndex(getClusterMembers(), ClusterMember::getServerId).get(memberId);
  }
}
