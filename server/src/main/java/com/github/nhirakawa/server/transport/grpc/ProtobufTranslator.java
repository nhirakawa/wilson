package com.github.nhirakawa.server.transport.grpc;

import java.time.Instant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nhirakawa.wilson.models.messages.HeartbeatRequest;
import com.github.nhirakawa.wilson.models.messages.HeartbeatResponse;
import com.github.nhirakawa.wilson.models.messages.ImmutableHeartbeatResponse;
import com.github.nhirakawa.wilson.models.messages.ImmutableVoteResponse;
import com.github.nhirakawa.wilson.models.messages.VoteRequest;
import com.github.nhirakawa.wilson.models.messages.VoteResponse;
import com.google.inject.Inject;

final class ProtobufTranslator {

  private final ObjectMapper objectMapper;

  @Inject
  ProtobufTranslator(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public static WilsonProtos.VoteRequest toProto(VoteRequest request) {
    return WilsonProtos.VoteRequest.newBuilder()
        .setTerm(request.getTerm())
        .setLastLogTerm(request.getLastLogTerm())
        .setLastLogIndex(request.getLastLogIndex())
        .setTimestamp(Instant.now().toEpochMilli())
        .build();
  }

  public static VoteResponse fromProto(WilsonProtos.VoteResponse voteResponse) {
    return ImmutableVoteResponse.builder()
        .setTerm(voteResponse.getCurrentTerm())
        .setVoteGranted(voteResponse.getVoteGranted())
        .build();
  }

  public static WilsonProtos.HeartbeatRequest toProto(HeartbeatRequest heartbeatRequest) {
    return WilsonProtos.HeartbeatRequest.newBuilder()
        .setTimestamp(Instant.now().toEpochMilli())
        .build();
  }

  public static HeartbeatResponse fromProto(WilsonProtos.HeartbeatResponse heartbeatResponse) {
    return ImmutableHeartbeatResponse.builder().build();
  }
}
