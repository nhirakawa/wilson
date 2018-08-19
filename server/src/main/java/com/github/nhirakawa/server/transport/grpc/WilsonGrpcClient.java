package com.github.nhirakawa.server.transport.grpc;

import java.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.nhirakawa.server.config.ConfigPath;
import com.github.nhirakawa.server.models.ClusterMemberModel;
import com.github.nhirakawa.server.models.messages.HeartbeatRequest;
import com.github.nhirakawa.server.models.messages.HeartbeatResponse;
import com.github.nhirakawa.server.models.messages.VoteRequestModel;
import com.github.nhirakawa.server.models.messages.VoteResponse;
import com.github.nhirakawa.server.transport.grpc.intercept.ClientHeaderInterceptor;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.GeneratedMessageV3;
import com.typesafe.config.Config;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.netty.NettyChannelBuilder;

public class WilsonGrpcClient {

  private static final Logger LOG = LoggerFactory.getLogger(WilsonGrpcClient.class);

  private final WilsonGrpc.WilsonFutureStub futureStub;
  private final ProtobufTranslator protobufTranslator;

  WilsonGrpcClient(ClusterMemberModel clusterMember,
                   ClientHeaderInterceptor clientHeaderInterceptor,
                   Config config,
                   SocketAddressProvider socketAddressProvider,
                   ProtobufTranslator protobufTranslator) {
    ManagedChannelBuilder<?> channelBuilder = config.getBoolean(ConfigPath.WILSON_LOCAL_CLUSTER.getPath())
        ? InProcessChannelBuilder.forName(clusterMember.getServerId())
        : NettyChannelBuilder.forAddress(socketAddressProvider.getSocketAddressFor(clusterMember));

    ManagedChannel channel = channelBuilder
        .intercept(clientHeaderInterceptor)
        .usePlaintext(true)
        .userAgent("Wilson/1.0")
        .build();

    this.futureStub = WilsonGrpc.newFutureStub(channel);
    this.protobufTranslator = protobufTranslator;
  }

  public VoteResponse requestVoteSync(VoteRequestModel voteRequest) {
    try {
      LOG.debug("pojo vote request {}", protobufTranslator.instance().writeValueAsString(voteRequest));
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    VoteRequestProto protoVoteRequest = protobufTranslator.toProto(voteRequest);
    try {
      LOG.debug("proto vote request {}", protobufTranslator.instance().writeValueAsString(protoVoteRequest));
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

    VoteResponseProto protoVoteResponse = fromListenableFuture(futureStub.requestVote(protoVoteRequest)).join();
    return protobufTranslator.fromProto(protoVoteResponse);
  }

  public HeartbeatResponse sendHeartbeatSync(HeartbeatRequest heartbeatRequestModel) {
    HeartbeatRequestProto protoHeartbeatRequest = protobufTranslator.toProto(heartbeatRequestModel);
    HeartbeatResponseProto protoHeartbeatResponse = fromListenableFuture(futureStub.heartbeat(protoHeartbeatRequest)).join();
    return protobufTranslator.fromProto(protoHeartbeatResponse);
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
}
