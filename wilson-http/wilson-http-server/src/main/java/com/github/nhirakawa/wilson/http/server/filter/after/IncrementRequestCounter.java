package com.github.nhirakawa.wilson.http.server.filter.after;

import javax.inject.Inject;
import spark.Filter;
import spark.Request;
import spark.Response;

public class IncrementRequestCounter implements Filter {

  @Inject
  public IncrementRequestCounter() {}

  @Override
  public void handle(Request request, Response response) throws Exception {
    // todo implement
  }
}
