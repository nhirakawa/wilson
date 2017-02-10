package com.github.nhirakawa.server.transport.netty;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.nhirakawa.wilson.models.messages.Message;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.netty.channel.Channel;

@Singleton
public class ConnectionManager {

  private static final Logger LOG = LogManager.getLogger(ConnectionManager.class);

  private final Map<String, Channel> socketAddressChannelMap;

  @Inject
  public ConnectionManager() {
    this.socketAddressChannelMap = new ConcurrentHashMap<>();
  }

  /**
   * Adds a channel for the remote address, if one does not already exist
   *
   * @return true if there is not already a channel for the address
   */
  public boolean add(String serverId, Channel channel) {
    if (socketAddressChannelMap.containsKey(serverId)) {
      LOG.trace("Open connection to {} already exists", serverId);
      return false;
    }

    LOG.trace("Adding connection to {}", serverId);
    socketAddressChannelMap.put(serverId, channel);
    return true;
  }

  public void broadcast(Message message) {
    for (Entry<String, Channel> entry : socketAddressChannelMap.entrySet()) {
      String id = entry.getKey();
      Channel channel = entry.getValue();
      if (channel.isOpen() && channel.isActive()) {
        LOG.trace("Sending message({}) to {}", message.getClass(), id);
        channel.writeAndFlush(message);
      }
    }
  }
}
