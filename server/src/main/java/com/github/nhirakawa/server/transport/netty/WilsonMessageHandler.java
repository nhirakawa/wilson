package com.github.nhirakawa.server.transport.netty;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.nhirakawa.wilson.models.messages.Message;
import com.google.inject.Inject;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class WilsonMessageHandler extends ChannelInboundHandlerAdapter {

  private static final Logger LOG = LogManager.getLogger(WilsonMessageHandler.class);

  private final ConnectionManager connectionManager;
  private final AtomicBoolean hasCheckedConnection;

  @Inject
  public WilsonMessageHandler(ConnectionManager connectionManager) {
    this.connectionManager = connectionManager;
    this.hasCheckedConnection = new AtomicBoolean(false);
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    super.channelInactive(ctx);
    LOG.trace("channelInactive");

  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    super.channelRead(ctx, msg);
    LOG.trace("channelRead");
    LOG.debug("Received ({})", msg.getClass().getSimpleName());

    if (!(msg instanceof Message)) {
      LOG.info("Received junk message");
      return;
    }

    Message message = (Message) msg;

    //do this only once
    if (!hasCheckedConnection.getAndSet(true)) {
      boolean added = connectionManager.add(message.getServerId(), ctx.channel());
      if (!added) {
        LOG.debug("Open connection to serverId={} already exists - closing now", message.getServerId());
        ctx.close();
      }
    }
  }

  @Override
  public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
    super.channelReadComplete(ctx);
    LOG.trace("channelReadComplete");
    ctx.flush();
  }
}
