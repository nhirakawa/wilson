package com.github.nh0815.server.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nh0815.server.netty.WilsonServer;
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
