package com.github.nhirakawa.wilson.http.server.route;

import com.github.nhirakawa.wilson.common.ObjectMapperWrapper;
import com.github.nhirakawa.wilson.models.messages.AppendEntriesRequest;
import com.github.nhirakawa.wilson.models.messages.AppendEntriesResponse;
import com.github.nhirakawa.wilson.protocol.StateMachineMessageApplier;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import javax.inject.Inject;
import org.jetbrains.annotations.NotNull;

public class AppendEntries implements Handler {
  private final StateMachineMessageApplier stateMachineMessageApplier;

  @Inject
  public AppendEntries(StateMachineMessageApplier stateMachineMessageApplier) {
    this.stateMachineMessageApplier = stateMachineMessageApplier;
  }

  @Override
  public void handle(@NotNull Context ctx) throws Exception {
    AppendEntriesRequest appendEntriesRequest = ObjectMapperWrapper.readValueFromInputStream(
      ctx.bodyAsInputStream(),
      AppendEntriesRequest.class
    );

    AppendEntriesResponse appendEntriesResponse = stateMachineMessageApplier.apply(
      appendEntriesRequest
    );

    ctx.result(ObjectMapperWrapper.writeValueAsBytes(appendEntriesResponse));
  }
}
