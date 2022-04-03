package com.github.nhirakawa.wilson.protocol;

import com.github.nhirakawa.wilson.common.NamedThreadFactory;
import com.github.nhirakawa.wilson.models.ClusterMember;
import com.github.nhirakawa.wilson.models.WilsonState;
import com.google.common.eventbus.EventBus;
import com.typesafe.config.Config;
import dagger.Module;
import dagger.Provides;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.Set;
import javax.inject.Singleton;

@Module
public class WilsonProtocolModule {
  private final Config config;
  private final ClusterMember localMember;
  private Set<ClusterMember> clusterMembers;

  public WilsonProtocolModule(
    Config config,
    ClusterMember localMember,
    Set<ClusterMember> clusterMembers
  ) {
    this.config = config;
    this.localMember = localMember;
    this.clusterMembers = clusterMembers;
  }

  @Provides
  protected Config provideConfig() {
    return config;
  }

  @Provides
  @LocalMember
  protected ClusterMember provideLocalMember() {
    return localMember;
  }

  @Provides
  protected Set<ClusterMember> provideClusterMembers() {
    return clusterMembers;
  }

  @Provides
  @Singleton
  AtomicReference<WilsonState> provideWilsonState() {
    WilsonState wilsonState = WilsonState.builder().build();
    return new AtomicReference<>(wilsonState);
  }

  @Provides
  @Singleton
  ScheduledExecutorService provideScheduledExecutorService(
    @LocalMember ClusterMember localMember
  ) {
    return Executors.newScheduledThreadPool(
      4,
      NamedThreadFactory.build("wilson-scheduled", localMember)
    );
  }

  @Provides
  @Singleton
  protected EventBus provideEventBus() {
    EventBus eventBus = new EventBus();
    return eventBus;
  }
}
