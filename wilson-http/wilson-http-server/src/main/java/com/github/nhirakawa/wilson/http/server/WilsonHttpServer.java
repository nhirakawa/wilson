package com.github.nhirakawa.wilson.http.server;

import com.github.nhirakawa.wilson.http.server.filter.SetRequestId;
import com.github.nhirakawa.wilson.http.server.filter.SetRequestStartedTimestamp;
import com.github.nhirakawa.wilson.http.server.route.AppendEntries;
import com.github.nhirakawa.wilson.http.server.route.RequestVote;
import com.google.common.util.concurrent.AbstractIdleService;
import static spark.Spark.*;

public class WilsonHttpServer extends AbstractIdleService {

	@Override
	protected void startUp() throws Exception {
		port(8080); // todo config
		before(new SetRequestId(), new SetRequestStartedTimestamp());
		post("/entries", new AppendEntries());
		post("/vote", new RequestVote());
	}

	@Override
	protected void shutDown() throws Exception {
		stop();
	}

}
