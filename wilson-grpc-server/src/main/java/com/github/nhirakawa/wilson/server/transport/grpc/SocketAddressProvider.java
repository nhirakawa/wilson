package com.github.nhirakawa.wilson.server.transport.grpc;

import com.github.nhirakawa.wilson.models.ClusterMemberModel;
import com.github.nhirakawa.wilson.common.config.ConfigPath;
import com.typesafe.config.Config;
import io.grpc.inprocess.InProcessSocketAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import javax.inject.Inject;

public class SocketAddressProvider {
  private final boolean isLocalMode;
  private final Map<ClusterMemberModel, SocketAddress> socketAddressMap;

  @Inject
  SocketAddressProvider(Config config) {
    this.isLocalMode =
      config.getBoolean(ConfigPath.WILSON_LOCAL_CLUSTER.getPath());
    this.socketAddressMap = new ConcurrentHashMap<>();
  }

  public SocketAddress getSocketAddressFor(ClusterMemberModel clusterMember) {
    return socketAddressMap.computeIfAbsent(
      clusterMember,
      this::getSocketAddress
    );
  }

  private SocketAddress getSocketAddress(ClusterMemberModel clusterMember) {
    if (isLocalMode) {
      return new InProcessSocketAddress(clusterMember.getServerId());
    } else {
      return new InetSocketAddress(
        clusterMember.getHost(),
        clusterMember.getPort()
      );
    }
  }
}
