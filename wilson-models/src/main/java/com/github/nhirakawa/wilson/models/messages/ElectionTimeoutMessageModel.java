package com.github.nhirakawa.wilson.models.messages;

import com.github.nhirakawa.wilson.models.style.WilsonStyle;
import java.time.Instant;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;

@Immutable
@WilsonStyle
public interface ElectionTimeoutMessageModel extends LocalWilsonMessage {
  @Value.Default
  default Instant getTimestamp() {
    return Instant.now();
  }

  long getElectionTimeout();
}
