package de.crafttogether.ctsuite.bungee.messaging;

import java.util.HashMap;

import net.md_5.bungee.api.plugin.Event;

public final class NetworkMessageEvent extends Event {
    private String sender;
    private String messageKey;
    private HashMap<String, Object> values;

    public NetworkMessageEvent(String sender, String messageKey, HashMap<String, Object> values) {
        this.sender = sender;
        this.messageKey = messageKey;
        this.values = values;
    }

    public String getMessageKey() {
        return this.messageKey;
    }

    public String getSender() {
        return this.sender;
    }

    public Object getValue(Object key) {
        return this.values.get(key);
    }

    public HashMap<String, Object> getValues() {
        return this.values;
    }
}