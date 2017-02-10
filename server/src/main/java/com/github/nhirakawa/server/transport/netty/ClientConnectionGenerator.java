package com.github.nhirakawa.server.transport.netty;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.nhirakawa.server.config.Configuration;
import com.github.nhirakawa.server.config.ServerInfo;
import com.github.nhirakawa.wilson.models.messages.HeartbeatMessage;
import com.github.nhirakawa.wilson.models.messages.Message;
import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.google.inject.Inject;
import com.google.inject.Provider;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class ClientConnectionGenerator {

  private static final Logger LOG = LogManager.getLogger(ClientConnectionGenerator.class);

  private final Configuration configuration;
  private final Retryer<Void> retryer;
  private final Provider<WilsonChannelInitializer> channelInitializerProvider;
  private final EventLoopGroup eventLoopGroup;

  @Inject
  public ClientConnectionGenerator(Configuration configuration,
                                   Retryer<Void> retryer,
                                   Provider<WilsonChannelInitializer> channelInitializerProvider) {
    this.configuration = configuration;
    this.retryer = retryer;
    this.channelInitializerProvider = channelInitializerProvider;
    this.eventLoopGroup = new NioEventLoopGroup();
  }

  public void generate() throws ExecutionException {
    for (ServerInfo serverInfo : configuration.getClusterAddresses()) {
      try {
        retryer.call(connect(serverInfo.getHost(), serverInfo.getPort()));
      } catch (RetryException e) {
        LOG.warn("Could not connect to host={}, port={}", serverInfo.getHost(), serverInfo.getPort(), e.getCause());
      }
    }
  }

  private Callable<Void> connect(String host, int port) {
    return () -> {
      LOG.trace("Attempting connection to host={}, port={}", host, port);
      Bootstrap bootstrap = new Bootstrap();
      bootstrap.group(eventLoopGroup)
          .channel(NioSocketChannel.class)
          .handler(channelInitializerProvider.get());

      bootstrap
          .connect(host, port)
          .sync()
          .channel()
          .writeAndFlush(buildHeartbeatMessage());
      return null;
    };
  }

  private Message buildHeartbeatMessage() {
    return HeartbeatMessage.builder()
        .setClusterId(configuration.getClusterId())
        .setServerId(configuration.getServerId())
        .build();
  }
}
