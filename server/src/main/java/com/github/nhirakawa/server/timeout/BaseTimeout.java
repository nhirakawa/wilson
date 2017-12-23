package com.github.nhirakawa.server.timeout;

import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

abstract class BaseTimeout {

  private static final Logger LOG = LoggerFactory.getLogger(BaseTimeout.class);

  private static final long INITIAL_DELAY = 0L;

  private final ScheduledExecutorService scheduledExecutorService;
  private final long period;

  private Optional<ScheduledFuture<?>> scheduledFuture;

  protected BaseTimeout(ScheduledExecutorService scheduledExecutorService,
                        long period) {
    this.scheduledExecutorService = scheduledExecutorService;
    this.period = period;

    this.scheduledFuture = Optional.empty();
  }

  protected abstract void doTimeout() throws Exception;

  public void start() {
    Preconditions.checkState(
        !scheduledFuture.isPresent(),
        "%s is already started",
        getClass().getSimpleName()
    );

    scheduledFuture = Optional.of(
        scheduledExecutorService.scheduleAtFixedRate(
            this::doSafeTimeout,
            INITIAL_DELAY,
            period,
            TimeUnit.MILLISECONDS
        )
    );
  }

  public void stop() {
    scheduledFuture.ifPresent(future -> future.cancel(false));
  }

  private void doSafeTimeout() {
    try {
      doTimeout();
    } catch (Exception e) {
      LOG.error("Timeout encountered exception", e);
    }
  }
}
