package com.github.nhirakawa.wilson.http.server;

import com.google.common.util.concurrent.AbstractIdleService;
import javax.inject.Inject;

public class WilsonServer extends AbstractIdleService {
  private final WilsonHttpServer wilsonHttpServer;

  @Inject
  WilsonServer(WilsonHttpServer wilsonHttpServer) {
    this.wilsonHttpServer = wilsonHttpServer;
  }

  @Override
  protected void startUp() throws Exception {
    wilsonHttpServer.startUp();
  }

  @Override
  protected void shutDown() throws Exception {
    wilsonHttpServer.shutDown();
  }
}
