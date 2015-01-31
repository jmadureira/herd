package io.herd.base;

public class ServerRuntimeException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = -6167577699643521996L;
    
    public ServerRuntimeException(Throwable t) {
        super(t);
    }

    public ServerRuntimeException(String message) {
        super(message);
    }

}
