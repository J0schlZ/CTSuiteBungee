package de.crafttogether.ctsuite.bungee.handlers;

import java.util.UUID;

import de.crafttogether.ctsuite.bungee.CTSuite;
import de.crafttogether.ctsuite.bungee.messaging.NetworkMessage;
import de.crafttogether.ctsuite.bungee.util.CTLocation;
import de.crafttogether.ctsuite.bungee.util.CTPlayer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;

public class TeleportHandler implements Listener {
	private CTSuite plugin;
	
	public TeleportHandler() {
		this.plugin = CTSuite.getInstance();
		this.plugin.getProxy().getPluginManager().registerListener(this.plugin, this);
	}
	
	public void toLocation(UUID uuid, CTLocation loc) {
		ProxiedPlayer p = plugin.getProxy().getPlayer(uuid);
		CTPlayer ctPlayer = plugin.getPlayerHandler().getPlayer(uuid);
		
		String server = plugin.getWorldHandler().findServer(loc.getWorld());
		String world = plugin.getWorldHandler().findWorld(loc.getServer());
		
		if (p == null || ctPlayer == null || server == null || world == null)
			return;
		
		loc.setServer(server);
		loc.setWorld(world);
		
		// Send pending teleport to target server
		NetworkMessage nm = new NetworkMessage("player.set.tppos");
		nm.put("uuid", uuid);
		nm.put("location", loc.toString());
    	nm.send(server);
		
		// Connect player to target server
		if (!server.equalsIgnoreCase(ctPlayer.server))
			p.connect(plugin.getProxy().getServerInfo(server));
	}
	
	public void toPlayer(UUID playerUUID, UUID targetUUID) {
		
	}
	
	public void toSpawn(UUID uuid, String spawn) {
		if (spawn.equalsIgnoreCase("global")) {
			
		}
		
		if (spawn.startsWith("server:")) {
			
		}
		
		if (spawn.startsWith("world:")) {
			
		}
	}
	
	public void location2String() {
		
	}
	
	public void String2Location() {
		
	}
}
