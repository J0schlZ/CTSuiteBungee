package de.crafttogether.ctsuite.bungee.handlers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import de.crafttogether.ctsuite.bungee.CTSuite;
import de.crafttogether.ctsuite.bungee.util.CTPlayer;
import de.crafttogether.ctsuite.bungee.util.PMessage;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class PlayerHandler {
    private CTSuite main;
    
    public HashMap<String, CTPlayer> players;
    public HashMap<String, ProxiedPlayer> proxiedPlayers;

    public PlayerHandler(CTSuite main) {
        this.main = main;
        players = new HashMap<String, CTPlayer>();
        proxiedPlayers = new HashMap<String, ProxiedPlayer>();
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
    
    public void addPlayer(ProxiedPlayer p) {
    	proxiedPlayers.put(p.getUniqueId().toString(), p);
    }
    
    public void updatePlayer(ProxiedPlayer p) {
    	String uuid = p.getUniqueId().toString();
    	players.get(uuid).server = p.getServer().getInfo().getName();
    }

    public void registerLogout(ProxiedPlayer p) {
    	final String uuid = p.getUniqueId().toString();
    	
        main.getProxy().getScheduler().runAsync(main, new Runnable() {
            public void run() {
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
        
        if (proxiedPlayers.containsKey(uuid))
        	proxiedPlayers.remove(uuid);
    }
    
    public ProxiedPlayer getProxiedPlayer(String uuid) {
    	if (proxiedPlayers.containsKey(uuid))
    		return proxiedPlayers.get(uuid);
    	else
    		return null;
    }
    
    public void setPrefix(String uuid, String prefix) {
    	players.get(uuid).prefix = prefix;
    }
    
    public void setSuffix(String uuid, String suffix) {
    	players.get(uuid).suffix = suffix;
    }
    
    public void setIsAllowedFlight(String uuid, boolean isAllowedFlight) {
    	if (players.containsKey(uuid)) {
	    	PMessage pm = new PMessage(main, "bukkit.player.set.isAllowedFlight");
	    	pm.put(uuid); // Using name instead of UUID because players on servers in offline-mode doesn't have same UUID as provied by Mojang
	    	pm.put(isAllowedFlight ? "true" : "false");
	    	pm.send(players.get(uuid).server);
    	}
    }
    
	public void updateIsAllowedFlight(String uuid, String senderUUID, boolean isAllowedFlight, boolean apply) {
		if (apply)
			setIsAllowedFlight(uuid, isAllowedFlight);
		
		ProxiedPlayer p = getProxiedPlayer(uuid);
		
		if (p.isConnected()) {
			new TextComponent();
			
			if (isAllowedFlight)
				p.sendMessage(TextComponent.fromLegacyText("&6Du kannst jetzt fliegen"));
			else
				p.sendMessage(TextComponent.fromLegacyText("&6Komm mal wieder auf den Boden"));
			
			players.get(uuid).isAllowedFlight = isAllowedFlight;
		}
		
        main.getProxy().getScheduler().runAsync(main, new Runnable() {
            public void run() {
                String sql = 
                  "UPDATE " + main.getTablePrefix() + "players SET " +
                    "allowed_flight = " + (isAllowedFlight ? 1 : 0)  + ", " +
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
	
	public void sendMessage(String uuid, TextComponent text) {
		ProxiedPlayer p = getProxiedPlayer(uuid);
		
		if (p != null) {
			p.sendMessage(text);
		}
	}
	
	public void broadcast (TextComponent text) {
		main.getProxy().broadcast(text);
	}
}
