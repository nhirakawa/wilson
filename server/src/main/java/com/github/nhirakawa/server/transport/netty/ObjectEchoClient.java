package com.github.nhirakawa.server.transport.netty;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.nhirakawa.server.guice.WilsonServerModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.json.JsonObjectDecoder;

public class ObjectEchoClient {

  private static final Logger LOG = LogManager.getLogger(ObjectEchoClient.class);

  private static final String HOST = "localhost";
  private static final int PORT = 8007;

  private final Provider<JsonObjectDecoder> jsonObjectDecoderProvider;
  private final Provider<MessageDecoder> messageDecoderProvider;
  private final Provider<MessageEncoder> messageEncoderProvider;

  @Inject
  public ObjectEchoClient(Provider<JsonObjectDecoder> jsonObjectDecoderProvider,
                          Provider<MessageDecoder> messageDecoderProvider,
                          Provider<MessageEncoder> messageEncoderProvider) {
    this.jsonObjectDecoderProvider = jsonObjectDecoderProvider;
    this.messageDecoderProvider = messageDecoderProvider;
    this.messageEncoderProvider = messageEncoderProvider;
  }

  public void start() {
    Injector injector = Guice.createInjector(new WilsonServerModule());
    Provider<ObjectEchoClientHandler> objectEchoClientHandlerProvider = injector.getProvider(ObjectEchoClientHandler.class);

    EventLoopGroup group = new NioEventLoopGroup();
    try {
      Bootstrap b = new Bootstrap();
      b.group(group)
          .channel(NioSocketChannel.class)
          .handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
              LOG.trace("initChannel");
              ChannelPipeline p = ch.pipeline();
              p.addLast(
                  jsonObjectDecoderProvider.get(),
                  messageDecoderProvider.get(),
                  messageEncoderProvider.get(),
                  objectEchoClientHandlerProvider.get());
            }
          });

      // Start the connection attempt.
      b.connect(HOST, PORT).sync().channel().closeFuture().sync();
    } catch (InterruptedException e) {
      LOG.error("Client was interrupted", e);
    } finally {
      group.shutdownGracefully();
    }
  }

  public static void main(String... args) {
    Guice.createInjector(new WilsonServerModule()).getInstance(ObjectEchoClient.class).start();
  }
}
