package com.github.nhirakawa.wilson.common.config;

import com.google.common.base.Preconditions;
import com.typesafe.config.Config;

public final class ConfigValidator {

  private ConfigValidator() {}

  public static void validate(Config config) {
    for (ConfigPath configPath : ConfigPath.values()) {
      Preconditions.checkArgument(
        config.hasPath(configPath.getPath()),
        "Could not find config path %s (%s)",
        configPath,
        configPath.getPath()
      );
    }
  }
}
