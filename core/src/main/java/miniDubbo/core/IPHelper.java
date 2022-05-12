package miniDubbo.core;

import java.net.InetAddress;

public class IPHelper
{
    public static String GetHostIP() throws Exception
    {
        String ip = InetAddress.getLocalHost().getHostAddress();
        return ip;
    }
}
