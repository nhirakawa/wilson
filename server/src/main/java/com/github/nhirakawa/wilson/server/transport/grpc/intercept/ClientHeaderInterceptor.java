package com.github.nhirakawa.wilson.server.transport.grpc.intercept;

import javax.inject.Inject;

import com.github.nhirakawa.wilson.server.config.ConfigPath;
import com.github.nhirakawa.wilson.server.dagger.LocalMember;
import com.github.nhirakawa.wilson.server.models.ClusterMember;
import com.typesafe.config.Config;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall.SimpleForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;

public class ClientHeaderInterceptor implements ClientInterceptor {

  private final String clusterId;
  private final String serverId;

  @Inject
  ClientHeaderInterceptor(Config config,
                          @LocalMember ClusterMember clusterMember) {
    this.clusterId = config.getString(ConfigPath.WILSON_CLUSTER_ID.getPath());
    this.serverId = clusterMember.getServerId();
  }

  @Override
  public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method,
                                                             CallOptions callOptions,
                                                             Channel next) {
    return new SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {
      @Override
      public void start(Listener<RespT> responseListener, Metadata headers) {
        headers.put(Keys.CLUSTER_ID_KEY, clusterId);
        headers.put(Keys.SERVER_ID_KEY, serverId);
        super.start(responseListener, headers);
      }
    };
  }
}
