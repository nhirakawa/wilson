package com.github.nhirakawa.wilson.server.transport.grpc;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class EagerSingletons {

	private final WilsonGrpcClientAdapter clientAdapter;

	@Inject
	EagerSingletons(WilsonGrpcClientAdapter clientAdapter) {
		this.clientAdapter = clientAdapter;
	}

	public void start() {

	}

}
