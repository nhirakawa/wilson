package com.github.nhirakawa.wilson.protocol.timeout;

import com.github.nhirakawa.wilson.models.messages.LeaderTimeoutMessage;
import com.github.nhirakawa.wilson.protocol.config.WilsonConfig;
import com.github.nhirakawa.wilson.protocol.StateMachineMessageApplier;
import javax.inject.Inject;

public class LeaderTimeout extends BaseTimeout {
  private final StateMachineMessageApplier stateMachineMessageApplier;

  @Inject
  LeaderTimeout(
    WilsonConfig wilsonConfig,
    StateMachineMessageApplier stateMachineMessageApplier
  ) {
    super(wilsonConfig.getLeaderTimeout());
    this.stateMachineMessageApplier = stateMachineMessageApplier;
  }

  @Override
  protected void runOneIteration() {
    stateMachineMessageApplier.apply(
      LeaderTimeoutMessage
        .builder()
        .setLeaderTimeout(getPeriod().toMillis())
        .build()
    );
  }
}
