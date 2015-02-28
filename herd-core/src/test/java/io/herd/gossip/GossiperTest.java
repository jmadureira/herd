package io.herd.gossip;

import static org.mockito.Mockito.*;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class GossiperTest {

    private InetSocketAddress localhost;
    private Gossiper gossiper;

    @Mock
    private GossipChangeListener listener;

    @Before
    public void setUp() throws Exception {
        this.localhost = new InetSocketAddress(InetAddress.getLocalHost(), 8080);
        this.gossiper = new Gossiper(localhost);
        this.gossiper.start();
        MockitoAnnotations.initMocks(this);
        this.gossiper.addChangeListener(listener);
    }

    @After
    public void tearDown() {
        this.gossiper.stop();
    }

    @Test
    public void testNotificationOnLocalNodeChange() {
        this.gossiper.addState(ApplicationState.SERVICE_NAME, "newService");
        this.gossiper.stop();

        verify(listener).onNodeChange(eq(this.localhost), eq(ApplicationState.SERVICE_NAME),
                VersionedValueMatcher.eqVersionedValue("newService"));
    }
}
