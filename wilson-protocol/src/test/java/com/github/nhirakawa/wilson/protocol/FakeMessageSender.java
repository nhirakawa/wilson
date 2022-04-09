package com.github.nhirakawa.wilson.protocol;

import com.github.nhirakawa.wilson.models.AppendEntriesResult;
import com.github.nhirakawa.wilson.models.ClusterMember;
import com.github.nhirakawa.wilson.models.messages.AppendEntriesRequest;
import com.github.nhirakawa.wilson.models.messages.AppendEntriesResponse;
import com.github.nhirakawa.wilson.models.messages.VoteRequest;
import com.github.nhirakawa.wilson.models.messages.VoteResponse;
import com.github.nhirakawa.wilson.protocol.service.MessageSender;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.List;

public class FakeMessageSender
  extends AbstractExecutionThreadService
  implements MessageSender {
  private final List<VoteRequest> voteRequests;
  private final List<AppendEntriesRequest> appendEntriesRequests;

  public FakeMessageSender() {
    this.voteRequests = new ArrayList<>();
    this.appendEntriesRequests = new ArrayList<>();
  }

  @Override
  protected void run() throws Exception {}

  @Override
  public CompletableFuture<VoteResponse> handleVote(
    ClusterMember clusterMember,
    VoteRequest request
  ) {
    voteRequests.add(request);
    return CompletableFuture.completedFuture(
      VoteResponse
        .builder()
        .setTerm(request.getTerm())
        .setVoteGranted(false)
        .build()
    );
  }

  @Override
  public CompletableFuture<AppendEntriesResponse> handleAppendEntries(
    ClusterMember clusterMember,
    AppendEntriesRequest request
  ) {
    appendEntriesRequests.add(request);
    return CompletableFuture.completedFuture(
      AppendEntriesResponse
        .builder()
        .setTerm(request.getTerm())
        .setResult(AppendEntriesResult.SUCCESS)
        .build()
    );
  }

  List<VoteRequest> getVoteRequests() {
    return voteRequests;
  }

  List<AppendEntriesRequest> getAppendEntriesRequests() {
    return appendEntriesRequests;
  }

  void clear() {
    voteRequests.clear();
    appendEntriesRequests.clear();
  }
}
