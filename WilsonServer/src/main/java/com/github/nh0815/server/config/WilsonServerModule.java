package com.github.nh0815.server.config;

import com.github.nhirakawa.wilson.models.config.WilsonCoreModule;
import com.google.inject.AbstractModule;

public class WilsonServerModule extends AbstractModule {

  @Override
  protected void configure() {
    install(new WilsonCoreModule());
  }

}
