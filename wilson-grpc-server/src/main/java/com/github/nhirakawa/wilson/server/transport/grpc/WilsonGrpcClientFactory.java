package com.github.nhirakawa.wilson.server.transport.grpc;

import com.github.nhirakawa.wilson.models.ClusterMemberModel;
import com.github.nhirakawa.wilson.server.dagger.WilsonDaggerModule;
import com.github.nhirakawa.wilson.server.transport.grpc.intercept.ClientHeaderInterceptor;
import com.typesafe.config.Config;
import java.util.concurrent.ExecutorService;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

class WilsonGrpcClientFactory {
  private final Provider<ClientHeaderInterceptor> clientHeaderInterceptorProvider;
  private final Provider<Config> configProvider;
  private final Provider<SocketAddressProvider> socketAddressProviderProvider;
  private final Provider<ProtobufTranslator> protobufTranslatorProvider;
  private final ExecutorService executorService;

  @Inject
  WilsonGrpcClientFactory(
    Provider<ClientHeaderInterceptor> clientHeaderInterceptorProvider,
    Provider<Config> configProvider,
    Provider<SocketAddressProvider> socketAddressProviderProvider,
    Provider<ProtobufTranslator> protobufTranslatorProvider,
    @Named(
      WilsonDaggerModule.GRPC_CLIENT_FUTURE_EXECUTOR
    ) ExecutorService executorService
  ) {
    this.clientHeaderInterceptorProvider = clientHeaderInterceptorProvider;
    this.configProvider = configProvider;
    this.socketAddressProviderProvider = socketAddressProviderProvider;
    this.protobufTranslatorProvider = protobufTranslatorProvider;
    this.executorService = executorService;
  }

  public WilsonGrpcClient create(ClusterMemberModel clusterMember) {
    return new WilsonGrpcClient(
      clusterMember,
      clientHeaderInterceptorProvider.get(),
      configProvider.get(),
      socketAddressProviderProvider.get(),
      protobufTranslatorProvider.get(),
      executorService
    );
  }
}
