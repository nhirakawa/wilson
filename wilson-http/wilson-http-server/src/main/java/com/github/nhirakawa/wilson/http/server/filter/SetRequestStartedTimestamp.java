package com.github.nhirakawa.wilson.http.server.filter;

import java.time.Clock;
import java.time.Instant;

import spark.Filter;
import spark.Request;
import spark.Response;

public class SetRequestStartedTimestamp implements Filter {

	// todo inject this
	private final Clock clock = Clock.systemUTC();

	@Override
	public void handle(Request request, Response response) {
		response.header("X-Wilson-Request-Started", clock.instant());
	}

}
