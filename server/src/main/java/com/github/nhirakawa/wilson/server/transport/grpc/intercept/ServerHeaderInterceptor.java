package com.github.nhirakawa.wilson.server.transport.grpc.intercept;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.Metadata;
import io.grpc.Metadata.Key;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;

public class ServerHeaderInterceptor implements ServerInterceptor {

  private static final Logger LOG = LoggerFactory.getLogger(ServerHeaderInterceptor.class);

  private static final Key<String> CLUSTER_ID = Key.of("X-Wilson-ClusterId", Metadata.ASCII_STRING_MARSHALLER);
  private static final Key<String> SERVER_ID = Key.of("X-Wilson-ServerId", Metadata.ASCII_STRING_MARSHALLER);

  @Override
  public <ReqT, RespT> Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call,
                                                    Metadata headers,
                                                    ServerCallHandler<ReqT, RespT> next) {
    Optional<String> clusterId = Optional.ofNullable(headers.get(CLUSTER_ID));
    if (!clusterId.isPresent()) {
      LOG.warn("clusterId not present");
      call.close(Status.INVALID_ARGUMENT, new Metadata());
    }

    Optional<String> serverId = Optional.ofNullable(headers.get(SERVER_ID));
    if (!serverId.isPresent()) {
      LOG.warn("serverId not present");
      call.close(Status.INVALID_ARGUMENT, new Metadata());
    }

    return next.startCall(call, headers);
  }
}
