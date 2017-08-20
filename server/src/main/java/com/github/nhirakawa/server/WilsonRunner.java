package com.github.nhirakawa.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.github.nhirakawa.server.cli.CliArguments;
import com.github.nhirakawa.server.config.ClusterMember;
import com.github.nhirakawa.server.config.Configuration;
import com.github.nhirakawa.server.guice.WilsonConfigModule;
import com.github.nhirakawa.server.guice.WilsonRaftModule;
import com.github.nhirakawa.server.guice.WilsonTransportModule;
import com.github.nhirakawa.server.transport.WilsonServer;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;

public class WilsonRunner {

  private static final Logger LOG = LoggerFactory.getLogger(WilsonRunner.class);

  public static void main(String... args) throws IOException {
    CliArguments cliArguments = new CliArguments(args);
    loadProperties(cliArguments);

    Injector parentInjector = Guice.createInjector(Stage.PRODUCTION, new WilsonConfigModule(cliArguments));
    Configuration configuration = parentInjector.getInstance(Configuration.class);

    if (cliArguments.isLocalMode()) {
      run(configuration.getClusterMembers(), parentInjector);
    } else {
      Preconditions.checkState(configuration.getLocalMember().isPresent(), "Not running in local mode, but no local member specified");
      run(Collections.singleton(configuration.getLocalMember().get()), parentInjector);
    }
  }

  private static void loadProperties(CliArguments cliArguments) throws IOException {
    Optional<String> maybeConfigurationFilePath = cliArguments.getConfigurationFilePath();
    if (!maybeConfigurationFilePath.isPresent()) {
      return;
    }

    String configurationFilePath = maybeConfigurationFilePath.get();
    Properties properties = new Properties();
    properties.load(new FileInputStream(configurationFilePath));
    Properties systemProperties = System.getProperties();

    for (Entry<Object, Object> entry : properties.entrySet()) {
      systemProperties.putIfAbsent(entry.getKey(), entry.getValue());
    }
  }

  private static void run(Collection<? extends ClusterMember> members,
                          Injector parentInjector) {
    ExecutorService executorService = getExecutorService();
    for (ClusterMember member : members) {
      executorService.submit(() -> runMember(member, parentInjector));
    }
  }

  private static void runMember(ClusterMember clusterMember,
                                Injector parentInjector) {
    MDC.put("serverId", clusterMember.getServerId());
    WilsonRaftModule wilsonRaftModule = new WilsonRaftModule(clusterMember.getServerId());
    Injector injector = parentInjector.createChildInjector(wilsonRaftModule, new WilsonTransportModule(clusterMember));

    try {
      injector.getInstance(WilsonServer.class).start();
    } catch (InterruptedException | IOException e) {
      LOG.error("Could not bootstrap {}", clusterMember, e);
      throw new RuntimeException(e);
    } finally {
      MDC.remove("serverId");
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
}
