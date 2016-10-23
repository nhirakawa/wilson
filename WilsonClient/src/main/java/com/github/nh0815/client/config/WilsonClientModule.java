package com.github.nh0815.client.config;

import com.github.nhirakawa.wilson.models.config.WilsonCoreModule;
import com.google.inject.AbstractModule;

public class WilsonClientModule extends AbstractModule {

  @Override
  protected void configure() {
    install(new WilsonCoreModule());
  }

}
