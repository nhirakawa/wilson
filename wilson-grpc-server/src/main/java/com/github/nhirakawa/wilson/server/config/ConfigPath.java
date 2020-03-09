package com.github.nhirakawa.wilson.server.config;

public enum ConfigPath {
  WILSON_CLUSTER_ID("wilson.clusterId"),
  WILSON_ELECTION_TIMEOUT("wilson.timeout.election"),
  WILSON_HEARTBEAT_TIMEOUT("wilson.timeout.heartbeat"),
  WILSON_LEADER_TIMEOUT("wilson.timeout.leader"),
  WILSON_LOCAL_ADDRESS("wilson.local"),
  WILSON_CLUSTER_ADDRESSES("wilson.cluster"),
  WILSON_LOCAL_CLUSTER("wilson.localCluster");
  private final String path;

  ConfigPath(String path) {
    this.path = path;
  }

  public String getPath() {
    return path;
  }
}
