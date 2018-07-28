package com.github.nhirakawa.server.models.messages;

import java.util.List;
import java.util.UUID;

import org.immutables.value.Value.Immutable;

import com.github.nhirakawa.server.models.style.WilsonStyle;

@WilsonStyle
@Immutable
public interface AppendEntriesRequestModel {

  long getTerm();
  UUID getLeaderId();
  long getPreviousLogIndex();
  long getPreviousLogTerm();
  List<String> getEntries();
  long getLeaderCommitIndex();

}
