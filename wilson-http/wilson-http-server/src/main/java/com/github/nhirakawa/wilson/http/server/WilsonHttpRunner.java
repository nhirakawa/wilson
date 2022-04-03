package com.github.nhirakawa.wilson.http.server;

public class WilsonHttpRunner {

  public static void main(String... args) throws Exception {
    DaggerWilsonHttpServerComponent.builder().build().getServer().run();
  }
}
