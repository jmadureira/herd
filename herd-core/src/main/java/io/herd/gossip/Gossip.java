package io.herd.gossip;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import io.herd.ServerRuntime;
import io.herd.base.Builder;
import io.herd.base.Interwebs;
import io.herd.base.ServerRuntimeException;

public class Gossip implements Builder<ServerRuntime> {

    private GossipConfiguration configuration;

    public Gossip(GossipConfiguration configuration) {
        this.configuration = configuration;
    }

    private boolean isNotLocalInstance(InetSocketAddress address) {
        return address.getPort() != configuration.getPort() || Interwebs.isNotLocalHost(address);
    }

    @Override
    public ServerRuntime build() {
        try {
            Gossiper gossiper = new Gossiper(new InetSocketAddress(InetAddress.getLocalHost(), configuration.getPort()));
            configuration.getSeedNodes()
                    .stream()
                    .map(Interwebs::toSocketAddress)
                    .filter(this::isNotLocalInstance)
                    .forEach(gossiper::addSeedNode);

            return new GossipServer(configuration.getServiceName(), configuration.getPort(), gossiper);
        } catch (UnknownHostException e) {
            throw new ServerRuntimeException(e);
        }
    }

}
