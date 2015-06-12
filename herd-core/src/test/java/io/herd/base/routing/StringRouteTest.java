package io.herd.base.routing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.collect.ImmutableMap;

@RunWith(Parameterized.class)
public class StringRouteTest {

    private String input;
    private String pattern;
    private boolean success;
    private Map<String, String> attributes;

    public StringRouteTest(String input, String pattern, boolean success, Map<String, String> attributes) {
        this.input = input;
        this.pattern = pattern;
        this.success = success;
        this.attributes = attributes;
    }

    @Test
    public void test() {
        if (success) {
            assertTrue(Routes.forString(pattern, "success").route(input).match());
            assertEquals("success", Routes.forString(pattern, "success").route(input).getTarget());
            assertEquals(attributes, Routes.forString(pattern, "success").route(input).getAttributes());
        } else {
            assertFalse(Routes.forString(pattern, "success").route(input).match());
        }
    }

    @Parameters(name = "{0} - {1} - {2}")
    public static Collection<Object[]> data() {
        return Arrays
                .asList(new Object[][] {
                        { "", "", true, Collections.emptyMap() },
                        { "/", "/", true, Collections.emptyMap() },
                        { "/a/", "/a/", true, Collections.emptyMap() },
                        { "/a", "a/", true, Collections.emptyMap() },
                        { "a/", "/a", true, Collections.emptyMap() },
                        { "a/b", "a/b", true, Collections.emptyMap() },
                        { "a/c", "a/b", false, null },
                        { "a/b/c", "a/b", false, null },
                        { "a/b/c", "a/b/:*", true, Collections.emptyMap() },
                        { "a/b/c", ":*", true, Collections.emptyMap() },
                        { "a/b/c", "/a/:id", false, null },
                        { "a/b/c", "/a/:id/c", true, ImmutableMap.builder().put("id", "b").build() },
                        { "a/b/c", "/:a/:b/:c", true,
                                ImmutableMap.builder().put("a", "a").put("b", "b").put("c", "c").build() },
                        { "a/b/c", "/a/*path", true, ImmutableMap.builder().put("path", "b/c").build() },
                        { "a/b/c/d", "/:a/b/*path", true,
                                ImmutableMap.builder().put("a", "a").put("path", "c/d").build() } });
    }
}
