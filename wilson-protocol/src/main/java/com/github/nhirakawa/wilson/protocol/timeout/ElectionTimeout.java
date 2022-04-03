package com.github.nhirakawa.wilson.protocol.timeout;

import com.github.nhirakawa.wilson.models.messages.ElectionTimeoutMessage;
import com.github.nhirakawa.wilson.protocol.config.WilsonConfig;
import com.github.nhirakawa.wilson.protocol.StateMachineMessageApplier;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ElectionTimeout extends BaseTimeout {
  private final StateMachineMessageApplier messageApplier;

  @Inject
  ElectionTimeout(
    WilsonConfig wilsonConfig,
    StateMachineMessageApplier messageApplier
  ) {
    super(wilsonConfig.getElectionTimeout());
    this.messageApplier = messageApplier;
  }

  @Override
  protected void runOneIteration() {
    messageApplier.apply(
      ElectionTimeoutMessage
        .builder()
        .setElectionTimeout(getPeriod().toMillis())
        .build()
    );
  }
}
