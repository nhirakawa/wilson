package com.github.nhirakawa.server.transport.grpc;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;

import com.github.nhirakawa.server.config.ConfigPath;
import com.github.nhirakawa.server.models.ClusterMemberModel;
import com.typesafe.config.Config;

import io.grpc.inprocess.InProcessSocketAddress;

public class SocketAddressProvider {

  private final boolean isLocalMode;
  private final Map<ClusterMemberModel, SocketAddress> socketAddressMap;

  @Inject
  SocketAddressProvider(Config config) {
    this.isLocalMode = config.getBoolean(ConfigPath.WILSON_LOCAL_CLUSTER.getPath());
    this.socketAddressMap = new ConcurrentHashMap<>();
  }

  public SocketAddress getSocketAddressFor(ClusterMemberModel clusterMember) {
    return socketAddressMap.computeIfAbsent(clusterMember, this::getSocketAddress);
  }

  private SocketAddress getSocketAddress(ClusterMemberModel clusterMember) {
    if (isLocalMode) {
      return new InProcessSocketAddress(clusterMember.getServerId());
    } else {
      return new InetSocketAddress(clusterMember.getHost(), clusterMember.getPort());
    }
  }
}
