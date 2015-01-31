package io.herd.gossip;

import static org.junit.Assert.assertFalse;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GossipServerTest {
    
    private GossipServer server;
    private InetSocketAddress localhost;
    
    @Before
    public void setUp() throws Exception {
        this.localhost = new InetSocketAddress(InetAddress.getLocalHost(), 8080);
        this.server = new GossipServer("TestServer", 8080, new Gossiper(localhost));
    }
    
    @After
    public void tearDown() {
        this.server.stop();
        assertFalse(this.server.isRunning());
    }
    

    @Test
    public void testStartStop() {
        this.server.start();
    }
}
