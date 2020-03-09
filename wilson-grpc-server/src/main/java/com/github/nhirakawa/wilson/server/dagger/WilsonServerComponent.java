package com.github.nhirakawa.wilson.server.dagger;

import com.github.nhirakawa.wilson.server.transport.WilsonServer;
import dagger.Component;
import javax.inject.Singleton;

@Singleton
@Component(modules = WilsonDaggerModule.class)
public interface WilsonServerComponent {
  WilsonServer create();
}
