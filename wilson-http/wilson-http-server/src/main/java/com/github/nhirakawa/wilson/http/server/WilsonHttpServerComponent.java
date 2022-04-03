package com.github.nhirakawa.wilson.http.server;

import com.github.nhirakawa.wilson.protocol.WilsonProtocolModule;
import dagger.Component;
import javax.inject.Singleton;

@Singleton
@Component(modules = { WilsonProtocolModule.class })
public interface WilsonHttpServerComponent {
  WilsonServer getServer();
}
