package com.github.nhirakawa.server.models.messages;

import java.util.UUID;

import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;

import com.github.nhirakawa.server.models.style.WilsonStyle;

@Immutable
@WilsonStyle
public interface UuidWilsonMessageModel {

  @Value.Default
  default String getUuid() {
    return UUID.randomUUID().toString();
  }
}
