package miniDubbo.client;

import java.util.concurrent.ConcurrentHashMap;


//封装了一个{requestID:  RpcFuture}的Map类
public class RPCMapRequestToFuture
{
    //key:Value =>  {requestID:  RpcFuture}
    private static ConcurrentHashMap<String, RPCFuture> processingRPC = new ConcurrentHashMap<>();

    public static void put(String requestId, RPCFuture rpcFuture)
    {
        processingRPC.put(requestId, rpcFuture);
    }

    public static RPCFuture get(String requestId)
    {
        return processingRPC.get(requestId);
    }

    public static void remove(String requestId)
    {
        processingRPC.remove(requestId);
    }
}
