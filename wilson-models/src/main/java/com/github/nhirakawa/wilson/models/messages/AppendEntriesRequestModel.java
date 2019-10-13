package com.github.nhirakawa.wilson.models.messages;

import java.util.List;

import org.immutables.value.Value.Immutable;

import com.github.nhirakawa.wilson.models.ClusterMember;
import com.github.nhirakawa.wilson.models.style.WilsonStyle;
import com.google.protobuf.ByteString;

@WilsonStyle
@Immutable
public interface AppendEntriesRequestModel {

	long getTerm();
	ClusterMember getLeader();
	long getLastLogIndex();
	long getLastLogTerm();
	List<ByteString> getEntries();
	long getLeaderCommitIndex();

}
