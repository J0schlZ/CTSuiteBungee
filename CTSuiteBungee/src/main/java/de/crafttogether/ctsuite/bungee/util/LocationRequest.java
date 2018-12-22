package de.crafttogether.ctsuite.bungee.util;

import java.util.UUID;

import de.crafttogether.ctsuite.bungee.CTSuite;
import de.crafttogether.ctsuite.bungee.messaging.NetworkMessage;
import de.crafttogether.ctsuite.bungee.messaging.NetworkMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class LocationRequest implements Listener {
	private CTSuite plugin;

	private String requestId;
	private CTPlayer target;
	private LocationResponse response;
	
	public LocationRequest(UUID uuid, LocationResponse response) {
		this.plugin = CTSuite.getInstance();
		System.out.println("request: "+ uuid);
		this.response = response;
		this.requestId = System.currentTimeMillis()+"";
		this.target = plugin.getPlayerHandler().getPlayer(uuid);
		
		if (this.target == null)
			return;
		
		this.plugin.getProxy().getPluginManager().registerListener(this.plugin, this);
		
		NetworkMessage nm = new NetworkMessage("player.request.location");
		nm.put("uuid", uuid);
		nm.put("requestId", this.requestId);
    	nm.send(target.server);
	}
	
	@EventHandler
    public void onNetworkMessage(NetworkMessageEvent ev) {
		if (!ev.getMessageKey().equalsIgnoreCase("player.response.location"))
			return;
		
		if (!((String) ev.getValue("requestId")).equals(this.requestId.toString()))
			return;
		
		CTLocation loc = null;
		loc = CTLocation.fromString((String) ev.getValue("location"));
		
		if (loc == null)
			return;
		
		this.plugin.getProxy().getPluginManager().unregisterListener(this);
		this.response.setLocation(loc);
		
		plugin.getLogger().info("[LocationResponse]: " + loc.toString());
		plugin.getProxy().getScheduler().runAsync(plugin, this.response);
	}
}
