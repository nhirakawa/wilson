package com.github.nhirakawa.wilson.protocol.timeout;

import com.github.nhirakawa.wilson.common.config.ConfigPath;
import com.github.nhirakawa.wilson.models.ClusterMember;
import com.github.nhirakawa.wilson.models.messages.HeartbeatTimeoutMessage;
import com.github.nhirakawa.wilson.protocol.LocalMember;
import com.github.nhirakawa.wilson.protocol.StateMachineMessageApplier;
import com.typesafe.config.Config;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class HeartbeatTimeout extends BaseTimeout {
  private final StateMachineMessageApplier applier;

  @Inject
  HeartbeatTimeout(
    ScheduledExecutorService scheduledExecutorService,
    Config config,
    StateMachineMessageApplier applier,
    @LocalMember ClusterMember clusterMember
  ) {
    super(config.getLong(ConfigPath.WILSON_HEARTBEAT_TIMEOUT.getPath()));
    this.applier = applier;
  }

  @Override
  protected void runOneIteration() {
    applier.apply(
      HeartbeatTimeoutMessage
        .builder()
        .setHeartbeatTimeout(getPeriod().toMillis())
        .build()
    );
  }
}
