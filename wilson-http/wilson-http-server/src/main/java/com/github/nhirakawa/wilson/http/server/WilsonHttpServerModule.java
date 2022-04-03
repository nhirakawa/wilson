package com.github.nhirakawa.wilson.http.server;

import com.github.nhirakawa.wilson.protocol.timeout.ElectionTimeout;
import com.github.nhirakawa.wilson.protocol.timeout.HeartbeatTimeout;
import com.github.nhirakawa.wilson.protocol.timeout.LeaderTimeout;
import com.github.nhirakawa.wilson.protocol.WilsonProtocolModule;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ServiceManager;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

@Module(includes = WilsonProtocolModule.class)
public class WilsonHttpServerModule {

  @Provides
  @Singleton
  static ServiceManager provideServiceManager(
    ElectionTimeout electionTimeout,
    LeaderTimeout leaderTimeout,
    HeartbeatTimeout heartbeatTimeout
  ) {
    return new ServiceManager(
      ImmutableList.of(electionTimeout, leaderTimeout, heartbeatTimeout)
    );
  }
}
