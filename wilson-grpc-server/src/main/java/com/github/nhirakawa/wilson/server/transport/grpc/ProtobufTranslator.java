package com.github.nhirakawa.wilson.server.transport.grpc;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nhirakawa.wilson.models.messages.AppendEntriesRequest;
import com.github.nhirakawa.wilson.models.messages.AppendEntriesResponse;
import com.github.nhirakawa.wilson.models.messages.VoteRequest;
import com.github.nhirakawa.wilson.models.messages.VoteRequestModel;
import com.github.nhirakawa.wilson.models.messages.VoteResponse;

final class ProtobufTranslator {

	private final ObjectMapper objectMapper;

	@Inject
	ProtobufTranslator(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public VoteRequestProto toProto(VoteRequestModel request) {
		return objectMapper.convertValue(request, VoteRequestProto.class);
	}

	public VoteRequest fromProto(VoteRequestProto voteRequestProto) {
		return objectMapper.convertValue(voteRequestProto, VoteRequest.class);
	}

	public VoteResponse fromProto(VoteResponseProto voteResponse) {
		return objectMapper.convertValue(voteResponse, VoteResponse.class);
	}

	public VoteResponseProto toProto(VoteResponse voteResponse) {
		return objectMapper.convertValue(voteResponse, VoteResponseProto.class);
	}

	public AppendEntriesRequest fromProto(AppendEntriesRequestProto appendEntriesRequestProto) {
		return objectMapper.convertValue(appendEntriesRequestProto, AppendEntriesRequest.class);
	}

	public AppendEntriesRequestProto toProto(AppendEntriesRequest appendEntriesRequest) {
		return objectMapper.convertValue(appendEntriesRequest, AppendEntriesRequestProto.class);
	}

	public AppendEntriesResponse fromProto(AppendEntriesResponseProto appendEntriesResponseProto) {
		return objectMapper.convertValue(appendEntriesResponseProto, AppendEntriesResponse.class);
	}

	public AppendEntriesResponseProto toProto(AppendEntriesResponse appendEntriesResponse) {
		return objectMapper.convertValue(appendEntriesResponse, AppendEntriesResponseProto.class);
	}

	public ObjectMapper instance() {
		return objectMapper;
	}

}
