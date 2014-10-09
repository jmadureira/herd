package io.herd.monitoring;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Function;
import java.util.function.Predicate;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import com.lmax.disruptor.EventHandler;

public class DefaultEventEmitterTest {

    private DefaultEventEmitter emitter;

    @Before
    public void setUp() {
        this.emitter = new DefaultEventEmitter();
    }

    @After
    public void tearDown() {
        this.emitter.stop();
    }

    @Test
    public void testStartStop() {
        this.emitter.start();
        assertTrue(this.emitter.isRunning());
    }

    @Test
    public void testEmit() {
        final List<Event> eventList = new ArrayList<>();
        EventHandler<Event> eventHandler = new EventHandler<Event>() {

            @Override
            public void onEvent(Event event, long sequence, boolean endOfBatch) throws Exception {
                eventList.add(event);
            }

        };
        this.emitter.withEventHandlers(eventHandler);
        this.emitter.start();
        this.emitter.emit(new Event("event", 123.56, true));
        assertTrue(waitForCondition((v) -> {
            return eventList.size() != 0;
        }));
        Event event = eventList.get(0);
        assertEquals("event", event.getId());
        assertEquals(123.56, event.getElapsedTime(), 0.0);
        assertTrue(event.isFailure());
    }
    
    private boolean waitForCondition(Predicate<Void> function) {
        int tries = 50;
        while (tries-- > 0) {
            if (function.test(null)) {
                return true;
            }
            LockSupport.parkNanos(1000);
        }
        return false;
    }
}
