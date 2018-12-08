package de.crafttogether.ctsuite.bungee.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import de.crafttogether.ctsuite.bungee.CTSuite;

public class CTPlayer {
	public UUID uuid = null;
	public String name = null;
	public String nickname = null;
	public String server = null;
	public String world = null;
	public CTLocation logoutLocation = null;
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
	
	public CTPlayer(UUID uuid) {
		this.uuid = uuid;
	}
	
	public void updateData(ResultSet rs) {
		try {
			this.name = rs.getString("name");
	    	this.nickname = rs.getString("nickname");
	    	this.server = rs.getString("server");
	    	this.world = rs.getString("world");
	    	this.logoutLocation = (rs.getString("logout_location") != null ? CTLocation.fromString(rs.getString("logout_location")) : null);
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
	
	public void save() {
		final CTSuite plugin = CTSuite.getInstance();
		final CTPlayer player = this;
		
		plugin.getProxy().getScheduler().runAsync(plugin, new Runnable() {
            public void run() {
                PreparedStatement statement = null;
                Connection connection = null;
                try {
                	player.playtime = (int) (System.currentTimeMillis() / 1000L - player.lastJoin);
                	
                	connection = plugin.getMySQLConnection();
                    statement = connection.prepareStatement("UPDATE " + plugin.getTablePrefix() + "players SET "
                    	+ "name = ?,"
                    	+ "nickname = ?,"
                    	+ "server = ?,"
                    	+ "world = ?,"
                    	+ "logout_location = ?,"
                    	+ "online = ?,"
                    	+ "gamemode = ?,"
                    	+ "fly = ?,"
                    	+ "flying = ?,"
                    	+ "vanished = ?,"
                    	+ "first_join = ?,"
                    	+ "last_join = ?,"
                    	+ "last_leave = ?,"
                    	+ "playtime = ?"
                    	+ " WHERE uuid = ?;");
                    statement.setString(1, player.name);
                    statement.setString(2, player.nickname);
                    statement.setString(3, player.server);
                    statement.setString(4, player.world);
                    statement.setString(5, player.logoutLocation == null ? null : player.logoutLocation.toString());
                    statement.setInt(6, player.isOnline ? 1: 0);
                    statement.setString(7, player.gameMode);
                    statement.setInt(8, player.isAllowedFlight ? 1 : 0);
                    statement.setInt(9, player.isFlying ? 1 : 0);
                    statement.setInt(10, player.isVanished ? 1 : 0);
                    statement.setInt(11, player.firstJoin);
                    statement.setInt(12, player.lastJoin);
                    statement.setInt(13, player.lastLeave);
                    statement.setInt(14, player.playtime);
                    statement.setString(15, player.uuid.toString());
        			statement.execute();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                
	           if (statement != null) {
	               try { statement.close(); }
	               catch (SQLException e) { e.printStackTrace(); }
	           }
	           
	           if (connection != null) {
	               try { connection.close(); }
	               catch (SQLException e) { e.printStackTrace(); }
	           }
            }
        });
	}
}
