package com.github.nhirakawa.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nhirakawa.client.guice.WilsonClientModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.Socket;

public class WilsonClient implements Runnable {
  private final ObjectMapper objectMapper;

  @Inject
  public WilsonClient(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public static void main(String... args) {
    Injector injector = Guice.createInjector(new WilsonClientModule());
    injector.getInstance(WilsonClient.class).run();
  }

  @Override
  public void run() {
    String host = "localhost";
    int port = 9000;
    try (Socket socket = new Socket(host, port)) {
      BufferedReader reader = new BufferedReader(
        new InputStreamReader(socket.getInputStream())
      );
      while (reader.ready()) {
        System.out.println(reader.readLine());
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
