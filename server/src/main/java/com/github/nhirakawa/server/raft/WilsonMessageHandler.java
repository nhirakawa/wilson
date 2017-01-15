package com.github.nhirakawa.server.raft;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

@Sharable
public class WilsonMessageHandler extends ChannelInboundHandlerAdapter {

  private static final Logger LOG = LogManager.getLogger(WilsonMessageHandler.class);

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) {
    LOG.info("{}", msg.getClass().getCanonicalName());
  }

  @Override
  public void channelReadComplete(ChannelHandlerContext ctx) {
    ctx.flush();
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    cause.printStackTrace();
    ctx.close();
  }
}
