package com.github.nhirakawa.server.transport.grpc;

import org.slf4j.MDC;

import com.github.nhirakawa.server.config.ClusterMember;
import com.github.nhirakawa.server.config.ImmutableClusterMember;
import com.github.nhirakawa.server.guice.LocalMember;
import com.github.nhirakawa.server.raft.StateMachineMessageApplier;
import com.github.nhirakawa.server.transport.grpc.WilsonProtos.HeartbeatRequest;
import com.github.nhirakawa.server.transport.grpc.WilsonProtos.HeartbeatResponse;
import com.github.nhirakawa.wilson.models.messages.ImmutableHeartbeatRequest;
import com.github.nhirakawa.wilson.models.messages.ImmutableVoteRequest;
import com.github.nhirakawa.wilson.models.messages.VoteRequest;
import com.github.nhirakawa.wilson.models.messages.VoteResponse;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.grpc.stub.StreamObserver;

@Singleton
public class WilsonGrpcService extends WilsonGrpc.WilsonImplBase {

  private final StateMachineMessageApplier messageApplier;
  private final ClusterMember localMember;

  @Inject
  WilsonGrpcService(StateMachineMessageApplier messageApplier,
                    @LocalMember ClusterMember localMember) {
    this.messageApplier = messageApplier;
    this.localMember = localMember;
  }

  @Override
  public void heartbeat(HeartbeatRequest request, StreamObserver<HeartbeatResponse> responseObserver) {
    MDC.put("serverId", localMember.getServerId());
    ImmutableHeartbeatRequest heartbeatRequest = ImmutableHeartbeatRequest.builder().build();
    messageApplier.apply(heartbeatRequest);
    responseObserver.onNext(HeartbeatResponse.getDefaultInstance());
    responseObserver.onCompleted();
    MDC.remove("serverId");
  }

  @Override
  public void requestVote(WilsonProtos.VoteRequest request, StreamObserver<WilsonProtos.VoteResponse> responseObserver) {
    MDC.put("serverId", localMember.getServerId());
    ClusterMember clusterMember = ImmutableClusterMember.builder()
        .setHost(request.getClusterMember().getHost())
        .setPort(request.getClusterMember().getPort())
        .build();
    VoteRequest voteRequest = ImmutableVoteRequest.builder()
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
    MDC.remove("serverId");
  }
}
