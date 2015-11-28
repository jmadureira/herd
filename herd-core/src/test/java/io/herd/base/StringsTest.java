package io.herd.base;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class StringsTest {

    @Test
    public void testTrimSlashes() {
        assertEquals(null, Strings.trimSlashes(null));
        assertEquals("", Strings.trimSlashes(""));
        assertEquals("", Strings.trimSlashes("/"));
        assertEquals("", Strings.trimSlashes("//"));
        assertEquals("", Strings.trimSlashes("///"));
        assertEquals("a", Strings.trimSlashes("/a"));
        assertEquals("a", Strings.trimSlashes("a/"));
        assertEquals("a", Strings.trimSlashes("/a/"));
        assertEquals("a/b", Strings.trimSlashes("/a/b/"));
    }
}
