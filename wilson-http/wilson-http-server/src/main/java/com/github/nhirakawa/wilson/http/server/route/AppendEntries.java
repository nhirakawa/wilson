package com.github.nhirakawa.wilson.http.server.route;

import javax.inject.Inject;
import spark.Request;
import spark.Response;
import spark.Route;

public class AppendEntries implements Route {

  @Inject
  public AppendEntries() {}

  @Override
  public Object handle(Request request, Response response) throws Exception {
    throw new UnsupportedOperationException();
  }
}
