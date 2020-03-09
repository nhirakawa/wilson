package com.github.nhirakawa.wilson.models.messages;

import com.github.nhirakawa.wilson.models.style.WilsonStyle;
import java.time.Instant;
import org.immutables.value.Value;

@Value.Immutable
@WilsonStyle
public interface HeartbeatTimeoutMessageModel {
  @Value.Default
  default Instant getTimestamp() {
    return Instant.now();
  }

  long getHeartbeatTimeout();
}
