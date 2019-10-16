package com.github.nhirakawa.wilson.models;

import java.util.List;

import org.immutables.value.Value.Immutable;

import com.github.nhirakawa.wilson.models.style.WilsonStyle;

@Immutable
@WilsonStyle
public interface LogItemModel {

	long getTerm();
	long getIndex();
	List<Byte> getBytes();

}
