package miniDubbo.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import miniDubbo.protocol.RPCResponse;


public class RPCClientHandler extends SimpleChannelInboundHandler<RPCResponse>
{
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RPCResponse rpcResponse) throws Exception
    {
        String requestId = rpcResponse.getRequestId();
        RPCFuture future = RPCMapRequestToFuture.get(requestId);
        if(future != null)
        {
            RPCMapRequestToFuture.remove(requestId);
            future.done(rpcResponse);
        }
    }
}
