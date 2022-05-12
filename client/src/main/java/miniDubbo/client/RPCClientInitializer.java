package miniDubbo.client;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import miniDubbo.protocol.RPCDecoder;
import miniDubbo.protocol.RPCEncoder;
import miniDubbo.protocol.RPCRequest;
import miniDubbo.protocol.RPCResponse;

public class RPCClientInitializer extends ChannelInitializer<SocketChannel>
{
    //初始话管道
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception
    {
        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline.addLast(new RPCEncoder(RPCRequest.class));
        pipeline.addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4,0,0));
        pipeline.addLast(new RPCDecoder(RPCResponse.class));
        pipeline.addLast(new RPCClientHandler());

    }
}
