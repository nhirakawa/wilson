package com.github.nhirakawa.wilson.http.server;

import com.github.nhirakawa.wilson.common.NamedThreadFactory;
import com.github.nhirakawa.wilson.http.server.banner.BannerUtil;
import com.github.nhirakawa.wilson.http.server.config.ConfigLoader;
import com.github.nhirakawa.wilson.http.server.dagger.DaggerWilsonHttpServerComponent;
import com.github.nhirakawa.wilson.protocol.config.WilsonConfig;
import com.github.nhirakawa.wilson.protocol.WilsonProtocolModule;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.List;

public class WilsonHttpClusterRunner {

  public static void main(String... args) {
    System.out.println(BannerUtil.load());

    List<WilsonConfig> wilsonConfigSet = ConfigLoader.loadCluster();

    ExecutorService executorService = Executors.newFixedThreadPool(
      wilsonConfigSet.size(),
      NamedThreadFactory.build("wilson-http-cluster-runner")
    );

    for (WilsonConfig wilsonConfig : wilsonConfigSet) {
      executorService.execute(
        () -> {
          run(wilsonConfig);
        }
      );
    }
  }

  private static void run(WilsonConfig wilsonConfig) {
    DaggerWilsonHttpServerComponent
      .builder()
      .wilsonProtocolModule(new WilsonProtocolModule(wilsonConfig))
      .build()
      .getServer()
      .run();
  }
}
