package com.github.nhirakawa.wilson.models.messages;

import java.util.List;
import java.util.UUID;

import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.github.nhirakawa.wilson.models.style.WilsonStyle;

@WilsonStyle
@Immutable
@JsonTypeName("AppendEntriesRequest")
interface AppendEntriesRequestIF extends Message {

  long getTerm();

  UUID getLeaderId();

  long getPreviousLogIndex();

  long getPreviousLogTerm();

  List<String> getEntries();

  long getLeaderCommitIndex();
}
