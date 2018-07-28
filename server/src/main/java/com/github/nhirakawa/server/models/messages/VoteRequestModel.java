package com.github.nhirakawa.server.models.messages;

import org.immutables.value.Value.Immutable;

import com.github.nhirakawa.server.models.ClusterMember;
import com.github.nhirakawa.server.models.style.WilsonStyle;

@WilsonStyle
@Immutable
public interface VoteRequestModel {

  ClusterMember getClusterMember();
  long getTerm();
  long getLastLogIndex();
  long getLastLogTerm();

}
