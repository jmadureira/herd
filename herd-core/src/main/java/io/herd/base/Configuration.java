package io.herd.base;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.herd.gossip.DefaultGossipConfiguration;
import io.herd.gossip.GossipConfiguration;

public class Configuration {

	@Valid
	@NotNull
	private GossipConfiguration gossiper = new DefaultGossipConfiguration();

	@JsonProperty
	public GossipConfiguration getGossiper() {
		return gossiper;
	}

	@JsonProperty
	public void setGossiper(GossipConfiguration gossiper) {
		this.gossiper = gossiper;
	}

	public String toString() {
		return gossiper.toString();
	}
}
