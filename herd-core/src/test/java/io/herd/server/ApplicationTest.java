package io.herd.server;

import io.herd.base.Configuration;

import org.junit.Test;

public class ApplicationTest {
    
    public static class TestConfiguration extends Configuration {
        
    }

    public class TestApplication extends Application<TestConfiguration> {

        @Override
        protected void initialize(TestConfiguration configuration) {
        }

    }

    @Test(expected = NullPointerException.class)
    public void testRunWithNullArgs() {

        TestApplication myApp = new TestApplication();
        myApp.run(null);
    }

    @Test
    public void testRun() {

        TestApplication myApp = new TestApplication();
        myApp.run(new String[] {});
    }
}
