package com.github.nhirakawa.server.transport.grpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.nhirakawa.server.guice.LocalMember;
import com.github.nhirakawa.server.models.ClusterMember;
import com.github.nhirakawa.server.models.ClusterMemberModel;
import com.github.nhirakawa.server.models.messages.HeartbeatRequest;
import com.github.nhirakawa.server.models.messages.VoteRequest;
import com.github.nhirakawa.server.models.messages.VoteResponse;
import com.github.nhirakawa.server.raft.StateMachineMessageApplier;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.grpc.stub.StreamObserver;

@Singleton
public class WilsonGrpcService extends WilsonGrpc.WilsonImplBase {

  private static final Logger LOG = LoggerFactory.getLogger(WilsonGrpcService.class);

  private final StateMachineMessageApplier messageApplier;
  private final ClusterMemberModel localMember;
  private final ProtobufTranslator protobufTranslator;

  @Inject
  WilsonGrpcService(StateMachineMessageApplier messageApplier,
                    @LocalMember ClusterMember localMember,
                    ProtobufTranslator protobufTranslator) {
    this.messageApplier = messageApplier;
    this.localMember = localMember;
    this.protobufTranslator = protobufTranslator;
  }

  @Override
  public void heartbeat(HeartbeatRequestProto request, StreamObserver<HeartbeatResponseProto> responseObserver) {
    HeartbeatRequest heartbeatRequest = HeartbeatRequest.builder().build();
    messageApplier.apply(heartbeatRequest);
    responseObserver.onNext(HeartbeatResponseProto.getDefaultInstance());
    responseObserver.onCompleted();
  }

  @Override
  public void requestVote(VoteRequestProto request, StreamObserver<VoteResponseProto> responseObserver) {
    try {
      LOG.debug("vote request {}", protobufTranslator.instance().writeValueAsString(request));
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

    VoteRequest voteRequest = protobufTranslator.fromProto(request);

    VoteResponse voteResponse = messageApplier.apply(voteRequest);

    VoteResponseProto voteResponseProto = protobufTranslator.toProto(voteResponse);

    responseObserver.onNext(voteResponseProto);
    responseObserver.onCompleted();
  }
}
