package com.github.nhirakawa.server.config;

import java.net.InetSocketAddress;

import org.immutables.value.Value.Default;
import org.immutables.value.Value.Derived;
import org.immutables.value.Value.Immutable;

import com.github.nhirakawa.wilson.models.style.WilsonStyle;

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
    return InetSocketAddress.createUnresolved(getHost(), getPort()).toString();
  }
}
