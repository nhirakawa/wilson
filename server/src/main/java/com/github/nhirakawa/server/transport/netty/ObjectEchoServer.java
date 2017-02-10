package com.github.nhirakawa.server.transport.netty;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import com.github.nhirakawa.server.config.Configuration;
import com.github.nhirakawa.server.guice.WilsonServerModule;
import com.google.inject.Guice;
import com.google.inject.Inject;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class ObjectEchoServer {

  private final Configuration configuration;
  private final WilsonChannelInitializer channelInitializer;
  private final ClientConnectionGenerator connectionGenerator;
  private final HeartbeatTask heartbeatTask;

  @Inject
  public ObjectEchoServer(WilsonChannelInitializer channelInitializer,
                          ClientConnectionGenerator connectionGenerator,
                          HeartbeatTask heartbeatTask,
                          Configuration configuration) {
    this.channelInitializer = channelInitializer;
    this.connectionGenerator = connectionGenerator;
    this.heartbeatTask = heartbeatTask;
    this.configuration = configuration;
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

      Channel channel = b.bind(configuration.getLocalAddress().getPort()).sync().channel();
      heartbeatTask.start();
      connectionGenerator.generate();
      channel.closeFuture().sync();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
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
