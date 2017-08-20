package com.github.nhirakawa.server.timeout;

import java.util.concurrent.ScheduledExecutorService;

import com.github.nhirakawa.server.config.ClusterMember;
import com.github.nhirakawa.server.config.Configuration;
import com.github.nhirakawa.server.guice.LocalMember;
import com.github.nhirakawa.server.raft.StateMachineMessageApplier;
import com.github.nhirakawa.wilson.models.messages.ImmutableHeartbeatTimeoutMessage;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class HeartbeatTimeout extends BaseTimeout {

  private final StateMachineMessageApplier applier;

  @Inject
  HeartbeatTimeout(ScheduledExecutorService scheduledExecutorService,
                   Configuration configuration,
                   StateMachineMessageApplier applier,
                   @LocalMember ClusterMember clusterMember) {
    super(scheduledExecutorService, configuration.getHeartbeatTimeoutWithJitter(), clusterMember);
    this.applier = applier;
  }

  @Override
  protected void doTimeout() {
    applier.apply(ImmutableHeartbeatTimeoutMessage.builder()
        .setHeartbeatTimeout(getPeriod())
        .build());
  }
}
