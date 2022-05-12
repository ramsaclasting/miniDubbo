package miniDubbo.core;

public class EndPoint
{
    private final String host;
    private final int port;

    public EndPoint(String host, int port)
    {
        this.host = host;
        this.port = port;
    }

    public String getHost()
    {
        return host;
    }

    public int getPort()
    {
        return port;
    }

    public String toString()
    {
        return host+":"+port;
    }

    public boolean equals(Object other)
    {
        if(!(other instanceof EndPoint))
        {
            return false;
        }
        EndPoint _other = (EndPoint) other;
        return _other.host.equals(this.host) &&
                _other.port == this.port;
    }

    public int hashCode()
    {
        return host.hashCode() + port;
    }


}
