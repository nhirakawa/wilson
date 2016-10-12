package com.github.nhirakawa.wilson.models;

import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;

@Immutable
@WilsonStyle
public abstract class AbstractDiscoveryMessage {

  private static final String WILSON = "57494C534F4E";

  @Value.Default
  public String getMessage() {
    return WILSON;
  }
}
