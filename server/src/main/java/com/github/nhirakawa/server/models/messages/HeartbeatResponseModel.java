package com.github.nhirakawa.server.models.messages;

import org.immutables.value.Value;

import com.github.nhirakawa.server.models.style.WilsonStyle;

@Value.Immutable
@WilsonStyle
public interface HeartbeatResponseModel extends SerializedWilsonMessage {

}
