package com.github.nhirakawa.server.raft;

import java.util.TimerTask;

import com.github.nhirakawa.wilson.models.messages.HeartbeatMessage;

import io.netty.channel.Channel;

class HeartbeatPublisher extends TimerTask {

  private final Channel channel;

  public HeartbeatPublisher(Channel channel) {
    this.channel = channel;
  }

  @Override
  public void run() {
    if (channel.isOpen()) {
      channel.pipeline().writeAndFlush(HeartbeatMessage.builder().build());
    } else {
      throw new RuntimeException("connection closed");
    }
  }
}
