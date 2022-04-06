package com.github.nhirakawa.wilson.protocol.service;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.AbstractIdleService;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeadLetterLogger extends AbstractIdleService {
  private static final Logger LOG = LoggerFactory.getLogger(
    DeadLetterLogger.class
  );

  private final EventBus eventBus;

  @Inject
  DeadLetterLogger(EventBus eventBus) {
    this.eventBus = eventBus;
  }

  @Override
  protected void startUp() throws Exception {
    eventBus.register(this);
  }

  @Override
  protected void shutDown() throws Exception {
    eventBus.register(this);
  }

  @Subscribe
  public void logDeadLetter(DeadEvent deadEvent) {
    LOG.warn(
      "Found dead letter - source:{}, event:{}",
      deadEvent.getSource(),
      deadEvent.getEvent()
    );
  }
}
