package com.github.nh0815.server.netty;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nhirakawa.wilson.models.AsdfMessage;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

@Sharable
public class WilsonServerHandler extends ChannelInboundHandlerAdapter {

  private static final Logger LOG = LogManager.getLogger(WilsonServerHandler.class);
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) {
    LOG.info("{}", msg.getClass());
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

  private AsdfMessage parseMessage(String message) throws IOException {
    return OBJECT_MAPPER.readValue(message, AsdfMessage.class);
  }
}
