package com.github.nhirakawa.wilson.http.server;

import com.github.nhirakawa.wilson.common.NamedThreadFactory;
import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.ServiceManager;
import com.google.common.util.concurrent.ServiceManager.Listener;
import java.util.concurrent.Executors;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WilsonServerManager {
  private final ServiceManager serviceManager;

  @Inject
  WilsonServerManager(ServiceManager serviceManager) {
    this.serviceManager = serviceManager;
  }

  public void run() {
    serviceManager.addListener(
      new FailedServiceWatchdog(),
      Executors.newSingleThreadExecutor(
        NamedThreadFactory.build("failed-service-watchdog")
      )
    );
    serviceManager.startAsync();
    serviceManager.awaitHealthy();
  }

  static class FailedServiceWatchdog extends Listener {
    private static final Logger LOG = LoggerFactory.getLogger(
      FailedServiceWatchdog.class
    );

    @Override
    public void failure(Service service) {
      LOG.error("Service {} failed", service.getClass().getSimpleName());
      System.exit(1);
    }
  }
}
