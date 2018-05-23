package com.github.nhirakawa.server.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nhirakawa.server.config.ConfigPath;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigRenderOptions;

public final class ObjectMapperWrapper {

  private static final ObjectMapper INSTANCE = buildInstance();
  private static final ConfigRenderOptions CONFIG_RENDER_OPTIONS = ConfigRenderOptions.defaults().setJson(true);

  public static <T> T readValueFromConfig(Config config, ConfigPath configPath) throws IOException {
    return INSTANCE.readValue(
        config.getObject(configPath.getPath()).render(CONFIG_RENDER_OPTIONS),
        new TypeReference<T>() {}
    );
  }

  private static ObjectMapper buildInstance() {
    return new ObjectMapper();
  }
}
