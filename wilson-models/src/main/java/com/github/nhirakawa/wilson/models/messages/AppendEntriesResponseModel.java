package com.github.nhirakawa.wilson.models.messages;

import com.github.nhirakawa.wilson.models.AppendEntriesResult;
import com.github.nhirakawa.wilson.models.style.WilsonStyle;
import org.immutables.value.Value.Immutable;

@WilsonStyle
@Immutable
public interface AppendEntriesResponseModel {
  long getTerm();
  AppendEntriesResult getResult();
}
