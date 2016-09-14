package com.github.nhirakawa.server.transport.grpc;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.github.nhirakawa.server.cli.CliArguments;
import com.github.nhirakawa.server.config.ClusterMember;
import com.google.inject.Inject;

import io.grpc.inprocess.InProcessSocketAddress;

public class SocketAddressProvider {

  private final boolean isLocalMode;
  private final Map<ClusterMember, SocketAddress> socketAddressMap;

  @Inject
  SocketAddressProvider(CliArguments cliArguments) {
    this.isLocalMode = cliArguments.isLocalMode();
    this.socketAddressMap = new ConcurrentHashMap<>();
  }

  public SocketAddress getSocketAddressFor(ClusterMember clusterMember) {
    return socketAddressMap.computeIfAbsent(clusterMember, this::getSocketAddress);
  }

  private SocketAddress getSocketAddress(ClusterMember clusterMember) {
    if (isLocalMode) {
      return new InProcessSocketAddress(clusterMember.getServerId());
    } else {
      return new InetSocketAddress(clusterMember.getHost(), clusterMember.getPort());
    }
  }
}
