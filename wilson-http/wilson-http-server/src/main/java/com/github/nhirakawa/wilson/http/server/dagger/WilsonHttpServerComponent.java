package com.github.nhirakawa.wilson.http.server.dagger;

import com.github.nhirakawa.wilson.http.server.WilsonServerManager;
import dagger.Component;
import javax.inject.Singleton;

@Singleton
@Component(modules = { WilsonHttpServerModule.class })
public interface WilsonHttpServerComponent {
  WilsonServerManager getServer();
}
