package io.herd.gossip;

import static io.herd.base.Sizes.sizeOf;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An {@link EndpointState} instance holds the {@link HeartBeatState} and all the {@link ApplicationState} information.
 * 
 */
public class EndpointState {

    public static final EndpointStateSerializer serializer = new EndpointStateSerializer();

    private volatile HeartBeatState heartBeatState;
    private final Map<ApplicationState, VersionedValue> applicationState = new ConcurrentHashMap<>();

    EndpointState(HeartBeatState initialHbState) {
        this.heartBeatState = initialHbState;
    }
    
    /**
     * Adds or updates a new {@link ApplicationState} to this {@link EndpointState}.
     * 
     * @param state The state being added/updated
     * @param value The new {@link VersionedValue}
     * @return The previous {@link VersionedValue} associated with this {@link ApplicationState} or <code>null</code> if
     *         there wasn't any previous value.
     */
    VersionedValue addApplicationState(ApplicationState state, VersionedValue value) {
        return applicationState.put(state, value);
    }

    /**
     * Returns a copy of this {@link EndpointState} with only the {@link ApplicationState}s that are greater than the
     * provided version. Passing a version of 0 or less yields the same result as copying this whole
     * {@link EndpointState}.
     * 
     * @param version The baseline version to which no older {@link ApplicationState}s should be returned.
     * @return a new {@link EndpointState} instance holding only the {@link ApplicationState}s that are greater than the
     *         specified version. Returns <code>null</code> if the version is higher than this {@link EndpointState}'s
     *         highest version
     * 
     * @see EndpointState#getMaxVersion()
     */
    public EndpointState copyState(long version) {
        long localVersion = getMaxVersion();
        if (localVersion > version) {
            EndpointState reqEndpoint = new EndpointState(getHeartBeatState());
            for (Entry<ApplicationState, VersionedValue> entry : applicationState.entrySet()) {
                VersionedValue value = entry.getValue();
                if (value.getVersion() > version) {
                    final ApplicationState key = entry.getKey();
                    reqEndpoint.addApplicationState(key, value);
                }
            }
            return reqEndpoint;
        }
        return null;
    }

    Map<ApplicationState, VersionedValue> getApplicationState() {
        return applicationState;
    }

    VersionedValue getApplicationState(ApplicationState key) {
        return applicationState.get(key);
    }

    /**
     * Returns the generation of this {@link EndpointState} which is basically the generation contained on the
     * {@link EndpointState}.
     * 
     * @return This {@link EndpointState}'s generation
     * @see EndpointState
     */
    int getGeneration() {
        return heartBeatState.generation;
    }
    
    HeartBeatState getHeartBeatState() {
        return heartBeatState;
    }

    /**
     * Returns the max version of this endpoint which is either the heartbeat version or a version from one of the
     * {@link ApplicationState}.
     * 
     * @return The max version this node currently know about this endpoint
     */
    public long getMaxVersion() {
        long maxVersion = heartBeatState.version;
        for (VersionedValue value : this.applicationState.values()) {
            maxVersion = Math.max(maxVersion, value.getVersion());
        }
        return maxVersion;
    }

    @Override
    public String toString() {
        return String.format("{heatbeatState=%s, applicationState=%s}", heartBeatState, applicationState);
    }
}

final class EndpointStateSerializer implements ISerializer<EndpointState> {

    @Override
    public EndpointState deserialize(ChannelHandlerContext ctx, ByteBuf in) {

        // deserialize the heartbeat state first
        HeartBeatState hbState = HeartBeatState.serializer.deserialize(ctx, in);

        EndpointState endpointState = new EndpointState(hbState);

        // deserialize the application states
        int length = in.readInt();
        for (int i = 0; i < length; i++) {
            endpointState.addApplicationState(ApplicationState.values()[in.readInt()],
                    VersionedValue.serializer.deserialize(ctx, in));
        }

        return endpointState;
    }

    @Override
    public int length(EndpointState t) {
        int length = HeartBeatState.serializer.length(t.getHeartBeatState());

        length += sizeOf(t.getApplicationState().size());
        for (Entry<ApplicationState, VersionedValue> entry : t.getApplicationState().entrySet()) {
            length += sizeOf(entry.getKey().ordinal());
            length += VersionedValue.serializer.length(entry.getValue());
        }

        return length;
    }

    @Override
    public void serialize(ChannelHandlerContext ctx, EndpointState t, ByteBuf out) {

        // serialize the heartbeatstate
        HeartBeatState hbState = t.getHeartBeatState();
        HeartBeatState.serializer.serialize(ctx, hbState, out);

        // serialize the applicationstate
        out.writeInt(t.getApplicationState().size());
        for (Entry<ApplicationState, VersionedValue> entry : t.getApplicationState().entrySet()) {
            out.writeInt(entry.getKey().ordinal());
            VersionedValue.serializer.serialize(ctx, entry.getValue(), out);
        }
    }

}