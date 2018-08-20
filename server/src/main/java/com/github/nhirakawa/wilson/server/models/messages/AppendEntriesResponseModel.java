package com.github.nhirakawa.wilson.server.models.messages;

import org.immutables.value.Value.Immutable;

import com.github.nhirakawa.wilson.server.models.style.WilsonStyle;
import com.github.nhirakawa.wilson.server.transport.grpc.AppendEntriesResponseProto.Result;

@WilsonStyle
@Immutable
public interface AppendEntriesResponseModel {

  long getTerm();
  Result getResult();

}
