package com.github.nhirakawa.wilson.http.server;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nhirakawa.wilson.common.NamedThreadFactory;
import com.github.nhirakawa.wilson.http.server.banner.BannerUtil;
import com.github.nhirakawa.wilson.http.server.config.ConfigLoader;
import com.github.nhirakawa.wilson.http.server.dagger.DaggerWilsonHttpServerComponent;
import com.github.nhirakawa.wilson.protocol.WilsonProtocolModule;
import com.github.nhirakawa.wilson.protocol.config.WilsonConfig;

public class WilsonHttpClusterRunner {
  private static final Logger LOG = LoggerFactory.getLogger(
    WilsonHttpClusterRunner.class
  );

  public static void main(String... args) {
    System.out.println(BannerUtil.load());

    List<WilsonConfig> wilsonConfigList = ConfigLoader.loadCluster();

    LOG.info("Loaded {} configs", wilsonConfigList.size());

    ExecutorService executorService = Executors.newFixedThreadPool(
      wilsonConfigList.size(),
      NamedThreadFactory.build("wilson-http-cluster-runner")
    );

    for (WilsonConfig wilsonConfig : wilsonConfigList) {
      executorService.execute(
        () -> {
          run(wilsonConfig);
        }
      );
    }
  }

  private static void run(WilsonConfig wilsonConfig) {
    LOG.info("Running {}", wilsonConfig);
    DaggerWilsonHttpServerComponent
      .builder()
      .wilsonProtocolModule(new WilsonProtocolModule(wilsonConfig))
      .build()
      .getServer()
      .run();
  }
}
