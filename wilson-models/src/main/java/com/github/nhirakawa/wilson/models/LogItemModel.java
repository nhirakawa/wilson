package com.github.nhirakawa.wilson.models;

import com.github.nhirakawa.wilson.models.style.WilsonStyle;
import java.util.List;
import org.immutables.value.Value.Immutable;

@Immutable
@WilsonStyle
public interface LogItemModel {
  long getTerm();
  long getIndex();
  List<Byte> getBytes();
}
