package com.github.nhirakawa.wilson.http.server.filter.before;

import com.github.nhirakawa.wilson.http.common.WilsonHeaders;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import java.util.UUID;
import javax.inject.Inject;

/**
 * If X-Wilson-Request-Id header is missing, set it
 */
public class SetRequestId implements Handler {

  @Inject
  SetRequestId() {}

  @Override
  public void handle(Context ctx) throws Exception {
    if (ctx.header(WilsonHeaders.requestId()) == null) {
      ctx.header(WilsonHeaders.requestId(), UUID.randomUUID().toString());
    }
  }
}
