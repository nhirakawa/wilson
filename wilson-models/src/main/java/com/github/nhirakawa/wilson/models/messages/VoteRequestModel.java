package com.github.nhirakawa.wilson.models.messages;

import com.github.nhirakawa.wilson.models.ClusterMember;
import com.github.nhirakawa.wilson.models.style.WilsonStyle;
import org.immutables.value.Value.Immutable;

@WilsonStyle
@Immutable
public interface VoteRequestModel {
  ClusterMember getClusterMember();
  long getTerm();
  long getLastLogIndex();
  long getLastLogTerm();
}
