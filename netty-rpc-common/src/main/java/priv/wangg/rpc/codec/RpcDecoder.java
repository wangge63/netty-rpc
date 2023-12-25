package priv.wangg.rpc.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class RpcDecoder extends ByteToMessageDecoder {

    private static final Logger logger = LoggerFactory.getLogger(RpcDecoder.class);

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        if (byteBuf.readableBytes() < 4) {
            return;
        }

        byteBuf.markReaderIndex();
        int dataLength = byteBuf.readInt();

        if (byteBuf.readableBytes() < dataLength) {
            byteBuf.resetReaderIndex();
            return;
        }

        byte[] data = new byte[dataLength];
        byteBuf.readBytes(data);

        Object obj = null;
        try {
            obj = deserialize(data);
            list.add(obj);
        }catch (Exception e) {
            logger.error("Decoder error: " + e.getMessage());
            throw e;
        }
    }

    protected abstract Object deserialize(byte[] data);
}