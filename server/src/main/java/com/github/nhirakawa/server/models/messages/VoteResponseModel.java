package com.github.nhirakawa.server.models.messages;

import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.github.nhirakawa.server.models.style.WilsonStyle;

@WilsonStyle
@Immutable
@JsonTypeName("VoteResponseModel")
public interface VoteResponseModel extends SerializedWilsonMessage {

  long getTerm();
  boolean isVoteGranted();

}
