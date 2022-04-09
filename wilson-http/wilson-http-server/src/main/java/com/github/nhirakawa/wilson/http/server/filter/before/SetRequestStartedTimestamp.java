package com.github.nhirakawa.wilson.http.server.filter.before;

import com.github.nhirakawa.wilson.http.common.WilsonHeaders;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import java.time.Clock;
import javax.inject.Inject;

/**
 * Set timestamp for start of processing
 */
public class SetRequestStartedTimestamp implements Handler {
  // todo inject this
  private final Clock clock = Clock.systemUTC();

  @Inject
  SetRequestStartedTimestamp() {}

  @Override
  public void handle(Context ctx) throws Exception {
    ctx.header(
      WilsonHeaders.requestStarted(),
      Long.toString(clock.instant().toEpochMilli())
    );
  }
}
