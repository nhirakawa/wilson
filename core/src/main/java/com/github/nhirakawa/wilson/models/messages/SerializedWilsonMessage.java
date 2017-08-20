package com.github.nhirakawa.wilson.models.messages;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY, property = "type")
@JsonSubTypes({
    @Type(value = ImmutableUuidWilsonMessage.class, name = "UuidMessage"),
    @Type(value = ImmutableAppendEntriesRequest.class, name = "AppendEntriesRequest"),
    @Type(value = ImmutableAppendEntriesResponse.class, name = "AppendEntriesResponse"),
    @Type(value = ImmutableHeartbeatMessage.class, name = "Heartbeat"),
    @Type(value = ImmutableVoteRequest.class, name = "VoteRequest"),
    @Type(value = ImmutableVoteResponse.class, name = "VoteResponse")
})
public interface SerializedWilsonMessage extends WilsonMessage {

}
