package com.github.nhirakawa.server.config;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.stream.Collectors;

import org.immutables.value.Value.Check;
import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Lazy;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.nhirakawa.wilson.models.style.WilsonStyle;
import com.google.common.hash.Hashing;

@Immutable
@WilsonStyle
public interface ConfigurationIF {

  String getClusterId();

  @JsonProperty("cluster")
  Collection<ServerInfo> getClusterAddresses();

  @JsonProperty("local")
  ServerInfo getLocalAddress();

  @Lazy
  default String getServerId() {
    return Hashing.md5().newHasher()
        .putString(getLocalAddress().getHost(), StandardCharsets.UTF_8)
        .putInt(getLocalAddress().getPort()).hash()
        .toString();
  }

  @Check
  default ConfigurationIF normalize() {
    if (getClusterAddresses().contains(getLocalAddress())) {
      return Configuration.builder()
          .setClusterAddresses(getClusterAddresses().stream()
              .filter(address -> !address.equals(getLocalAddress()))
              .collect(Collectors.toSet()))
          .setLocalAddress(getLocalAddress())
          .setClusterId(getClusterId())
          .build();
    }

    return this;
  }
}
