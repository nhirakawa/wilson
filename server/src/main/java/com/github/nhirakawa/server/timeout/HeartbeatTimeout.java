package com.github.nhirakawa.server.timeout;

import java.util.concurrent.ScheduledExecutorService;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.github.nhirakawa.server.config.ConfigPath;
import com.github.nhirakawa.server.dagger.LocalMember;
import com.github.nhirakawa.server.models.ClusterMember;
import com.github.nhirakawa.server.models.messages.HeartbeatTimeoutMessage;
import com.github.nhirakawa.server.raft.StateMachineMessageApplier;
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
