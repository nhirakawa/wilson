package com.github.nhirakawa.wilson.http.server;

import static spark.Spark.after;
import static spark.Spark.before;
import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.stop;

import com.github.nhirakawa.wilson.http.server.filter.after.IncrementRequestCounter;
import com.github.nhirakawa.wilson.http.server.filter.before.SetContentEncoding;
import com.github.nhirakawa.wilson.http.server.filter.before.SetRequestId;
import com.github.nhirakawa.wilson.http.server.filter.before.SetRequestStartedTimestamp;
import com.github.nhirakawa.wilson.http.server.route.AppendEntries;
import com.github.nhirakawa.wilson.http.server.route.RequestVote;
import com.github.nhirakawa.wilson.protocol.config.WilsonConfig;
import com.google.common.util.concurrent.AbstractIdleService;
import javax.inject.Inject;
import javax.inject.Provider;

public class WilsonHttpServer extends AbstractIdleService {
  private final WilsonConfig wilsonConfig;
  private final Provider<SetContentEncoding> setContentEncodingProvider;
  private final Provider<SetRequestId> setRequestIdProvider;
  private final Provider<SetRequestStartedTimestamp> setRequestStartedTimestampProvider;
  private final Provider<IncrementRequestCounter> incrementRequestCounterProvider;
  private final Provider<AppendEntries> appendEntriesProvider;
  private final Provider<RequestVote> requestVoteProvider;

  @Inject
  public WilsonHttpServer(
    WilsonConfig wilsonConfig,
    Provider<SetContentEncoding> setContentEncodingProvider,
    Provider<SetRequestId> setRequestIdProvider,
    Provider<SetRequestStartedTimestamp> setRequestStartedTimestampProvider,
    Provider<IncrementRequestCounter> incrementRequestCounterProvider,
    Provider<AppendEntries> appendEntriesProvider,
    Provider<RequestVote> requestVoteProvider
  ) {
    this.wilsonConfig = wilsonConfig;
    this.setContentEncodingProvider = setContentEncodingProvider;
    this.setRequestIdProvider = setRequestIdProvider;
    this.setRequestStartedTimestampProvider =
      setRequestStartedTimestampProvider;
    this.incrementRequestCounterProvider = incrementRequestCounterProvider;
    this.appendEntriesProvider = appendEntriesProvider;
    this.requestVoteProvider = requestVoteProvider;
  }

  @Override
  protected void startUp() throws Exception {
    port(wilsonConfig.getLocalMember().getPort());
    before(
      setRequestIdProvider.get(),
      setRequestStartedTimestampProvider.get()
    );
    post("/raft/entries", appendEntriesProvider.get());
    post("/raft/vote", requestVoteProvider.get());
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
