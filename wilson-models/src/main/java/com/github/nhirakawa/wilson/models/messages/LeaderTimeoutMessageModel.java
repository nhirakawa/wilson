package com.github.nhirakawa.wilson.models.messages;

import com.github.nhirakawa.wilson.models.style.WilsonStyle;
import java.time.Instant;
import org.immutables.value.Value.Default;
import org.immutables.value.Value.Immutable;

@Immutable
@WilsonStyle
public interface LeaderTimeoutMessageModel extends LocalWilsonMessage {
  @Default
  default Instant getTimestamp() {
    return Instant.now();
  }

  long getLeaderTimeout();
}
