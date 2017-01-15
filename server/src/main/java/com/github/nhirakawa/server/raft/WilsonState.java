package com.github.nhirakawa.server.raft;

import com.google.inject.Singleton;

@Singleton
public class WilsonState {

  private final LeaderState leaderState;

  public WilsonState() {
    this.leaderState = LeaderState.FOLLOWER;
  }
}
