package com.github.nhirakawa.wilson.server.transport.grpc.intercept;

import io.grpc.Metadata;
import io.grpc.Metadata.Key;

final class Keys {

	public static final Key<String> CLUSTER_ID_KEY = Key.of("X-Wilson-ClusterId", Metadata.ASCII_STRING_MARSHALLER);
	public static final Key<String> SERVER_ID_KEY = Key.of("X-Wilson-ServerId", Metadata.ASCII_STRING_MARSHALLER);

	private Keys() {
	}

}
