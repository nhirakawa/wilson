package com.github.nhirakawa.server.timeout;

import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nhirakawa.server.config.ClusterMember;
import com.github.nhirakawa.server.config.Configuration;
import com.github.nhirakawa.server.guice.LocalMember;
import com.github.nhirakawa.server.raft.StateMachineMessageApplier;
import com.github.nhirakawa.wilson.models.messages.ImmutableElectionTimeoutMessage;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ElectionTimeout extends BaseTimeout {

  private static final Logger LOG = LoggerFactory.getLogger(ElectionTimeout.class);

  private final StateMachineMessageApplier messageApplier;

  @Inject
  ElectionTimeout(ScheduledExecutorService scheduledExecutorService,
                  Configuration configuration,
                  StateMachineMessageApplier messageApplier,
                  @LocalMember ClusterMember clusterMember) {
    super(scheduledExecutorService, configuration.getElectionTimeoutWithJitter(), clusterMember);
    this.messageApplier = messageApplier;
  }

  @Override
  protected void doTimeout() {
    messageApplier.apply(
        ImmutableElectionTimeoutMessage.builder()
            .setElectionTimeout(getPeriod())
            .build()
    );
  }
}
