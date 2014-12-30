package io.herd.gossip;

import java.util.concurrent.atomic.AtomicLong;

/**
 * A unique number version generator within the scope of this VM.
 * <p/>
 * Used to ensure that any state of this node is given an unique number.
 * <p/>
 * Only used by the gossiper and shouldn't be used anywhere else.
 */
final class VersionGenerator {

    /*
     * AtomicLong offers more guarantees than LongAdder
     */
    private static final AtomicLong version = new AtomicLong(0);

    /**
     * @return A new version after being incremented. Calling this method never returns the same value twice.
     */
    public static long nextVersion() {
        return version.incrementAndGet();
    }
}
