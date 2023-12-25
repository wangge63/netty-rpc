package priv.wangg.rpc.codec.kryo;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import priv.wangg.rpc.codec.RpcEncoder;

public class KryoEncoder extends RpcEncoder {

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        KryoSerializer serializer = new KryoSerializer();
        byte[] bytes = serializer.serialize(msg);
        out.writeInt(bytes.length);
        out.writeBytes(bytes);
    }
}
