package io.herd.thrift;

import io.netty.channel.CombinedChannelDuplexHandler;

public class ThriftFrameCodec extends CombinedChannelDuplexHandler<ThriftFrameDecoder, ThriftFrameEncoder> {

    public ThriftFrameCodec(int maxFrameSize) {
        init(new ThriftFrameDecoder(maxFrameSize), new ThriftFrameEncoder(maxFrameSize));
    }

}
