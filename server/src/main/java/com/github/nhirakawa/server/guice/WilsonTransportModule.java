package com.github.nhirakawa.server.guice;

import java.lang.reflect.Modifier;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;

import com.github.nhirakawa.server.config.ConfigPath;
import com.github.nhirakawa.server.models.ClusterMember;
import com.github.nhirakawa.server.transport.grpc.SocketAddressProvider;
import com.github.nhirakawa.server.transport.grpc.WilsonGrpcClient.WilsonGrpcClientFactory;
import com.github.nhirakawa.server.transport.grpc.WilsonGrpcClientAdapter;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.WaitStrategies;
import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.Multibinder;
import com.google.protobuf.util.JsonFormat;
import com.hubspot.jackson.datatype.protobuf.ProtobufModule;
import com.typesafe.config.Config;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerInterceptor;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.netty.NettyServerBuilder;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class WilsonTransportModule extends AbstractModule {

  private static final Logger LOG = LoggerFactory.getLogger(WilsonTransportModule.class);

  private final ClusterMember localMember;
  private final Set<ClusterMember> clusterMembers;

  public WilsonTransportModule(ClusterMember localMember,
                               Set<ClusterMember> clusterMembers) {
    this.localMember = localMember;
    this.clusterMembers = clusterMembers;
  }

  @Override
  protected void configure() {
    Multibinder<BindableService> serviceMultibinder = Multibinder.newSetBinder(binder(), BindableService.class);
    Reflections reflections = new Reflections("com.github.nhirakawa.server");
    reflections.getSubTypesOf(BindableService.class).stream()
        .filter(this::isConcreteClass)
        .peek(this::logBindableService)
        .forEach(clazz -> serviceMultibinder.addBinding().to(clazz));

    Multibinder<ServerInterceptor> serverInterceptorMultiBinder = Multibinder.newSetBinder(binder(), ServerInterceptor.class);
    reflections.getSubTypesOf(ServerInterceptor.class).stream()
        .filter(this::isConcreteClass)
        .peek(this::logServerInterceptor)
        .forEach(clazz -> serverInterceptorMultiBinder.addBinding().to(clazz));

    install(new FactoryModuleBuilder().build(WilsonGrpcClientFactory.class));

    bind(WilsonGrpcClientAdapter.class).asEagerSingleton();
    bind(JsonFormat.Printer.class).toInstance(JsonFormat.printer().includingDefaultValueFields());
    bind(EventBus.class).toInstance(new EventBus());
  }

  @Provides
  @Singleton
  ObjectMapper provideObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();

    objectMapper.registerModule(new GuavaModule());
    objectMapper.registerModule(new ProtobufModule());

    objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    return objectMapper;
  }

  @Provides
  @Singleton
  ScheduledExecutorService provideScheduledExecutorService(@LocalMember ClusterMember clusterMember) {
    ScheduledExecutorService executor = Executors.newScheduledThreadPool(
        4,
        getNamedThreadFactory("wilson-scheduled", clusterMember)
    );
    return executor;
  }

  @Provides
  @Singleton
  Retryer<Void> provideRetyrer() {
    return RetryerBuilder.<Void>newBuilder()
        .withWaitStrategy(WaitStrategies.fixedWait(5L, TimeUnit.SECONDS))
//        .withStopStrategy(StopStrategies.stopAfterAttempt(10))
        .build();
  }

  @Provides
  @Singleton
  Server provideServer(Config config,
                       SocketAddressProvider socketAddressProvider,
                       Set<BindableService> services,
                       Set<ServerInterceptor> serverInterceptors,
                       @LocalMember ClusterMember clusterMember) {
    ExecutorService executorService = Executors.newCachedThreadPool(
        getNamedThreadFactory("grpc-server", clusterMember)
    );

    if (config.getBoolean(ConfigPath.WILSON_LOCAL_CLUSTER.getPath())) {
      InProcessServerBuilder builder = InProcessServerBuilder.forName(clusterMember.getServerId());
      services.forEach(builder::addService);
      serverInterceptors.forEach(builder::intercept);
      builder.executor(executorService);
      return builder.build();
    } else {
      NettyServerBuilder builder = NettyServerBuilder.forAddress(socketAddressProvider.getSocketAddressFor(clusterMember));
      builder.channelType(NioServerSocketChannel.class);
      services.forEach(builder::addService);
      serverInterceptors.forEach(builder::intercept);
      builder.executor(executorService);
      return builder.build();
    }
  }

  @Provides
  @Singleton
  @LocalMember
  ClusterMember provideLocalMember() {
    return localMember;
  }

  @Provides
  @Singleton
  Set<ClusterMember> provideClusterMembers() {
    return clusterMembers;
  }

  private boolean isConcreteClass(Class<?> clazz) {
    return !clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers());
  }

  private void logBindableService(Class<?> clazz) {
    LOG.debug("Found BindableServce {}", clazz.getCanonicalName());
  }

  private void logServerInterceptor(Class<?> clazz) {
    LOG.debug("Found ServerInterceptor {}", clazz.getCanonicalName());
  }

  private static ThreadFactory getNamedThreadFactory(String namespace,
                                                     ClusterMember clusterMember) {
    String format = String.format("%s-%s-%s", namespace, clusterMember.getHost(), clusterMember.getPort());
    return new ThreadFactoryBuilder()
        .setNameFormat(format + "-%s")
        .build();
  }
}
