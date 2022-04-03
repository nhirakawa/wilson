package com.github.nhirakawa.wilson.http.server.banner;

import com.google.common.io.Resources;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class BannerUtil {

  private BannerUtil() {
    throw new UnsupportedOperationException();
  }

  public static String load() {
    try {
      return Resources.toString(
        Resources.getResource("banner.txt"),
        StandardCharsets.UTF_8
      );
    } catch (IllegalArgumentException | IOException ignored) {
      return "wilson";
    }
  }
}
