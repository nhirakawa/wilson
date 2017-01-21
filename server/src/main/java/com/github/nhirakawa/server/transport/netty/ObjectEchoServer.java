package com.github.nhirakawa.server.transport.netty;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.nhirakawa.server.guice.WilsonServerModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Provider;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.json.JsonObjectDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class ObjectEchoServer {

  private static final Logger LOG = LogManager.getLogger(ObjectEchoServer.class);
  private static final int PORT = 8007;

  private final Provider<MessageEncoder> messageEncoderProvider;
  private final Provider<JsonObjectDecoder> jsonObjectDecoderProvider;
  private final Provider<MessageDecoder> messageDecoderProvider;

  @Inject
  public ObjectEchoServer(Provider<MessageEncoder> messageEncoderProvider,
                          Provider<JsonObjectDecoder> jsonObjectDecoderProvider,
                          Provider<MessageDecoder> messageDecoderProvider) {
    this.messageEncoderProvider = messageEncoderProvider;
    this.jsonObjectDecoderProvider = jsonObjectDecoderProvider;
    this.messageDecoderProvider = messageDecoderProvider;
  }

  public void start() {

    EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    EventLoopGroup workerGroup = new NioEventLoopGroup();
    try {
      ServerBootstrap b = new ServerBootstrap();
      b.group(bossGroup, workerGroup)
          .channel(NioServerSocketChannel.class)
          .handler(new LoggingHandler(LogLevel.INFO))
          .childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
              LOG.trace("initChannel");
              ChannelPipeline p = ch.pipeline();
              p.addLast(
                  jsonObjectDecoderProvider.get(),
                  messageDecoderProvider.get(),
                  messageEncoderProvider.get(),
                  new ObjectEchoServerHandler());
            }
          });

      b.bind(PORT).sync().channel().closeFuture().sync();
    } catch (InterruptedException e) {
      LOG.error("Server was interrupted", e);
    } finally {
      bossGroup.shutdownGracefully();
      workerGroup.shutdownGracefully();
    }
  }

  public static void main(String... args) {
    Guice.createInjector(new WilsonServerModule()).getInstance(ObjectEchoServer.class).start();
  }
}
