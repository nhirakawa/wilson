package com.github.nhirakawa.wilson.http.server.filter.before;

import java.util.UUID;
import javax.inject.Inject;
import spark.Filter;
import spark.Request;
import spark.Response;

public class SetRequestId implements Filter {

  @Inject
  SetRequestId() {}

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
