package com.github.nhirakawa.server.transport.netty;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.nhirakawa.server.guice.WilsonServerModule;
import com.github.nhirakawa.wilson.models.messages.HeartbeatMessage;
import com.google.inject.Guice;
import com.google.inject.Inject;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class ObjectEchoClient {

  private static final Logger LOG = LogManager.getLogger(ObjectEchoClient.class);

  private static final String HOST = "localhost";
  private static final int PORT = 8007;

  private final WilsonChannelInitializer channelInitializer;

  @Inject
  public ObjectEchoClient(WilsonChannelInitializer channelInitializer) {
    this.channelInitializer = channelInitializer;
  }

  public void start() {
    EventLoopGroup group = new NioEventLoopGroup();
    try {
      Bootstrap b = new Bootstrap();
      b.group(group)
          .channel(NioSocketChannel.class)
          .handler(channelInitializer);

      Channel channel = b.connect(HOST, PORT).sync().channel();
      channel.writeAndFlush(HeartbeatMessage.builder().build());
      channel.close();
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
