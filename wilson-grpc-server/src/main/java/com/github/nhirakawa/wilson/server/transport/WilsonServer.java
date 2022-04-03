package com.github.nhirakawa.wilson.server.transport;

import com.github.nhirakawa.wilson.models.ClusterMember;
import com.github.nhirakawa.wilson.models.ClusterMemberModel;
import com.github.nhirakawa.wilson.server.dagger.LocalMember;
import com.github.nhirakawa.wilson.protocol.timeout.ElectionTimeout;
import com.github.nhirakawa.wilson.protocol.timeout.HeartbeatTimeout;
import com.github.nhirakawa.wilson.protocol.timeout.LeaderTimeout;
import com.github.nhirakawa.wilson.server.transport.grpc.EagerSingletons;
import io.grpc.Server;
import java.io.IOException;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WilsonServer {
  private static final Logger LOG = LoggerFactory.getLogger(WilsonServer.class);

  private final Server server;
  private final LeaderTimeout leaderTimeout;
  private final ElectionTimeout electionTimeout;
  private final HeartbeatTimeout heartbeatTimeout;
  private final EagerSingletons eagerSingletons;
  private final ClusterMemberModel clusterMember;

  @Inject
  public WilsonServer(
    Server server,
    LeaderTimeout leaderTimeout,
    ElectionTimeout electionTimeout,
    HeartbeatTimeout heartbeatTimeout,
    EagerSingletons eagerSingletons,
    @LocalMember ClusterMember clusterMember
  ) {
    this.server = server;
    this.leaderTimeout = leaderTimeout;
    this.electionTimeout = electionTimeout;
    this.heartbeatTimeout = heartbeatTimeout;
    this.eagerSingletons = eagerSingletons;
    this.clusterMember = clusterMember;
  }

  public void start() throws InterruptedException, IOException {
    server.start();
    LOG.info("Wilson grpc server started for {}", clusterMember.getServerId());
    leaderTimeout.start();
    electionTimeout.start();
    heartbeatTimeout.start();
    eagerSingletons.start();
    server.awaitTermination();
  }
}
