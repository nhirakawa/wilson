package com.github.nhirakawa.wilson.http.server.filter.before;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import javax.inject.Inject;

/**
 * Set Content-Type according to Accept
 */
public class SetContentEncoding implements Handler {

  @Inject
  SetContentEncoding() {}

  @Override
  public void handle(Context ctx) throws Exception {
    //    ctx.contentType(ContentType.APPLICATION_GZ);
  }
}
