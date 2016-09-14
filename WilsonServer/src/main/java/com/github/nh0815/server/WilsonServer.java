package com.github.nh0815.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class WilsonServer implements Runnable {

  private static final String MULTICAST_ADDRESS = "224.0.0.1";
  private static final int MULTICAST_PORT = 11224;

  public WilsonServer() {

  }

  @Override
  public void run() {
    try {
      InetAddress address = InetAddress.getByName(MULTICAST_ADDRESS);
      try (DatagramSocket socket = new DatagramSocket()) {
        System.out.printf("Listening on %d%n", MULTICAST_PORT);
        while (true) {
          System.out.println("Sending data...");
          byte[] data = UUID.randomUUID().toString().getBytes();
          DatagramPacket packet = new DatagramPacket(data, data.length, address, MULTICAST_PORT);
          socket.send(packet);
          Thread.sleep(TimeUnit.SECONDS.toMillis(1));
        }
      } catch (IOException | InterruptedException e) {
        e.printStackTrace();
      }
    } catch (UnknownHostException e) {
      e.printStackTrace();
    }
  }

  public static void main(String... args) {
    new WilsonServer().run();
  }
}
