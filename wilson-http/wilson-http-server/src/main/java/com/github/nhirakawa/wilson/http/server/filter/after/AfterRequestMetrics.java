package com.github.nhirakawa.wilson.http.server.filter.after;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import javax.inject.Inject;

/**
 * Process metrics after the request
 */
public class AfterRequestMetrics implements Handler {

  @Inject
  public AfterRequestMetrics() {}

  @Override
  public void handle(Context ctx) throws Exception {}
}
