package com.github.nhirakawa.wilson.models.messages;

import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.github.nhirakawa.wilson.models.style.WilsonStyle;

@WilsonStyle
@Immutable
@JsonTypeName("AppendEntriesResponse")
public interface AppendEntriesResponse extends SerializedWilsonMessage {

  long getTerm();

  boolean isSuccess();
}
