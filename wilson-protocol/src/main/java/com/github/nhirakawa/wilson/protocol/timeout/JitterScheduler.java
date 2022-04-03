package com.github.nhirakawa.wilson.protocol.timeout;

import com.google.common.util.concurrent.AbstractScheduledService.CustomScheduler;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

class JitterScheduler extends CustomScheduler {
  private static final Duration MIN_JITTER = Duration.ofMillis(10);
  private static final Duration MAX_JITTER = Duration.ofMillis(150);

  private final Duration period;

  protected JitterScheduler(Duration period) {
    this.period = period;
  }

  @Override
  protected Schedule getNextSchedule() {
    Duration delay = calculateWithJitter();
    return new Schedule(delay.toMillis(), TimeUnit.MILLISECONDS);
  }

  private Duration calculateWithJitter() {
    long jitterMillis = ThreadLocalRandom
      .current()
      .nextLong(MIN_JITTER.toMillis(), MAX_JITTER.toMillis());
    Duration jitter = Duration.ofMillis(jitterMillis);

    return period.plus(jitter);
  }
}
