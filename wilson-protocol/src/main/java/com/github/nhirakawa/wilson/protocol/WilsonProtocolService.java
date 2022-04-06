package com.github.nhirakawa.wilson.protocol;

import com.github.nhirakawa.wilson.protocol.annotation.WilsonProtocol;
import com.google.common.util.concurrent.AbstractIdleService;
import com.google.common.util.concurrent.ServiceManager;
import javax.inject.Inject;

public class WilsonProtocolService extends AbstractIdleService {
  private final ServiceManager serviceManager;

  @Inject
  WilsonProtocolService(@WilsonProtocol ServiceManager serviceManager) {
    this.serviceManager = serviceManager;
  }

  @Override
  protected void startUp() throws Exception {
    serviceManager.startAsync();
    serviceManager.awaitHealthy();
  }

  @Override
  protected void shutDown() throws Exception {
    serviceManager.stopAsync();
    serviceManager.awaitStopped();
  }
}
