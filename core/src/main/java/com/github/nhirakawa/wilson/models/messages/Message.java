package com.github.nhirakawa.wilson.models.messages;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = UuidMessage.class, name = "UuidMessage"),
    @JsonSubTypes.Type(value = AppendEntriesRequest.class, name = "AppendEntriesRequest"),
    @JsonSubTypes.Type(value = AppendEntriesResponse.class, name = "AppendEntriesResponse"),
    @JsonSubTypes.Type(value = HeartbeatMessage.class, name = "Heartbeat"),
    @JsonSubTypes.Type(value = VoteRequest.class, name = "VoteRequest"),
    @JsonSubTypes.Type(value = VoteResponse.class, name = "VoteResponse")
})
public interface Message {
}
