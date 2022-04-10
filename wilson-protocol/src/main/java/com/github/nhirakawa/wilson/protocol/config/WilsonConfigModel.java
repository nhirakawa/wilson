package com.github.nhirakawa.wilson.protocol.config;

import com.github.nhirakawa.wilson.models.ClusterMember;
import com.github.nhirakawa.wilson.models.style.WilsonStyle;
import com.google.common.base.Preconditions;
import java.time.Duration;
import java.util.Set;
import org.immutables.value.Value;

@Value.Immutable
@WilsonStyle
public interface WilsonConfigModel {
  String getClusterId();
  Duration getLeaderTimeout();
  Duration getHeartbeatTimeout();
  Duration getElectionTimeout();
  ClusterMember getLocalMember();
  Set<ClusterMember> getClusterMembers();

  @Value.Check
  default void validate() {
    Preconditions.checkArgument(
      !getClusterMembers().contains(getLocalMember()),
      "Cluster members cannot contain local member"
    );
  }
}
