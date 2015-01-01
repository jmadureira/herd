package io.herd.gossip;

import static io.herd.base.Collections.isEmpty;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = DefaultGossipConfiguration.class)
public interface GossipConfiguration {

	/**
	 * @return The socket port where the gossiper will receive gossips.
	 */
	int getPort();

	/**
	 * @return The collection of seed nodes.
	 */
	Set<String> getSeedNodes();

	/**
	 * @return The name of the gossip service.
	 */
	String getServiceName();

	/**
	 * Returns whether gossiper is enabled on this system or not. In practice to
	 * indicate that gossiper is enabled one only needs to specify at least one
	 * seed node.
	 * 
	 * @return <code>true</code> if gossiper is enabled on this system or
	 *         <code>false</code>.
	 * @see GossipConfiguration#getSeedNodes()
	 */
	default boolean isEnabled() {
		return !isEmpty(getSeedNodes());
	}
}
