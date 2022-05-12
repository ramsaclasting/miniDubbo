package miniDubbo.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class RPCDecoder extends ByteToMessageDecoder
{
    protected Class<?> type;

    public RPCDecoder(Class<?> type)
    {
        this.type = type;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception
    {
        //如果读不出一个int，为下面readint做铺垫
        if(byteBuf.readableBytes() < 4)
        {
            return;
        }
        //标记readindex
        byteBuf.markReaderIndex();

        //读数据大小
        int dataLength = byteBuf.readInt();
        if(byteBuf.readableBytes() < dataLength)
        {
            byteBuf.resetReaderIndex();
            return;
        }

        byte[] data = new byte[dataLength];
        byteBuf.readBytes(data);

        Object obj = SerializationUntil.Deserialize(data, type);
        list.add(obj);
    }

}
