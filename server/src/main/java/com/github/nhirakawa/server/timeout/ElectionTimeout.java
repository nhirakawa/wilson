package com.github.nhirakawa.server.timeout;

import java.util.concurrent.ScheduledExecutorService;

import com.github.nhirakawa.server.config.ClusterMember;
import com.github.nhirakawa.server.config.ClusterMemberModel;
import com.github.nhirakawa.server.config.ConfigPath;
import com.github.nhirakawa.server.guice.LocalMember;
import com.github.nhirakawa.server.raft.StateMachineMessageApplier;
import com.github.nhirakawa.wilson.models.messages.ElectionTimeoutMessage;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;

@Singleton
public class ElectionTimeout extends BaseTimeout {

  private final StateMachineMessageApplier messageApplier;

  @Inject
  ElectionTimeout(ScheduledExecutorService scheduledExecutorService,
                  Config config,
                  StateMachineMessageApplier messageApplier,
                  @LocalMember ClusterMember clusterMember) {
    super(
        scheduledExecutorService,
        config.getLong(ConfigPath.WILSON_ELECTION_TIMEOUT.getPath()),
        clusterMember
    );

    this.messageApplier = messageApplier;
  }

  @Override
  protected void doTimeout() {
    messageApplier.apply(
        ElectionTimeoutMessage.builder()
            .setElectionTimeout(getPeriod())
            .build()
    );
  }
}
