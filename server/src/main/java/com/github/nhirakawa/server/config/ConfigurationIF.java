package com.github.nhirakawa.server.config;

import java.util.Collection;
import java.util.stream.Collectors;

import org.immutables.value.Value.Check;
import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.nhirakawa.wilson.models.style.WilsonStyle;

@Immutable
@WilsonStyle
public interface ConfigurationIF {

  @JsonProperty("cluster")
  Collection<ServerInfo> getClusterAddresses();

  @JsonProperty("local")
  ServerInfo getLocalAddress();

  @Check
  default ConfigurationIF normalize() {
    if (getClusterAddresses().contains(getLocalAddress())) {
      return Configuration.builder()
          .setClusterAddresses(getClusterAddresses().stream()
              .filter(address -> !address.equals(getLocalAddress()))
              .collect(Collectors.toSet()))
          .setLocalAddress(getLocalAddress())
          .build();
    }

    return this;
  }
}
