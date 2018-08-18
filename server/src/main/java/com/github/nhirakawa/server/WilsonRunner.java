package com.github.nhirakawa.server;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.nhirakawa.server.config.ConfigPath;
import com.github.nhirakawa.server.config.ConfigValidator;
import com.github.nhirakawa.server.dagger.DaggerWilsonServerComponent;
import com.github.nhirakawa.server.dagger.WilsonDaggerModule;
import com.github.nhirakawa.server.jackson.ObjectMapperWrapper;
import com.github.nhirakawa.server.models.ClusterMember;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.common.io.Resources;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;

public class WilsonRunner {

  private static final Logger LOG = LoggerFactory.getLogger(WilsonRunner.class);

  public static void main(String... args) throws IOException {
    printBanner();

    Config config = ConfigFactory.load();
    ConfigValidator.validate(config);
    boolean isLocalCluster = config.getBoolean(ConfigPath.WILSON_LOCAL_CLUSTER.getPath());

    if (isLocalCluster) {
      runLocalCluster(config);
    } else {
      runLocalMember(config);
    }
  }

  private static void runLocalCluster(Config config) throws IOException {
    Set<ClusterMember> clusterMembers = ObjectMapperWrapper.instance().readValue(
        config.getList(ConfigPath.WILSON_CLUSTER_ADDRESSES.getPath()).render(ConfigRenderOptions.concise()),
        new TypeReference<Set<ClusterMember>>() {}
    );

    ExecutorService executorService = getExecutorService();

    for (ClusterMember clusterMember : clusterMembers) {
      executorService.execute(() -> runMember(clusterMember, clusterMembers, config));
    }
  }

  private static void runLocalMember(Config config) throws IOException {
    ClusterMember localClusterMember = ObjectMapperWrapper.instance().readValue(
        config.getObject(ConfigPath.WILSON_LOCAL_ADDRESS.getPath()).render(ConfigRenderOptions.concise()),
        new TypeReference<ClusterMember>() {}
    );

    Set<ClusterMember> clusterMembers = ObjectMapperWrapper.instance().readValue(
        config.getList(ConfigPath.WILSON_CLUSTER_ADDRESSES.getPath()).render(ConfigRenderOptions.concise()),
        new TypeReference<Set<ClusterMember>>() {}
    );

    runMember(localClusterMember, clusterMembers, config);
  }

  private static void runMember(ClusterMember clusterMember, Set<ClusterMember> clusterMembers, Config config) {
    Config configWithLocalMember = ConfigFactory.parseMap(
        ImmutableMap.of(
            "wilson.local.host", clusterMember.getHost(),
            "wilson.local.port", clusterMember.getPort()
        )
    )
        .withFallback(config);

    Set<ClusterMember> clusterWithoutLocalMember = Sets.difference(
        clusterMembers,
        Collections.singleton(clusterMember)
    );

    try {
      DaggerWilsonServerComponent.builder()
          .wilsonDaggerModule(new WilsonDaggerModule(configWithLocalMember, clusterMember, clusterWithoutLocalMember))
          .build()
          .create()
          .start();
    } catch (IOException | InterruptedException e) {
      LOG.error("Could not bootstrap {}", clusterMember, e);
      Throwables.throwIfUnchecked(e);
      throw new RuntimeException(e);
    }
  }

  private static ExecutorService getExecutorService() {
    ThreadFactory threadFactory = new ThreadFactoryBuilder()
        .setNameFormat("wilson-runner-%s")
        .setDaemon(false)
        .setUncaughtExceptionHandler((t, e) -> LOG.error("Uncaught exception in thread {}", t, e))
        .build();

    return Executors.newFixedThreadPool(10, threadFactory);
  }

  private static void printBanner() {
    try {
      URL url = Resources.getResource("banner.txt");
      String banner = Resources.toString(url, StandardCharsets.UTF_8);
      LOG.info("\n{}\n", banner);
    } catch (IllegalArgumentException | IOException ignored) {
    }
  }
}
