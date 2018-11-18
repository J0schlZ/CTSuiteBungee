package de.crafttogether.ctsuite.bungee.handlers;

import de.crafttogether.ctsuite.bungee.CTSuite;
import de.crafttogether.ctsuite.bungee.util.CTPlayer;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

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
        	ProxiedPlayer p = main.getProxy().getPlayer(uuid);
        	
        	String sql = 
        	  "SELECT id, name FROM " + main.getTablePrefix() + "players " +
        	  "WHERE uuid = '" + con.getUniqueId() + "'";
            
        	ResultSet rs = main.getHikari().getConnection().createStatement().executeQuery(sql);

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
                
                // Spieler in DB gefunden.
                
                CTPlayer ctPlayer = new CTPlayer();
                players.put(uuid, ctPlayer);
                
                players.get(uuid).uuid = uuid;
                players.get(uuid).name = name;
                
                main.getProxy().broadcast(name + " gefunden");
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

                main.getProxy().broadcast(name + " nicht gefunden");
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
                    "server = '" + p.getServer().getInfo().getName() + "', " +
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
    
    public void updatePrefixSuffix(String uuid, String prefix, String suffix) {
    	players.get(uuid).prefix = prefix;
    	players.get(uuid).suffix = suffix;
    	main.getMessageHandler().broadcast("Prefix Suffix WALLAH");;
    }


    /*public void setGamemode(final ProxiedPlayer p, final String gamemode) {
        this.gamemode.put(p, gamemode);
        //sendGamemodeToServer(p, gamemode);

        main.getProxy().getScheduler().runAsync(main, new Runnable() {
            public void run() {
                try {
                    String sql = "UPDATE " + main.getTablePrefix() + "players SET gamemode = '" + gamemode + "', flying = "
                            + (gamemode.equals("CREATIVE") || gamemode.equals("SPECTATOR") ? "1" : "0") + " WHERE uuid = " +
                            "'" + p.getUniqueId() + "'";
                    main.getHikari().getConnection().createStatement().execute(sql);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });

        if (flying.contains(p))
            sendFlyToServer(p);
    }
 	*/
}
