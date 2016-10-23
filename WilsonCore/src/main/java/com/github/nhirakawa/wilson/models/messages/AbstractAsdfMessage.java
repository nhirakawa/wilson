package com.github.nhirakawa.wilson.models.messages;

import org.immutables.value.Value.Immutable;

import com.github.nhirakawa.wilson.models.style.WilsonStyle;

@WilsonStyle
@Immutable
public abstract class AbstractAsdfMessage implements WilsonMessage {

  public abstract String getAsdf();
}
