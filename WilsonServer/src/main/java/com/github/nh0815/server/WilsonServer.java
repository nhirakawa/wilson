package com.github.nh0815.server;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.json.JsonObjectDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class WilsonServer {

  private static final Logger LOG = LogManager.getLogger(WilsonServer.class);

  private final int port;

  public WilsonServer(int port) {
    this.port = port;
  }

  public void start() {
    ServerBootstrap serverBootstrap = new ServerBootstrap();
    EventLoopGroup parentGroup = new NioEventLoopGroup(1);
    EventLoopGroup workerGroup = new NioEventLoopGroup();
    serverBootstrap.group(parentGroup, workerGroup)
        .channel(NioServerSocketChannel.class)
        .option(ChannelOption.SO_BACKLOG, 100)
        .handler(new LoggingHandler(LogLevel.INFO))
        .childHandler(new ChannelInitializer<SocketChannel>() {
          @Override
          protected void initChannel(SocketChannel ch) throws Exception {
            ch.pipeline().addLast(new JsonObjectDecoder(), new WilsonMessageDecoder(), new WilsonServerHandler());
          }
        });

    try {
      ChannelFuture future = serverBootstrap.bind(port).sync();
      future.channel().closeFuture().sync();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      parentGroup.shutdownGracefully();
      workerGroup.shutdownGracefully();
    }
  }

  public static void main(String... args) {
    new WilsonServer(9000).start();
  }

}
