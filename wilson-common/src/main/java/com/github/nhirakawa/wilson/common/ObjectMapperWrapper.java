package com.github.nhirakawa.wilson.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.github.nhirakawa.wilson.common.config.ConfigPath;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigRenderOptions;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;

public final class ObjectMapperWrapper {
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

  public static byte[] writeValueAsBytes(Object object) {
    try {
      return INSTANCE.writeValueAsBytes(object);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public static String writeValueAsString(Object object) {
    try {
      return INSTANCE.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public static InputStream writeValueAsInputStream(Object object) {
    try {
      return new ByteArrayInputStream(INSTANCE.writeValueAsBytes(object));
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public static <T> T readValueFromInputStream(
    InputStream inputStream,
    Class<T> clazz
  ) {
    try {
      return INSTANCE.readValue(inputStream, clazz);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static <T> T readValueFromString(String string, Class<T> clazz) {
    try {
      return INSTANCE.readValue(string, clazz);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  private static ObjectMapper buildInstance() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new GuavaModule());
    return objectMapper;
  }
}
