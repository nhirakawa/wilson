package com.github.nhirakawa.server.models.messages;

import java.time.Instant;

import org.immutables.value.Value;

import com.github.nhirakawa.server.models.style.WilsonStyle;

@Value.Immutable
@WilsonStyle
public interface HeartbeatTimeoutMessageModel {

  @Value.Default
  default Instant getTimestamp() {
    return Instant.now();
  }

  long getHeartbeatTimeout();

}
