package de.crafttogether.ctsuite.bungee.util;

import java.sql.ResultSet;
import java.sql.SQLException;

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
	
	public void updateData(ResultSet rs) {
		try {
			this.name = rs.getString("name");
	    	this.nickname = rs.getString("nickname");
	    	this.server = rs.getString("server");
	    	this.world = rs.getString("world");
	    	this.isOnline = (rs.getInt("online") == 1) ? true : false;
	    	this.gamemode = rs.getInt("gamemode");
	    	this.isFlying = (rs.getInt("flying") == 1) ? true : false;
	    	this.isAllowedFlight = (rs.getInt("allowed_flight") == 1) ? true : false;
	    	this.isVanished = (rs.getInt("vanished") == 1) ? true : false;
	    	this.firstJoin = rs.getLong("first_join");
	    	this.lastSeen = rs.getLong("last_seen");
    	} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
