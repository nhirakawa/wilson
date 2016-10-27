package com.github.nhirakawa.server.config;

import java.util.Collection;

import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.github.nhirakawa.wilson.models.style.WilsonStyle;

@Immutable
@WilsonStyle
@JsonIgnoreProperties(ignoreUnknown = true)
@Value.Style(jdkOnly = true)
public interface WilsonConfigurationIF {

  Collection<WilsonServerInfo> getServers();

}
