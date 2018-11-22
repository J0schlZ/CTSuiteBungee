package de.crafttogether.ctsuite.bungee.util;

import de.crafttogether.ctsuite.bungee.CTSuite;
import de.crafttogether.ctsuite.bungee.handlers.PlayerHandler;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class CTPlayer {
	public String uuid = null;
	public String name = null;
	public String nickname = null;
	public String server = null;
	public String world = null;
	public boolean isOnline = false;
	public Integer gamemode = 0;
	public Boolean isFlying = false;
	public Boolean isAllowedFlight = false;
	public Boolean isVanished = false;
	public long firstJoin = 0;
	public long lastSeen = 0;
	
	public String suffix;
	public String prefix;
	
	private PlayerHandler playerHandler;
	
	public CTPlayer(String uuid) {
		this.uuid = uuid;
		this.playerHandler = CTSuite.getInstance().getPlayerHandler();
	}
	
	public ProxiedPlayer getProxiedPlayer() {
		if (playerHandler.proxiedPlayers.containsKey(uuid))
    		return playerHandler.proxiedPlayers.get(uuid);
    	else
    		return null;
	}
}
