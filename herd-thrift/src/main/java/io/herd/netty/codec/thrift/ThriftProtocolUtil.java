package io.herd.netty.codec.thrift;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TJSONProtocol;
import org.apache.thrift.protocol.TProtocolFactory;

/**
 * Utility class containing static methods used when dealing with thrift code.
 * 
 * @author joaomadureira
 *
 */
final class ThriftProtocolUtil {

    private static final short LBRACKET = 0x5b;
    private static final short RBRACKET = 0x5d;

    /**
     * Tries to guess the protocol used to read the contents of the give buffer. If unable to determine the protocol it
     * will default to {@link TBinaryProtocol.Factory}.
     * 
     * @param holder A holder of a {@link ByteBuf} containing the data.
     * @return A protocol needed to read the contents of the buffer.
     * @see #guessProtocolFactory(ByteBufHolder, TProtocolFactory)
     */
    public static final TProtocolFactory guessProtocolFactory(ByteBufHolder holder) {
        return guessProtocolFactory(holder, new TBinaryProtocol.Factory(true, true));
    }

    /**
     * Tries to guess the protocol used to read the contents of the give buffer. If unable to determine the protocol it
     * will return the fallback {@link TProtocolFactory} factory.
     * 
     * @param holder A holder of a {@link ByteBuf} containing the data.
     * @param defaultFactory
     * @return A protocol needed to read the contents of the buffer.
     */
    public static final TProtocolFactory guessProtocolFactory(ByteBufHolder holder, TProtocolFactory defaultFactory) {

        ByteBuf buf = holder.content();
        short firstByte = buf.getUnsignedByte(0);
        short lastByte = buf.getUnsignedByte(buf.readerIndex() + buf.readableBytes() - 1);

        /*
         * If the first and last bytes are opening/closing brackets we guess the protocol as being TJSONProtocol.
         */
        if (firstByte == LBRACKET && lastByte == RBRACKET) {
            return new TJSONProtocol.Factory();
        }

        if (firstByte == 0x80) {
            return new TBinaryProtocol.Factory(true, true);
        }

        /*
         * A first byte of value > 16 indicates TCompactProtocol was used, and the first byte encodes a delta field id
         * (id <= 15) and a field type.
         */
        if (firstByte > 0x10) {
            return new TCompactProtocol.Factory();
        }

        return defaultFactory;
    }

    private ThriftProtocolUtil() {

    }
}
