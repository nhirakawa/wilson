package com.github.nh0815.client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nh0815.client.config.WilsonClientModule;
import com.github.nhirakawa.wilson.models.messages.AsdfMessage;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

public class WilsonClient implements Runnable {

  private final ObjectMapper objectMapper;

  @Inject
  public WilsonClient(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public void run() {
    String host = "localhost";
    int port = 9000;
    AsdfMessage message = AsdfMessage.builder()
        .setAsdf("fdsa")
        .build();
    try (Socket socket = new Socket(host, port)) {
      PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
      String jsonMessage = objectMapper.writeValueAsString(message);
      writer.println(jsonMessage);
      socket.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void main(String... args) {
    Injector injector = Guice.createInjector(new WilsonClientModule());
    injector.getInstance(WilsonClient.class).run();
  }

}
