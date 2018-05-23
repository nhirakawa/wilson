package com.github.nhirakawa.server;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nhirakawa.server.config.ClusterMember;
import com.github.nhirakawa.server.config.ClusterMemberModel;
import com.github.nhirakawa.server.config.ConfigPath;
import com.github.nhirakawa.server.guice.WilsonRaftModule;
import com.github.nhirakawa.server.guice.WilsonTransportModule;
import com.github.nhirakawa.server.jackson.ObjectMapperWrapper;
import com.github.nhirakawa.server.transport.WilsonServer;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class WilsonRunner {

  private static final Logger LOG = LoggerFactory.getLogger(WilsonRunner.class);

  public static void main(String... args) throws IOException {
    printBanner();

    Config config = ConfigFactory.load();
    boolean isLocalCluster = config.getBoolean(ConfigPath.WILSON_LOCAL_CLUSTER.getPath());

    if (isLocalCluster) {
      runLocalCluster(config);
    } else {
      runLocalMember(config);
    }
  }

  private static void runLocalCluster(Config config) throws IOException {
    Set<ClusterMember> clusterMembers = ObjectMapperWrapper.readValueFromConfig(config, ConfigPath.WILSON_CLUSTER_ADDRESSES);

    ExecutorService executorService = getExecutorService();

    for (ClusterMember clusterMember : clusterMembers) {
      executorService.submit(() -> runMember(clusterMember, config));
    }
  }

  private static void runLocalMember(Config config) throws IOException {
    ClusterMember localClusterMember = ObjectMapperWrapper.readValueFromConfig(config, ConfigPath.WILSON_LOCAL_ADDRESS);
    runMember(localClusterMember, config);
  }

  private static void runMember(ClusterMember clusterMember, Config config) {
    Config configWithLocalMember = ConfigFactory.parseMap(
        ImmutableMap.of(
            "wilson.local.host", clusterMember.getHost(),
            "wilson.local.port", clusterMember.getPort()
        )
    )
        .withFallback(config);

    Injector injector = Guice.createInjector(new WilsonRaftModule(configWithLocalMember), new WilsonTransportModule(clusterMember));

    try {
      injector.getInstance(WilsonServer.class).start();
    } catch (InterruptedException | IOException e) {
      LOG.error("Could not bootstrap {}", clusterMember, e);
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
