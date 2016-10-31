package com.github.nhirakawa.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nhirakawa.client.guice.WilsonClientModule;
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
      BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      while (reader.ready()) {
        System.out.println(reader.readLine());
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void main(String... args) {
    Injector injector = Guice.createInjector(new WilsonClientModule());
    injector.getInstance(WilsonClient.class).run();
  }

}
