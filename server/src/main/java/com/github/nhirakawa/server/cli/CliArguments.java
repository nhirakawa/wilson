package com.github.nhirakawa.server.cli;

import java.util.Optional;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Ints;

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

  public String getHost() {
    return Optional.ofNullable(commandLine.getOptionValue(CliOption.HOST.getOpt())).orElse("localhost");
  }

  public int getPort() {
    return Optional.ofNullable(commandLine.getOptionValue(CliOption.PORT.getOpt())).map(Integer::parseInt).orElse(8000);
  }

  private static void validate(CommandLine commandLine) {
    boolean hasLocalMode = commandLine.hasOption(CliOption.LOCAL_MODE.getOpt());
    boolean hasHost = commandLine.hasOption(CliOption.HOST.getOpt());
    boolean hasPort = commandLine.hasOption(CliOption.PORT.getOpt());

    if (hasLocalMode) {
      Preconditions.checkArgument(!hasHost, "Cannot set host if running cluster locally");
      Preconditions.checkArgument(!hasPort, "Cannot set port if running cluster locally");
    }

    if (hasPort) {
      String maybePort = commandLine.getOptionValue(CliOption.PORT.getOpt());
      Integer port = Ints.tryParse(maybePort);
      Preconditions.checkArgument(port != null, "Could not parse port %s as integer", maybePort);
      Preconditions.checkArgument(port >= 0, "Port must be greater than or equal to 0 (%s)", maybePort);
    }
  }

}
