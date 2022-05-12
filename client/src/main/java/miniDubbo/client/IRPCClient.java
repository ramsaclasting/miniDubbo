package miniDubbo.client;

public interface IRPCClient
{
    <T> T creat(Class<T> type) throws Exception;
}
