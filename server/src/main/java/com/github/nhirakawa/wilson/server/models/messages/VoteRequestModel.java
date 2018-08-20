package com.github.nhirakawa.wilson.server.models.messages;

import org.immutables.value.Value.Immutable;

import com.github.nhirakawa.wilson.server.models.ClusterMember;
import com.github.nhirakawa.wilson.server.models.style.WilsonStyle;

@WilsonStyle
@Immutable
public interface VoteRequestModel {

  ClusterMember getClusterMember();
  long getTerm();
  long getLastLogIndex();
  long getLastLogTerm();

}
