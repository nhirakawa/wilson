package com.github.nhirakawa.server.transport.netty;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.nhirakawa.server.transport.netty.codec.MessageDecoder;
import com.github.nhirakawa.server.transport.netty.codec.MessageEncoder;
import com.google.inject.Inject;
import com.google.inject.Provider;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.json.JsonObjectDecoder;

public class WilsonChannelInitializer extends ChannelInitializer<SocketChannel> {

  private static final Logger LOG = LogManager.getLogger(WilsonChannelInitializer.class);

  private final Provider<MessageDecoder> messageDecoderProvider;
  private final Provider<MessageEncoder> messageEncoderProvider;
  private final Provider<JsonObjectDecoder> jsonObjectDecoderProvider;
  private final Provider<ObjectEchoServerHandler> serverHandlerProvider;

  @Inject
  public WilsonChannelInitializer(
      Provider<MessageDecoder> messageDecoderProvider,
      Provider<MessageEncoder> messageEncoderProvider,
      Provider<JsonObjectDecoder> jsonObjectDecoderProvider,
      Provider<ObjectEchoServerHandler> serverHandlerProvider) {
    this.messageDecoderProvider = messageDecoderProvider;
    this.messageEncoderProvider = messageEncoderProvider;
    this.jsonObjectDecoderProvider = jsonObjectDecoderProvider;
    this.serverHandlerProvider = serverHandlerProvider;
  }

  @Override
  protected void initChannel(SocketChannel ch) throws Exception {
    LOG.trace("initChannel");
    ch.pipeline().addLast(
        jsonObjectDecoderProvider.get(),
        messageDecoderProvider.get(),
        messageEncoderProvider.get(),
        serverHandlerProvider.get());
  }
}
