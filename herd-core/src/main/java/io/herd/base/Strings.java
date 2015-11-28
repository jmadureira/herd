package io.herd.base;

/**
 * Static utility methods pertaining to {@code String} or {@code CharSequence} instances.
 */
public final class Strings {

    public static boolean isEmpty(String string) {
        return string == null || string.isEmpty();
    }

    public static boolean isNotEmpty(String string) {
        return !isEmpty(string);
    }

    public static String trim(String path, char ch) {
        if (path == null || path.isEmpty()) {
            return path;
        }
        int st = 0;
        while (st < path.length() && path.charAt(st) == ch) {
            st++;
        }
        int len = path.length() - 1;
        while (len > st && path.charAt(len) == ch) {
            len--;
        }
        return path.substring(st, len + 1);
    }

    public static String trimSlashes(String path) {
        return trim(path, '/');
    }

    private Strings() {

    }
}
