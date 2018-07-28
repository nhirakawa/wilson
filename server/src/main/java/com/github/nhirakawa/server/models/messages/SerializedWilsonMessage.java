package com.github.nhirakawa.server.models.messages;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY, property = "type")
@JsonSubTypes({
    @Type(value = UuidWilsonMessage.class, name = "UuidMessage"),
    @Type(value = AppendEntriesRequest.class, name = "AppendEntriesRequestModel"),
    @Type(value = AppendEntriesResponse.class, name = "AppendEntriesResponseModel"),
    @Type(value = HeartbeatRequest.class, name = "Heartbeat"),
    @Type(value = VoteRequest.class, name = "VoteRequestModel"),
    @Type(value = VoteResponse.class, name = "VoteResponseModel")
})
public interface SerializedWilsonMessage extends WilsonMessage {

}
