package com.github.nhirakawa.wilson.protocol.service;

import com.github.nhirakawa.wilson.models.ClusterMember;
import com.github.nhirakawa.wilson.models.messages.AppendEntriesRequest;
import com.github.nhirakawa.wilson.models.messages.AppendEntriesResponse;
import com.github.nhirakawa.wilson.models.messages.VoteRequest;
import com.github.nhirakawa.wilson.models.messages.VoteResponse;
import com.google.common.util.concurrent.Service;
import java.util.concurrent.CompletableFuture;

public interface MessageSender extends Service {
  CompletableFuture<VoteResponse> handleVote(
    ClusterMember clusterMember,
    VoteRequest request
  );
  CompletableFuture<AppendEntriesResponse> handleAppendEntries(
    ClusterMember clusterMember,
    AppendEntriesRequest request
  );
}
