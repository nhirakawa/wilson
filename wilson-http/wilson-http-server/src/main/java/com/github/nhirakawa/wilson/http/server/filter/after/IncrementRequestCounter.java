package com.github.nhirakawa.wilson.http.server.filter.after;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import javax.inject.Inject;
import org.jetbrains.annotations.NotNull;

public class IncrementRequestCounter implements Handler {

  @Inject
  public IncrementRequestCounter() {}

  @Override
  public void handle(@NotNull Context ctx) throws Exception {}
}
