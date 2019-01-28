package de.crafttogether.ctsuite.bungee.handlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import de.crafttogether.ctsuite.bungee.CTSuite;
import de.crafttogether.ctsuite.bungee.messaging.NetworkMessage;
import de.crafttogether.ctsuite.bungee.messaging.NetworkMessageEvent;
import de.crafttogether.ctsuite.bungee.messaging.ServerConnectedEvent;
import de.crafttogether.ctsuite.bungee.util.CTLocation;
import de.crafttogether.ctsuite.bungee.util.CTPlayer;
import de.crafttogether.ctsuite.bungee.util.LocationRequest;
import de.crafttogether.ctsuite.bungee.util.LocationResponse;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PlayerHandler implements Listener {
	private CTSuite plugin;
    public HashMap<UUID, String> uuids; // uuid, name
    public ConcurrentHashMap<UUID, CTPlayer> players; // all players
	
	public PlayerHandler() {
		this.plugin = CTSuite.getInstance();
		this.uuids = new HashMap<UUID, String>();
		this.players = new ConcurrentHashMap<UUID, CTPlayer>();
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
				
			case "player.update.leaved.server":
				this.onPlayerLeavedServer(
					UUID.fromString((String) ev.getValue("uuid")),
					CTLocation.fromString((String) ev.getValue("location"))
				);
				break;
				
			case "player.update.kicked.server":
				this.onPlayerLeavedServer(
					UUID.fromString((String) ev.getValue("uuid")),
					CTLocation.fromString((String) ev.getValue("location"))
				);
				this.onPlayerKickedServer(
					UUID.fromString((String) ev.getValue("uuid")),
					(String) ev.getValue("reason"),
					(String) ev.getValue("message")
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
                
			case "player.teleport.location":				
				this.onPlayerTeleportLocation(
					UUID.fromString((String) ev.getValue("uuid")),
					CTLocation.fromString((String) ev.getValue("location"))
				);
				break;
                
			case "player.teleport.player":
				this.onPlayerTeleportPlayer(
					(String) ev.getValue("senderUUID"),
					(String) ev.getValue("playerName"),
					(String) ev.getValue("targetName")
				);
				break;
                
			case "player.teleport.spawn":
				this.plugin.getTeleportHandler().toSpawn(
					UUID.fromString((String) ev.getValue("uuid")),
					(String) ev.getValue("spawn")
				);
				break;
		}
    }

	private void onPlayerChangedGamemode(UUID uuid, String gameMode) {
		if (!this.players.containsKey(uuid)) return;
		this.players.get(uuid).gameMode = gameMode;
		this.players.get(uuid).save();
	}

	private void onToggledFlight(UUID uuid, Boolean isFlying) {
		if (!this.players.containsKey(uuid)) return;
		this.players.get(uuid).isFlying = isFlying;
		this.players.get(uuid).save();
	}

	private void onPlayerWorldChange(UUID uuid, String world) {
		if (!this.players.containsKey(uuid)) return;
		this.players.get(uuid).world = world;
		this.players.get(uuid).save();
	}

	private void onPlayerJoinedServer(UUID uuid, String server, String world, String prefix, String suffix) {
		if (!this.players.containsKey(uuid)) return;
		this.players.get(uuid).server = server;
		this.players.get(uuid).world = world;
		this.players.get(uuid).prefix = prefix;
		this.players.get(uuid).suffix = suffix;
		this.players.get(uuid).save();
	}
	
	private void onPlayerKickedServer(UUID uuid, String reason, String leaveMessage) {
		if (!this.players.containsKey(uuid)) return;
		// TODO Auto-generated method stub
	}

	private void onPlayerLeavedServer(UUID uuid, CTLocation logoutLocation) {
		if (!this.players.containsKey(uuid)) return;
		this.players.get(uuid).logoutLocation = logoutLocation;
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

			if (ctSender != null && ctSender.isOnline && !ctSender.uuid.equals(ctTarget.uuid)) {			
				ProxiedPlayer sender = plugin.getProxy().getPlayer(ctSender.uuid);
				if (sender != null && sender.isConnected()) {
					HashMap<String, String> placeHolders = new HashMap<String, String>();
					placeHolders.put("sender", ctSender.name);
					placeHolders.put("player", ctTarget.name);
					
					if (isAllowedFlight)
						plugin.getMessageHandler().send(sender, plugin.getMessageHandler().getMessage("command.fly.feedback.enabled", placeHolders));
					else
						plugin.getMessageHandler().send(sender, plugin.getMessageHandler().getMessage("command.fly.feedback.disabled", placeHolders));
				}
			}
    		
			if (ctTarget.isOnline && ctTarget.isAllowedFlight != isAllowedFlight) {
				ProxiedPlayer target = plugin.getProxy().getPlayer(ctTarget.uuid);
				if (target != null && target.isConnected()) {	
					HashMap<String, String> placeHolders = new HashMap<String, String>();
					placeHolders.put("sender", ctSender != null ? ctSender.name : "CONSOLE");
					placeHolders.put("player", ctTarget.name);
					
					if (isAllowedFlight)
						plugin.getMessageHandler().send(target, plugin.getMessageHandler().getMessage("command.fly.targetFeedback.enabled", placeHolders));
					else
						plugin.getMessageHandler().send(target, plugin.getMessageHandler().getMessage("command.fly.targetFeedback.disabled", placeHolders));
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
				plugin.getMessageHandler().send(sender, plugin.getMessageHandler().getMessage("info.noPlayer", placeHolders));
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
			
			if (ctSender != null && ctSender.isOnline && !ctSender.uuid.equals(ctTarget.uuid)) {
				ProxiedPlayer sender = plugin.getProxy().getPlayer(ctSender.uuid);
				
				if (sender != null && sender.isConnected()) {
					HashMap<String, String> placeHolders = new HashMap<String, String>();
					placeHolders.put("sender", ctSender != null ? ctSender.name : "CONSOLE");
					placeHolders.put("player", ctTarget.name);
					placeHolders.put("gamemode", gameMode);
					plugin.getMessageHandler().send(sender, plugin.getMessageHandler().getMessage("command.gamemode.feedback", placeHolders));
				}
			}
			
			if (ctTarget.isOnline) {
				ProxiedPlayer target = plugin.getProxy().getPlayer(ctTarget.uuid);
				if (target != null && target.isConnected()) {
					HashMap<String, String> placeHolders = new HashMap<String, String>();
					placeHolders.put("sender", ctSender != null ? ctSender.name : "CONSOLE");
					placeHolders.put("player", ctTarget.name);
					placeHolders.put("gamemode", gameMode);
					plugin.getMessageHandler().send(target, plugin.getMessageHandler().getMessage("command.gamemode.targetFeedback", placeHolders));
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
				plugin.getMessageHandler().send(sender, plugin.getMessageHandler().getMessage("info.noPlayer", placeHolders));
			}
		}
	}
	
	public void onPlayerTeleportLocation(UUID uuid, CTLocation loc) {
		ProxiedPlayer player = plugin.getProxy().getPlayer(uuid);
		HashMap<String, String> placeHolders = new HashMap<String, String>();
		
		if (player != null && player.isConnected()) {
			if (plugin.getWorldHandler().findServer(loc.getServer()) == null) {
				placeHolders.put("sender", player.getName());
				placeHolders.put("server", loc.getServer());
				plugin.getMessageHandler().send(player, plugin.getMessageHandler().getMessage("info.noServer", placeHolders));
				return;
			}
			
			if (plugin.getWorldHandler().findWorld(loc.getWorld()) == null) {
				placeHolders.put("sender", player.getName());
				placeHolders.put("world", loc.getWorld());
				plugin.getMessageHandler().send(player, plugin.getMessageHandler().getMessage("info.noWorld", placeHolders));
				return;
			}
			
			plugin.getTeleportHandler().toLocation(uuid, loc);
			plugin.getMessageHandler().send(player.getUniqueId(), plugin.getMessageHandler().getMessage("teleport.location", placeHolders));
		}
	}
	
	public void onPlayerTeleportPlayer(String senderUUID, String playerName, String targetName) {
		HashMap<String, String> placeHolders = new HashMap<String, String>();
		CTPlayer ctPlayer = getPlayer(playerName);
		CTPlayer ctTarget = getPlayer(targetName);
		CTPlayer ctSender = getPlayer(senderUUID);

		if (ctPlayer == null) {
			placeHolders.put("player", playerName);
			plugin.getMessageHandler().send(ctSender.uuid, plugin.getMessageHandler().getMessage("info.noPlayer", placeHolders));
			return;
		}
		
		if (ctTarget == null) {
			placeHolders.put("target", targetName);
			plugin.getMessageHandler().send(ctSender.uuid, plugin.getMessageHandler().getMessage("info.noPlayer", placeHolders));
			return;
		}
		
		placeHolders.put("sender", (ctSender != null) ? ctSender.name : senderUUID);
		placeHolders.put("player", ctPlayer.name);
		placeHolders.put("target", ctTarget.name);
		
		// Player to Player
		if (ctPlayer.isOnline && ctPlayer.isOnline) {
			plugin.getTeleportHandler().toPlayer(ctPlayer.uuid, ctTarget.uuid);
			
			if (ctSender != null && ctSender.isOnline) {				
				if (ctPlayer.uuid.equals(ctSender.uuid))
					plugin.getMessageHandler().send(ctSender.uuid, plugin.getMessageHandler().getMessage("teleport.player", placeHolders));
				else
					plugin.getMessageHandler().send(ctSender.uuid, plugin.getMessageHandler().getMessage("teleport.player.other", placeHolders));
			}
		}
		
		// Player to players last location
		else if (!ctTarget.isOnline) {
			plugin.getTeleportHandler().toLocation(ctPlayer.uuid, ctTarget.logoutLocation);
			
			if (ctSender != null && ctSender.isOnline) {				
				if (ctPlayer.uuid.equals(ctSender.uuid))
					plugin.getMessageHandler().send(ctSender.uuid, plugin.getMessageHandler().getMessage("teleport.player.offline", placeHolders));
				else
					plugin.getMessageHandler().send(ctSender.uuid, plugin.getMessageHandler().getMessage("teleport.player.offline.other", placeHolders));
			}
		}
		
		// Set spawn to players location
		else if (!ctPlayer.isOnline) {
			new LocationRequest(UUID.fromString(senderUUID), new LocationResponse() {
				@Override
				public void run() { // Async context
					CTLocation loc = this.getLocation();
					placeHolders.put("location", loc.getServer() + ", " + loc.getWorld() + ", " + loc.getX() + ", " + loc.getY() + ", " + loc.getZ());
					plugin.getPlayerHandler().setLoginLocation(ctPlayer.uuid, loc);
					
					if (ctSender != null && ctSender.isOnline)
						plugin.getMessageHandler().send(ctSender.uuid, plugin.getMessageHandler().getMessage("teleport.player.setspawn", placeHolders));
				}
			});
		}
		
		// Set spawn to players last location
		else {
			CTLocation loc = ctTarget.logoutLocation;
			placeHolders.put("location", loc.getServer() + ", " + loc.getWorld() + ", " + loc.getX() + ", " + loc.getY() + ", " + loc.getZ());
			plugin.getPlayerHandler().setLoginLocation(ctPlayer.uuid, ctTarget.logoutLocation);
			
			if (ctSender != null && ctSender.isOnline)
				plugin.getMessageHandler().send(ctSender.uuid, plugin.getMessageHandler().getMessage("teleport.player.setspawn", placeHolders));
		}
	}

	protected void setLoginLocation(UUID uuid, CTLocation location) {
		if (!this.players.containsKey(uuid)) return;
		this.players.get(uuid).loginLocation = location;
		this.players.get(uuid).save();
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
                
                if (ctPlayer == null) {
                	plugin.getLogger().warning("Spieler nicht gefunden!!");
                	plugin.getMessageHandler().send(uuid, ChatColor.translateAlternateColorCodes('&', "&c[CTSuiteBungee]: Spieler nicht gefunden!!"));
                }
                else {
	                if (!name.equalsIgnoreCase(ctPlayer.name)) {
	                	// Name Changed
	                }
	                
	                ctPlayer.name = name;
	                ctPlayer.isOnline = true;
	                ctPlayer.lastJoin = (int) (System.currentTimeMillis() / 1000L);
	       
	                this.players.put(uuid, ctPlayer);
	                this.uuids.put(uuid, name);
                }
                
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
