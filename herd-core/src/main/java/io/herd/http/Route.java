package io.herd.http;

import java.util.regex.Pattern;

/**
 * A route wraps a {@link Handler} and the context path that {@link Handler} responds to.
 * 
 * @author joaomadureira
 *
 */
class Route {

    private Pattern context;
    private Handler handler;
    
    public Route(String contextPath, Handler handler) {
        this.context = Pattern.compile(contextPath);
        this.handler = handler;
    }
    
    public boolean matches(String uri) {
        return context.matcher(uri).matches();
    }
    
    public Handler getHandler() {
        return handler;
    }
}
