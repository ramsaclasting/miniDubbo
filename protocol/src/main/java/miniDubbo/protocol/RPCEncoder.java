package miniDubbo.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class RPCEncoder extends MessageToByteEncoder
{
    private Class<?> type;

    public RPCEncoder(Class<?> type)
    {
        this.type = type;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception
    {
        if(type.isInstance(o))
        {
            byte[] data = SerializationUntil.Serialize(o);
            byteBuf.writeInt(data.length);
            byteBuf.writeBytes(data);
        }
    }
}
