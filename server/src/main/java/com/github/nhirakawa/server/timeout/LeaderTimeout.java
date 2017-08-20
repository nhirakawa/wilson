package com.github.nhirakawa.server.timeout;

import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nhirakawa.server.config.ClusterMember;
import com.github.nhirakawa.server.config.Configuration;
import com.github.nhirakawa.server.guice.LocalMember;
import com.github.nhirakawa.server.raft.StateMachineMessageApplier;
import com.github.nhirakawa.wilson.models.messages.ImmutableLeaderTimeoutMessage;
import com.google.inject.Inject;

public class LeaderTimeout extends BaseTimeout {

  private static final Logger LOG = LoggerFactory.getLogger(LeaderTimeout.class);

  private final StateMachineMessageApplier stateMachineMessageApplier;

  @Inject
  LeaderTimeout(ScheduledExecutorService scheduledExecutorService,
                Configuration configuration,
                StateMachineMessageApplier stateMachineMessageApplier,
                @LocalMember ClusterMember clusterMember) {
    super(scheduledExecutorService, configuration.getLeaderTimeoutWithJitter(), clusterMember);
    this.stateMachineMessageApplier = stateMachineMessageApplier;
  }

  @Override
  protected void doTimeout() {
    stateMachineMessageApplier.apply(
        ImmutableLeaderTimeoutMessage.builder()
            .setLeaderTimeout(getPeriod())
            .build()
    );
  }
}
