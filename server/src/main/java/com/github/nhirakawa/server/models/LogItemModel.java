package com.github.nhirakawa.server.models;

import java.util.List;

import org.immutables.value.Value.Immutable;

import com.github.nhirakawa.server.models.style.WilsonStyle;

@Immutable
@WilsonStyle
public interface LogItemModel {

  long getTerm();
  long getIndex();
  List<Byte> getBytes();

}
