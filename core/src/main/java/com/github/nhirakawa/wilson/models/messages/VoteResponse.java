package com.github.nhirakawa.wilson.models.messages;

import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.github.nhirakawa.wilson.models.style.WilsonStyle;

@WilsonStyle
@Immutable
@JsonTypeName("VoteResponse")
public interface VoteResponse extends SerializedWilsonMessage {

  long getTerm();

  boolean isVoteGranted();
}
