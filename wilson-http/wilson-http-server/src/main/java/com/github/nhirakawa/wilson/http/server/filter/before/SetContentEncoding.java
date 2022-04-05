package com.github.nhirakawa.wilson.http.server.filter.before;

import io.javalin.http.ContentType;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import javax.inject.Inject;
import org.jetbrains.annotations.NotNull;

public class SetContentEncoding implements Handler {

  @Inject
  SetContentEncoding() {}

  @Override
  public void handle(@NotNull Context ctx) throws Exception {
    ctx.contentType(ContentType.APPLICATION_GZ);
  }
}
