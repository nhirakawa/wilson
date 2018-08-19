package com.github.nhirakawa.server.transport.grpc;

import javax.inject.Inject;
import javax.inject.Provider;

import com.github.nhirakawa.server.models.ClusterMemberModel;
import com.github.nhirakawa.server.transport.grpc.intercept.ClientHeaderInterceptor;
import com.typesafe.config.Config;

class WilsonGrpcClientFactory {

  private final Provider<ClientHeaderInterceptor> clientHeaderInterceptorProvider;
  private final Provider<Config> configProvider;
  private final Provider<SocketAddressProvider> socketAddressProviderProvider;
  private final Provider<ProtobufTranslator> protobufTranslatorProvider;

  @Inject
  WilsonGrpcClientFactory(Provider<ClientHeaderInterceptor> clientHeaderInterceptorProvider,
                          Provider<Config> configProvider,
                          Provider<SocketAddressProvider> socketAddressProviderProvider,
                          Provider<ProtobufTranslator> protobufTranslatorProvider) {
    this.clientHeaderInterceptorProvider = clientHeaderInterceptorProvider;
    this.configProvider = configProvider;
    this.socketAddressProviderProvider = socketAddressProviderProvider;
    this.protobufTranslatorProvider = protobufTranslatorProvider;
  }

  public WilsonGrpcClient create(ClusterMemberModel clusterMember) {
    return new WilsonGrpcClient(clusterMember, clientHeaderInterceptorProvider.get(), configProvider.get(), socketAddressProviderProvider.get(), protobufTranslatorProvider.get());
  }
}
