package com.github.nhirakawa.wilson.models;

import java.net.InetSocketAddress;
import java.util.Set;

import org.immutables.value.Value.Immutable;

@Immutable
@WilsonStyle
public abstract class AbstractDiscoveryResponse {

  public abstract Set<InetSocketAddress> getMembers();

}
