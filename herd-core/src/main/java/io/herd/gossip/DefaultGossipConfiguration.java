package io.herd.gossip;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.base.Objects;

@JsonTypeName("default")
public class DefaultGossipConfiguration implements GossipConfiguration {

    private int port;

    private String serviceName = "gossiper";
    
    private Set<String> seedNodes;

    @JsonProperty
    public int getPort() {
        return port;
	}

    @JsonProperty
	public Set<String> getSeedNodes() {
		return seedNodes;
	}

    @JsonProperty
    public String getServiceName() {
        return serviceName;
    }

	@JsonProperty
	public void setPort(int port) {
		this.port = port;
	}

	@JsonProperty
	public void setSeedNodes(Set<String> seedNodes) {
		this.seedNodes = seedNodes;
	}

	@JsonProperty
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

	@Override
    public String toString() {
        return Objects
                .toStringHelper(this)
                .add("serviceName", serviceName)
                .add("port", port)
                .add("seedNodes", seedNodes)
                .toString();
    }

}
