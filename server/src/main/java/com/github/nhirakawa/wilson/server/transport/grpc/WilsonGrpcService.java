package com.github.nhirakawa.wilson.server.transport.grpc;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.github.nhirakawa.wilson.server.dagger.LocalMember;
import com.github.nhirakawa.wilson.server.models.ClusterMember;
import com.github.nhirakawa.wilson.server.models.ClusterMemberModel;
import com.github.nhirakawa.wilson.server.models.messages.AppendEntriesRequest;
import com.github.nhirakawa.wilson.server.models.messages.AppendEntriesResponse;
import com.github.nhirakawa.wilson.server.models.messages.VoteRequest;
import com.github.nhirakawa.wilson.server.models.messages.VoteResponse;
import com.github.nhirakawa.wilson.server.raft.StateMachineMessageApplier;

import io.grpc.stub.StreamObserver;

@Singleton
public class WilsonGrpcService extends WilsonGrpc.WilsonImplBase {

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
  public void appendEntries(AppendEntriesRequestProto request, StreamObserver<AppendEntriesResponseProto> responseObserver) {
    AppendEntriesRequest appendEntriesRequest = protobufTranslator.fromProto(request);
    AppendEntriesResponse appendEntriesResponse = messageApplier.apply(appendEntriesRequest);
    responseObserver.onNext(protobufTranslator.toProto(appendEntriesResponse));
    responseObserver.onCompleted();
  }

  @Override
  public void requestVote(VoteRequestProto request, StreamObserver<VoteResponseProto> responseObserver) {
    VoteRequest voteRequest = protobufTranslator.fromProto(request);

    VoteResponse voteResponse = messageApplier.apply(voteRequest);

    VoteResponseProto voteResponseProto = protobufTranslator.toProto(voteResponse);

    responseObserver.onNext(voteResponseProto);
    responseObserver.onCompleted();
  }
}
