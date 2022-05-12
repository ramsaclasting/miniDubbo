package miniDubbo.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import miniDubbo.protocol.RPCRequest;
import miniDubbo.protocol.RPCResponse;

import java.lang.reflect.Method;
import java.util.Map;

public class RPCServerHandler extends SimpleChannelInboundHandler<RPCRequest>
{
    // key: package.IMyTestService
    // value: new MyTestService();
    private final Map<String, Object> handlerMap;

    public RPCServerHandler(Map<String, Object> handlerMap)
    {
        this.handlerMap = handlerMap;
    }

    //处理服务调用，处理socket的handle事件
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RPCRequest rpcRequest) throws Exception
    {
        RPCResponse response = new RPCResponse();
        response.setRequestId(rpcRequest.getRequestId());

        //通过反射机制调用方法

        //从rpcReques中获取要调用的类、方法、参数等信息
        String className = rpcRequest.getClassName();
        String methodName = rpcRequest.getMethodName();
        Class<?>[] parameterTypes = rpcRequest.getParameterTypes();
        Object[] parameters = rpcRequest.getParameters();

        //获取类实体
        Object handlerInstance = handlerMap.get(className);
        Class<?> handlerType = handlerInstance.getClass();

        //JDK反射机制调用过程
        Method method = handlerType.getMethod(methodName, parameterTypes);
        method.setAccessible(true);
        Object result = method.invoke(handlerInstance, parameters);
        response.setResult(result);
        channelHandlerContext.writeAndFlush(response);

    }



}
