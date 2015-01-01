package io.herd.gossip;

import io.herd.ServerRuntime;
import io.herd.base.Builder;

public class Gossip implements Builder<ServerRuntime> {
    
    private GossipConfiguration configuration;
    private final Gossiper gossiper;

    public Gossip(GossipConfiguration configuration) {
        this.configuration = configuration;
        this.gossiper = Gossiper.instance;
    }

    @Override
    public ServerRuntime build() {
        return new GossipServer(
                configuration.getServiceName(),
                configuration.getPort(),
                gossiper);
    }

}
