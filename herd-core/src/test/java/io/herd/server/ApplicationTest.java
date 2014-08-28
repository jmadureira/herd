package io.herd.server;

import org.junit.Test;

public class ApplicationTest {

    private class TestApplication extends Application {

        @Override
        protected void initialize() {
        }

        @Override
        protected void configure() {
            // TODO Auto-generated method stub
            
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
