package com.github.nhirakawa.server.transport.grpc;

import java.time.Instant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nhirakawa.server.models.messages.HeartbeatRequest;
import com.github.nhirakawa.server.models.messages.HeartbeatRequestModel;
import com.github.nhirakawa.server.models.messages.HeartbeatResponse;
import com.github.nhirakawa.server.models.messages.HeartbeatResponseModel;
import com.github.nhirakawa.server.models.messages.VoteRequest;
import com.github.nhirakawa.server.models.messages.VoteRequestModel;
import com.github.nhirakawa.server.models.messages.VoteResponse;
import com.google.inject.Inject;

final class ProtobufTranslator {

  private final ObjectMapper objectMapper;

  @Inject
  ProtobufTranslator(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public VoteRequestProto toProto(VoteRequestModel request) {
    return objectMapper.convertValue(request, VoteRequestProto.class);
  }

  public VoteRequest fromProto(VoteRequestProto voteRequestProto) {
    return objectMapper.convertValue(voteRequestProto, VoteRequest.class);
  }

  public VoteResponse fromProto(VoteResponseProto voteResponse) {
    return objectMapper.convertValue(voteResponse, VoteResponse.class);
  }

  public VoteResponseProto toProto(VoteResponse voteResponse) {
    return objectMapper.convertValue(voteResponse, VoteResponseProto.class);
  }

  public HeartbeatRequestProto toProto(HeartbeatRequest heartbeatRequestModel) {
    return objectMapper.convertValue(heartbeatRequestModel, HeartbeatRequestProto.class);
  }

  public HeartbeatResponse fromProto(HeartbeatResponseProto heartbeatResponse) {
    return objectMapper.convertValue(heartbeatResponse, HeartbeatResponse.class);
  }

  public ObjectMapper instance() {
    return objectMapper;
  }
}
