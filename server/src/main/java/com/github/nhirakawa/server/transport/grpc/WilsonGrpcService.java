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
                    @LocalMember ClusterMember localMember) {
    this.messageApplier = messageApplier;
    this.localMember = localMember;
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

    VoteResponseProto response = VoteResponseProto.newBuilder()
        .setClusterMember(
            ClusterMemberProto.newBuilder()
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
