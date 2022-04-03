package com.github.nhirakawa.wilson.http.server;

import static spark.Spark.*;

import com.github.nhirakawa.wilson.http.server.filter.SetContentEncoding;
import com.github.nhirakawa.wilson.http.server.filter.SetRequestId;
import com.github.nhirakawa.wilson.http.server.filter.SetRequestStartedTimestamp;
import com.github.nhirakawa.wilson.http.server.route.AppendEntries;
import com.github.nhirakawa.wilson.http.server.route.RequestVote;
import com.google.common.util.concurrent.AbstractIdleService;
import javax.inject.Inject;
import javax.inject.Provider;

public class WilsonHttpServer extends AbstractIdleService {
  private final Provider<SetContentEncoding> setContentEncodingProvider;
  private final Provider<SetRequestId> setRequestIdProvider;
  private final Provider<SetRequestStartedTimestamp> setRequestStartedTimestampProvider;

  @Inject
  public WilsonHttpServer(
    Provider<SetContentEncoding> setContentEncodingProvider,
    Provider<SetRequestId> setRequestIdProvider,
    Provider<SetRequestStartedTimestamp> setRequestStartedTimestampProvider
  ) {
    this.setContentEncodingProvider = setContentEncodingProvider;
    this.setRequestIdProvider = setRequestIdProvider;
    this.setRequestStartedTimestampProvider =
      setRequestStartedTimestampProvider;
  }

  @Override
  protected void startUp() throws Exception {
    port(8080); // todo config
    before(setContentEncodingProvider.get(), setRequestIdProvider.get(), setRequestStartedTimestampProvider.get());
    post("/raft/entries", new AppendEntries());
    post("/raft/vote", new RequestVote());
    after(new SetContentEncoding());
  }

  @Override
  protected void shutDown() throws Exception {
    stop();
  }
}
