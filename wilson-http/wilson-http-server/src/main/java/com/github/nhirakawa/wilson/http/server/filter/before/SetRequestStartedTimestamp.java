package com.github.nhirakawa.wilson.http.server.filter.before;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import java.time.Clock;
import javax.inject.Inject;
import org.jetbrains.annotations.NotNull;

/**
 * Set timestamp for start of processing
 */
public class SetRequestStartedTimestamp implements Handler {
  // todo inject this
  private final Clock clock = Clock.systemUTC();

  @Inject
  SetRequestStartedTimestamp() {}

  @Override
  public void handle(@NotNull Context ctx) throws Exception {
    ctx.header(
      "X-Wilson-Request-Started",
      Long.toString(clock.instant().toEpochMilli())
    );
  }
}
