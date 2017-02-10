package com.github.nhirakawa.server.transport.netty;

import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.nhirakawa.server.config.Configuration;
import com.github.nhirakawa.wilson.models.messages.HeartbeatMessage;
import com.github.nhirakawa.wilson.models.messages.Message;
import com.google.inject.Inject;

public class HeartbeatTask {

  private static final Logger LOG = LogManager.getLogger(HeartbeatTask.class);

  private final Configuration configuration;
  private final ScheduledExecutorService scheduledExecutorService;
  private final ConnectionManager connectionManager;

  private Optional<ScheduledFuture<?>> scheduledFuture;

  @Inject
  public HeartbeatTask(Configuration configuration,
                       ScheduledExecutorService scheduledExecutorService,
                       ConnectionManager connectionManager) {
    this.configuration = configuration;
    this.scheduledExecutorService = scheduledExecutorService;
    this.connectionManager = connectionManager;
    this.scheduledFuture = Optional.empty();
  }

  public void start() {
    if (scheduledFuture.isPresent()) {
      return;
    }

    scheduledFuture = Optional.of(scheduledExecutorService.scheduleAtFixedRate(
        () -> {
//          LOG.debug("heartbeat");
          connectionManager.broadcast(buildHeartbeatMessage());
        },
        1,
        1,
        TimeUnit.SECONDS
    ));
  }

  private Message buildHeartbeatMessage() {
    return HeartbeatMessage.builder()
        .setClusterId(configuration.getClusterId())
        .setServerId(configuration.getServerId())
        .build();
  }
}
