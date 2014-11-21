package io.herd.base;

import static java.lang.invoke.MethodHandles.publicLookup;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

/**
 * Collection of utility methods to work with reflection API.
 * 
 * @author joaomadureira
 *
 */
public final class Reflections {

    /**
     * Convenience method around {@link MethodHandles.Lookup#unreflect(Method)} which creates direct
     * {@link MethodHandle} to a {@link Method}.
     * 
     * @param method The target {@link Method}.
     * @return a {@link MethodHandle} which can invoke the reflected method.
     * @throws NullPointerException exception if no {@link Method} was provided.
     * @throws RuntimeException if access checking fails or if the method's variable arity modifier bit is set and
     *             asVarargsCollector fails
     * @see MethodHandles.Lookup#unreflect(Method)
     */
    public static final MethodHandle asMethodHandle(Method method) {
        Preconditions.checkNotNull(method, "Method must not be null");
        try {
            return publicLookup().unreflect(method);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static final Method findAnnotatedMethod(Class<? extends Annotation> annotation, Class<?> clazz) {
        Preconditions.checkNotNull(annotation, "Annotation must not be null");
        Preconditions.checkNotNull(clazz, "Class must not be null");
        Class<?> searchClass = clazz;
        while (searchClass != null) {
            for (Method method : searchClass.getDeclaredMethods()) {
                if (method.getDeclaredAnnotation(annotation) != null) {
                    return method;
                }
            }
            searchClass = searchClass.getSuperclass();
        }
        return null;
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

    private Reflections() {

    }

}
