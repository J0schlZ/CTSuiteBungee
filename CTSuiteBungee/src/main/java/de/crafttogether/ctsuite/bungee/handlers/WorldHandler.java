package de.crafttogether.ctsuite.bungee.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import de.crafttogether.ctsuite.bungee.CTSuite;
import de.crafttogether.ctsuite.bungee.messaging.NetworkMessageEvent;
import de.crafttogether.ctsuite.bungee.messaging.ServerConnectedEvent;
import de.crafttogether.ctsuite.bungee.messaging.ServerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class WorldHandler implements Listener {
	private CTSuite plugin;
	public ArrayList<String> server; // available server
	public HashMap<String, String> worlds; // world, server (available worlds)

	public WorldHandler() {
		this.plugin = CTSuite.getInstance();
		this.server = new ArrayList<String>();
		this.worlds = new HashMap<String, String>();
		this.plugin.getProxy().getPluginManager().registerListener(this.plugin, this);
	}
	
	@EventHandler
    public void onServerConnected(ServerConnectedEvent event) {
		if (!this.server.contains(event.getName()))
			this.server.add(event.getName());
    }
	
	@EventHandler
    public void onServerDisconnect(ServerDisconnectEvent ev) {
		if (this.server.contains(ev.getName()))
			this.server.remove(ev.getName());
    }
	
	@EventHandler
	@SuppressWarnings("unchecked")
    public void onNetworkMessage(NetworkMessageEvent ev) {
		switch(ev.getMessageKey()) {
			case "data.update.world.list":
				ArrayList<String> worlds = (ArrayList<String>) ev.getValues().get("worlds");
				for (String world : worlds) {
					String[] worldData = world.split(":");
					this.worlds.put(worldData[0], worldData[1]);
				}
				break;
				
			case "data.update.world.loaded":
				this.worlds.put((String) ev.getValues().get("world"), ev.getSender());
				break;
				
			case "data.update.world.unloaded":
				this.worlds.remove((String) ev.getValues().get("world"), ev.getSender());
				break;
		}
    }

	public String findServer(String serverName) {
		for (String server : this.server) {
			if (server.equalsIgnoreCase(serverName))
				return server;
		}
		return null;
	}

	public String findWorld(String worldName) {
		for (Entry<String, String> entry : this.worlds.entrySet()) {
			if (entry.getKey().equalsIgnoreCase(worldName))
				return entry.getKey();
		}
		return null;
	}
}
