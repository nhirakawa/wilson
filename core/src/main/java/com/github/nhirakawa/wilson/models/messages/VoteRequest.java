package com.github.nhirakawa.wilson.models.messages;

import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.github.nhirakawa.wilson.models.style.WilsonStyle;

@WilsonStyle
@Immutable
@JsonTypeName("VoteRequest")
public interface VoteRequest extends SerializedWilsonMessage {

  long getTerm();

  long getLastLogIndex();

  long getLastLogTerm();
}
