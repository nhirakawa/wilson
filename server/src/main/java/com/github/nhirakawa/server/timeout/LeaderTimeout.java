package com.github.nhirakawa.server.timeout;

import java.util.concurrent.ScheduledExecutorService;

import com.github.nhirakawa.server.config.ClusterMemberModel;
import com.github.nhirakawa.server.config.ConfigPath;
import com.github.nhirakawa.server.guice.LocalMember;
import com.github.nhirakawa.server.raft.StateMachineMessageApplier;
import com.github.nhirakawa.wilson.models.messages.LeaderTimeoutMessage;
import com.google.inject.Inject;
import com.typesafe.config.Config;

public class LeaderTimeout extends BaseTimeout {

  private final StateMachineMessageApplier stateMachineMessageApplier;

  @Inject
  LeaderTimeout(ScheduledExecutorService scheduledExecutorService,
                Config config,
                StateMachineMessageApplier stateMachineMessageApplier,
                @LocalMember ClusterMemberModel clusterMember) {
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
        LeaderTimeoutMessage.builder()
            .setLeaderTimeout(getPeriod())
            .build()
    );
  }
}
