package com.github.nhirakawa.wilson.server.models.messages;

import java.util.UUID;

import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;

import com.github.nhirakawa.wilson.server.models.style.WilsonStyle;

@Immutable
@WilsonStyle
public interface UuidWilsonMessageModel {

  @Value.Default
  default String getUuid() {
    return UUID.randomUUID().toString();
  }
}
