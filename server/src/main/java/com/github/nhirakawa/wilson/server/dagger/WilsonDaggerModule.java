package com.github.nhirakawa.wilson.server.dagger;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Singleton;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.github.nhirakawa.wilson.server.config.ConfigPath;
import com.github.nhirakawa.wilson.server.models.ClusterMember;
import com.github.nhirakawa.wilson.server.models.WilsonState;
import com.github.nhirakawa.wilson.server.transport.grpc.SocketAddressProvider;
import com.github.nhirakawa.wilson.server.transport.grpc.WilsonGrpcService;
import com.github.nhirakawa.wilson.server.transport.grpc.intercept.LoggingInterceptor;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.WaitStrategies;
import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.protobuf.util.JsonFormat;
import com.hubspot.jackson.datatype.protobuf.ProtobufModule;
import com.typesafe.config.Config;

import dagger.Module;
import dagger.Provides;
import io.grpc.Server;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.netty.NettyServerBuilder;
import io.netty.channel.socket.nio.NioServerSocketChannel;

@Module
public class WilsonDaggerModule {

  private final Config config;
  private final ClusterMember localMember;
  private Set<ClusterMember> clusterMembers;

  public WilsonDaggerModule(Config config,
                            ClusterMember localMember,
                            Set<ClusterMember> clusterMembers) {
    this.config = config;
    this.localMember = localMember;
    this.clusterMembers = clusterMembers;
  }

  @Provides
  protected Config provideConfig() {
    return config;
  }

  @Provides
  @LocalMember
  protected ClusterMember provideLocalMember() {
    return localMember;
  }

  @Provides
  protected Set<ClusterMember> provideClusterMembers() {
    return clusterMembers;
  }

  @Provides
  @Singleton
  AtomicReference<WilsonState> provideWilsonState() {
    WilsonState wilsonState = WilsonState.builder().build();
    return new AtomicReference<>(wilsonState);
  }

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
  protected EventBus provideEventBus() {
    EventBus eventBus = new EventBus();
    return eventBus;
  }

  @Provides
  @Singleton
  Retryer<Void> provideRetryer() {
    return RetryerBuilder.<Void>newBuilder()
        .withWaitStrategy(WaitStrategies.fixedWait(5, TimeUnit.SECONDS))
        .build();
  }

  @Provides
  @Singleton
  ScheduledExecutorService provideScheduledExecutorService(@LocalMember ClusterMember localMember) {
    return Executors.newScheduledThreadPool(
        4,
        getNamedThreadFactory("wilson-scheduled", localMember)
    );
  }

  @Provides
  @Singleton
  protected Server provideServer(Config config,
                                 SocketAddressProvider socketAddressProvider,
                                 WilsonGrpcService wilsonGrpcService,
                                 LoggingInterceptor loggingInterceptor,
                                 @LocalMember ClusterMember localMember) {
    ExecutorService executorService = Executors.newCachedThreadPool(
        getNamedThreadFactory("grpc-server", localMember)
    );

    if (config.getBoolean(ConfigPath.WILSON_LOCAL_CLUSTER.getPath())) {
      InProcessServerBuilder builder = InProcessServerBuilder.forName(localMember.getServerId());
      builder.addService(wilsonGrpcService);
      builder.intercept(loggingInterceptor);
      builder.executor(executorService);
      return builder.build();
    } else {
      NettyServerBuilder builder = NettyServerBuilder.forAddress(socketAddressProvider.getSocketAddressFor(localMember));
      builder.channelType(NioServerSocketChannel.class);
      builder.addService(wilsonGrpcService);
      builder.intercept(loggingInterceptor);
      builder.executor(executorService);
      return builder.build();
    }
  }

  private static ThreadFactory getNamedThreadFactory(String namespace,
                                                     ClusterMember clusterMember) {
    String format = String.format("%s-%s-%s", namespace, clusterMember.getHost(), clusterMember.getPort());
    return new ThreadFactoryBuilder()
        .setNameFormat(format + "-%s")
        .build();
  }

}
