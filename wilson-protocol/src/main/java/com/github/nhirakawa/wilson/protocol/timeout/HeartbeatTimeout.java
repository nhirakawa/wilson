package com.github.nhirakawa.wilson.protocol.timeout;

import com.github.nhirakawa.wilson.models.messages.HeartbeatTimeoutMessage;
import com.github.nhirakawa.wilson.protocol.config.WilsonConfig;
import com.github.nhirakawa.wilson.protocol.StateMachineMessageApplier;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class HeartbeatTimeout extends BaseTimeout {
  private final StateMachineMessageApplier applier;

  @Inject
  HeartbeatTimeout(
    WilsonConfig wilsonConfig,
    StateMachineMessageApplier applier
  ) {
    super(wilsonConfig.getHeartbeatTimeout());
    this.applier = applier;
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
}
