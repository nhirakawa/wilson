package com.github.nhirakawa.wilson.http.server;

import com.github.nhirakawa.wilson.common.ObjectMapperWrapper;
import io.javalin.plugin.json.JsonMapper;
import java.io.InputStream;
import javax.inject.Inject;

public class JacksonJsonMapper implements JsonMapper {

  @Inject
  JacksonJsonMapper() {}

  @Override
  public String toJsonString(Object obj) {
    return ObjectMapperWrapper.writeValueAsString(obj);
  }

  @Override
  public InputStream toJsonStream(Object obj) {
    return ObjectMapperWrapper.writeValueAsInputStream(obj);
  }

  @Override
  public <T> T fromJsonString(String json, Class<T> targetClass) {
    return ObjectMapperWrapper.readValueFromString(json, targetClass);
  }

  @Override
  public <T> T fromJsonStream(InputStream json, Class<T> targetClass) {
    return ObjectMapperWrapper.readValueFromInputStream(json, targetClass);
  }
}
