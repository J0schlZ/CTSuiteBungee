package de.crafttogether.ctsuite.bungee.handlers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import de.crafttogether.ctsuite.bungee.CTSuite;
import de.crafttogether.ctsuite.bungee.util.CTPlayer;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class PlayerHandler {
    private CTSuite main;
    
    public HashMap<String, CTPlayer> players;

    public PlayerHandler(CTSuite main) {
        this.main = main;
        players = new HashMap<String, CTPlayer>();
    }

    public void registerLogin(PendingConnection con) {
        try {
        	String uuid = con.getUniqueId().toString();
        	String name = con.getName();
        	
        	CTPlayer ctPlayer = new CTPlayer();
            players.put(uuid, ctPlayer);
            players.get(uuid).uuid = uuid;
            players.get(uuid).name = name;
            
            String sql;
            ResultSet rs = null;
            
            try {
            	sql = 
            	  "SELECT id, name FROM " + main.getTablePrefix() + "players " +
            	  "WHERE uuid = '" + con.getUniqueId() + "'";
            	rs = main.getHikari().getConnection().createStatement().executeQuery(sql);
        	} catch (SQLException e) {
                e.printStackTrace();
            }

            if (rs.next()) {                               
                try {
                    sql = 
                      "UPDATE " + main.getTablePrefix() + "players SET " + 
                        "name = '" + name + "', " + 
                        "online = '1', " +
                        "last_seen = now() " +
                      "WHERE uuid = '" + uuid + "'";
                    
                    main.getHikari().getConnection().createStatement().execute(sql);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                sql =
                  "INSERT INTO " + main.getTablePrefix() + "players (uuid, name, online, last_seen) " + 
                  "VALUES (" +
                     "'" + uuid + "', " +
                     "'" + name + "', " + 
                     "'1', " + 
                     "now()" +
                   ")";
                try {
                    main.getHikari().getConnection().createStatement().execute(sql);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void registerLogout(ProxiedPlayer p) {
        main.getProxy().getScheduler().runAsync(main, new Runnable() {
            public void run() {
            	String uuid = p.getUniqueId().toString();
                String sql = 
                  "UPDATE " + main.getTablePrefix() + "players SET " +
                    "name = '" + p.getName() + "', " +
                    "online = 0, " + 
                    "last_seen = now() " +
                  "WHERE uuid = '" + uuid + "'";
                try {
                    main.getHikari().getConnection().createStatement().execute(sql);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    
    public void setPrefix(String uuid, String prefix) {
    	players.get(uuid).prefix = prefix;
    }
    
    public void setSuffix(String uuid, String suffix) {
    	players.get(uuid).suffix = suffix;
    }
}
