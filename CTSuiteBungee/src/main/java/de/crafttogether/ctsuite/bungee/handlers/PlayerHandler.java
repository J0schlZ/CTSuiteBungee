package de.crafttogether.ctsuite.bungee.handlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import de.crafttogether.ctsuite.bungee.CTSuite;
import de.crafttogether.ctsuite.bungee.util.CTPlayer;
import de.crafttogether.ctsuite.bungee.util.PMessage;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class PlayerHandler {
    private CTSuite main;
    
    public HashMap<String, String> names; // uuid, name
    public HashMap<String, CTPlayer> players;
    public HashMap<String, ProxiedPlayer> proxiedPlayers;

    public PlayerHandler(CTSuite main) {
        this.main = main;
        players = new HashMap<String, CTPlayer>();
        names = new HashMap<String, String>();
        proxiedPlayers = new HashMap<String, ProxiedPlayer>();
    }
   
    public void registerLogin(PendingConnection con) {
    	ResultSet resultSet = null;
        PreparedStatement statement = null;
        Connection connection = null;
        
    	String uuid = con.getUniqueId().toString();
    	String name = con.getName();
        
        try {
			connection = main.getConnection();
			statement = connection.prepareStatement("SELECT * FROM " + main.getTablePrefix() + "players WHERE uuid = ?;");
			statement.setString(1, uuid);
			resultSet = statement.executeQuery();
        	
            if (!resultSet.next())
            	firstLogin(uuid, name);
            else {
                CTPlayer ctPlayer = new CTPlayer(uuid);
                ctPlayer.updateData(resultSet);
                
                if (!name.equalsIgnoreCase(ctPlayer.name)) {
                	// Name Changed
                }
                
                ctPlayer.name = name;
                ctPlayer.isOnline = true;
                ctPlayer.lastSeen = System.currentTimeMillis() / 1000;
                
                players.put(uuid, ctPlayer);
                names.put(uuid, name);
                
            	main.getProxy().getScheduler().runAsync(main, new Runnable() {
                    public void run() {
                        PreparedStatement statement = null;
                        Connection connection = null;
                        
		                try {
		                	connection = main.getConnection();
							statement = connection.prepareStatement("UPDATE " + main.getTablePrefix() + "players SET name = ?, online = 1, last_seen = now() WHERE uuid = ?;");
							statement.setString(1, name);
							statement.setString(2, uuid);
							statement.execute();
		    			} catch (SQLException e) {
							e.printStackTrace();
						} finally {
				            if (statement != null) {
				                try { statement.close(); }
				                catch (SQLException e) { e.printStackTrace(); }
				            }
				            if (connection != null) {
				                try { connection.close(); }
				                catch (SQLException e) { e.printStackTrace(); }
				            }
				        }
                    }
            	});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (resultSet != null) {
                try { resultSet.close(); }
                catch (SQLException e) { e.printStackTrace(); }
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
    }
    
    public void firstLogin (String uuid, String name) {   	
    	main.getProxy().getScheduler().runAsync(main, new Runnable() {
            public void run() {
                PreparedStatement statement = null;
                Connection connection = null;
                
            	long currentTimestamp = System.currentTimeMillis() / 1000;
				
				try {
					connection = main.getConnection();
					statement = connection.prepareStatement(
					  "INSERT INTO " + main.getTablePrefix() + "players " + 
					  "(uuid, name, online, first_join, last_seen) " +
					  "VALUES (?,?,?,?,?);"
					);
					
	                statement.setString(1, uuid);
	                statement.setString(2, name);
	                statement.setInt(3, 1);
	                statement.setLong(4, currentTimestamp);
	                statement.setLong(5, currentTimestamp);
	    			statement.execute();
				} catch (SQLException e) {
					e.printStackTrace();
				} finally {
		            if (statement != null) {
		                try { statement.close(); }
		                catch (SQLException e) { e.printStackTrace(); }
		            }
		            if (connection != null) {
		                try { connection.close(); }
		                catch (SQLException e) { e.printStackTrace(); }
		            }
		        }
				
				CTPlayer ctPlayer =  new CTPlayer(uuid);
				ctPlayer.name = name;
                ctPlayer.isOnline = true;
                ctPlayer.firstJoin = currentTimestamp;
                ctPlayer.lastSeen = currentTimestamp;
                
                players.put(uuid, ctPlayer);
                names.put(uuid, name);
            }
        });
    }

    public void readPlayersFromDB() {
    	 main.getProxy().getScheduler().runAsync(main, new Runnable() {
             public void run() {
            	 ResultSet resultSet = null;
            	 PreparedStatement statement = null;
            	 Connection connection = null;
            	 
                 try {
                	connection = main.getConnection();
                	statement = connection.prepareStatement("SELECT * FROM " + main.getTablePrefix() + "players");
         			resultSet = statement.executeQuery();

                    while (resultSet.next()) {
                    	String uuid = resultSet.getString("uuid");
                    	String name = resultSet.getString("name");
                    	
                    	CTPlayer ctPlayer = new CTPlayer(uuid);
                    	ctPlayer.updateData(resultSet);
                    	players.put(uuid, ctPlayer);
                        names.put(uuid, name);
                    }
                 } catch (SQLException e) {
                	 e.printStackTrace();
                 } finally {
		            if (resultSet != null) {
		                try { resultSet.close(); }
		                catch (SQLException e) { e.printStackTrace(); }
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
             }
    	 });
    }
    
    public void addProxiedPlayer(ProxiedPlayer p) {
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
                PreparedStatement statement = null;
                Connection connection = null;
                
                try {
                	connection = main.getConnection();
                    statement = connection.prepareStatement("UPDATE " + main.getTablePrefix() + "players SET name = ?, online = 0, last_seen = now() WHERE uuid = ?;");
                    statement.setString(1, p.getName());
                    statement.setString(2, uuid);
        			statement.execute();
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
		           if (statement != null) {
		               try { statement.close(); }
		               catch (SQLException e) { e.printStackTrace(); }
		           }
		           if (connection != null) {
		               try { connection.close(); }
		               catch (SQLException e) { e.printStackTrace(); }
		           }
		        }
            }
        });
        
        if (players.containsKey(uuid))
        	players.get(uuid).isOnline = false;
        
        if (proxiedPlayers.containsKey(uuid))
        	proxiedPlayers.remove(uuid);
    }
    
    public String getUUID(String playerName) {
    	for (String uuid : names.keySet()) {
    		String name = names.get(uuid);
    		if (name.equalsIgnoreCase(playerName))
    			return uuid;
    	}
    	return null;

    }
    
    public String getName(String uuid) {
    	if (names.containsKey(uuid))
    		return names.get(uuid);
    	return null;
    }
    
    public CTPlayer getPlayer(String uuid) {
		if (players.containsKey(uuid))
			return players.get(uuid);
		return null;
    }
    
    public CTPlayer getPlayerByName(String name) {
    	String uuid = getUUID(name);
    	if (uuid != null && players.containsKey(uuid)) {
			return players.get(uuid);
		}
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
    
	public void updateIsAllowedFlight(String playerName, String senderUUID, String mode, boolean apply) {
		Boolean isAllowedFlight;
		CTPlayer ctSender = getPlayer(senderUUID);
		CTPlayer ctPlayer = getPlayerByName(playerName);
		
		if (ctPlayer != null) {
			ProxiedPlayer player = ctPlayer.getProxiedPlayer();
			
			if (mode.equalsIgnoreCase("on"))
				isAllowedFlight = true;
			else if (mode.equalsIgnoreCase("off"))
				isAllowedFlight = false;
			else {
				if (players.get(ctPlayer.uuid).isAllowedFlight)
					isAllowedFlight = false;
				else
					isAllowedFlight = true;
			}
			
			if (apply)
    			setIsAllowedFlight(ctPlayer.uuid, isAllowedFlight);

			if (players.get(ctPlayer.uuid).isAllowedFlight != isAllowedFlight) {				
				if (player != null && player.isConnected()) {
					new TextComponent();
					
					if (isAllowedFlight)
						player.sendMessage(new TextComponent("Du kannst jetzt fliegen"));
					else
						player.sendMessage(new TextComponent("Komm mal wieder auf den Boden"));
				}
				
				if (senderUUID != "CONSOLE" && ctSender != null && !ctPlayer.uuid.equals(ctSender.uuid)) {
					ProxiedPlayer sender = ctSender.getProxiedPlayer();
					players.get(ctPlayer.uuid).isAllowedFlight = isAllowedFlight;
					if (sender != null && sender.isConnected()) {
						if (isAllowedFlight)
							sender.sendMessage(new TextComponent("Du hast den Flugmodus für " + ctPlayer.name + " aktiviert."));
						else
							sender.sendMessage(new TextComponent("Du hast den Flugmodus für " + ctPlayer.name + " deaktiviert."));
					}
				}
			}
			
			players.get(ctPlayer.uuid).isAllowedFlight = isAllowedFlight;
			
			main.getProxy().getScheduler().runAsync(main, new Runnable() {
	            public void run() {
	                PreparedStatement statement = null;
	                Connection connection = null;
	                
	                try {
	                	connection = main.getConnection();
	                    statement = connection.prepareStatement("UPDATE " + main.getTablePrefix() + "players SET allowed_flight = ? WHERE uuid = ?;");
	                    statement.setInt(1, (isAllowedFlight ? 1 : 0));
	                    statement.setString(2, ctPlayer.uuid);
	        			statement.execute();
	                } catch (SQLException e) {
	                    e.printStackTrace();
	                }
					finally {
			           if (statement != null) {
			               try { statement.close(); }
			               catch (SQLException e) { e.printStackTrace(); }
			           }
			           if (connection != null) {
			               try { connection.close(); }
			               catch (SQLException e) { e.printStackTrace(); }
			           }
			        }
	            }
	        });
		}
		else {
			if (senderUUID != "CONSOLE" && ctSender != null) {
				ProxiedPlayer sender = ctSender.getProxiedPlayer();

				if (sender != null && sender.isConnected()) {
					sender.sendMessage(new TextComponent("Es wurde kein Spieler mit dem Namen '" + playerName + "' gefunden"));
				}
			}
		}
	}
	
	public void sendMessage(String uuid, TextComponent text) {
		if (players.containsKey(uuid)) {
			CTPlayer ctPlayer = players.get(uuid);
			ProxiedPlayer p = ctPlayer.getProxiedPlayer();
			
			if (p != null && p.isConnected()) {
				p.sendMessage(text);
			}
		}
	}
	
	public void broadcast (TextComponent text) {
		main.getProxy().broadcast(text);
	}
}
