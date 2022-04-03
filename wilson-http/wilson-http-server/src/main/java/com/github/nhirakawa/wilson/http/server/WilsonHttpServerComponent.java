package com.github.nhirakawa.wilson.http.server;

import dagger.Component;
import javax.inject.Singleton;

@Singleton
@Component(modules = { WilsonHttpServerModule.class })
public interface WilsonHttpServerComponent {
  WilsonServerManager getServer();
}
