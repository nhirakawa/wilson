package com.github.nhirakawa.server.guice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nhirakawa.server.netty.WilsonServer;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class WilsonServerModule extends AbstractModule {

  @Override
  protected void configure() {
    install(new FactoryModuleBuilder().build(WilsonServer.Factory.class));
  }

  @Provides
  @Singleton
  public ObjectMapper provideObjectMapper() {
    return new ObjectMapper();
  }

}
