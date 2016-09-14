package com.github.nhirakawa.wilson.models.messages;

import org.immutables.value.Value;

import com.github.nhirakawa.wilson.models.style.WilsonStyle;

@Value.Immutable
@WilsonStyle
public interface HeartbeatResponse extends SerializedWilsonMessage {

}
