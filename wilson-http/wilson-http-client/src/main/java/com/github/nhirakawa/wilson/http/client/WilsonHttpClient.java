package com.github.nhirakawa.wilson.http.client;

import com.github.nhirakawa.wilson.common.ObjectMapperWrapper;
import com.github.nhirakawa.wilson.http.common.WilsonHeaders;
import com.github.nhirakawa.wilson.models.ClusterMember;
import com.github.nhirakawa.wilson.models.messages.AppendEntriesRequest;
import com.github.nhirakawa.wilson.models.messages.AppendEntriesResponse;
import com.github.nhirakawa.wilson.models.messages.VoteRequest;
import com.github.nhirakawa.wilson.models.messages.VoteResponse;
import com.google.common.base.Preconditions;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import javax.inject.Singleton;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Singleton
public class WilsonHttpClient {
  private final OkHttpClient okHttpClient;

  @Inject
  WilsonHttpClient(OkHttpClient okHttpClient) {
    this.okHttpClient = okHttpClient;
  }

  public CompletableFuture<VoteResponse> requestVote(
    ClusterMember clusterMember,
    VoteRequest voteRequest
  ) {
    Request request = new Request.Builder()
      .header(WilsonHeaders.serverId(), clusterMember.getServerId())
      .url(
        new HttpUrl.Builder()
          .scheme("http")
          .host(clusterMember.getHost())
          .port(clusterMember.getPort())
          .addPathSegment("/raft/vote")
          .build()
      )
      .post(
        RequestBody.create(ObjectMapperWrapper.writeValueAsBytes(voteRequest))
      )
      .build();

    CompletableFuture<VoteResponse> future = new CompletableFuture<>();

    okHttpClient
      .newCall(request)
      .enqueue(
        new Callback() {

          @Override
          public void onFailure(Call call, IOException e) {
            future.completeExceptionally(e);
          }

          @Override
          public void onResponse(Call call, Response response)
            throws IOException {
            if (response.isSuccessful()) {
              VoteResponse voteResponse = ObjectMapperWrapper.readValue(
                response.body().byteStream(),
                VoteResponse.class
              );
              future.complete(voteResponse);
            } else {
              future.completeExceptionally(toException(response));
            }
          }
        }
      );

    return future;
  }

  public CompletableFuture<AppendEntriesResponse> sendHeartbeat(
    ClusterMember clusterMember,
    AppendEntriesRequest appendEntriesRequest
  ) {
    Request request = new Request.Builder()
      .header(WilsonHeaders.serverId(), clusterMember.getServerId())
      .url(
        new HttpUrl.Builder()
          .scheme("http")
          .host(clusterMember.getHost())
          .port(clusterMember.getPort())
          .addPathSegment("/raft/entries")
          .build()
      )
      .post(
        RequestBody.create(
          ObjectMapperWrapper.writeValueAsBytes(appendEntriesRequest)
        )
      )
      .build();

    CompletableFuture<AppendEntriesResponse> future = new CompletableFuture<>();

    okHttpClient
      .newCall(request)
      .enqueue(
        new Callback() {

          @Override
          public void onFailure(Call call, IOException e) {
            future.completeExceptionally(e);
          }

          @Override
          public void onResponse(Call call, Response response)
            throws IOException {
            if (response.isSuccessful()) {
              AppendEntriesResponse appendEntriesResponse = ObjectMapperWrapper.readValue(
                response.body().byteStream(),
                AppendEntriesResponse.class
              );
              future.complete(appendEntriesResponse);
            } else {
              future.completeExceptionally(toException(response));
            }
          }
        }
      );

    return future;
  }

  public Exception toException(Response response) {
    Preconditions.checkArgument(!response.isSuccessful());

    return new RuntimeException(
      String.format(
        "Received %s response - %s",
        response.code(),
        response.body()
      )
    );
  }
}
