package de.crafttogether.ctsuite.bungee.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import de.crafttogether.ctsuite.bungee.handlers.PlayerHandler;

public class CTPlayer {
	public UUID uuid = null;
	public String name = null;
	public String nickname = null;
	public String server = null;
	public String world = null;
	public Boolean isOnline = null;
	public String gameMode = null;
	public Boolean isFlying = null;
	public Boolean isAllowedFlight = null;
	public Boolean isVanished = null;
	public int firstJoin = 0;
	public int lastJoin = 0;
	public int lastLeave = 0;
	public int playtime = 0;
	
	public String suffix;
	public String prefix;
	
	private PlayerHandler playerHandler;
	
	public CTPlayer(UUID uuid) {
		this.uuid = uuid;
	}
	
	public void updateData(ResultSet rs) {
		try {
			this.name = rs.getString("name");
	    	this.nickname = rs.getString("nickname");
	    	this.server = rs.getString("server");
	    	this.world = rs.getString("world");
	    	this.isOnline = (rs.getInt("online") == 1) ? true : false;
	    	this.gameMode = rs.getString("gamemode");
	    	this.isFlying = (rs.getInt("flying") == 1) ? true : false;
	    	this.isAllowedFlight = (rs.getInt("fly") == 1) ? true : false;
	    	this.isVanished = (rs.getInt("vanished") == 1) ? true : false;
	    	this.firstJoin = rs.getInt("first_join");
	    	this.lastJoin = rs.getInt("last_join");
	    	this.lastLeave = rs.getInt("last_leave");
	    	this.playtime = rs.getInt("playtime");
    	} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
