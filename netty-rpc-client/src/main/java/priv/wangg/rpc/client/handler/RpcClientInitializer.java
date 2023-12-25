package priv.wangg.rpc.client.handler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import priv.wangg.rpc.codec.Serializer;
import priv.wangg.rpc.codec.protostuff.ProtostuffDecoder;
import priv.wangg.rpc.codec.protostuff.ProtostuffEncoder;
import priv.wangg.rpc.codec.protostuff.ProtostuffSerializer;
import priv.wangg.rpc.model.Beat;
import priv.wangg.rpc.model.RpcResponse;

import java.util.concurrent.TimeUnit;

public class RpcClientInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        Serializer serializer = new ProtostuffSerializer();
        ChannelPipeline cp = ch.pipeline();
        cp.addLast(new IdleStateHandler(0, 0, Beat.BEAT_INTERVAL, TimeUnit.SECONDS));
        cp.addLast(new ProtostuffEncoder());
        cp.addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0));
        cp.addLast(new ProtostuffDecoder(RpcResponse.class));
        cp.addLast(new RpcClientHandler());
    }
}
