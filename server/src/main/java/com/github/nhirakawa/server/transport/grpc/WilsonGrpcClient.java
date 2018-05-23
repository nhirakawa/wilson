package com.github.nhirakawa.server.transport.grpc;

import java.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;

import com.github.nhirakawa.server.cli.CliArguments;
import com.github.nhirakawa.server.config.ClusterMemberModel;
import com.github.nhirakawa.server.transport.grpc.intercept.ClientHeaderInterceptor;
import com.github.nhirakawa.wilson.models.messages.HeartbeatRequest;
import com.github.nhirakawa.wilson.models.messages.HeartbeatRequestModel;
import com.github.nhirakawa.wilson.models.messages.HeartbeatResponse;
import com.github.nhirakawa.wilson.models.messages.HeartbeatResponseModel;
import com.github.nhirakawa.wilson.models.messages.VoteRequestModel;
import com.github.nhirakawa.wilson.models.messages.VoteResponse;
import com.github.nhirakawa.wilson.models.messages.VoteResponseModel;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.protobuf.GeneratedMessageV3;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.netty.NettyChannelBuilder;

public class WilsonGrpcClient {

  private final WilsonGrpc.WilsonBlockingStub stub;
  private final WilsonGrpc.WilsonFutureStub futureStub;

  @AssistedInject
  WilsonGrpcClient(@Assisted ClusterMemberModel clusterMember,
                   ClientHeaderInterceptor clientHeaderInterceptor,
                   CliArguments cliArguments,
                   SocketAddressProvider socketAddressProvider) {
    ManagedChannelBuilder<?> channelBuilder = cliArguments.isLocalMode()
        ? InProcessChannelBuilder.forName(clusterMember.getServerId())
        : NettyChannelBuilder.forAddress(socketAddressProvider.getSocketAddressFor(clusterMember));

    ManagedChannel channel = channelBuilder
        .intercept(clientHeaderInterceptor)
        .usePlaintext(true)
        .userAgent("Wilson/1.0")
        .build();

    this.stub = WilsonGrpc.newBlockingStub(channel);
    this.futureStub = WilsonGrpc.newFutureStub(channel);
  }

  public VoteResponse requestVoteSync(VoteRequestModel voteRequest) {
    WilsonProtos.VoteRequest protoVoteRequest = ProtobufTranslator.toProto(voteRequest);
    WilsonProtos.VoteResponse protoVoteResponse = stub.requestVote(protoVoteRequest);
    return ProtobufTranslator.fromProto(protoVoteResponse);
  }

  public HeartbeatResponse sendHeartbeatSync(HeartbeatRequest heartbeatRequestModel) {
    WilsonProtos.HeartbeatRequest protoHeartbeatRequest = ProtobufTranslator.toProto(heartbeatRequestModel);
    WilsonProtos.HeartbeatResponse protoHeartbeatResponse = stub.heartbeat(protoHeartbeatRequest);
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
