package com.github.nhirakawa.wilson.protocol.timeout;

import com.github.nhirakawa.wilson.common.config.ConfigPath;
import com.github.nhirakawa.wilson.models.messages.ElectionTimeoutMessage;
import com.github.nhirakawa.wilson.protocol.StateMachineMessageApplier;
import com.typesafe.config.Config;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ElectionTimeout extends BaseTimeout {
  private final StateMachineMessageApplier messageApplier;

  @Inject
  ElectionTimeout(Config config, StateMachineMessageApplier messageApplier) {
    super(config.getLong(ConfigPath.WILSON_ELECTION_TIMEOUT.getPath()));
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
