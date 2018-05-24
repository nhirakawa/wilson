package com.github.nhirakawa.server.timeout;

import java.util.concurrent.ScheduledExecutorService;

import com.github.nhirakawa.server.config.ClusterMember;
import com.github.nhirakawa.server.config.ClusterMemberModel;
import com.github.nhirakawa.server.config.ConfigPath;
import com.github.nhirakawa.server.guice.LocalMember;
import com.github.nhirakawa.server.raft.StateMachineMessageApplier;
import com.github.nhirakawa.wilson.models.messages.HeartbeatTimeoutMessage;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;

@Singleton
public class HeartbeatTimeout extends BaseTimeout {

  private final StateMachineMessageApplier applier;

  @Inject
  HeartbeatTimeout(ScheduledExecutorService scheduledExecutorService,
                   Config config,
                   StateMachineMessageApplier applier,
                   @LocalMember ClusterMember clusterMember) {
    super(
        scheduledExecutorService,
        config.getLong(ConfigPath.WILSON_HEARTBEAT_TIMEOUT.getPath()),
        clusterMember
    );

    this.applier = applier;
  }

  @Override
  protected void doTimeout() {
    applier.apply(
        HeartbeatTimeoutMessage.builder()
            .setHeartbeatTimeout(getPeriod())
            .build()
    );
  }
}
