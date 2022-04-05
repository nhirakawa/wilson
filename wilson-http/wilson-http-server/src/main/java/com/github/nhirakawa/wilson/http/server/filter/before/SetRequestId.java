package com.github.nhirakawa.wilson.http.server.filter.before;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import java.util.UUID;
import javax.inject.Inject;
import org.jetbrains.annotations.NotNull;

/**
 * If X-Wilson-Request-Id header is missing, set it
 */
public class SetRequestId implements Handler {

  @Inject
  SetRequestId() {}

  @Override
  public void handle(@NotNull Context ctx) throws Exception {
    if (ctx.header("X-Wilson-Request-Id") == null) {
      ctx.header("X-Wilson-Request-Id", UUID.randomUUID().toString());
    }
  }
}
