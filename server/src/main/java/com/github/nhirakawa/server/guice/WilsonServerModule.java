package com.github.nhirakawa.server.guice;

import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.github.nhirakawa.server.config.Configuration;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class WilsonServerModule extends AbstractModule {

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

  @Provides
  @Singleton
  public Retryer<Void> provideRetyrer() {
    return RetryerBuilder.<Void>newBuilder()
        .withWaitStrategy(WaitStrategies.fixedWait(5, TimeUnit.SECONDS))
        .withStopStrategy(StopStrategies.stopAfterAttempt(5))
        .retryIfExceptionOfType(IOException.class)
        .build();
  }

}
