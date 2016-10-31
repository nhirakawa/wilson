package com.github.nhirakawa.server.config;

import java.util.Collection;

import org.immutables.value.Value.Immutable;

import com.github.nhirakawa.wilson.models.style.WilsonStyle;

@Immutable
@WilsonStyle
public interface ConfigurationIF {

  Collection<ServerInfo> getServers();
}
