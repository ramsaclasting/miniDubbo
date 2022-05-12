package miniDubbo.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import miniDubbo.protocol.RPCDecoder;
import miniDubbo.protocol.RPCEncoder;
import miniDubbo.protocol.RPCRequest;
import miniDubbo.protocol.RPCResponse;

import java.util.Map;

public class RPCServerInitializer extends ChannelInitializer<SocketChannel>
{
    private final Map<String, Object> handlerMap;

    public RPCServerInitializer(Map<String, Object> handlerMap)
    {
        this.handlerMap = handlerMap;
    }

    //初始化管道
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception
    {
        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline.addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0));
        pipeline.addLast(new RPCDecoder(RPCRequest.class));
        pipeline.addLast(new RPCEncoder(RPCResponse.class));
        pipeline.addLast(new RPCServerHandler(handlerMap));
    }
}
