package miniDubbo.core.loadbalance;

import java.util.Map;

public class SelectFirstLoadBalance implements ILoadBalance
{
    @Override
    public int select(Map<String, String> config, int amount) throws Exception
    {
        return 0;
    }
}
