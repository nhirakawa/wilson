package com.github.nhirakawa.server.transport.grpc;

import com.github.nhirakawa.server.config.ClusterMember;
import com.github.nhirakawa.server.config.ClusterMemberModel;
import com.github.nhirakawa.server.guice.LocalMember;
import com.github.nhirakawa.server.raft.StateMachineMessageApplier;
import com.github.nhirakawa.wilson.models.messages.HeartbeatRequest;
import com.github.nhirakawa.wilson.models.messages.VoteRequest;
import com.github.nhirakawa.wilson.models.messages.VoteResponse;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.grpc.stub.StreamObserver;

@Singleton
public class WilsonGrpcService extends WilsonGrpc.WilsonImplBase {

  private final StateMachineMessageApplier messageApplier;
  private final ClusterMemberModel localMember;

  @Inject
  WilsonGrpcService(StateMachineMessageApplier messageApplier,
                    @LocalMember ClusterMemberModel localMember) {
    this.messageApplier = messageApplier;
    this.localMember = localMember;
  }

  @Override
  public void heartbeat(WilsonProtos.HeartbeatRequest request, StreamObserver<WilsonProtos.HeartbeatResponse> responseObserver) {
    HeartbeatRequest heartbeatRequest = HeartbeatRequest.builder().build();
    messageApplier.apply(heartbeatRequest);
    responseObserver.onNext(WilsonProtos.HeartbeatResponse.getDefaultInstance());
    responseObserver.onCompleted();
  }

  @Override
  public void requestVote(WilsonProtos.VoteRequest request, StreamObserver<WilsonProtos.VoteResponse> responseObserver) {
    ClusterMember clusterMember = ClusterMember.builder()
        .setHost(request.getClusterMember().getHost())
        .setPort(request.getClusterMember().getPort())
        .build();

    VoteRequest voteRequest = VoteRequest.builder()
        .setTerm(request.getTerm())
        .setLastLogTerm(request.getLastLogTerm())
        .setLastLogIndex(request.getLastLogIndex())
        .build();

    VoteResponse voteResponse = messageApplier.apply(voteRequest, clusterMember);

    WilsonProtos.VoteResponse response = WilsonProtos.VoteResponse.newBuilder()
        .setClusterMember(
            WilsonProtos.ClusterMember.newBuilder()
            .setHost(localMember.getHost())
            .setPort(localMember.getPort())
        )
        .setVoteGranted(voteResponse.isVoteGranted())
        .setCurrentTerm(voteResponse.getTerm())
        .build();
    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }
}
