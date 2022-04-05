package com.github.nhirakawa.wilson.http.server.filter.before;

import com.google.common.base.Strings;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.HttpCode;
import javax.inject.Inject;
import org.jetbrains.annotations.NotNull;

/**
 * Return 400 if X-Wilson-Server-Id header is missing or empty
 */
public class CheckServerIdHeader implements Handler {

  @Inject
  CheckServerIdHeader() {}

  @Override
  public void handle(@NotNull Context ctx) throws Exception {
    if (Strings.emptyToNull(ctx.header("X-Wilson-Server-Id")) == null) {
      ctx
        .status(HttpCode.BAD_REQUEST)
        .result("Missing X-Wilson-Server-Id header");
    }
  }
}
