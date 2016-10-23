package com.github.nhirakawa.wilson.models.messages;

import java.util.UUID;

import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.github.nhirakawa.wilson.models.style.WilsonStyle;

@WilsonStyle
@Immutable
@JsonTypeName("VoteRequest")
interface VoteRequestIF extends Message {

  long getTerm();

  UUID getCandidateId();

  long getLastLogIndex();

  long getLogTerm();
}
