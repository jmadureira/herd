package io.herd.gossip;

import static org.junit.Assert.assertEquals;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import org.junit.Test;

public class HeartBeatStateTest {

    @Test
    public void testSerializeDeserialize() {

        HeartBeatState state = new HeartBeatState(5, 1234L);

        HeartBeatStateSerializer serializer = HeartBeatState.serializer;

        ByteBuf buffer = Unpooled.buffer();

        serializer.serialize(null, state, buffer);
        HeartBeatState newState = serializer.deserialize(null, buffer);

        assertEquals(state.generation, newState.generation);
        assertEquals(state.version, newState.version);
    }
}
