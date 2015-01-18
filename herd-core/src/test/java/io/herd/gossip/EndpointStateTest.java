package io.herd.gossip;

import static org.junit.Assert.assertEquals;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import org.junit.Test;

public class EndpointStateTest {

    @Test
    public void testSerializeDeserialize() {

        HeartBeatState heartBeatState = new HeartBeatState(5, 1234L);
        EndpointState endpointState = new EndpointState(heartBeatState);

        endpointState.addApplicationState(ApplicationState.SERVICE_NAME, new VersionedValue("Hello"));

        EndpointStateSerializer serializer = EndpointState.serializer;

        ByteBuf buffer = Unpooled.buffer();

        serializer.serialize(null, endpointState, buffer);
        EndpointState newState = serializer.deserialize(null, buffer);

        assertEquals(endpointState.getHeartBeatState().generation, newState.getHeartBeatState().generation);
        assertEquals(endpointState.getHeartBeatState().version, newState.getHeartBeatState().version);
        assertEquals(endpointState.getApplicationState(ApplicationState.SERVICE_NAME).getValue(), newState
                .getApplicationState(ApplicationState.SERVICE_NAME).getValue());
        assertEquals(endpointState.getApplicationState(ApplicationState.SERVICE_NAME).getVersion(), newState
                .getApplicationState(ApplicationState.SERVICE_NAME).getVersion());
    }
}
