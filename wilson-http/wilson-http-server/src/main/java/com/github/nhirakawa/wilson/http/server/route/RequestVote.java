package com.github.nhirakawa.wilson.http.server.route;

import com.github.nhirakawa.wilson.common.ObjectMapperWrapper;
import com.github.nhirakawa.wilson.models.messages.VoteRequest;
import com.github.nhirakawa.wilson.protocol.StateMachineMessageApplier;
import javax.inject.Inject;
import spark.Request;
import spark.Response;
import spark.Route;

public class RequestVote implements Route {
  private final StateMachineMessageApplier stateMachineMessageApplier;

  @Inject
  public RequestVote(StateMachineMessageApplier stateMachineMessageApplier) {
    this.stateMachineMessageApplier = stateMachineMessageApplier;
  }

  @Override
  public Object handle(Request request, Response response) throws Exception {
    VoteRequest voteRequest = ObjectMapperWrapper
      .instance()
      .readValue(request.bodyAsBytes(), VoteRequest.class);
    return stateMachineMessageApplier.apply(voteRequest);
  }
}
