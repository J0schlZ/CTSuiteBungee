package de.crafttogether.ctsuite.bungee.handlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.UUID;

import de.crafttogether.ctsuite.bungee.CTSuite;
import de.crafttogether.ctsuite.bungee.messaging.NetworkMessage;
import de.crafttogether.ctsuite.bungee.messaging.NetworkMessageEvent;
import de.crafttogether.ctsuite.bungee.messaging.ServerConnectedEvent;
import de.crafttogether.ctsuite.bungee.util.CTPlayer;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PlayerHandler implements Listener {
	private CTSuite plugin;
    public HashMap<UUID, String> uuids; // uuid, name
    public HashMap<UUID, CTPlayer> players; // all players
	
	public PlayerHandler() {
		this.plugin = CTSuite.getInstance();
		this.uuids = new HashMap<UUID, String>();
		this.players = new HashMap<UUID, CTPlayer>();
		this.plugin.getProxy().getPluginManager().registerListener(this.plugin, this);
	}
	
	@EventHandler
    public void onServerConnected(ServerConnectedEvent event) {
		ArrayList<String> onlinePlayers = new ArrayList<String>();
		for (Entry<UUID, CTPlayer> entry : this.players.entrySet()) {
			CTPlayer ctPlayer = entry.getValue();
			if (ctPlayer.isOnline == false) continue;
			onlinePlayers.add(ctPlayer.uuid.toString() + ":" + ctPlayer.name + ":" + ctPlayer.server + ":" + ctPlayer.world);
		}
		
		// 'uuid:name:server:world'
		NetworkMessage nm = new NetworkMessage("update.data.players.list");
		nm.put("players", onlinePlayers);
		nm.send(event.getName());
    }
	
	@EventHandler
    public void onNetworkMessage(NetworkMessageEvent ev) {
		switch(ev.getMessageKey())
		{
			case "player.update.joined.server":
				this.onPlayerJoinedServer(
					UUID.fromString((String) ev.getValue("uuid")),
					(String) ev.getSender(),
					(String) ev.getValue("world"),
					(String) ev.getValue("prefix"),
					(String) ev.getValue("suffix")
				);
				break;
				
			case "player.cmd.fly":
				this.onFlyCommand(
					(String) ev.getValue("targetName"),
					(String) ev.getValue("senderUUID"),
					(String) ev.getValue("fly"),
					(Boolean) ev.getValue("apply")
				);
				break;
				
			case "player.cmd.gamemode":
				this.onGamemodeCommand(
					(String) ev.getValue("targetName"),
					(String) ev.getValue("senderUUID"),
					(String) ev.getValue("gamemode"),
					(Boolean) ev.getValue("apply")
				);
				break;
	            
            case "player.update.world": 
                this.onPlayerWorldChange(
                	UUID.fromString((String) ev.getValue("uuid")),
                	(String) ev.getValue("world")
                );
                break;
	            
            case "player.update.flying": 
                this.onToggledFlight(
                	UUID.fromString((String) ev.getValue("uuid")),
                	(Boolean) ev.getValue("flying")
                );
                break;
	            
            case "player.update.gamemode": 
                this.onPlayerChangedGamemode(
                	UUID.fromString((String) ev.getValue("uuid")),
                	(String) ev.getValue("gamemode")
                );
                break;
		}
    }
	
	private void onPlayerChangedGamemode(UUID uuid, String gameMode) {
		this.players.get(uuid).gameMode = gameMode;
		this.players.get(uuid).save();
	}

	private void onToggledFlight(UUID uuid, Boolean isFlying) {
		this.players.get(uuid).isFlying = isFlying;
		this.players.get(uuid).save();
	}

	private void onPlayerWorldChange(UUID uuid, String world) {
		this.players.get(uuid).world = world;
		this.players.get(uuid).save();
	}

	private void onPlayerJoinedServer(UUID uuid, String server, String world, String prefix, String suffix) {
		this.players.get(uuid).server = server;
		this.players.get(uuid).world = world;
		this.players.get(uuid).prefix = prefix;
		this.players.get(uuid).suffix = suffix;
		this.players.get(uuid).save();
	}
	
	private void onFlyCommand(String targetName, String senderUUID, String mode, boolean apply) {	
		Boolean isAllowedFlight;
		UUID targetUUID = getUUID(targetName);
		CTPlayer ctSender = null;
		CTPlayer ctTarget = getPlayer(targetUUID);
		
		if (!senderUUID.equalsIgnoreCase("CONSOLE"))
			ctSender = getPlayer(UUID.fromString(senderUUID));

		if (ctTarget != null) {			
			if (mode.equalsIgnoreCase("on"))
				isAllowedFlight = true;
			else if (mode.equalsIgnoreCase("off"))
				isAllowedFlight = false;
			else {
				if (ctTarget.isAllowedFlight)
					isAllowedFlight = false;
				else
					isAllowedFlight = true;
			}
			
			// Send to server
			if (apply) {
				NetworkMessage nm = new NetworkMessage("player.set.fly");
	    		nm.put("uuid", targetUUID); 
	    		nm.put("fly", isAllowedFlight);
	    		nm.send(ctTarget.server);
			}
	    	
    		if (!isAllowedFlight)
    			players.get(targetUUID).isFlying = false;
    		
			if (ctTarget.isOnline && ctTarget.isAllowedFlight != isAllowedFlight) {
				ProxiedPlayer target = plugin.getProxy().getPlayer(ctTarget.uuid);
				if (target != null && target.isConnected()) {	
					HashMap<String, String> placeHolders = new HashMap<String, String>();
					placeHolders.put("sender", ctSender != null ? ctSender.name : "CONSOLE");
					placeHolders.put("player", ctTarget.name);
					
					if (isAllowedFlight)
						plugin.getMessageHandler().send(target, plugin.getMessageHandler().getMessage("fly.enabled", placeHolders));
					else
						plugin.getMessageHandler().send(target, plugin.getMessageHandler().getMessage("fly.disabled", placeHolders));
				}
			}
			
			if (ctSender != null && ctSender.isOnline && !ctSender.uuid.equals(ctTarget.uuid)) {			
				ProxiedPlayer sender = plugin.getProxy().getPlayer(ctSender.uuid);
				if (sender != null && sender.isConnected()) {
					HashMap<String, String> placeHolders = new HashMap<String, String>();
					placeHolders.put("sender", ctSender.name);
					placeHolders.put("player", ctTarget.name);
					
					if (isAllowedFlight)
						plugin.getMessageHandler().send(sender, plugin.getMessageHandler().getMessage("fly.enabled.other", placeHolders));
					else
						plugin.getMessageHandler().send(sender, plugin.getMessageHandler().getMessage("fly.disabled.other", placeHolders));
				}
			}
			
			players.get(targetUUID).isAllowedFlight = isAllowedFlight;
			
    		plugin.getProxy().getScheduler().runAsync(plugin, new Runnable() {
	            public void run() {
	                PreparedStatement statement = null;
	                Connection connection = null;
	                
	                try {
	                	String isFlying = (isAllowedFlight ? "": "flying = 0, ");
	                	
	                	connection = plugin.getMySQLConnection();
	                    statement = connection.prepareStatement("UPDATE " + plugin.getTablePrefix() + "players SET " + isFlying + "fly = ? WHERE uuid = ?;");
	                    statement.setInt(1, (isAllowedFlight ? 1 : 0));
	                    statement.setString(2, ctTarget.uuid.toString());
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
			if (senderUUID != "CONSOLE" && ctSender != null && ctSender.isOnline) {
				ProxiedPlayer sender = plugin.getProxy().getPlayer(ctSender.uuid);
				HashMap<String, String> placeHolders = new HashMap<String, String>();
				placeHolders.put("target", targetName);
				placeHolders.put("sender", ctSender.name);
				plugin.getMessageHandler().send(sender, plugin.getMessageHandler().getMessage("player.notfound", placeHolders));
			}
		}
	}

	public void onGamemodeCommand(String targetName, String senderUUID, String gameMode, boolean apply) {
		UUID targetUUID = getUUID(targetName);
		CTPlayer ctSender = null;
		CTPlayer ctTarget = getPlayer(targetUUID);
		gameMode = gameMode.toUpperCase();
		
		if (!senderUUID.equalsIgnoreCase("CONSOLE"))
			ctSender = getPlayer(UUID.fromString(senderUUID));
		
		if (ctTarget != null) {
			
			// Send to server
			if (apply) {
				NetworkMessage nm = new NetworkMessage("player.set.gamemode");
	    		nm.put("uuid", targetUUID); 
	    		nm.put("gamemode", gameMode);
	    		nm.send(ctTarget.server);
			}
			
			if (ctTarget.isOnline) {
				ProxiedPlayer target = plugin.getProxy().getPlayer(ctTarget.uuid);
				if (target != null && target.isConnected()) {
					HashMap<String, String> placeHolders = new HashMap<String, String>();
					placeHolders.put("sender", ctSender != null ? ctSender.name : "CONSOLE");
					placeHolders.put("player", ctTarget.name);
					placeHolders.put("gamemode", gameMode);
					plugin.getMessageHandler().send(target, plugin.getMessageHandler().getMessage("gamemode.changed", placeHolders));
				}
			}
			
			if (ctSender != null && ctSender.isOnline && !ctSender.uuid.equals(ctTarget.uuid)) {
				ProxiedPlayer sender = plugin.getProxy().getPlayer(ctSender.uuid);
				
				if (sender != null && sender.isConnected()) {
					HashMap<String, String> placeHolders = new HashMap<String, String>();
					placeHolders.put("sender", ctSender != null ? ctSender.name : "CONSOLE");
					placeHolders.put("player", ctTarget.name);
					placeHolders.put("gamemode", gameMode);
					plugin.getMessageHandler().send(sender, plugin.getMessageHandler().getMessage("gamemode.changed.other", placeHolders));
				}
			}
			
			players.get(ctTarget.uuid).gameMode = gameMode;
			final String finalMode = gameMode;
			
			plugin.getProxy().getScheduler().runAsync(plugin, new Runnable() {
	            public void run() {
	                PreparedStatement statement = null;
	                Connection connection = null;
	                
	                try {
	                	String isFlying = ((finalMode.equals("SURVIVAL") || finalMode.equals("ADVENTURE")) ? "flying = 0, fly = 0, " : (finalMode.equals("SPECTATOR") ? "flying = 1," : ""));
	                	
	                	connection = plugin.getMySQLConnection();
	                    statement = connection.prepareStatement("UPDATE " + plugin.getTablePrefix() + "players SET " + isFlying + "gamemode = ? WHERE uuid = ?;");
	                    statement.setString(1, finalMode);
	                    statement.setString(2, ctTarget.uuid.toString());
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
		else {
			if (senderUUID != "CONSOLE" && ctSender != null && ctSender.isOnline) {
				ProxiedPlayer sender = plugin.getProxy().getPlayer(ctSender.uuid);
				HashMap<String, String> placeHolders = new HashMap<String, String>();
				placeHolders.put("target", targetName);
				placeHolders.put("sender", ctSender.name);
				plugin.getMessageHandler().send(sender, plugin.getMessageHandler().getMessage("player.notfound", placeHolders));
			}
		}
	}
	
	public void firstLogin (UUID uuid, String name) {    	
    	this.plugin.getProxy().getScheduler().runAsync(plugin, new Runnable() {
            public void run() {
            	// Broadcast Firstjoin
                plugin.getProxy().getScheduler().schedule(plugin, new Runnable() {
                    public void run() {
                    	HashMap<String, String> placeHolder = new HashMap<String, String>();
                    	placeHolder.put("player", name);        	
                    	plugin.getMessageHandler().broadcast(plugin.getMessageHandler().getMessage("join.broadcast.firstjoin", placeHolder));
                    }
                }, 1, TimeUnit.SECONDS);
            	
                PreparedStatement statement = null;
                Connection connection = null;
                
            	int currentTimestamp = (int) (System.currentTimeMillis() / 1000L);
            	
				try {
					connection = plugin.getMySQLConnection();
					statement = connection.prepareStatement(
					  "INSERT INTO " + plugin.getTablePrefix() + "players " + 
					  "(uuid, name, online, first_join, last_join) " +
					  "VALUES (?,?,1,?,?);"
					);
					
	                statement.setString(1, uuid.toString());
	                statement.setString(2, name);
	                statement.setInt(3, currentTimestamp);
	                statement.setInt(4, currentTimestamp);
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
				
				CTPlayer ctPlayer = new CTPlayer(uuid);				
				ctPlayer.name = name;
                ctPlayer.isOnline = true;
                ctPlayer.firstJoin = currentTimestamp;
                ctPlayer.lastJoin = currentTimestamp;
                
                players.put(uuid, ctPlayer);
                uuids.put(uuid, name);
            }
        });
    }
	
    public void registerLogin(PendingConnection con) {
    	ResultSet resultSet = null;
        PreparedStatement statement = null;
        Connection connection = null;
        
    	UUID uuid = con.getUniqueId();
    	String name = con.getName();
        
        try {
			connection = this.plugin.getMySQLConnection();
			statement = connection.prepareStatement("SELECT * FROM " + this.plugin.getTablePrefix() + "players WHERE uuid = ?;");
			statement.setString(1, uuid.toString());
			resultSet = statement.executeQuery();
        	
            if (!resultSet.next())
            	firstLogin(uuid, name);
            else {
                CTPlayer ctPlayer = getPlayer(uuid);
                
                if (ctPlayer == null)
                	plugin.getLogger().warning("Spieler nicht gefunden!!");
                	
                if (!name.equalsIgnoreCase(ctPlayer.name)) {
                	// Name Changed
                }
                
                ctPlayer.name = name;
                ctPlayer.isOnline = true;
                ctPlayer.lastJoin = (int) (System.currentTimeMillis() / 1000L);
       
                this.players.put(uuid, ctPlayer);
                this.uuids.put(uuid, name);
                
                this.plugin.getProxy().getScheduler().runAsync(this.plugin, new Runnable() {
                    public void run() {
                        PreparedStatement statement = null;
                        Connection connection = null;
                        
		                try {
		                	connection = plugin.getMySQLConnection();
							statement = connection.prepareStatement("UPDATE " + plugin.getTablePrefix() + "players SET name = ?, online = 1, last_join = ? WHERE uuid = ?;");
							statement.setString(1, name);
							statement.setInt(2, (int) System.currentTimeMillis() / 1000);
							statement.setString(3, uuid.toString());
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
    
    public void registerLogout(ProxiedPlayer p) {
    	final UUID uuid = p.getUniqueId();
    	final int lastJoin = players.get(uuid).lastJoin;
    	final int lastLeave = (int) (System.currentTimeMillis() / 1000L);
    	final int playTime = (int) (System.currentTimeMillis() / 1000L) - lastJoin;
    	
        players.get(uuid).isOnline = false;
        players.get(uuid).lastLeave = lastLeave;
        players.get(uuid).playtime = playTime;
    	
        plugin.getProxy().getScheduler().runAsync(plugin, new Runnable() {
            public void run() {
                PreparedStatement statement = null;
                Connection connection = null;
                
                try {
                	connection = plugin.getMySQLConnection();
                    statement = connection.prepareStatement("UPDATE " + plugin.getTablePrefix() + "players SET online = 0, last_leave = ?, playtime = playtime + ? WHERE uuid = ?;");
                    statement.setInt(1, lastLeave);
                    statement.setInt(2, playTime);
                    statement.setString(3, uuid.toString());
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
    
	public void readPlayersFromDB() {
		plugin.getProxy().getScheduler().runAsync(plugin, new Runnable() {
			public void run() {
				System.out.println("Read Players from DB");
				ResultSet resultSet = null;
				PreparedStatement statement = null;
				Connection connection = null;
				
				try {
					connection = plugin.getMySQLConnection();
					statement = connection.prepareStatement("SELECT * FROM " + plugin.getTablePrefix() + "players");
					resultSet = statement.executeQuery();
					
					while (resultSet.next()) {
						CTPlayer ctPlayer = new CTPlayer(UUID.fromString(resultSet.getString("uuid")));
				        ctPlayer.updateData(resultSet);
				        players.put(ctPlayer.uuid, ctPlayer);
				        uuids.put(ctPlayer.uuid, ctPlayer.name);
				        System.out.println("DBPlayer " + ctPlayer.name + " - " + ctPlayer.uuid.toString());
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
	
	public CTPlayer getPlayer(UUID uuid) {
		if (this.players.containsKey(uuid))
			return this.players.get(uuid);
		return null;
	}
	
	public CTPlayer getPlayer (String playerName) {
		UUID uuid = getUUID(playerName);
		if (uuid != null)
			return getPlayer(uuid);
		return null;
	}
	
	public UUID getUUID(String playerName) {
		for (Entry<UUID, String> entry : this.uuids.entrySet()) {
			if (entry.getValue().equalsIgnoreCase(playerName))
				return entry.getKey();
		}
		return null;
	}

	public String getName(UUID uuid) {
		if (this.uuids.containsKey(uuid))
			return this.uuids.get(uuid);
		return null;
	}
}
