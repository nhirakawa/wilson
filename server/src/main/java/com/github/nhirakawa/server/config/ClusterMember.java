package com.github.nhirakawa.server.config;

import java.nio.charset.StandardCharsets;

import org.immutables.value.Value.Default;
import org.immutables.value.Value.Derived;
import org.immutables.value.Value.Immutable;

import com.github.nhirakawa.wilson.models.style.WilsonStyle;
import com.google.common.hash.Hashing;

@Immutable
@WilsonStyle
public abstract class ClusterMember {

  @Default
  public String getHost() {
    return "localhost";
  }

  public abstract int getPort();

  @Derived
  public String getServerId() {
    return Hashing.murmur3_128().newHasher()
        .putString(getHost(), StandardCharsets.UTF_8)
        .putInt(getPort())
        .hash()
        .toString();
  }
}
