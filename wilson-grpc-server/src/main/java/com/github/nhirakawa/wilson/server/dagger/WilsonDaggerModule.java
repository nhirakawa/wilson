package com.github.nhirakawa.wilson.server.dagger;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.github.nhirakawa.wilson.common.NamedThreadFactory;
import com.github.nhirakawa.wilson.models.ClusterMember;
import com.github.nhirakawa.wilson.protocol.LocalMember;
import com.github.nhirakawa.wilson.common.config.ConfigPath;
import com.github.nhirakawa.wilson.server.transport.grpc.intercept.LoggingInterceptor;
import com.github.nhirakawa.wilson.server.transport.grpc.SocketAddressProvider;
import com.github.nhirakawa.wilson.server.transport.grpc.WilsonGrpcService;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.WaitStrategies;
import com.google.protobuf.util.JsonFormat;
import com.hubspot.jackson.datatype.protobuf.ProtobufModule;
import com.typesafe.config.Config;
import dagger.Module;
import dagger.Provides;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.netty.NettyServerBuilder;
import io.grpc.Server;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

@Module
public class WilsonDaggerModule {

  @Provides
  @Singleton
  protected static ObjectMapper provideObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();

    objectMapper.registerModule(new GuavaModule());
    objectMapper.registerModule(new ProtobufModule());

    objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    return objectMapper;
  }



  @Provides
  @Singleton
  protected JsonFormat.Printer provideJsonFormatPrinter() {
    return JsonFormat.printer().includingDefaultValueFields();
  }

  @Provides
  @Singleton
  Retryer<Void> provideRetryer() {
    return RetryerBuilder
      .<Void>newBuilder()
      .withWaitStrategy(WaitStrategies.fixedWait(5, TimeUnit.SECONDS))
      .build();
  }

  @Provides
  @Singleton
  protected Server provideServer(
    Config config,
    SocketAddressProvider socketAddressProvider,
    WilsonGrpcService wilsonGrpcService,
    LoggingInterceptor loggingInterceptor,
    @LocalMember ClusterMember localMember
  ) {
    ExecutorService executorService = Executors.newCachedThreadPool(
        NamedThreadFactory.build("grpc-server", localMember)
    );

    if (config.getBoolean(ConfigPath.WILSON_LOCAL_CLUSTER.getPath())) {
      InProcessServerBuilder builder = InProcessServerBuilder.forName(
        localMember.getServerId()
      );
      builder.addService(wilsonGrpcService);
      builder.intercept(loggingInterceptor);
      builder.executor(executorService);
      return builder.build();
    } else {
      NettyServerBuilder builder = NettyServerBuilder.forAddress(
        socketAddressProvider.getSocketAddressFor(localMember)
      );
      builder.channelType(NioServerSocketChannel.class);
      builder.addService(wilsonGrpcService);
      builder.intercept(loggingInterceptor);
      builder.executor(executorService);
      return builder.build();
    }
  }
}
