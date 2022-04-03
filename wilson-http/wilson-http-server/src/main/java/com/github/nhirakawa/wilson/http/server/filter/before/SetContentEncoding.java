package com.github.nhirakawa.wilson.http.server.filter.before;

import spark.Filter;
import spark.Request;
import spark.Response;

public class SetContentEncoding implements Filter {

  @Override
  public void handle(Request request, Response response) {
    response.header("Content-Encoding", "gzip");
  }
}
