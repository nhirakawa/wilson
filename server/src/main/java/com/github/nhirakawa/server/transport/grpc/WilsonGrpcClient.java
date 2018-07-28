package com.github.nhirakawa.server.transport.grpc;

import java.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;

import com.github.nhirakawa.server.config.ClusterMemberModel;
import com.github.nhirakawa.server.config.ConfigPath;
import com.github.nhirakawa.server.transport.grpc.intercept.ClientHeaderInterceptor;
import com.github.nhirakawa.wilson.models.messages.HeartbeatRequest;
import com.github.nhirakawa.wilson.models.messages.HeartbeatResponse;
import com.github.nhirakawa.wilson.models.messages.VoteRequestModel;
import com.github.nhirakawa.wilson.models.messages.VoteResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.protobuf.GeneratedMessageV3;
import com.typesafe.config.Config;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.netty.NettyChannelBuilder;

public class WilsonGrpcClient {

  private final WilsonGrpc.WilsonFutureStub futureStub;

  @AssistedInject
  WilsonGrpcClient(@Assisted ClusterMemberModel clusterMember,
                   ClientHeaderInterceptor clientHeaderInterceptor,
                   Config config,
                   SocketAddressProvider socketAddressProvider) {
    ManagedChannelBuilder<?> channelBuilder = config.getBoolean(ConfigPath.WILSON_LOCAL_CLUSTER.getPath())
        ? InProcessChannelBuilder.forName(clusterMember.getServerId())
        : NettyChannelBuilder.forAddress(socketAddressProvider.getSocketAddressFor(clusterMember));

    ManagedChannel channel = channelBuilder
        .intercept(clientHeaderInterceptor)
        .usePlaintext(true)
        .userAgent("Wilson/1.0")
        .build();

    this.futureStub = WilsonGrpc.newFutureStub(channel);
  }

  public VoteResponse requestVoteSync(VoteRequestModel voteRequest) {
    WilsonProtos.VoteRequest protoVoteRequest = ProtobufTranslator.toProto(voteRequest);
    WilsonProtos.VoteResponse protoVoteResponse = fromListenableFuture(futureStub.requestVote(protoVoteRequest)).join();
    return ProtobufTranslator.fromProto(protoVoteResponse);
  }

  public HeartbeatResponse sendHeartbeatSync(HeartbeatRequest heartbeatRequestModel) {
    WilsonProtos.HeartbeatRequest protoHeartbeatRequest = ProtobufTranslator.toProto(heartbeatRequestModel);
    WilsonProtos.HeartbeatResponse protoHeartbeatResponse = fromListenableFuture(futureStub.heartbeat(protoHeartbeatRequest)).join();
    return ProtobufTranslator.fromProto(protoHeartbeatResponse);
  }

  private static <T extends GeneratedMessageV3> CompletableFuture<T> fromListenableFuture(ListenableFuture<T> future) {
    CompletableFuture<T> completableFuture = new CompletableFuture<>();
    Futures.addCallback(future, new FutureCallback<T>() {
      @Override
      public void onSuccess(@Nullable T result) {
        completableFuture.complete(result);
      }

      @Override
      public void onFailure(Throwable t) {
        completableFuture.completeExceptionally(t);
      }
    });
    return completableFuture;
  }

  public interface WilsonGrpcClientFactory {
    WilsonGrpcClient create(ClusterMemberModel clusterMember);
  }
}
