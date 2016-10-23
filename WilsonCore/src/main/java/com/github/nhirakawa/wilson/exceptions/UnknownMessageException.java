package com.github.nhirakawa.wilson.exceptions;

public class UnknownMessageException extends RuntimeException {

  public UnknownMessageException(String message) {
    super(String.format("Unknown message type: %s", message));
  }
}
