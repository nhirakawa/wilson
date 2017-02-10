package com.github.nhirakawa.server.transport.netty;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.nhirakawa.server.guice.WilsonServerModule;
import com.google.inject.Guice;
import com.google.inject.Inject;

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
  public ObjectEchoServer(WilsonChannelInitializer channelInitializer) {
    this.channelInitializer = channelInitializer;
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

  public static void main(String... args) throws IOException {
    String configurationLocation = "conf/wilsonserver.properties";
    if (args.length > 0) {
      configurationLocation = args[0];
    }

    Properties properties = new Properties();
    properties.load(new FileInputStream(configurationLocation));
    Properties systemProperties = System.getProperties();

    for (Entry<Object, Object> entry : properties.entrySet()) {
      systemProperties.putIfAbsent(entry.getKey(), entry.getValue());
    }

    Guice.createInjector(new WilsonServerModule()).getInstance(ObjectEchoServer.class).start();
  }
}
