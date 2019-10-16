package com.github.nhirakawa.wilson.server.dagger;

import javax.inject.Singleton;

import com.github.nhirakawa.wilson.server.transport.WilsonServer;

import dagger.Component;

@Singleton
@Component(modules = WilsonDaggerModule.class)
public interface WilsonServerComponent {

	WilsonServer create();

}
