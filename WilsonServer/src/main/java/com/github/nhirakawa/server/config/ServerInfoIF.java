package com.github.nhirakawa.server.config;

import org.immutables.value.Value.Immutable;

import com.github.nhirakawa.wilson.models.style.WilsonStyle;

@Immutable
@WilsonStyle
public interface ServerInfoIF {

  String getHost();

  int getPort();
}
