package io.herd.base;

/**
 * General exception for problems related to configuration.
 * 
 * @author joaomadureira
 *
 */
public class ConfigurationException extends RuntimeException {

    private static final long serialVersionUID = 2284585899571128470L;

    public ConfigurationException(Throwable t) {
        super(t);
    }

}
