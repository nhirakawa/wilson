package com.github.nhirakawa.wilson.models.messages;

import org.immutables.value.Value.Immutable;

import com.github.nhirakawa.wilson.models.AppendEntriesResult;
import com.github.nhirakawa.wilson.models.style.WilsonStyle;

@WilsonStyle
@Immutable
public interface AppendEntriesResponseModel {

  long getTerm();
  AppendEntriesResult getResult();

}
