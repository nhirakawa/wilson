package com.github.nhirakawa.wilson.http.server.filter.before;

import javax.inject.Inject;
import spark.Filter;
import spark.Request;
import spark.Response;

public class SetContentEncoding implements Filter {

  @Inject
  SetContentEncoding() {}

  @Override
  public void handle(Request request, Response response) {
    response.header("Content-Encoding", "gzip");
  }
}
