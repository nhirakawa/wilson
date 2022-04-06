package com.github.nhirakawa.wilson.protocol.timeout;

import com.google.common.util.concurrent.AbstractScheduledService;
import java.time.Duration;
import org.slf4j.Logger;

abstract class BaseTimeout extends AbstractScheduledService {
  private final Duration period;

  protected BaseTimeout(long period) {
    this(Duration.ofMillis(period));
  }

  protected BaseTimeout(Duration period) {
    this.period = period;
  }

  protected abstract Logger logger();

  @Override
  protected Scheduler scheduler() {
    return new JitterScheduler(getPeriod());
  }

  public Duration getPeriod() {
    return period;
  }

  @Override
  protected void startUp() throws Exception {
    logger().info("Starting up");
  }

  @Override
  protected void shutDown() throws Exception {
    logger().debug("Shutting down");
  }

  @Override
  protected abstract String serviceName();
}
