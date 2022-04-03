package com.github.nhirakawa.wilson.http.server.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.nhirakawa.wilson.common.ObjectMapperWrapper;
import com.github.nhirakawa.wilson.models.ClusterMember;
import com.github.nhirakawa.wilson.protocol.config.WilsonConfig;
import com.google.common.collect.Sets;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public final class ConfigLoader {

  private ConfigLoader() {
    throw new UnsupportedOperationException();
  }

  /**
   * @return the config for the local member
   */
  public static WilsonConfig loadSingle() {
    Config config = ConfigFactory.load();

    ClusterMember localClusterMember = parseLocalClusterMember(config);
    Set<ClusterMember> clusterMembers = parseClusterMembers(config);

    return WilsonConfig
      .builder()
      .setClusterId(config.getString("wilson.clusterId"))
      .setHeartbeatTimeout(
        Duration.ofMillis(config.getLong("wilson.timeout.heartbeat"))
      )
      .setLeaderTimeout(
        Duration.ofMillis(config.getLong("wilson.timeout.leader"))
      )
      .setElectionTimeout(
        Duration.ofMillis(config.getLong("wilson.timeout.election"))
      )
      .setLocalMember(localClusterMember)
      .setClusterMembers(clusterMembers)
      .build();
  }

  /**
   * @return a list of configs, one for each member to run in the JVM
   */
  public static List<WilsonConfig> loadCluster() {
    Config config = ConfigFactory.load();

    Set<ClusterMember> clusterMembers = parseClusterMembers(config);

    List<WilsonConfig> configs = new ArrayList<>(clusterMembers.size());

    for (ClusterMember clusterMember : clusterMembers) {
      WilsonConfig wilsonConfig = WilsonConfig
        .builder()
        .setClusterId(config.getString("wilson.clusterId"))
        .setHeartbeatTimeout(
          Duration.ofMillis(config.getLong("wilson.timeout.heartbeat"))
        )
        .setLeaderTimeout(
          Duration.ofMillis(config.getLong("wilson.timeout.leader"))
        )
        .setElectionTimeout(
          Duration.ofMillis(config.getLong("wilson.timeout.election"))
        )
        .setLocalMember(clusterMember)
        .setClusterMembers(
          Sets.difference(clusterMembers, Collections.singleton(clusterMember))
        )
        .build();

      configs.add(wilsonConfig);
    }

    return Collections.unmodifiableList(configs);
  }

  private static ClusterMember parseLocalClusterMember(Config config) {
    try {
      return ObjectMapperWrapper
        .instance()
        .readValue(
          config
            .getObject("wilson.local")
            .render(ConfigRenderOptions.concise()),
          ClusterMember.class
        );
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  private static Set<ClusterMember> parseClusterMembers(Config config) {
    try {
      return ObjectMapperWrapper
        .instance()
        .readValue(
          config
            .getObject("wilson.cluster")
            .render(ConfigRenderOptions.concise()),
          new TypeReference<Set<ClusterMember>>() {}
        );
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}
