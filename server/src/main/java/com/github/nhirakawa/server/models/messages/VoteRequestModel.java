package com.github.nhirakawa.server.models.messages;

import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.annotation.JsonTypeName;

import com.github.nhirakawa.server.models.ClusterMember;
import com.github.nhirakawa.server.models.style.WilsonStyle;

@WilsonStyle
@Immutable
@JsonTypeName("VoteRequestModel")
public interface VoteRequestModel extends SerializedWilsonMessage {

//  ClusterMember getClusterMember();
  long getTerm();
  long getLastLogIndex();
  long getLastLogTerm();

}
