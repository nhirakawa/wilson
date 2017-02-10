package com.github.nhirakawa.server.guice;

import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.github.nhirakawa.server.config.Configuration;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

public class WilsonServerModule extends AbstractModule {

  public static final String DEFAULT_OBJECT_MAPPER = "default.object.mapper";

  @Override
  protected void configure() {
    JavaPropsMapper propsMapper = new JavaPropsMapper();
    propsMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    try {
      bind(Configuration.class).toInstance(propsMapper.readValue(System.getProperties(), Configuration.class));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Provides
  @Singleton
  @Named(DEFAULT_OBJECT_MAPPER)
  public ObjectMapper provideObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new GuavaModule());
    return objectMapper;
  }

  @Provides
  @Singleton
  public ScheduledExecutorService provideScheduledExecutorService() {
    ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors());
    executor.setRemoveOnCancelPolicy(true);
    return executor;
  }

}
