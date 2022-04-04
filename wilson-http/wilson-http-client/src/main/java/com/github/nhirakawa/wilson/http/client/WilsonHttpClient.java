package com.github.nhirakawa.wilson.http.client;

import com.github.nhirakawa.wilson.common.ObjectMapperWrapper;
import com.github.nhirakawa.wilson.models.ClusterMember;
import com.github.nhirakawa.wilson.models.messages.AppendEntriesRequest;
import com.github.nhirakawa.wilson.models.messages.AppendEntriesResponse;
import com.github.nhirakawa.wilson.models.messages.VoteRequest;
import com.github.nhirakawa.wilson.models.messages.VoteResponse;
import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Singleton;
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

  public VoteResponse requestVote(
    ClusterMember clusterMember,
    VoteRequest voteRequest
  ) {
    Request request = new Request.Builder()
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

    try (Response response = okHttpClient.newCall(request).execute()) {
      return ObjectMapperWrapper.readValue(
        response.body().byteStream(),
        VoteResponse.class
      );
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public AppendEntriesResponse sendHeartbeat(
    ClusterMember clusterMember,
    AppendEntriesRequest appendEntriesRequest
  ) {
    Request request = new Request.Builder()
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

    try (Response response = okHttpClient.newCall(request).execute()) {
      return ObjectMapperWrapper.readValue(
        response.body().byteStream(),
        AppendEntriesResponse.class
      );
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
