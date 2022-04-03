package com.github.nhirakawa.wilson.http.server;

import com.github.nhirakawa.wilson.http.server.banner.BannerUtil;
import com.github.nhirakawa.wilson.http.server.config.ConfigLoader;
import com.github.nhirakawa.wilson.http.server.dagger.DaggerWilsonHttpServerComponent;
import com.github.nhirakawa.wilson.protocol.config.WilsonConfig;
import com.github.nhirakawa.wilson.protocol.WilsonProtocolModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WilsonHttpRunner {
  private static final Logger LOG = LoggerFactory.getLogger(
    WilsonHttpRunner.class
  );

  public static void main(String... args) throws Exception {
    System.out.println(BannerUtil.load());

    WilsonConfig wilsonConfig = ConfigLoader.loadSingle();

    DaggerWilsonHttpServerComponent
      .builder()
      .wilsonProtocolModule(new WilsonProtocolModule(wilsonConfig))
      .build()
      .getServer()
      .run();
  }
}
