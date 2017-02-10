package com.github.nhirakawa.server.transport.netty;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.nhirakawa.server.transport.netty.codec.MessageDecoder;
import com.github.nhirakawa.server.transport.netty.codec.MessageEncoder;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.json.JsonObjectDecoder;

public class WilsonChannelInitializer extends ChannelInitializer<SocketChannel> {

  private static final Logger LOG = LogManager.getLogger(WilsonChannelInitializer.class);

  private final Provider<MessageDecoder> messageDecoderProvider;
  private final Provider<MessageEncoder> messageEncoderProvider;
  private final Provider<JsonObjectDecoder> jsonObjectDecoderProvider;
  private final ChannelInboundHandlerAdapter handler;

  @AssistedInject
  public WilsonChannelInitializer(
      @Assisted ChannelInboundHandlerAdapter handler,
      Provider<MessageDecoder> messageDecoderProvider,
      Provider<MessageEncoder> messageEncoderProvider,
      Provider<JsonObjectDecoder> jsonObjectDecoderProvider) {
    this.handler = handler;
    this.messageDecoderProvider = messageDecoderProvider;
    this.messageEncoderProvider = messageEncoderProvider;
    this.jsonObjectDecoderProvider = jsonObjectDecoderProvider;
  }

  @Override
  protected void initChannel(SocketChannel ch) throws Exception {
    LOG.trace("initChannel");
    ch.pipeline().addLast(
        jsonObjectDecoderProvider.get(),
        messageDecoderProvider.get(),
        messageEncoderProvider.get(),
        handler);
  }

  public interface Factory {
    WilsonChannelInitializer create(ChannelInboundHandlerAdapter handler);
  }
}
