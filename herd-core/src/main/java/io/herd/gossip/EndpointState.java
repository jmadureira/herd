package io.herd.gossip;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * An {@link EndpointState} instance holds the {@link HeartBeatState} and all the {@link ApplicationState} information.
 * 
 * @author joaomadureira
 * 
 */
public class EndpointState {

    public static final EndpointStateSerializer serializer = new EndpointStateSerializer();
    
    private volatile HeartBeatState heartBeatState;
    
    EndpointState(HeartBeatState initialHbState) {
        this.heartBeatState = initialHbState;
    }
    
    HeartBeatState getHeartBeatState() {
        return heartBeatState;
    }
}

final class EndpointStateSerializer implements ISerializer<EndpointState> {

    @Override
    public void serialize(ChannelHandlerContext ctx, EndpointState t, ByteBuf out) {
        
        HeartBeatState hbState = t.getHeartBeatState();
        HeartBeatState.serializer.serialize(ctx, hbState, out);
    }

    @Override
    public EndpointState deserialize(ChannelHandlerContext ctx, ByteBuf in) {
        
        HeartBeatState hbState = HeartBeatState.serializer.deserialize(ctx, in);
        return new EndpointState(hbState);
    }

    @Override
    public int length(EndpointState t) {
        return HeartBeatState.serializer.length(t.getHeartBeatState());
    }
    
}