package com.github.nhirakawa.wilson.models.messages;

import com.github.nhirakawa.wilson.models.ClusterMember;
import com.github.nhirakawa.wilson.models.style.WilsonStyle;
import java.util.List;
import org.immutables.value.Value.Immutable;

@WilsonStyle
@Immutable
public interface AppendEntriesRequestModel {
  long getTerm();

  ClusterMember getLeader();

  long getLastLogIndex();

  long getLastLogTerm();

  List<byte[]> getEntries();

  long getLeaderCommitIndex();
}
