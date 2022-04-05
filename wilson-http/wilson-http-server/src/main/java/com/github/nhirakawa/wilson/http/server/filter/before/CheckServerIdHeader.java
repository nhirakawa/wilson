package com.github.nhirakawa.wilson.http.server.filter.before;

import javax.inject.Inject;

import org.jetbrains.annotations.NotNull;

import com.github.nhirakawa.wilson.http.common.WilsonHeaders;
import com.google.common.base.Strings;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.HttpCode;

/**
 * Return 400 if X-Wilson-Server-Id header is missing or empty
 */
public class CheckServerIdHeader implements Handler {

  @Inject
  CheckServerIdHeader() {}

  @Override
  public void handle(@NotNull Context ctx) throws Exception {
    if (Strings.emptyToNull(ctx.header(WilsonHeaders.serverId())) == null) {
      ctx
        .status(HttpCode.BAD_REQUEST)
        .result("Missing X-Wilson-Server-Id header");
    }
  }
}