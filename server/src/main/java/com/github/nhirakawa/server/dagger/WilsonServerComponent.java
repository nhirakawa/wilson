package com.github.nhirakawa.server.dagger;

import javax.inject.Singleton;

import com.github.nhirakawa.server.transport.WilsonServer;

import dagger.Component;

@Singleton
@Component(modules = WilsonDaggerModule.class)
public interface WilsonServerComponent {

  WilsonServer create();

}
