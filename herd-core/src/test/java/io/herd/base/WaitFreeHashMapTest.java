package io.herd.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class WaitFreeHashMapTest {

    @Test
    public void testPutGet() {
        int key = 1;
        String value = "value";
        Map<Integer, String> map = new WaitFreeHashMap<>(64);
        map.put(key, value);
        assertEquals(value, map.get(key));
    }

    @Test
    public void testPutAllGetIntKeys() {
        Map<Integer, String> originalMap = new HashMap<>(4);
        for (int i = 0; i < 100; i++) {
            originalMap.put(i, "value" + i);
        }
        Map<Integer, String> map = new WaitFreeHashMap<>(4);
        map.putAll(originalMap);
        for (int i = 0; i < 100; i++) {
            assertEquals("value" + i, map.get(i));
        }
    }

    @Test
    public void testPutAllGetLongKeys() {
        Map<Long, String> originalMap = new HashMap<>(4);
        for (long i = 0; i < 100; i++) {
            originalMap.put(i, "value" + i);
        }
        Map<Long, String> map = new WaitFreeHashMap<>(4);
        map.putAll(originalMap);
        for (long i = 0; i < 100; i++) {
            assertEquals("value" + i, map.get(i));
        }
    }

    @Test
    public void testPutAllGetDoubleKeys() {
        Map<Double, String> originalMap = new HashMap<>(4);
        for (double i = 0; i < 100; i++) {
            originalMap.put(i, "value" + i);
        }
        Map<Double, String> map = new WaitFreeHashMap<>(4);
        map.putAll(originalMap);
        for (double i = 0; i < 100; i++) {
            assertEquals("value" + i, map.get(i));
        }
    }

    @Test
    public void testPutAllGetStringKeys() {
        Map<String, String> originalMap = new HashMap<>(4);
        for (int i = 0; i < 100; i++) {
            originalMap.put("key" + i, "value" + i);
        }
        Map<String, String> map = new WaitFreeHashMap<>(4);
        map.putAll(originalMap);
        for (int i = 0; i < 100; i++) {
            assertEquals("value" + i, map.get("key" + i));
        }
    }

    @Test
    public void testPutAllContainsStringKeys() {
        Map<String, String> originalMap = new HashMap<>(4);
        for (int i = 0; i < 100; i++) {
            originalMap.put("key" + i, "value" + i);
        }
        Map<String, String> map = new WaitFreeHashMap<>(4);
        map.putAll(originalMap);
        for (int i = 0; i < 100; i++) {
            assertTrue(map.containsKey("key" + i));
        }
    }
}
