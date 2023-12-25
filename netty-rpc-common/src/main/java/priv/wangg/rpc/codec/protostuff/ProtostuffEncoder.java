package priv.wangg.rpc.codec.protostuff;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import priv.wangg.rpc.codec.RpcEncoder;

public class ProtostuffEncoder extends RpcEncoder {

    private final ProtostuffSerializerPool pool = ProtostuffSerializerPool.getInstnce();

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
        ProtostuffSerializer serializer = pool.borrow();
        byte[] bytes = serializer.serialize(o);
        byteBuf.writeInt(bytes.length);
        byteBuf.writeBytes(bytes);
        pool.restore(serializer);
    }
}
