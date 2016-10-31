package com.github.nhirakawa.server.config;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.immutables.value.Value.Derived;
import org.immutables.value.Value.Immutable;

import com.github.nhirakawa.wilson.models.style.WilsonStyle;

@Immutable
@WilsonStyle
public abstract class AbstractServerInfo {

  public abstract String getHost();

  public abstract int getPort();

  @Derived
  public SocketAddress getAddress() {
    return new InetSocketAddress(getHost(), getPort());
  }
}
