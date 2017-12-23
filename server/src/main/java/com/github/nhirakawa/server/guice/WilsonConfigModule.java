package com.github.nhirakawa.server.guice;

import java.io.IOException;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.github.nhirakawa.server.cli.CliArguments;
import com.github.nhirakawa.server.config.Configuration;
import com.github.nhirakawa.server.config.ImmutableConfiguration;
import com.google.inject.AbstractModule;

/**
 * A Guice module that provides the original CliArguments that were passed in, and the canonical Configuration
 */
public class WilsonConfigModule extends AbstractModule {

  private final CliArguments cliArguments;

  public WilsonConfigModule(CliArguments cliArguments) {
    this.cliArguments = cliArguments;
  }

  @Override
  protected void configure() {
    JavaPropsMapper propsMapper = new JavaPropsMapper();
    propsMapper.registerModule(new GuavaModule());
    propsMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    try {
      bind(Configuration.class).toInstance(propsMapper.readValue(System.getProperties(), ImmutableConfiguration.class));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    bind(CliArguments.class).toInstance(cliArguments);
  }

}
