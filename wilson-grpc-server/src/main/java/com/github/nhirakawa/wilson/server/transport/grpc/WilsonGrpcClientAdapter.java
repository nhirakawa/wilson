package com.github.nhirakawa.wilson.server.transport.grpc;

import com.github.nhirakawa.wilson.models.ClusterMember;
import com.github.nhirakawa.wilson.models.ClusterMemberModel;
import com.github.nhirakawa.wilson.models.messages.AppendEntriesRequest;
import com.github.nhirakawa.wilson.models.messages.VoteRequest;
import com.github.nhirakawa.wilson.models.messages.VoteResponse;
import com.github.nhirakawa.wilson.server.raft.StateMachineMessageApplier;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class WilsonGrpcClientAdapter {
  private static final Logger LOG = LoggerFactory.getLogger(
    WilsonGrpcClientAdapter.class
  );

  private final WilsonGrpcClientFactory clientFactory;
  private final Set<ClusterMember> peers;
  private final StateMachineMessageApplier messageApplier;

  private final Map<ClusterMemberModel, WilsonGrpcClient> clientMap;
  private final Retryer<Void> retryer;

  @Inject
  WilsonGrpcClientAdapter(
    WilsonGrpcClientFactory clientFactory,
    EventBus eventBus,
    Set<ClusterMember> peers,
    StateMachineMessageApplier messageApplier
  ) {
    this.clientFactory = clientFactory;
    this.peers = peers;
    this.messageApplier = messageApplier;

    this.clientMap = new ConcurrentHashMap<>();
    this.retryer =
      RetryerBuilder
        .<Void>newBuilder()
        .retryIfException()
        .withStopStrategy(StopStrategies.stopAfterDelay(1L, TimeUnit.SECONDS))
        .withWaitStrategy(
          WaitStrategies.fixedWait(1000L, TimeUnit.MILLISECONDS)
        )
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

  private void requestVoteWithRetries(
    VoteRequest voteRequest,
    ClusterMember clusterMember
  ) {
    VoteResponse voteResponse = requestVote(clusterMember, voteRequest);
    messageApplier.apply(voteResponse, clusterMember);
  }

  private VoteResponse requestVote(
    ClusterMember clusterMember,
    VoteRequest voteRequest
  ) {
    WilsonGrpcClient client = getClientForMember(clusterMember);
    VoteResponse voteResponse = client.requestVoteSync(voteRequest);
    return voteResponse;
  }

  @Subscribe
  public void broadcastHeartbeat(AppendEntriesRequest appendEntriesRequest) {
    for (ClusterMember clusterMember : peers) {
      LOG.debug("Sending heartbeat to {}", clusterMember);
      sendHeartbeatWithRetries(appendEntriesRequest, clusterMember);
    }
  }

  private void sendHeartbeatWithRetries(
    AppendEntriesRequest appendEntriesRequest,
    ClusterMember clusterMember
  ) {
    WilsonGrpcClient client = getClientForMember(clusterMember);
    try {
      retryer.call(
        () -> {
          client.sendHeartbeat(appendEntriesRequest);
          return null;
        }
      );
    } catch (ExecutionException | RetryException e) {
      Throwable cause = e.getCause();
      LOG.error(
        "Encountered exception while requesting vote ({}: {})",
        cause.getClass().getSimpleName(),
        cause.getMessage()
      );
    }
  }

  private WilsonGrpcClient getClientForMember(
    ClusterMemberModel clusterMember
  ) {
    return clientMap.computeIfAbsent(clusterMember, this::getClient);
  }

  private WilsonGrpcClient getClient(ClusterMemberModel clusterMember) {
    return clientFactory.create(clusterMember);
  }
}
