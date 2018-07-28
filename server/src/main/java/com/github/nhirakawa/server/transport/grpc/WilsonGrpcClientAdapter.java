package com.github.nhirakawa.server.transport.grpc;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nhirakawa.server.models.ClusterMember;
import com.github.nhirakawa.server.models.ClusterMemberModel;
import com.github.nhirakawa.server.models.messages.HeartbeatRequest;
import com.github.nhirakawa.server.models.messages.VoteRequest;
import com.github.nhirakawa.server.models.messages.VoteResponse;
import com.github.nhirakawa.server.raft.StateMachineMessageApplier;
import com.github.nhirakawa.server.transport.grpc.WilsonGrpcClient.WilsonGrpcClientFactory;
import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class WilsonGrpcClientAdapter {

  private static final Logger LOG = LoggerFactory.getLogger(WilsonGrpcClientAdapter.class);

  private final WilsonGrpcClientFactory clientFactory;
  private final Set<ClusterMember> peers;
  private final StateMachineMessageApplier messageApplier;

  private final Map<ClusterMemberModel, WilsonGrpcClient> clientMap;
  private final Retryer<Void> retryer;

  @Inject
  WilsonGrpcClientAdapter(WilsonGrpcClientFactory clientFactory,
                          EventBus eventBus,
                          Set<ClusterMember> peers,
                          StateMachineMessageApplier messageApplier) {
    this.clientFactory = clientFactory;
    this.peers = peers;
    this.messageApplier = messageApplier;

    this.clientMap = new ConcurrentHashMap<>();
    this.retryer = RetryerBuilder.<Void>newBuilder()
        .retryIfException()
        .withStopStrategy(StopStrategies.stopAfterDelay(1L, TimeUnit.SECONDS))
        .withWaitStrategy(WaitStrategies.fixedWait(1000L, TimeUnit.MILLISECONDS))
        .build();

    eventBus.register(this);
  }

  @Subscribe
  public void handleDeadEvent(DeadEvent deadEvent) {
    LOG.debug("Found DeadEvent {}", deadEvent);
  }

  @Subscribe
  public void broadcastVoteRequest(VoteRequest voteRequest) {
    for (ClusterMember clusterMember : peers) {
      requestVoteWithRetries(voteRequest, clusterMember);
    }
  }

  private void requestVoteWithRetries(VoteRequest voteRequest, ClusterMember clusterMember) {
    VoteResponse voteResponse = requestVote(clusterMember, voteRequest);
    messageApplier.apply(voteResponse, clusterMember);
  }

  private VoteResponse requestVote(ClusterMember clusterMember,
                                   VoteRequest voteRequest) {
    WilsonGrpcClient client = getClientForMember(clusterMember);
    VoteResponse voteResponse = client.requestVoteSync(voteRequest);
    return voteResponse;
  }

  @Subscribe
  public void broadcastHeartbeat(HeartbeatRequest heartbeatRequest) {
    for (ClusterMember clusterMember : peers) {
      LOG.debug("Sending heartbeat to {}", clusterMember);
      sendHeartbeatWithRetries(heartbeatRequest, clusterMember);
    }
  }

  private void sendHeartbeatWithRetries(HeartbeatRequest message, ClusterMember clusterMember) {
    WilsonGrpcClient client = getClientForMember(clusterMember);
    try {
      retryer.call(() -> {
        client.sendHeartbeatSync(message);
        return null;
      });
    } catch (ExecutionException | RetryException e) {
      Throwable cause = e.getCause();
      LOG.error("Encountered exception while requesting vote ({}: {})", cause.getClass().getSimpleName(), cause.getMessage());
    }
  }

  private WilsonGrpcClient getClientForMember(ClusterMemberModel clusterMember) {
    return clientMap.computeIfAbsent(clusterMember, this::getClient);
  }

  private WilsonGrpcClient getClient(ClusterMemberModel clusterMember) {
    return clientFactory.create(clusterMember);
  }
}
