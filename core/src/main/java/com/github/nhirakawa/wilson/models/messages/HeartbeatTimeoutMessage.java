package com.github.nhirakawa.wilson.models.messages;

import java.time.Instant;

import org.immutables.value.Value;

import com.github.nhirakawa.wilson.models.style.WilsonStyle;

@Value.Immutable
@WilsonStyle
public interface HeartbeatTimeoutMessage {

  @Value.Default
  default Instant getTimestamp() {
    return Instant.now();
  }

  long getHeartbeatTimeout();

}
