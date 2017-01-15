package com.github.nhirakawa.server.raft;


import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.github.nhirakawa.server.config.Configuration;
import com.github.nhirakawa.server.guice.WilsonServerModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
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
  private final Provider<JsonObjectDecoder> jsonObjectDecoderProvider;
  private final Provider<WilsonMessageDecoder> messageDecoderProvider;
  private final WilsonMessageHandler messageHandler;
  private final WilsonMessageEncoder messageEncoder;
  private final ScheduledExecutorService scheduledExecutorService;

  @AssistedInject
  public WilsonServer(@Assisted int port,
                      Provider<JsonObjectDecoder> jsonObjectDecoderProvider,
                      Provider<WilsonMessageDecoder> messageDecoderProvider,
                      WilsonMessageHandler messageHandler,
                      WilsonMessageEncoder messageEncoder,
                      Configuration configuration,
                      ScheduledExecutorService scheduledExecutorService) {
    this.port = port;
    this.jsonObjectDecoderProvider = jsonObjectDecoderProvider;
    this.messageDecoderProvider = messageDecoderProvider;
    this.messageHandler = messageHandler;
    this.messageEncoder = messageEncoder;
    this.scheduledExecutorService = scheduledExecutorService;
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
            ch.pipeline().addLast(jsonObjectDecoderProvider.get(), messageDecoderProvider.get(), messageHandler, messageEncoder);
            scheduledExecutorService.scheduleWithFixedDelay(new HeartbeatPublisher(ch), 0, 100, TimeUnit.MILLISECONDS);
          }
        });

    try {
      Channel ch = serverBootstrap.bind(port).sync().channel();
      ch.closeFuture().sync();
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
