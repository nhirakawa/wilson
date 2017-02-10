package com.github.nhirakawa.server.transport.netty;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nhirakawa.server.guice.WilsonServerModule;
import com.github.nhirakawa.wilson.models.messages.HeartbeatMessage;
import com.github.nhirakawa.wilson.models.messages.Message;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ObjectEchoClientHandler extends ChannelInboundHandlerAdapter {

  private static final Logger LOG = LogManager.getLogger(ObjectEchoClientHandler.class);

  private final ObjectMapper objectMapper;

  @Inject
  public ObjectEchoClientHandler(@Named(WilsonServerModule.DEFAULT_OBJECT_MAPPER) ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws JsonProcessingException {
    LOG.trace("channelActive");
    Message message = HeartbeatMessage.builder().build();
    LOG.info("created message: {}", message);
    ctx.writeAndFlush(message);
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws JsonProcessingException {
    LOG.trace("channelRead");
    LOG.info("received message: {}", msg);
    ctx.close();
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
