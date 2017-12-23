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
  DEVELOPMENT_MODE(
      Option.builder("d")
          .argName("developmentMode")
          .longOpt("development-mode")
          .desc("Allow extra configuration options for local development")
          .required(false)
          .hasArg(false)
          .build()
  ),
  LOCAL_PORTS(
      Option.builder("p")
          .argName("localPorts")
          .longOpt("local-ports")
          .desc("Specify ports for multiple cluster members (in development mode only)")
          .required(false)
          .hasArgs()
          .valueSeparator(',')
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
