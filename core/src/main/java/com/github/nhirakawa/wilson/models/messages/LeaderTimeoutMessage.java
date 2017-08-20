package com.github.nhirakawa.wilson.models.messages;

import java.time.Instant;

import org.immutables.value.Value.Default;
import org.immutables.value.Value.Immutable;

import com.github.nhirakawa.wilson.models.style.WilsonStyle;

@Immutable
@WilsonStyle
public interface LeaderTimeoutMessage extends LocalWilsonMessage {

  @Default
  default Instant getTimestamp() {
    return Instant.now();
  }

}
