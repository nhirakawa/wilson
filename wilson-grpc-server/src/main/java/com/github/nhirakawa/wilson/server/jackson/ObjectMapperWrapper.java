package com.github.nhirakawa.wilson.server.jackson;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nhirakawa.wilson.server.config.ConfigPath;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigRenderOptions;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ObjectMapperWrapper {
  private static final Logger LOG = LoggerFactory.getLogger(
    ObjectMapperWrapper.class
  );

  private static final ObjectMapper INSTANCE = buildInstance();
  private static final ConfigRenderOptions CONFIG_RENDER_OPTIONS = ConfigRenderOptions.concise();

  public static <T> T readValueFromConfig(
    Config config,
    ConfigPath configPath,
    TypeReference<T> typeReference
  )
    throws IOException {
    return INSTANCE.readValue(
      config.getObject(configPath.getPath()).render(CONFIG_RENDER_OPTIONS),
      typeReference
    );
  }

  public static ObjectMapper instance() {
    return INSTANCE;
  }

  private static ObjectMapper buildInstance() {
    return new ObjectMapper();
  }
}
