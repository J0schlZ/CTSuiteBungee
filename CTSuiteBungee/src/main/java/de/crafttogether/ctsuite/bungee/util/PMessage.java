package de.crafttogether.ctsuite.bungee.util;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import de.crafttogether.ctsuite.bungee.CTSuite;
import net.md_5.bungee.api.config.ServerInfo;

public class PMessage {
	private CTSuite main;
	
	private String messageName;
	private ArrayList<String> values;
	
	public PMessage(CTSuite main, String name) {
		this.main = main;
		this.messageName = name;
		this.values = new ArrayList<String>();
	}
	
	public void put(String value) {
		this.values.add(value);
	}
	
	public void send(ServerInfo server) {
		if (server != null) {			
			main.getProxy().getScheduler().runAsync(main, new Runnable() {
	    		@Override
	    		public void run() {
	                ByteArrayOutputStream b = new ByteArrayOutputStream();
	                DataOutputStream out = new DataOutputStream(b);
	                
	                try {                        
	                    out.writeUTF(messageName);
	                    for(int i = 0; i < values.size(); i++)
	    	        		out.writeUTF(values.get(i));
	                    
	                    b.close();
	                    out.close();
	                    server.sendData("ctsuite:bukkit", b.toByteArray());
	                    
	                    System.out.println("[PMessage][Bungee->" + server.getName() + "]: " + messageName);
	                } catch (IOException e) {
	                    e.printStackTrace();
	                } finally {
						try {
							b.close();
							out.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
	                }
	    		}
	    	});
		}
		else {
			System.out.println("[PMessage]: Unable to send this message! [" + messageName + "]");
		}
	}
	
	public void send(String serverName) {
		ServerInfo server = null;
		try { server = main.getProxy().getServers().get(serverName); } catch (Exception e) { }
		this.send(server);
	}
	
	public void sendAll() {
    	Map<String, ServerInfo> servers = main.getProxy().getServers();
    	for (String server : servers.keySet())
    		this.send(server);
	}
}
