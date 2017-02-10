package com.github.nhirakawa.server.transport.netty.codec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nhirakawa.wilson.models.messages.Message;
import com.google.inject.Inject;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class MessageEncoder extends MessageToByteEncoder<Message> {

  private final ObjectMapper objectMapper;

  @Inject
  public MessageEncoder(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
    out.writeBytes(objectMapper.writeValueAsBytes(msg));
  }
}
