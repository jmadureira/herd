package io.herd.eventbus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.locks.LockSupport;
import java.util.function.Predicate;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DisruptorEventBusTest {

    private DisruptorEventBus eventBus;
    
    public static class Listener {
        
        private volatile String event;
        
        @Subscribe
        public void onEvent(String event) {
            this.event = event;
        }
    }

    @Before
    public void setUp() {
        this.eventBus = new DisruptorEventBus();
    }

    @After
    public void tearDown() {
        this.eventBus.stop();
    }

    @Test
    public void testStartStop() {
        this.eventBus.start();
        assertTrue(this.eventBus.isRunning());
    }

    @Test
    public void testEmit() {
        
        Listener l = new Listener();

        this.eventBus.subscribe(l);
        this.eventBus.start();
        
        this.eventBus.emit("Hello");
        
        assertTrue(waitForCondition((v) -> {
            return "Hello".equals(l.event);
        }));
        assertEquals("Hello", l.event);
    }
    
    private boolean waitForCondition(Predicate<Void> function) {
        int tries = 50;
        while (tries-- > 0) {
            if (function.test(null)) {
                return true;
            }
            LockSupport.parkNanos(100000);
        }
        return false;
    }
}
