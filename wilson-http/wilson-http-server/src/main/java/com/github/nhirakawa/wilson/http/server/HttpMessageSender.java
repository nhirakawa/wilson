package com.github.nhirakawa.wilson.http.server;

import com.github.nhirakawa.wilson.http.client.WilsonHttpClient;
import com.github.nhirakawa.wilson.models.ClusterMember;
import com.github.nhirakawa.wilson.models.messages.AppendEntriesRequest;
import com.github.nhirakawa.wilson.models.messages.AppendEntriesResponse;
import com.github.nhirakawa.wilson.models.messages.VoteRequest;
import com.github.nhirakawa.wilson.models.messages.VoteResponse;
import com.github.nhirakawa.wilson.protocol.service.MessageSender;
import com.google.common.util.concurrent.AbstractIdleService;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;

public class HttpMessageSender
  extends AbstractIdleService
  implements MessageSender {
  private final WilsonHttpClient wilsonHttpClient;

  @Inject
  HttpMessageSender(WilsonHttpClient wilsonHttpClient) {
    this.wilsonHttpClient = wilsonHttpClient;
  }

  @Override
  protected void startUp() throws Exception {}

  @Override
  protected void shutDown() throws Exception {}

  @Override
  public CompletableFuture<VoteResponse> handleVote(
    ClusterMember clusterMember,
    VoteRequest request
  ) {
    return wilsonHttpClient.requestVote(clusterMember, request);
  }

  @Override
  public CompletableFuture<AppendEntriesResponse> handleAppendEntries(
    ClusterMember clusterMember,
    AppendEntriesRequest request
  ) {
    return wilsonHttpClient.sendHeartbeat(clusterMember, request);
  }
}
