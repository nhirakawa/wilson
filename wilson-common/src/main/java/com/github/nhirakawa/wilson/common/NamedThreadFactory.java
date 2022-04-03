package com.github.nhirakawa.wilson.common;

import com.github.nhirakawa.wilson.models.ClusterMember;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.concurrent.ThreadFactory;

public final class NamedThreadFactory {

  private NamedThreadFactory() {
    throw new UnsupportedOperationException();
  }

  public static ThreadFactory build(String namespace) {
    return new ThreadFactoryBuilder().setNameFormat(namespace + "-%s").build();
  }

  public static ThreadFactory build(
    String namespace,
    ClusterMember clusterMember
  ) {
    String format = String.format(
      "%s-%s-%s",
      namespace,
      clusterMember.getHost(),
      clusterMember.getPort()
    );
    return new ThreadFactoryBuilder().setNameFormat(format + "-%s").build();
  }
}
