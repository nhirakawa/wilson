package com.github.nhirakawa.server.config;

import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;

import com.github.nhirakawa.wilson.models.style.WilsonStyle;

@Immutable
@WilsonStyle
@Value.Style(jdkOnly = true)
public interface WilsonServerInfoIF {

  String getAddress();

  int getPort();

}
