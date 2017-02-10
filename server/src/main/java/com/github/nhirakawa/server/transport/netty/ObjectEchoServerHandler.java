package com.github.nhirakawa.server.transport.netty;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ObjectEchoServerHandler extends ChannelInboundHandlerAdapter {

  private static final Logger LOG = LogManager.getLogger(ObjectEchoServerHandler.class);

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    super.channelActive(ctx);
    LOG.trace("channelActive");
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) {
    LOG.trace("channelRead");
    LOG.info("received message of type {}:{}", msg.getClass().getSimpleName(), msg);
    LOG.info("sending message of type {}:{}", msg.getClass().getSimpleName(), msg);
    ctx.write(msg);
  }

  @Override
  public void channelReadComplete(ChannelHandlerContext ctx) {
    LOG.trace("channelReadComplete");
    ctx.flush();
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    cause.printStackTrace();
    ctx.close();
  }
}
