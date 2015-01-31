package io.herd.base;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class EqualityTest {

    @Test
    public void testAlwaysTrue() {
        assertTrue(Equality.from().append(1L, 1L).append("hello", "hello").isEquals());
    }
    
    @Test
    public void testAlwaysFalse() {
        assertFalse(Equality.from(false).append(1L, 1L).append("hello", "hello").isEquals());
    }
}
