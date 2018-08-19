package com.github.nhirakawa.wilson.server.models.messages;

import java.time.Instant;

import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;

import com.github.nhirakawa.wilson.server.models.style.WilsonStyle;

@Immutable
@WilsonStyle
public interface ElectionTimeoutMessageModel extends LocalWilsonMessage {

  @Value.Default
  default Instant getTimestamp() {
    return Instant.now();
  }

  long getElectionTimeout();

}
