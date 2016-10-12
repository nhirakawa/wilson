package com.github.nh0815.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.util.Arrays;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nhirakawa.wilson.models.DiscoveryMessage;
import com.github.nhirakawa.wilson.models.DiscoveryResponse;

public class WilsonServer {

  private static final int DATAGRAM_MAX_SIZE = 8192;
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  public WilsonServer() {

  }

  private Set<InetSocketAddress> discoverClusterMembers() throws IOException {
    InetAddress group = InetAddress.getByName("224.0.0.1");
    MulticastSocket socket = new MulticastSocket(8080);
    socket.joinGroup(group);

    byte[] messageBytes = OBJECT_MAPPER.writeValueAsBytes(DiscoveryMessage.builder().build());
    DatagramPacket request = new DatagramPacket(messageBytes, messageBytes.length, group, 8080);
    socket.send(request);

    DatagramPacket responsePacket = new DatagramPacket(new byte[DATAGRAM_MAX_SIZE], DATAGRAM_MAX_SIZE);
    socket.receive(responsePacket);
    byte[] responseBytes = Arrays.copyOf(responsePacket.getData(), responsePacket.getLength());
    DiscoveryResponse response = OBJECT_MAPPER.readValue(responseBytes, DiscoveryResponse.class);
    return response.getMembers();
  }

}
