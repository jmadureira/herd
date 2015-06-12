package io.herd.base.routing;

import static io.herd.base.Preconditions.checkNotNull;
import static io.herd.base.Strings.isEmpty;
import static io.herd.base.Strings.trimSlashes;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Joiner;

abstract class AbstractRoute<Input, Output> implements Route<Input, Output> {

    static class SimpleRouted<Output> implements Routed<Output> {

        private final Map<String, String> attributes;
        private final Output target;

        SimpleRouted(Map<String, String> attributes, Output target) {
            this.attributes = attributes;
            this.target = target;
        }

        @Override
        public Map<String, String> getAttributes() {
            return attributes;
        }

        @Override
        public Output getTarget() {
            return target;
        }

        @Override
        public boolean match() {
            return true;
        }
    }

    private final Output target;
    private final String path;
    private final String[] tokens;

    public AbstractRoute(String pattern, Output target) {
        this.path = trimSlashes(checkNotNull(pattern, "Cannot create a route using a null pattern"));
        this.tokens = isEmpty(path) ? new String[0] : path.split("/");
        this.target = target;
    }

    @Override
    public String getPattern() {
        return path;
    }

    protected abstract String[] getTokens(Input input);

    @SuppressWarnings("unchecked")
    @Override
    public Routed<Output> route(Input input) {
        String[] ts = getTokens(input);
        Map<String, String> attributes = new HashMap<>();
        // match empty route
        if (ts.length == 0 && tokens.length == 0) {
            return new SimpleRouted<Output>(attributes, target);
        }
        int index = 0;
        while (index < tokens.length) {
            if (ts.length <= index) {
                return FAILED;
            }
            if (tokens[index].charAt(0) == ':') {
                if (":*".equals(tokens[index])) {
                    return new SimpleRouted<Output>(attributes, target);
                } else {
                    attributes.put(tokens[index].replaceFirst(":", ""), ts[index]);
                }
            } else if (tokens[index].charAt(0) == '*' && tokens[index].length() != 1) {
                attributes.put(tokens[index].replaceFirst("\\*", ""),
                        Joiner.on('/').join(Arrays.copyOfRange(ts, index, ts.length)));
                return new SimpleRouted<Output>(attributes, target);
            } else {
                if (!tokens[index].equals(ts[index])) {
                    return FAILED;
                }
            }
            index++;
        }
        if (ts.length != tokens.length) {
            return FAILED;
        }
        return new SimpleRouted<Output>(attributes, target);
    }

    @Override
    public String toString() {
        return String.format("{ path: '%s', target: '%s' }", path, target);
    }
}
