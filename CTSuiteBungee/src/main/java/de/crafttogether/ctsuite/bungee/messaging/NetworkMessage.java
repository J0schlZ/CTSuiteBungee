package de.crafttogether.ctsuite.bungee.messaging;

import java.util.HashMap;

import de.crafttogether.ctsuite.bungee.CTSuite;
import de.crafttogether.ctsuite.bungee.messaging.adapter.Sockets4MC;

public class NetworkMessage {
    private String messageKey;
    private String receiver;
    private String adapter;
    private HashMap <String, Object> values;

    public NetworkMessage(String messageKey) {
        this.messageKey = messageKey;
        this.values = new HashMap <String, Object> ();
        this.adapter = CTSuite.getInstance().getMessagingService();
    }

    public void put(String key, Object value) {
        this.values.put(key, value);
    }

    public void send(String server) {
    	this.receiver = server;
    	
    	switch(this.adapter) {
    		case "Sockets4MC": Sockets4MC.getInstance().send(this.messageKey, this.receiver, this.values); break;
    	}
    }
    
    public void setAdapter(String adapter) {
    	this.adapter = adapter;
    }
    
    public String getAdapter() {
    	return this.adapter;
    }
    
    public String getMessageKey() {
    	return this.messageKey;
    }
    
    public String getReciever() {
    	return this.receiver;
    }
    
    public HashMap<String, Object> getValues() {
    	return this.values;
    }
}