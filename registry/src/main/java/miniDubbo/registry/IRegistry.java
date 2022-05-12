package miniDubbo.registry;

import miniDubbo.core.EndPoint;

import java.util.List;

public interface IRegistry
{
    void Register(String serviceName, int port) throws Exception;

    void UnRegister(String serviceName);

    List<EndPoint> Search(String serviceName) throws Exception;

    void Watch(ICallBackEvent callbcak);

    void KeepAlive();
}
