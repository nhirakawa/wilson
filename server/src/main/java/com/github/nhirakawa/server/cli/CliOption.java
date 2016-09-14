package com.github.nhirakawa.server.cli;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public enum CliOption {

  CONFIGURATION_LOCATION(
      Option.builder("config")
          .argName("config")
          .longOpt("configuration-file")
          .desc("Path to configuration file")
          .required(false)
          .hasArg()
          .numberOfArgs(1)
          .type(String.class)
          .build()
  ),

  HOST(
      Option.builder("host")
          .argName("host")
          .longOpt("host")
          .desc("Host of local cluster member")
          .required(false)
          .hasArg()
          .numberOfArgs(1)
          .type(String.class)
          .build()
  ),

  PORT(
      Option.builder("port")
          .argName("port")
          .longOpt("port")
          .desc("Port of local cluster member")
          .hasArg()
          .numberOfArgs(1)
          .type(Integer.class)
          .build()
  ),

  LOCAL_MODE(
      Option.builder("local")
          .argName("local mode")
          .longOpt("local-mode")
          .desc("Run entire cluster locally")
          .required(false)
          .hasArg(false)
          .build()
  );

  private final Option option;

  CliOption(Option option) {
    this.option = option;
  }

  String getOpt() {
    return option.getOpt();
  }

  static Options getOptions() {
    Options options = new Options();
    for (CliOption option : CliOption.values()) {
      options.addOption(option.option);
    }
    return options;
  }
}
