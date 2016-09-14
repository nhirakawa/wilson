package com.github.nh0815.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

public class WilsonClient implements Runnable {

  private static final String MULTICAST_ADDRESS = "224.0.0.1";
  private static final int MULTICAST_PORT = 11224;
  private static final int MAX_PACKET_SIZE = 65507;

  public WilsonClient() {

  }

  @Override
  public void run() {
    try {
      InetAddress address = InetAddress.getByName(MULTICAST_ADDRESS);
      try (MulticastSocket socket = new MulticastSocket(MULTICAST_PORT)) {
        socket.joinGroup(address);
        while (true) {
          byte[] buffer = new byte[MAX_PACKET_SIZE];
          DatagramPacket packet = new DatagramPacket(buffer, MAX_PACKET_SIZE);
          socket.receive(packet);
          System.out.println(new String(packet.getData(), 0, packet.getLength()));
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    } catch (UnknownHostException e) {
      e.printStackTrace();
    }
  }

  public static void main(String... args) {
    new WilsonClient().run();
  }

}
