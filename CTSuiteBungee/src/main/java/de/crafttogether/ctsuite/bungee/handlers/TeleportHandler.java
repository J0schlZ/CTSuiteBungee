package de.crafttogether.ctsuite.bungee.handlers;

import java.util.HashMap;
import java.util.UUID;

import de.crafttogether.ctsuite.bungee.CTSuite;
import de.crafttogether.ctsuite.bungee.messaging.NetworkMessage;
import de.crafttogether.ctsuite.bungee.util.CTPlayer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;

public class TeleportHandler implements Listener {
	private CTSuite plugin;
    public HashMap<UUID, String> uuids; // uuid, name
    public HashMap<UUID, CTPlayer> players; // all players
	
	public TeleportHandler() {
		this.plugin = CTSuite.getInstance();
		this.uuids = new HashMap<UUID, String>();
		this.players = new HashMap<UUID, CTPlayer>();
		this.plugin.getProxy().getPluginManager().registerListener(this.plugin, this);
	}
	
	public void onPlayerTPPos(UUID uuid, Double x, Double y, Double z, String world, String server, Float yaw, Float pitch) {
		ProxiedPlayer p = plugin.getProxy().getPlayer(uuid);
		
		CTPlayer ctPlayer = plugin.getPlayerHandler().getPlayer(uuid);
		System.out.println("tppos " + (p == null ? "no" : "yes") + " " + (ctPlayer == null ? "no" : "yes"));
		
		if (p == null || ctPlayer == null)
			return;
		
		System.out.println("Teleport player " + ctPlayer.name + " to:");
		System.out.println(x + ", " + y + ", " + z + ", " + world + ", " + server + ", " + yaw + ", " + pitch);
		
		// Send pending teleport to target server
		NetworkMessage nm = new NetworkMessage("player.set.tppos");
		nm.put("uuid", uuid);
		nm.put("x", x);
    	nm.put("y", y);
    	nm.put("z", z);
    	nm.put("world", world);
    	nm.put("yaw", "" + yaw);
    	nm.put("pitch", "" + pitch);
    	nm.send(server);
		
		// Connect player to target server
		if (!server.equals(ctPlayer.server))
			p.connect(plugin.getProxy().getServerInfo(server));
	}
}
