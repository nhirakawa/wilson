package com.github.nhirakawa.wilson.protocol.timeout;

import com.google.common.util.concurrent.AbstractScheduledService;
import java.time.Duration;

abstract class BaseTimeout extends AbstractScheduledService {
  private final Duration period;

  protected BaseTimeout(long period) {
    this(Duration.ofMillis(period));
  }

  protected BaseTimeout(Duration period) {
    this.period = period;
  }

  @Override
  protected Scheduler scheduler() {
    return new JitterScheduler(getPeriod());
  }

  public Duration getPeriod() {
    return period;
  }
}
