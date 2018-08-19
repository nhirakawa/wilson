package com.github.nhirakawa.server.timeout;

import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.github.nhirakawa.server.models.ClusterMemberModel;
import com.github.nhirakawa.server.dagger.LocalMember;
import com.google.common.base.Preconditions;

abstract class BaseTimeout {

  private static final Logger LOG = LoggerFactory.getLogger(BaseTimeout.class);

  private static final long INITIAL_DELAY = 0L;

  private final ScheduledExecutorService scheduledExecutorService;
  private final long period;
  private final String serverId;

  private Optional<ScheduledFuture<?>> scheduledFuture;

  protected BaseTimeout(ScheduledExecutorService scheduledExecutorService,
                        long period,
                        @LocalMember ClusterMemberModel clusterMember) {
    this.scheduledExecutorService = scheduledExecutorService;
    this.period = period + ThreadLocalRandom.current().nextInt(50, 150);
    this.serverId = clusterMember.getServerId();

    this.scheduledFuture = Optional.empty();
  }

  protected abstract void doTimeout() throws Exception;

  protected long getPeriod() {
    return period;
  }

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
    MDC.put("serverId", serverId);
    try {
      doTimeout();
    } catch (Exception e) {
      LOG.error("Timeout encountered exception", e);
    } finally {
      MDC.remove("serverId");
    }
  }
}
