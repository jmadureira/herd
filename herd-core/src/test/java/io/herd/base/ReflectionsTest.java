package io.herd.base;

import static org.junit.Assert.assertEquals;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

public class ReflectionsTest {

    public static class Counter {

        public AtomicInteger counter = new AtomicInteger(0);

        public int count() {
            return counter.incrementAndGet();
        }
    }

    @Test
    public void testAsMethodHandle() throws Throwable {

        Counter c1 = new Counter();

        Method method = c1.getClass().getMethod("count");
        MethodHandle methodHandle = Reflections.asMethodHandle(method);

        assertEquals(0, c1.counter.get());
        int counter = (int) methodHandle.invokeExact(c1);
        assertEquals(1, c1.counter.get());
        assertEquals(counter, c1.counter.get());
    }
}
