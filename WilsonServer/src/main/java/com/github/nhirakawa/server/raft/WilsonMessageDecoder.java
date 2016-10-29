package com.github.nhirakawa.server.raft;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nhirakawa.wilson.exceptions.UnknownMessageException;
import com.github.nhirakawa.wilson.models.messages.Message;
import com.google.inject.Inject;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 * Decode a sequence of bytes into a Wilson message
 */
class WilsonMessageDecoder extends ByteToMessageDecoder {

  private static final Logger LOG = LogManager.getLogger(WilsonMessageDecoder.class);

  private final ObjectMapper objectMapper;

  @Inject
  public WilsonMessageDecoder(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    out.add(parseWilsonMessage(in));
  }

  private Message parseWilsonMessage(ByteBuf buffer) throws IOException {
    byte[] bytes = readBytesFromBuffer(buffer);
    Optional<Message> message = tryParseWilsonMessage(bytes);
    if (message.isPresent()) {
      return message.get();
    }
    throw new UnknownMessageException(new String(bytes));
  }

  private Optional<Message> tryParseWilsonMessage(byte[] bytes) {
    try {
      return Optional.of(objectMapper.readValue(bytes, Message.class));
    } catch (IOException e) {
      LOG.warn("Could not parse as message ", new String(bytes));
      return Optional.empty();
    }
  }

  private static byte[] readBytesFromBuffer(ByteBuf buffer) {
    byte[] bytes = new byte[buffer.readableBytes()];
    buffer.readBytes(bytes);
    return bytes;
  }

}
