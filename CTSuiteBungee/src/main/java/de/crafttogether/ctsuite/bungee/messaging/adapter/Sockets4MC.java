package de.crafttogether.ctsuite.bungee.messaging.adapter;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.json.simple.JSONObject;

import de.crafttogether.ctsuite.bungee.CTSuite;
import de.crafttogether.ctsuite.bungee.messaging.NetworkMessageEvent;
import de.crafttogether.ctsuite.bungee.messaging.ServerConnectedEvent;
import de.crafttogether.ctsuite.bungee.messaging.ServerDisconnectEvent;
import fr.rhaz.sockets.Connection;
import fr.rhaz.sockets.MultiSocket;

import static fr.rhaz.minecraft.sockets.Sockets4Bungee.onSocketEnable;
import static fr.rhaz.minecraft.sockets.Sockets4MC.getSocket;

public class Sockets4MC {
    private static Sockets4MC instance;
    private CTSuite plugin;
	
    public Sockets4MC(){
    	instance = this;
    	this.plugin = CTSuite.getInstance();
    	
        onSocketEnable(plugin, "default", (socket) -> {
            plugin.getLogger().info("Socket #default is available");

            socket.onReady(connection -> {
            	plugin.getLogger().info("Connection to " + connection.getTargetName() + " is available");
            	ServerConnectedEvent event = new ServerConnectedEvent(connection.getTargetName());
            	plugin.getProxy().getPluginManager().callEvent(event);
            });

            socket.onDisconnect(connection -> {
            	plugin.getLogger().info("Lost connection to " + connection.getTargetName());
            	ServerDisconnectEvent event = new ServerDisconnectEvent(connection.getTargetName());
            	plugin.getProxy().getPluginManager().callEvent(event);
            });

            socket.onMessage("ctsuite", (connection, msg) -> {
            	HashMap<String, Object> values = new HashMap<String, Object>();
            	
            	for (Entry<String, Object> entry : msg.entrySet()) {
            		if (!entry.getKey().equalsIgnoreCase("messageKey") && !entry.getKey().equalsIgnoreCase("channel"))
            			values.put(entry.getKey(), entry.getValue());
            	}
            	
            	String sender = connection.getTargetName();
            	String messageKey = msg.get("messageKey").toString();
            	
            	NetworkMessageEvent event = new NetworkMessageEvent(sender, messageKey, values);
            	plugin.getProxy().getPluginManager().callEvent(event);
            	
            	CTSuite.getInstance().getLogger().info("[NMessage] (" + sender + " -> bungee): " + messageKey);
            });
        });        
    }

    @SuppressWarnings("unchecked")
	public void send(String messageKey, String serverName, HashMap<String, Object> values) {		
        MultiSocket socket = null;
        
        socket = getSocket("default");
       
        if(socket == null) {
        	System.out.println("Socket #default is not available");
        	return;
        }
        
        Map<String, Connection> peers = socket.getPeers();
    	
    	if (serverName.equalsIgnoreCase("all")) {
    		peers = socket.getPeers();

			for (Entry<String, Connection> entry : peers.entrySet()) {
				Connection conn = entry.getValue();
				send(messageKey, conn.getTargetName(), values);
			}
			return;
		}
		
		if (serverName.equalsIgnoreCase("servers")) {
    		peers = socket.getPeers();

			for (Entry<String, Connection> entry : peers.entrySet()) {
				Connection conn = entry.getValue();
				if (conn.getTargetName().equalsIgnoreCase("proxy")) continue;
				send(messageKey, conn.getTargetName(), values);
			}
			return;
		}
		
		if (serverName.contains(",")) {
			String[] servers = serverName.split(",");
			for (String server : servers)
				send(messageKey, server, values);
			return;
		}

        Connection connection = socket.getConnection(serverName);
        
        if(connection == null) {
        	System.out.println("Connection to "+serverName+" is not available");
        	return;
        }

        JSONObject jsonObj = new JSONObject();

    	jsonObj.put("messageKey", messageKey);
        for (Entry<String, Object> entry : values.entrySet())
        	jsonObj.put(entry.getKey(), entry.getValue());
        	
        connection.msg("ctsuite", jsonObj);
        
        CTSuite.getInstance().getLogger().info("[NMessage] (bungee -> " + serverName + "): " + messageKey);
    }
    
    public static Sockets4MC getInstance() {
    	return instance;
    }
}
