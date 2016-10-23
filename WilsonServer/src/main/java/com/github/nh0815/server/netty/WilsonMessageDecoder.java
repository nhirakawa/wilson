package com.github.nh0815.server.netty;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nhirakawa.wilson.models.AsdfMessage;
import com.github.nhirakawa.wilson.models.WilsonMessage;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 * Decode a sequence of bytes into a Wilson message
 */
public class WilsonMessageDecoder extends ByteToMessageDecoder {

  private final ObjectMapper objectMapper;

  public WilsonMessageDecoder() {
    this.objectMapper = new ObjectMapper();
  }

  @Override
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    out.add(parseWilsonMessage(in));
  }

  private WilsonMessage parseWilsonMessage(ByteBuf buffer) throws IOException {
    byte[] bytes = readBytesFromBuffer(buffer);
    return objectMapper.readValue(bytes, AsdfMessage.class);
  }

  private static byte[] readBytesFromBuffer(ByteBuf buffer) {
    byte[] bytes = new byte[buffer.readableBytes()];
    buffer.readBytes(bytes);
    return bytes;
  }

}
