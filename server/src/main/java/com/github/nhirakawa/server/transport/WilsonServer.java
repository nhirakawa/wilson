package com.github.nhirakawa.server.transport;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nhirakawa.server.config.ClusterMember;
import com.github.nhirakawa.server.guice.LocalMember;
import com.github.nhirakawa.server.timeout.ElectionTimeout;
import com.github.nhirakawa.server.timeout.HeartbeatTimeout;
import com.github.nhirakawa.server.timeout.LeaderTimeout;
import com.google.inject.Inject;

import io.grpc.Server;

public class WilsonServer {

  private static final Logger LOG = LoggerFactory.getLogger(WilsonServer.class);

  private final Server server;
  private final LeaderTimeout leaderTimeout;
  private final ElectionTimeout electionTimeout;
  private final HeartbeatTimeout heartbeatTimeout;
  private final ClusterMember clusterMember;

  @Inject
  public WilsonServer(Server server,
                      LeaderTimeout leaderTimeout,
                      ElectionTimeout electionTimeout,
                      HeartbeatTimeout heartbeatTimeout,
                      @LocalMember ClusterMember clusterMember) {
    this.server = server;
    this.leaderTimeout = leaderTimeout;
    this.electionTimeout = electionTimeout;
    this.heartbeatTimeout = heartbeatTimeout;
    this.clusterMember = clusterMember;
  }

  public void start() throws InterruptedException, IOException {
    server.start();
    LOG.info("Wilson grpc server started for {}", clusterMember.getServerId());
    leaderTimeout.start();
    electionTimeout.start();
    heartbeatTimeout.start();
    server.awaitTermination();
  }
}
