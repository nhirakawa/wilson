package com.github.nhirakawa.wilson.http.server;

import static spark.Spark.*;

import com.github.nhirakawa.wilson.http.server.filter.after.IncrementRequestCounter;
import com.github.nhirakawa.wilson.http.server.filter.before.SetContentEncoding;
import com.github.nhirakawa.wilson.http.server.filter.before.SetRequestId;
import com.github.nhirakawa.wilson.http.server.filter.before.SetRequestStartedTimestamp;
import com.github.nhirakawa.wilson.http.server.route.AppendEntries;
import com.github.nhirakawa.wilson.http.server.route.RequestVote;
import com.google.common.util.concurrent.AbstractIdleService;
import javax.inject.Inject;
import javax.inject.Provider;

public class WilsonHttpServer extends AbstractIdleService {
  private final Provider<SetContentEncoding> setContentEncodingProvider;
  private final Provider<SetRequestId> setRequestIdProvider;
  private final Provider<SetRequestStartedTimestamp> setRequestStartedTimestampProvider;
  private final Provider<IncrementRequestCounter> incrementRequestCounterProvider;
  private final Provider<AppendEntries> appendEntriesProvider;

  @Inject
  public WilsonHttpServer(
    Provider<SetContentEncoding> setContentEncodingProvider,
    Provider<SetRequestId> setRequestIdProvider,
    Provider<SetRequestStartedTimestamp> setRequestStartedTimestampProvider,
    Provider<IncrementRequestCounter> incrementRequestCounterProvider,
    Provider<AppendEntries> appendEntriesProvider
  ) {
    this.setContentEncodingProvider = setContentEncodingProvider;
    this.setRequestIdProvider = setRequestIdProvider;
    this.setRequestStartedTimestampProvider =
      setRequestStartedTimestampProvider;
    this.incrementRequestCounterProvider = incrementRequestCounterProvider;
    this.appendEntriesProvider = appendEntriesProvider;
  }

  @Override
  protected void startUp() throws Exception {
    port(8080); // todo config
    before(
      setRequestIdProvider.get(),
      setRequestStartedTimestampProvider.get()
    );
    post("/raft/entries", appendEntriesProvider.get());
    post("/raft/vote", new RequestVote());
    after(
      setContentEncodingProvider.get(),
      incrementRequestCounterProvider.get()
    );
  }

  @Override
  protected void shutDown() throws Exception {
    stop();
  }
}
