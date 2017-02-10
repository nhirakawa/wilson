package com.github.nhirakawa.server.transport.netty.codec;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nhirakawa.wilson.models.messages.Message;
import com.google.inject.Inject;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class MessageDecoder extends ByteToMessageDecoder {

  private final ObjectMapper objectMapper;

  @Inject
  public MessageDecoder(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    byte[] bytes = extractBytes(in);
    out.add(parseAsMessage(bytes));
  }

  private byte[] extractBytes(ByteBuf buffer) {
    byte[] bytes = new byte[buffer.readableBytes()];
    buffer.readBytes(bytes);
    return bytes;
  }

  private Message parseAsMessage(byte[] bytes) throws IOException {
    return objectMapper.readValue(bytes, Message.class);
  }
}
