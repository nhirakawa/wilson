package com.github.nhirakawa.wilson.protocol.timeout;

import com.github.nhirakawa.wilson.common.config.ConfigPath;
import com.github.nhirakawa.wilson.models.ClusterMember;
import com.github.nhirakawa.wilson.models.messages.LeaderTimeoutMessage;
import com.github.nhirakawa.wilson.protocol.LocalMember;
import com.github.nhirakawa.wilson.protocol.StateMachineMessageApplier;
import com.typesafe.config.Config;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Inject;

public class LeaderTimeout extends BaseTimeout {
  private final StateMachineMessageApplier stateMachineMessageApplier;

  @Inject
  LeaderTimeout(
    ScheduledExecutorService scheduledExecutorService,
    Config config,
    StateMachineMessageApplier stateMachineMessageApplier,
    @LocalMember ClusterMember clusterMember
  ) {
    super(
      scheduledExecutorService,
      config.getLong(ConfigPath.WILSON_LEADER_TIMEOUT.getPath()),
      clusterMember
    );
    this.stateMachineMessageApplier = stateMachineMessageApplier;
  }

  @Override
  protected void doTimeout() {
    stateMachineMessageApplier.apply(
      LeaderTimeoutMessage.builder().setLeaderTimeout(getPeriod()).build()
    );
  }
}
