package com.github.nhirakawa.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nhirakawa.server.cli.CliArguments;

public class WilsonRunner {

  private static final Logger LOG = LoggerFactory.getLogger(WilsonRunner.class);

  public static void main(String... args) throws IOException {
    CliArguments cliArguments = new CliArguments(args);
    loadProperties(cliArguments);

    System.exit(0);
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
}
