package io.herd.base.routing;


public final class Routes {

    public static <Output> Route<String, Output> forString(String pattern, Output target) {
        return new StringRoute<Output>(pattern, target);
    }
    
}
