package com.github.nhirakawa.wilson.models.messages;

import java.util.UUID;

import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.github.nhirakawa.wilson.models.style.WilsonStyle;

@WilsonStyle
@Immutable
@JsonTypeName("Heartbeat")
interface HeartbeatMessageIF extends Message {

  long getTerm();

  UUID getLeaderId();

  long getPreviousLogIndex();

  long getLeaderCommitIndex();
}
