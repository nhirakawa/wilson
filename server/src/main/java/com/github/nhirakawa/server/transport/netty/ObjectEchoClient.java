package com.github.nhirakawa.server.transport.netty;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.nhirakawa.server.guice.WilsonServerModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provider;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

public final class ObjectEchoClient {

  private static final Logger LOG = LogManager.getLogger(ObjectEchoClient.class);

  private static final String HOST = "localhost";
  private static final int PORT = 8007;

  public static void main(String[] args) throws Exception {
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
                  new ObjectEncoder(),
                  new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                  objectEchoClientHandlerProvider.get());
            }
          });

      // Start the connection attempt.
      b.connect(HOST, PORT).sync().channel().closeFuture().sync();
    } finally {
      group.shutdownGracefully();
    }
  }
}
