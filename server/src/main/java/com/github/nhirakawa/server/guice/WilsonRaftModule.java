package com.github.nhirakawa.server.guice;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import com.github.nhirakawa.server.raft.ImmutableWilsonState;
import com.google.common.base.MoreObjects;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class WilsonRaftModule extends AbstractModule {

  private final String memberId;

  /**
   * Construct a new module for a given cluster member
   * @param memberId - the id of the cluster member that this module provides for
   */
  public WilsonRaftModule(String memberId) {
    this.memberId = memberId;
  }

  @Override
  protected void configure() {

  }

  @Provides
  @Singleton
  AtomicReference<ImmutableWilsonState> provideAtomicWilsonState() {
    ImmutableWilsonState wilsonState = ImmutableWilsonState.builder().build();
    return new AtomicReference<>(wilsonState);
  }

  @Override
  public int hashCode() {
    return Objects.hash(memberId);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }

    if (!(obj instanceof WilsonRaftModule)) {
      return false;
    }

    WilsonRaftModule other = ((WilsonRaftModule) obj);
    return memberId.equals(other.memberId);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(WilsonRaftModule.class)
        .add("memberId", memberId)
        .toString();
  }
}
