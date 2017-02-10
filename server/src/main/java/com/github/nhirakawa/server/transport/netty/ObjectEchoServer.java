package com.github.nhirakawa.server.transport.netty;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.nhirakawa.server.guice.WilsonServerModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Provider;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class ObjectEchoServer {

  private static final Logger LOG = LogManager.getLogger(ObjectEchoServer.class);
  private static final int PORT = 8007;

  private final WilsonChannelInitializer channelInitializer;

  @Inject
  public ObjectEchoServer(Provider<ObjectEchoServerHandler> echoServerHandlerProvider,
                          WilsonChannelInitializer.Factory channelInitializerFactory) {
    this.channelInitializer = channelInitializerFactory.create(echoServerHandlerProvider.get());
  }

  public void start() {
    EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    EventLoopGroup workerGroup = new NioEventLoopGroup();
    try {
      ServerBootstrap b = new ServerBootstrap();
      b.group(bossGroup, workerGroup)
          .channel(NioServerSocketChannel.class)
          .handler(new LoggingHandler(LogLevel.INFO))
          .childHandler(channelInitializer);

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
