package com.github.nhirakawa.server.transport.grpc.intercept;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;

import io.grpc.ForwardingServerCallListener.SimpleForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;

@SuppressWarnings("unused")
public class LoggingInterceptor implements ServerInterceptor {

  private static final Logger LOG = LoggerFactory.getLogger(LoggingInterceptor.class);

  private final JsonFormat.Printer printer;

  @Inject
  LoggingInterceptor(JsonFormat.Printer printer) {
    this.printer = printer;
  }

  @Override
  public <ReqT, RespT> Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call,
                                                    Metadata headers,
                                                    ServerCallHandler<ReqT, RespT> next) {
    return new SimpleForwardingServerCallListener<ReqT>(next.startCall(call, headers)) {
      @Override
      public void onMessage(ReqT message) {
        if (message.getClass().isAssignableFrom(MessageOrBuilder.class)) {
          try {
            LOG.info("{}", printer.print((MessageOrBuilder) message));
          } catch (InvalidProtocolBufferException e) {
            LOG.warn("Could not log message for type {}: {} ({})", message.getClass(), message, e.getMessage());
          }
        }

        super.onMessage(message);
      }
    };
  }
}
