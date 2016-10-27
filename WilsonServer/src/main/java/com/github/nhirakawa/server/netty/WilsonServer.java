package com.github.nhirakawa.server.netty;


import com.github.nhirakawa.server.guice.WilsonServerModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

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

  private final int port;
  private final JsonObjectDecoder jsonObjectDecoder;
  private final WilsonMessageDecoder messageDecoder;
  private final WilsonMessageHandler messageHandler;

  @AssistedInject
  public WilsonServer(@Assisted int port,
                      JsonObjectDecoder jsonObjectDecoder,
                      WilsonMessageDecoder messageDecoder,
                      WilsonMessageHandler messageHandler) {
    this.port = port;
    this.jsonObjectDecoder = jsonObjectDecoder;
    this.messageDecoder = messageDecoder;
    this.messageHandler = messageHandler;
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
            ch.pipeline().addLast(jsonObjectDecoder, messageDecoder, messageHandler);
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

  public interface Factory {

    WilsonServer create(int port);

  }

  public static void main(String... args) {
    Injector injector = Guice.createInjector(new WilsonServerModule());
    injector.getInstance(WilsonServer.Factory.class).create(9000).start();
  }

}
