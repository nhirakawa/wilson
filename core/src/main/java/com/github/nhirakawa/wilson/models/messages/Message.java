package com.github.nhirakawa.wilson.models.messages;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY, property = "type")
@JsonSubTypes({
    @Type(value = UuidMessage.class, name = "UuidMessage"),
    @Type(value = AppendEntriesRequest.class, name = "AppendEntriesRequest"),
    @Type(value = AppendEntriesResponse.class, name = "AppendEntriesResponse"),
    @Type(value = HeartbeatMessage.class, name = "Heartbeat"),
    @Type(value = VoteRequest.class, name = "VoteRequest"),
    @Type(value = VoteResponse.class, name = "VoteResponse")
})
public interface Message {

  String getClusterId();

  String getServerId();

}
