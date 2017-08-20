package com.github.nhirakawa.server.timeout;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.nhirakawa.server.config.Configuration;
import com.github.nhirakawa.server.raft.StateMachineMessageApplier;
import com.github.nhirakawa.wilson.models.messages.ImmutableLeaderTimeoutMessage;
import com.google.inject.Inject;

public class LeaderTimeout extends BaseTimeout {

  private static final Logger LOG = LoggerFactory.getLogger(LeaderTimeout.class);

  private final StateMachineMessageApplier stateMachineMessageApplier;

  @Inject
  LeaderTimeout(ScheduledExecutorService scheduledExecutorService,
                Configuration configuration,
                StateMachineMessageApplier stateMachineMessageApplier) {
    super(scheduledExecutorService, configuration.getLeaderTimeout());
    this.stateMachineMessageApplier = stateMachineMessageApplier;
  }

  @Override
  protected void doTimeout() throws InterruptedException, MalformedURLException, JsonProcessingException, URISyntaxException, ExecutionException {
    stateMachineMessageApplier.apply(ImmutableLeaderTimeoutMessage.builder().build());
  }
}
