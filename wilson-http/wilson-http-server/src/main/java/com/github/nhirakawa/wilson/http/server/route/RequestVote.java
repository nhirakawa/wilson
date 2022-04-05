package com.github.nhirakawa.wilson.http.server.route;

import com.github.nhirakawa.wilson.common.ObjectMapperWrapper;
import com.github.nhirakawa.wilson.models.messages.VoteRequest;
import com.github.nhirakawa.wilson.models.messages.VoteResponse;
import com.github.nhirakawa.wilson.protocol.StateMachineMessageApplier;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import javax.inject.Inject;
import org.jetbrains.annotations.NotNull;

public class RequestVote implements Handler {
  private final StateMachineMessageApplier stateMachineMessageApplier;

  @Inject
  public RequestVote(StateMachineMessageApplier stateMachineMessageApplier) {
    this.stateMachineMessageApplier = stateMachineMessageApplier;
  }

  @Override
  public void handle(@NotNull Context ctx) throws Exception {
    VoteRequest voteRequest = ObjectMapperWrapper
      .instance()
      .readValue(ctx.bodyAsBytes(), VoteRequest.class);

    VoteResponse voteResponse = stateMachineMessageApplier.apply(voteRequest);

    ctx.result(ObjectMapperWrapper.writeValueAsBytes(voteResponse));
  }
}
