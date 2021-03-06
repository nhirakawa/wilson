package com.github.nhirakawa.wilson.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.nhirakawa.wilson.models.style.WilsonStyle;
import java.net.InetSocketAddress;
import org.immutables.value.Value.Default;
import org.immutables.value.Value.Derived;
import org.immutables.value.Value.Immutable;

@Immutable
@WilsonStyle
public abstract class ClusterMemberModel {

  @Default
  public String getHost() {
    return "localhost";
  }

  public abstract int getPort();

  @Derived
  @JsonIgnore
  public String getServerId() {
    return InetSocketAddress.createUnresolved(getHost(), getPort()).toString();
  }
}
