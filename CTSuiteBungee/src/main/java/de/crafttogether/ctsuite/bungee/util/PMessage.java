package de.crafttogether.ctsuite.bungee.util;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import de.crafttogether.ctsuite.bungee.CTSuite;
import net.md_5.bungee.api.config.ServerInfo;

public class PMessage {
	private CTSuite main;
	
	private String messageName;
	private ArrayList<String> values;
	
	public PMessage(CTSuite main, String name) {
		this.main = main;
		this.messageName = name;
		values = new ArrayList<String>();
	}
	
	public void put(String value) {
		values.add(value);
	}
	
	public void send(String serverName) {
		ServerInfo serverInfo = null;
		try { serverInfo = main.getProxy().getServers().get(serverName); } catch (Exception e) { }
		final ServerInfo serverInstance = serverInfo;
		
		if (serverInstance != null) {
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
	                    serverInstance.sendData("ctsuite:bukkit", b.toByteArray());
	                    
	                    System.out.println("[PMessage][Bungee->" + serverName + "]: " + messageName);
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
			System.out.println("[PMessage]: Unkown Server '" + serverName + "' Unable to send this message! [" + messageName + "]");
		}
	}
}
