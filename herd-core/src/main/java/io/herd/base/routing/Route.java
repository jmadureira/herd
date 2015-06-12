package io.herd.base.routing;

import java.util.Map;

public interface Route<Input, Output> {
    
    interface Routed<Output> {

        Map<String, String> getAttributes();

        Output getTarget();

        boolean match();
    }

    @SuppressWarnings("rawtypes")
    Routed FAILED = new Routed() {

        @Override
        public Map<String, String> getAttributes() {
            return null;
        }

        @Override
        public Object getTarget() {
            return null;
        }

        @Override
        public boolean match() {
            return false;
        }

    };

    String getPattern();

    Routed<Output> route(Input input);
}