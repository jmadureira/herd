package io.herd.base.routing;

import static io.herd.base.Strings.isEmpty;
import static io.herd.base.Strings.trimSlashes;

public class StringRoute<Output> extends AbstractRoute<String, Output> {

    public StringRoute(String pattern, Output target) {
        super(pattern, target);
    }

    @Override
    protected String[] getTokens(String input) {
        String string = trimSlashes(input);
        return isEmpty(string) ? new String[0] : string.split("/");
    }

}
