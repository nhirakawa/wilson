package com.github.nhirakawa.server.timeout;

import java.util.concurrent.ScheduledExecutorService;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.github.nhirakawa.server.config.ConfigPath;
import com.github.nhirakawa.server.dagger.LocalMember;
import com.github.nhirakawa.server.models.ClusterMember;
import com.github.nhirakawa.server.models.messages.ElectionTimeoutMessage;
import com.github.nhirakawa.server.raft.StateMachineMessageApplier;
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
