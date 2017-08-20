package com.github.nhirakawa.server.guice;

import java.lang.reflect.Modifier;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.github.nhirakawa.server.cli.CliArguments;
import com.github.nhirakawa.server.config.ClusterMember;
import com.github.nhirakawa.server.config.Configuration;
import com.github.nhirakawa.server.config.ImmutableClusterMember;
import com.github.nhirakawa.server.transport.grpc.SocketAddressProvider;
import com.github.nhirakawa.server.transport.grpc.WilsonGrpcClient.WilsonGrpcClientFactory;
import com.github.nhirakawa.server.transport.grpc.WilsonGrpcClientAdapter;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.WaitStrategies;
import com.google.common.collect.ImmutableSet;
import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.Multibinder;
import com.google.protobuf.util.JsonFormat;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerInterceptor;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.netty.NettyServerBuilder;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class WilsonTransportModule extends AbstractModule {

  private static final Logger LOG = LoggerFactory.getLogger(WilsonTransportModule.class);

  private final ClusterMember localMember;

  public WilsonTransportModule(ClusterMember localMember) {
    this.localMember = localMember;
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
    bind(Key.get(ClusterMember.class, LocalMember.class)).toInstance(localMember);
    bind(JsonFormat.Printer.class).toInstance(JsonFormat.printer().includingDefaultValueFields());
    bind(EventBus.class).toInstance(new EventBus());
  }

  @Provides
  @Singleton
  ObjectMapper provideObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new GuavaModule());
    return objectMapper;
  }

  @Provides
  @Singleton
  ScheduledExecutorService provideScheduledExecutorService() {
    ScheduledExecutorService executor = Executors.newScheduledThreadPool(
        4,
        getNamedThreadFactory("wilson-scheduled")
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
  Server provideServer(CliArguments cliArguments,
                       SocketAddressProvider socketAddressProvider,
                       Configuration configuration,
                       Set<BindableService> services,
                       Set<ServerInterceptor> serverInterceptors,
                       @LocalMember ClusterMember clusterMember) {
    if (cliArguments.isLocalMode()) {
      InProcessServerBuilder builder = InProcessServerBuilder.forName(clusterMember.getServerId());
      services.forEach(builder::addService);
      serverInterceptors.forEach(builder::intercept);
      return builder.build();
    } else {
      NettyServerBuilder builder = NettyServerBuilder.forAddress(socketAddressProvider.getSocketAddressFor(clusterMember));
      builder.channelType(NioServerSocketChannel.class);
      services.forEach(builder::addService);
      serverInterceptors.forEach(builder::intercept);
      return builder.build();
    }

//    NettyServerBuilder builder = NettyServerBuilder.forAddress(socketAddressProvider.getSocketAddressFor(clusterMember));
//    if (cliArguments.isLocalMode()) {
//      builder.channelType(LocalServerChannel.class);
//    } else {
//      builder.channelType(NioServerSocketChannel.class);
//    }
//    services.forEach(builder::addService);
//    serverInterceptors.forEach(builder::intercept);
//    return builder.build();
  }

  @Provides
  @Singleton
  Set<ImmutableClusterMember> providePeers(Configuration configuration,
                                           @LocalMember ClusterMember clusterMember) {
    return configuration.getClusterMembers().stream()
        .filter(member -> !member.equals(clusterMember))
        .collect(ImmutableSet.toImmutableSet());
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

  private static ThreadFactory getNamedThreadFactory(String namespace) {
    return new ThreadFactoryBuilder()
        .setNameFormat(namespace + "-%s")
        .build();
  }
}
