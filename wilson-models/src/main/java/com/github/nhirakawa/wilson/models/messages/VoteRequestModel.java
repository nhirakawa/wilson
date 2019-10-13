package com.github.nhirakawa.wilson.models.messages;

import org.immutables.value.Value.Immutable;

import com.github.nhirakawa.wilson.models.ClusterMember;
import com.github.nhirakawa.wilson.models.style.WilsonStyle;

@WilsonStyle
@Immutable
public interface VoteRequestModel {

  ClusterMember getClusterMember();
  long getTerm();
  long getLastLogIndex();
  long getLastLogTerm();

}
