package com.github.nhirakawa.server.transport.grpc;

import java.time.Instant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nhirakawa.server.models.messages.HeartbeatRequest;
import com.github.nhirakawa.server.models.messages.HeartbeatRequestModel;
import com.github.nhirakawa.server.models.messages.HeartbeatResponse;
import com.github.nhirakawa.server.models.messages.HeartbeatResponseModel;
import com.github.nhirakawa.server.models.messages.VoteRequestModel;
import com.github.nhirakawa.server.models.messages.VoteResponse;
import com.google.inject.Inject;

final class ProtobufTranslator {

  private final ObjectMapper objectMapper;

  @Inject
  ProtobufTranslator(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public static VoteRequestProto toProto(VoteRequestModel request) {
    return VoteRequestProto.newBuilder()
        .setTerm(request.getTerm())
        .setLastLogTerm(request.getLastLogTerm())
        .setLastLogIndex(request.getLastLogIndex())
        .setTimestamp(Instant.now().toEpochMilli())
        .build();
  }

  public static VoteResponse fromProto(VoteResponseProto voteResponse) {
    return VoteResponse.builder()
        .setTerm(voteResponse.getCurrentTerm())
        .setVoteGranted(voteResponse.getVoteGranted())
        .build();
  }

  public static HeartbeatRequestProto toProto(HeartbeatRequest heartbeatRequestModel) {
    return HeartbeatRequestProto.newBuilder()
        .setTimestamp(Instant.now().toEpochMilli())
        .build();
  }

  public static HeartbeatResponse fromProto(HeartbeatResponseProto heartbeatResponse) {
    return HeartbeatResponse.builder().build();
  }
}
