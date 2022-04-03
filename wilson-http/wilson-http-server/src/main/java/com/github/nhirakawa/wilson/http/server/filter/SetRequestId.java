package com.github.nhirakawa.wilson.http.server.filter;

import java.util.UUID;
import spark.Filter;
import spark.Request;
import spark.Response;

public class SetRequestId implements Filter {

  @Override
  public void handle(Request request, Response response) {
    String existingRequestId = request.headers("X-Wilson-Request-Id");
    if (existingRequestId == null) {
      response.header("X-Wilson-Request-Id", UUID.randomUUID().toString());
    } else {
      response.header("X-Wilson-Request-Id", existingRequestId);
    }
  }
}
