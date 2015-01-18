package io.herd.gossip;

import static org.junit.Assert.assertFalse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GossipServerTest {
    
    private GossipServer server;
    
    @Before
    public void setUp() {
        this.server = new GossipServer("TestServer", 8080, new Gossiper());
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
