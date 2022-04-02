package com.github.nhirakawa.wilson.server.transport.grpc;

import com.github.nhirakawa.wilson.models.ClusterMemberModel;
import com.github.nhirakawa.wilson.models.messages.AppendEntriesRequest;
import com.github.nhirakawa.wilson.models.messages.AppendEntriesResponse;
import com.github.nhirakawa.wilson.models.messages.VoteRequestModel;
import com.github.nhirakawa.wilson.models.messages.VoteResponse;
import com.github.nhirakawa.wilson.server.config.ConfigPath;
import com.github.nhirakawa.wilson.server.dagger.WilsonDaggerModule;
import com.github.nhirakawa.wilson.server.transport.grpc.intercept.ClientHeaderInterceptor;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.GeneratedMessageV3;
import com.typesafe.config.Config;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.netty.NettyChannelBuilder;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import javax.annotation.Nullable;
import javax.inject.Named;

public class WilsonGrpcClient {
  private final WilsonGrpc.WilsonFutureStub futureStub;
  private final ProtobufTranslator protobufTranslator;
  private final ExecutorService executorService;

  WilsonGrpcClient(
    ClusterMemberModel clusterMember,
    ClientHeaderInterceptor clientHeaderInterceptor,
    Config config,
    SocketAddressProvider socketAddressProvider,
    ProtobufTranslator protobufTranslator,
    ExecutorService executorService
  ) {
    ManagedChannelBuilder<?> channelBuilder = config.getBoolean(
      ConfigPath.WILSON_LOCAL_CLUSTER.getPath()
    )
      ? InProcessChannelBuilder.forName(clusterMember.getServerId())
      : NettyChannelBuilder.forAddress(
      socketAddressProvider.getSocketAddressFor(clusterMember)
    );

    ManagedChannel channel = channelBuilder
      .intercept(clientHeaderInterceptor)
      .usePlaintext(true)
      .userAgent("Wilson/1.0")
      .build();

    this.futureStub = WilsonGrpc.newFutureStub(channel);
    this.protobufTranslator = protobufTranslator;
    this.executorService = executorService;
  }

  private <T extends GeneratedMessageV3> CompletableFuture<T> fromListenableFuture(
    ListenableFuture<T> future
  ) {
    CompletableFuture<T> completableFuture = new CompletableFuture<>();
    Futures.addCallback(
      future,
      new FutureCallback<T>() {

        @Override
        public void onSuccess(@Nullable T result) {
          completableFuture.complete(result);
        }

        @Override
        public void onFailure(Throwable t) {
          completableFuture.completeExceptionally(t);
        }
      },
      executorService
    );
    return completableFuture;
  }

  public VoteResponse requestVoteSync(VoteRequestModel voteRequest) {
    VoteRequestProto protoVoteRequest = protobufTranslator.toProto(voteRequest);
    VoteResponseProto protoVoteResponse = fromListenableFuture(
        futureStub.requestVote(protoVoteRequest)
      )
      .join();
    return protobufTranslator.fromProto(protoVoteResponse);
  }

  public AppendEntriesResponse sendHeartbeat(
    AppendEntriesRequest appendEntriesRequest
  ) {
    AppendEntriesRequestProto appendEntriesRequestProto = protobufTranslator.toProto(
      appendEntriesRequest
    );
    return fromListenableFuture(
        futureStub.appendEntries(appendEntriesRequestProto)
      )
      .thenApply(protobufTranslator::fromProto)
      .join();
  }
}
