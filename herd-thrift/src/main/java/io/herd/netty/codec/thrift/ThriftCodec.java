package io.herd.netty.codec.thrift;

import io.netty.channel.CombinedChannelDuplexHandler;

public class ThriftCodec extends CombinedChannelDuplexHandler<ThriftDecoder, ThriftEncoder> {

    public ThriftCodec(int maxFrameSize) {
        init(new ThriftDecoder(maxFrameSize), new ThriftEncoder(maxFrameSize));
    }

}
