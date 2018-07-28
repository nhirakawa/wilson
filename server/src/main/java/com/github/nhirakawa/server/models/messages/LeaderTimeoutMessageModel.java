package com.github.nhirakawa.server.models.messages;

import java.time.Instant;

import org.immutables.value.Value.Default;
import org.immutables.value.Value.Immutable;

import com.github.nhirakawa.server.models.style.WilsonStyle;

@Immutable
@WilsonStyle
public interface LeaderTimeoutMessageModel extends LocalWilsonMessage {

  @Default
  default Instant getTimestamp() {
    return Instant.now();
  }

  long getLeaderTimeout();

}
