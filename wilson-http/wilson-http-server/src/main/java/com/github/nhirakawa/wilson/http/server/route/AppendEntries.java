package com.github.nhirakawa.wilson.http.server.route;

import com.github.nhirakawa.wilson.common.ObjectMapperWrapper;
import com.github.nhirakawa.wilson.models.messages.AppendEntriesRequest;
import com.github.nhirakawa.wilson.protocol.StateMachineMessageApplier;
import javax.inject.Inject;
import spark.Request;
import spark.Response;
import spark.Route;

public class AppendEntries implements Route {
  private final StateMachineMessageApplier stateMachineMessageApplier;

  @Inject
  public AppendEntries(StateMachineMessageApplier stateMachineMessageApplier) {
    this.stateMachineMessageApplier = stateMachineMessageApplier;
  }

  @Override
  public Object handle(Request request, Response response) throws Exception {
    AppendEntriesRequest appendEntriesRequest = ObjectMapperWrapper
      .instance()
      .readValue(request.bodyAsBytes(), AppendEntriesRequest.class);

    return stateMachineMessageApplier.apply(appendEntriesRequest);
  }
}
