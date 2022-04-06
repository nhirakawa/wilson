package com.github.nhirakawa.wilson.protocol;

import com.github.nhirakawa.wilson.common.NamedThreadFactory;
import com.github.nhirakawa.wilson.models.ClusterMember;
import com.github.nhirakawa.wilson.models.WilsonState;
import com.github.nhirakawa.wilson.protocol.annotation.LocalMember;
import com.github.nhirakawa.wilson.protocol.annotation.WilsonProtocol;
import com.github.nhirakawa.wilson.protocol.config.WilsonConfig;
import com.github.nhirakawa.wilson.protocol.service.DeadLetterLogger;
import com.github.nhirakawa.wilson.protocol.service.MessageSender;
import com.github.nhirakawa.wilson.protocol.service.timeout.ElectionTimeout;
import com.github.nhirakawa.wilson.protocol.service.timeout.HeartbeatTimeout;
import com.github.nhirakawa.wilson.protocol.service.timeout.LeaderTimeout;
import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.ServiceManager;
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

  @Provides
  @Singleton
  @WilsonProtocol
  ServiceManager provideWilsonProtocolServiceManager(
    DeadLetterLogger deadLetterLogger,
    MessageSender messageSender,
    ElectionTimeout electionTimeout,
    HeartbeatTimeout heartbeatTimeout,
    LeaderTimeout leaderTimeout
  ) {
    return new ServiceManager(
      ImmutableList.of(
        deadLetterLogger,
        messageSender,
        electionTimeout,
        heartbeatTimeout,
        leaderTimeout
      )
    );
  }
}
