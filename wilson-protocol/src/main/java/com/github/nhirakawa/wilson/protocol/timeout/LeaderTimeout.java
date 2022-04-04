package com.github.nhirakawa.wilson.protocol.timeout;

import com.github.nhirakawa.wilson.models.messages.LeaderTimeoutMessage;
import com.github.nhirakawa.wilson.protocol.config.WilsonConfig;
import com.github.nhirakawa.wilson.protocol.StateMachineMessageApplier;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LeaderTimeout extends BaseTimeout {
  private static final Logger LOG = LoggerFactory.getLogger(
    LeaderTimeout.class
  );

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

  @Override
  protected Logger logger() {
    return LOG;
  }
}
