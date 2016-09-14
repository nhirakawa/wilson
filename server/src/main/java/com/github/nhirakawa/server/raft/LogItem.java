package com.github.nhirakawa.server.raft;

import java.util.List;

import org.immutables.value.Value.Immutable;

import com.github.nhirakawa.wilson.models.style.WilsonStyle;

@Immutable
@WilsonStyle
public interface LogItem {

  long getTerm();

  long getIndex();

  List<Byte> getBytes();
}
