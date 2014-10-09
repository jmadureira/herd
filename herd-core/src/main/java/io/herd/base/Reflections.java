package io.herd.base;

import io.herd.monitoring.Timed;

import java.lang.reflect.Method;

/**
 * Collection of utility methods to work with reflection API.
 * 
 * @author joaomadureira
 *
 */
public final class Reflections {

    private Reflections() {

    }

    /**
     * Attempts to find a {@link Method} with the specified <code>name</code> on the given <code>clazz</code>. If not
     * found it will search upwards on the hierarchy until the method is found (or not).
     * 
     * @param methodName The name of the method being searched.
     * @param clazz The target class.
     * @return The {@link Method} with the given name or <code>null</code> if the method wasn't found.
     */
    public static final Method findMethod(String methodName, Class<?> clazz) {
        Preconditions.checkNotEmpty(methodName, "Method name must be valid.");
        Preconditions.checkNotNull(clazz, "Class must not be null");
        Class<?> searchClass = clazz;
        while (searchClass != null) {
            for (Method method : searchClass.getDeclaredMethods()) {
                if (methodName.equals(method.getName())) {
                    return method;
                }
            }
            searchClass = searchClass.getSuperclass();
        }
        return null;
    }

}
