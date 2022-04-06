package com.github.nhirakawa.wilson.protocol.timeout;

import com.github.nhirakawa.wilson.models.messages.ElectionTimeoutMessage;
import com.github.nhirakawa.wilson.protocol.config.WilsonConfig;
import com.github.nhirakawa.wilson.protocol.StateMachineMessageApplier;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ElectionTimeout extends BaseTimeout {
  private static final Logger LOG = LoggerFactory.getLogger(
    ElectionTimeout.class
  );

  private final WilsonConfig wilsonConfig;
  private final StateMachineMessageApplier messageApplier;

  @Inject
  ElectionTimeout(
    WilsonConfig wilsonConfig,
    StateMachineMessageApplier messageApplier
  ) {
    super(wilsonConfig.getElectionTimeout());
    this.wilsonConfig = wilsonConfig;
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

  @Override
  protected Logger logger() {
    return LOG;
  }

  @Override
  protected String serviceName() {
    return String.format(
      "%s-%s",
      ElectionTimeout.class.getSimpleName(),
      wilsonConfig.getLocalMember().getServerId()
    );
  }
}
