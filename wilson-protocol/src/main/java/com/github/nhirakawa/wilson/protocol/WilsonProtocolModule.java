package com.github.nhirakawa.wilson.protocol;

import com.github.nhirakawa.wilson.common.NamedThreadFactory;
import com.github.nhirakawa.wilson.models.ClusterMember;
import com.github.nhirakawa.wilson.models.WilsonState;
import com.github.nhirakawa.wilson.protocol.config.WilsonConfig;
import com.google.common.eventbus.EventBus;
import dagger.Module;
import dagger.Provides;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.Set;
import javax.inject.Singleton;

@Module
public class WilsonProtocolModule {
  private final WilsonConfig wilsonConfig;

  public WilsonProtocolModule(WilsonConfig wilsonConfig) {
    this.wilsonConfig = wilsonConfig;
  }

  @Provides
  protected WilsonConfig provideConfig() {
    return wilsonConfig;
  }

  @Provides
  @LocalMember
  static ClusterMember provideLocalMember(WilsonConfig wilsonConfig) {
    return wilsonConfig.getLocalMember();
  }

  @Provides
  static Set<ClusterMember> provideClusterMembers(WilsonConfig wilsonConfig) {
    return wilsonConfig.getClusterMembers();
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
