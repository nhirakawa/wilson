package com.github.nhirakawa.wilson.server.models.messages;

import org.immutables.value.Value.Immutable;

import com.github.nhirakawa.wilson.server.models.style.WilsonStyle;

@WilsonStyle
@Immutable
public interface VoteResponseModel {

  long getTerm();
  boolean isVoteGranted();

}
