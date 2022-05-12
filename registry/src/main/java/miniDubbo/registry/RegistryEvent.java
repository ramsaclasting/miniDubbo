package miniDubbo.registry;

import miniDubbo.core.Tuple;

public class RegistryEvent
{
    public miniDubbo.core.Tuple<String, String> PreTuple = new Tuple<>();
    public Tuple<String, String> Tuple = new Tuple<>();

    public Tuple<String, String> getPreTuple()
    {
        return PreTuple;
    }

    public Tuple<String, String> getTuple()
    {
        return Tuple;
    }

    //事件类型枚举
    public static enum EventType
    {
        PUT,
        DELETE,
        UNRECOGNIZED;

        private EventType(){}
    }
    private EventType eventType = EventType.UNRECOGNIZED;
    public EventType getEventType(){return eventType;}

    //注册事件构造器类
    public static RegistryEventBuilder Builder(){return new RegistryEventBuilder();}
    public static class RegistryEventBuilder
    {
        private RegistryEvent registryEvent = new RegistryEvent();

        public RegistryEventBuilder preKey(String key)
        {
            registryEvent.PreTuple.setKey(key);
            return this;
        }

        public RegistryEventBuilder preValue(String value)
        {
            registryEvent.PreTuple.setValue(value);
            return this;
        }

        public RegistryEventBuilder key(String key)
        {
            registryEvent.Tuple.setKey(key);
            return this;
        }

        public RegistryEventBuilder value(String value)
        {
            registryEvent.Tuple.setValue(value);
            return this;
        }

        public RegistryEventBuilder eventType(EventType eventType)
        {
            registryEvent.eventType = eventType;
            return this;
        }

        public RegistryEvent build()
        {
            return registryEvent;
        }
    }

}
