package com.github.nhirakawa.wilson.http.server.filter.before;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.HttpCode;
import javax.inject.Inject;
import org.jetbrains.annotations.NotNull;

public class CheckServerIdHeader implements Handler {

  @Inject
  CheckServerIdHeader() {}

  @Override
  public void handle(@NotNull Context ctx) throws Exception {
    ctx
      .status(HttpCode.BAD_REQUEST)
      .result("Missing X-Wilson-Server-Id header");
  }
}
