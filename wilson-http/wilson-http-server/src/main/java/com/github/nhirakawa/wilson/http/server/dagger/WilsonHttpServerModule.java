package com.github.nhirakawa.wilson.http.server.dagger;

import com.github.nhirakawa.wilson.http.client.WilsonHttpClientModule;
import com.github.nhirakawa.wilson.http.server.WilsonHttpServer;
import com.github.nhirakawa.wilson.protocol.WilsonProtocolModule;
import com.github.nhirakawa.wilson.protocol.WilsonProtocolService;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ServiceManager;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

@Module(
  includes = {
    WilsonProtocolModule.class, Declarations.class, WilsonHttpClientModule.class
  }
)
public class WilsonHttpServerModule {

  @Provides
  @Singleton
  static ServiceManager provideServiceManager(
    WilsonHttpServer wilsonHttpServer,
    WilsonProtocolService wilsonProtocolService
  ) {
    return new ServiceManager(
      ImmutableList.of(wilsonHttpServer, wilsonProtocolService)
    );
  }
}
