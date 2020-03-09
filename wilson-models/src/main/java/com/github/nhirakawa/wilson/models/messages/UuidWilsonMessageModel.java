package com.github.nhirakawa.wilson.models.messages;

import com.github.nhirakawa.wilson.models.style.WilsonStyle;
import java.util.UUID;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;

@Immutable
@WilsonStyle
public interface UuidWilsonMessageModel {
  @Value.Default
  default String getUuid() {
    return UUID.randomUUID().toString();
  }
}
