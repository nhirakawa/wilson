package com.github.nhirakawa.wilson.protocol.service.timeout;

import com.github.nhirakawa.wilson.models.messages.HeartbeatTimeoutMessage;
import com.github.nhirakawa.wilson.protocol.config.WilsonConfig;
import com.github.nhirakawa.wilson.protocol.StateMachineMessageApplier;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class HeartbeatTimeout extends BaseTimeout {
  private static final Logger LOG = LoggerFactory.getLogger(
    HeartbeatTimeout.class
  );

  private final WilsonConfig wilsonConfig;
  private final StateMachineMessageApplier applier;

  @Inject
  HeartbeatTimeout(
    WilsonConfig wilsonConfig,
    StateMachineMessageApplier applier
  ) {
    super(wilsonConfig.getHeartbeatTimeout());
    this.applier = applier;
    this.wilsonConfig = wilsonConfig;
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

  @Override
  protected Logger logger() {
    return LOG;
  }

  @Override
  protected String serviceName() {
    return String.format(
      "%s-%s",
      HeartbeatTimeout.class.getSimpleName(),
      wilsonConfig.getLocalMember().getServerId()
    );
  }
}
