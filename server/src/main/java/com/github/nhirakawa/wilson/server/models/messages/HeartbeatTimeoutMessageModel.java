package com.github.nhirakawa.wilson.server.models.messages;

import java.time.Instant;

import org.immutables.value.Value;

import com.github.nhirakawa.wilson.server.models.style.WilsonStyle;

@Value.Immutable
@WilsonStyle
public interface HeartbeatTimeoutMessageModel {

  @Value.Default
  default Instant getTimestamp() {
    return Instant.now();
  }

  long getHeartbeatTimeout();

}
