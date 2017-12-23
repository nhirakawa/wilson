package com.github.nhirakawa.server.timeout;

import java.util.concurrent.ScheduledExecutorService;

import com.github.nhirakawa.server.config.Configuration;
import com.github.nhirakawa.server.raft.StateMachineMessageApplier;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ElectionTimeout extends BaseTimeout {

  private final StateMachineMessageApplier messageApplier;

  @Inject
  ElectionTimeout(ScheduledExecutorService scheduledExecutorService,
                  Configuration configuration,
                  StateMachineMessageApplier messageApplier) {
    super(scheduledExecutorService, configuration.getElectionTimeout());
    this.messageApplier = messageApplier;
  }

  @Override
  protected void doTimeout() throws Exception {

  }
}
