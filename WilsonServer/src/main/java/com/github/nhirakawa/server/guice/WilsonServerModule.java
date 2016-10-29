package com.github.nhirakawa.server.guice;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.github.nhirakawa.server.config.WilsonConfiguration;
import com.github.nhirakawa.server.raft.WilsonServer;
import com.google.common.io.Resources;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Named;

public class WilsonServerModule extends AbstractModule {

  public static final String DEFAULT_OBJECT_MAPPER = "default.object.mapper";
  public static final String YAML_OBJECT_MAPPER = "yaml.object.mapper";

  @Override
  protected void configure() {
    install(new FactoryModuleBuilder().build(WilsonServer.Factory.class));
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
  @Named(YAML_OBJECT_MAPPER)
  public ObjectMapper provideYamlObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
    objectMapper.registerModule(new GuavaModule());
    return objectMapper;
  }

  @Provides
  @Singleton
  public WilsonConfiguration provideConfiguration(@Named(YAML_OBJECT_MAPPER) ObjectMapper objectMapper) throws IOException {
    String fileString = Resources.toString(Resources.getResource("wilsonserver.yaml"), StandardCharsets.UTF_8);
    return objectMapper.readValue(fileString, WilsonConfiguration.class);
  }

}
