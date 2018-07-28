package com.github.nhirakawa.server.guice;

import java.util.concurrent.atomic.AtomicReference;

import com.github.nhirakawa.server.models.WilsonState;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.typesafe.config.Config;

public class WilsonRaftModule extends AbstractModule {

  private final Config config;


  public WilsonRaftModule(Config config) {
    this.config = config;
  }

  @Override
  protected void configure() {

  }

  @Provides
  @Singleton
  Config provideConfig() {
    return config;
  }

  @Provides
  @Singleton
  AtomicReference<WilsonState> provideAtomicWilsonState() {
    WilsonState wilsonState = WilsonState.builder().build();
    return new AtomicReference<>(wilsonState);
  }
}
