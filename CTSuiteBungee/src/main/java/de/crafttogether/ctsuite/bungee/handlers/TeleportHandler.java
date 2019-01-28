package de.crafttogether.ctsuite.bungee.handlers;

import java.util.HashMap;
import java.util.UUID;

import de.crafttogether.ctsuite.bungee.CTSuite;
import de.crafttogether.ctsuite.bungee.messaging.NetworkMessage;
import de.crafttogether.ctsuite.bungee.util.CTLocation;
import de.crafttogether.ctsuite.bungee.util.CTPlayer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;

public class TeleportHandler implements Listener {
	private CTSuite plugin;
	
	public TeleportHandler() {
		this.plugin = CTSuite.getInstance();
		this.plugin.getProxy().getPluginManager().registerListener(this.plugin, this);
	}
	
	public void toLocation(UUID uuid, CTLocation ctLoc) {
		CTPlayer ctPlayer = plugin.getPlayerHandler().getPlayer(uuid);
		
		String serverName = plugin.getWorldHandler().findServer(ctLoc.getServer());
		String worldName = plugin.getWorldHandler().findWorld(ctLoc.getWorld());
		
		if (ctPlayer == null || serverName == null || worldName == null)
			return;
		
		ctLoc.setServer(serverName);
		ctLoc.setWorld(worldName);
		
		// Send pending teleport to target server
		NetworkMessage nm = new NetworkMessage("player.teleport.location");
		nm.put("uuid", uuid);
		nm.put("location", ctLoc.toString());
    	nm.send(serverName);
		
		// Connect player to target server
		if (!serverName.equalsIgnoreCase(ctPlayer.server)) {
			ProxiedPlayer p = plugin.getProxy().getPlayer(uuid);
			if (p != null) {
				ServerInfo server = plugin.getProxy().getServerInfo(serverName);
				
	        	if (server != null)
	        		p.connect(server);
	        	else {
					HashMap<String, String> placeHolders = new HashMap<String, String>();
					placeHolders.put("player", ctPlayer.name);
					placeHolders.put("server", serverName);
					plugin.getMessageHandler().send(p, plugin.getMessageHandler().getMessage("info.connFailed", placeHolders));
	        	}
			}
		}
	}
	
	public void toPlayer(UUID playerUUID, UUID targetUUID) {
		CTPlayer ctPlayer = plugin.getPlayerHandler().getPlayer(playerUUID);
		CTPlayer ctTarget = plugin.getPlayerHandler().getPlayer(targetUUID);

		if (ctPlayer == null || ctTarget == null || !ctTarget.isOnline)
			return;
		
		// Send pending teleport to target server
		NetworkMessage nm = new NetworkMessage("player.teleport.player");
		nm.put("playerUUID", playerUUID);
		nm.put("targetUUID", targetUUID);
    	nm.send(ctPlayer.server);
		
		// Connect player to target server
		if (!ctPlayer.server.equalsIgnoreCase(ctTarget.server)) {
			ProxiedPlayer p = plugin.getProxy().getPlayer(playerUUID);
			
			if (p != null) {
				ServerInfo server = plugin.getProxy().getServerInfo(ctPlayer.server);
				
	        	if (server != null)
	        		p.connect(server);
	        	else {
					HashMap<String, String> placeHolders = new HashMap<String, String>();
					placeHolders.put("player", ctPlayer.name);
					placeHolders.put("server", ctPlayer.server);
					plugin.getMessageHandler().send(p, plugin.getMessageHandler().getMessage("info.connFailed", placeHolders));
	        	}
			}
		}
	}
	
	public void toSpawn(UUID uuid, String spawn) {
		if (spawn.equalsIgnoreCase("global")) {
			
		}
		
		if (spawn.startsWith("server:")) {
			
		}
		
		if (spawn.startsWith("world:")) {
			
		}
	}
}
