package com.github.nh0815.server.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class WilsonServerModule extends AbstractModule {

  @Override
  protected void configure() {

  }

  @Provides
  @Singleton
  public ObjectMapper provideObjectMapper() {
    return new ObjectMapper();
  }

}
