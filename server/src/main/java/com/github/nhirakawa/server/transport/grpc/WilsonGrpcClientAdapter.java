package com.github.nhirakawa.server.transport.grpc;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nhirakawa.server.cli.CliArguments;
import com.github.nhirakawa.server.config.ClusterMember;
import com.github.nhirakawa.server.config.Configuration;
import com.github.nhirakawa.server.config.ImmutableClusterMember;
import com.github.nhirakawa.server.raft.StateMachineMessageApplier;
import com.github.nhirakawa.server.transport.grpc.WilsonGrpcClient.WilsonGrpcClientFactory;
import com.github.nhirakawa.wilson.models.messages.HeartbeatRequest;
import com.github.nhirakawa.wilson.models.messages.ImmutableHeartbeatRequest;
import com.github.nhirakawa.wilson.models.messages.SerializedWilsonMessage;
import com.github.nhirakawa.wilson.models.messages.VoteRequest;
import com.github.nhirakawa.wilson.models.messages.VoteResponse;
import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class WilsonGrpcClientAdapter {

  private static final Logger LOG = LoggerFactory.getLogger(WilsonGrpcClientAdapter.class);

  private static final String THREAD_NAME_FORMAT = "WilsonGrpcClientAdapter-%s";
  private static final ThreadFactory THREAD_FACTORY = new ThreadFactoryBuilder().setNameFormat(THREAD_NAME_FORMAT).build();

  private final WilsonGrpcClientFactory clientFactory;
  private final SocketAddressProvider socketAddressProvider;
  private final Configuration configuration;
  private final Set<ImmutableClusterMember> peers;
  private final CliArguments cliArguments;
  private final StateMachineMessageApplier messageApplier;

  private final Map<ClusterMember, WilsonGrpcClient> clientMap;
  private final ExecutorService executorService;
  private final Retryer<? super SerializedWilsonMessage> retryer;

  @Inject
  WilsonGrpcClientAdapter(WilsonGrpcClientFactory clientFactory,
                          SocketAddressProvider socketAddressProvider,
                          Configuration configuration,
                          EventBus eventBus,
                          Set<ImmutableClusterMember> peers,
                          CliArguments cliArguments,
                          StateMachineMessageApplier messageApplier) {
    this.clientFactory = clientFactory;
    this.socketAddressProvider = socketAddressProvider;
    this.configuration = configuration;
    this.peers = peers;
    this.cliArguments = cliArguments;
    this.messageApplier = messageApplier;

    this.clientMap = new ConcurrentHashMap<>();
    this.executorService = Executors.newFixedThreadPool(configuration.getNumClientThreads(), THREAD_FACTORY);
    this.retryer = RetryerBuilder.<SerializedWilsonMessage>newBuilder()
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
    try {
      VoteResponse voteResponse = (VoteResponse) retryer.call(() -> requestVote(clusterMember, voteRequest));
      messageApplier.apply(voteResponse, clusterMember);
    } catch (ExecutionException | RetryException e) {
      Throwable cause = e.getCause();
      LOG.error("Encountered exception while requesting vote ({}: {})", cause.getClass().getSimpleName(), cause.getMessage(), cause);
    }
  }

  private VoteResponse requestVote(ClusterMember clusterMember,
                                   VoteRequest voteRequest) {
    WilsonGrpcClient client = getClientForMember(clusterMember);
    VoteResponse voteResponse = client.requestVoteSync(voteRequest);
    return voteResponse;
  }

  @Subscribe
  public void broadcastHeartbeat(ImmutableHeartbeatRequest heartbeatRequest) {
    for (ClusterMember clusterMember : peers) {
      LOG.debug("Sending heartbeat to {}", clusterMember);
      sendHeartbeatWithRetries(heartbeatRequest, clusterMember);
    }
  }

  private void sendHeartbeatWithRetries(HeartbeatRequest message, ClusterMember clusterMember) {
    WilsonGrpcClient client = getClientForMember(clusterMember);
    try {
      retryer.call(() -> client.sendHeartbeatSync(message));
    } catch (ExecutionException | RetryException e) {
      Throwable cause = e.getCause();
      LOG.error("Encountered exception while requesting vote ({}: {})", cause.getClass().getSimpleName(), cause.getMessage());
    }
  }

  private WilsonGrpcClient getClientForMember(ClusterMember clusterMember) {
    return clientMap.computeIfAbsent(clusterMember, this::getClient);
  }

  private WilsonGrpcClient getClient(ClusterMember clusterMember) {
    return clientFactory.create(clusterMember);
  }
}
