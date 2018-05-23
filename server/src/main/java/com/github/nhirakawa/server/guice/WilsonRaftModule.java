package com.github.nhirakawa.server.guice;

import static com.github.nhirakawa.server.jackson.ObjectMapperWrapper.readValueFromConfig;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import com.github.nhirakawa.server.config.ClusterMember;
import com.github.nhirakawa.server.config.ConfigPath;
import com.github.nhirakawa.server.jackson.ObjectMapperWrapper;
import com.github.nhirakawa.server.raft.WilsonState;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.typesafe.config.Config;

public class WilsonRaftModule extends AbstractModule {

  private final Config config;


  public WilsonRaftModule(Config config) {
    this.config = config;
  }

  @Override
  protected void configure() {
    bind(Config.class).toInstance(config);
  }

  @Provides
  @Singleton
  AtomicReference<WilsonState> provideAtomicWilsonState() {
    WilsonState wilsonState = WilsonState.builder().build();
    return new AtomicReference<>(wilsonState);
  }

  @Provides
  @Singleton
  @LocalMember
  ClusterMember provideLocalClusterMember(Config config) throws IOException {
    return readValueFromConfig(config, ConfigPath.WILSON_LOCAL_ADDRESS);
  }

  @Provides
  @Singleton
  Set<ClusterMember> provideClusterMembers(Config config,
                                           @LocalMember ClusterMember clusterMember) throws IOException {
    Set<ClusterMember> clusterMembers = ObjectMapperWrapper.readValueFromConfig(config, ConfigPath.WILSON_CLUSTER_ADDRESSES);
    return clusterMembers.stream()
        .filter(member -> !Objects.equals(member, clusterMember))
        .collect(ImmutableSet.toImmutableSet());
  }
}
