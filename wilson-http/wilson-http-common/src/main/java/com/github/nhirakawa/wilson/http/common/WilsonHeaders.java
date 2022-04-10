package com.github.nhirakawa.wilson.http.common;

public final class WilsonHeaders {
  private static final String REQUEST_ID = "X-Wilson-Request-Id";
  private static final String SERVER_ID = "X-Wilson-Server-Id";
  private static final String REQUEST_STARTED = "X-Wilson-Request-Started";

  private WilsonHeaders() {
    throw new UnsupportedOperationException();
  }

  public static String requestId() {
    return REQUEST_ID;
  }

  public static String serverId() {
    return SERVER_ID;
  }

  public static String requestStarted() {
    return REQUEST_STARTED;
  }
}
