package com.github.nhirakawa.server.cli;

import java.util.Optional;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;

import com.google.common.base.Preconditions;

public class CliArguments {

  private final CommandLine commandLine;

  public CliArguments(String... args) {
    this.commandLine = parseArgs(args);
  }

  private CommandLine parseArgs(String... args) {
    try {
      CommandLine commandLine = new DefaultParser().parse(CliOption.getOptions(), args);
      validate(commandLine);
      return commandLine;
    } catch (ParseException | IllegalArgumentException | IllegalStateException e) {
      HelpFormatter helpFormatter = new HelpFormatter();
      helpFormatter.printHelp("wilson", CliOption.getOptions());
      throw new RuntimeException(e);
    }
  }

  public Optional<String> getConfigurationFilePath() {
    if (!commandLine.hasOption(CliOption.CONFIGURATION_LOCATION.getOpt())) {
      return Optional.empty();
    }

    return Optional.ofNullable(commandLine.getOptionValue(CliOption.CONFIGURATION_LOCATION.getOpt()));
  }

  public boolean isLocalMode() {
    return commandLine.hasOption(CliOption.LOCAL_MODE.getOpt());
  }

  private static void validate(CommandLine commandLine) {
    boolean localPortsAndDevelopmentMode = !commandLine.hasOption(CliOption.LOCAL_PORTS.getOpt()) || commandLine.hasOption(CliOption.DEVELOPMENT_MODE.getOpt());
    Preconditions.checkArgument(localPortsAndDevelopmentMode, "Local ports can only be set in development mode");
  }

}
