package de.crafttogether.ctsuite.bungee.events;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import de.crafttogether.ctsuite.bungee.CTSuite;
import de.crafttogether.ctsuite.bungee.messaging.NetworkMessage;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PlayerLoginListener implements Listener {
    private CTSuite plugin;

    public PlayerLoginListener() {
        this.plugin = CTSuite.getInstance();
    }

    @EventHandler
    public void onPlayerLogin(LoginEvent ev) {
        ev.registerIntent(plugin);
        
        String uuid = ev.getConnection().getUniqueId().toString();
        
        ResultSet resultSet = null;
        PreparedStatement statement = null;
        Connection connection = null;
        
        try {
        	connection = plugin.getMySQLConnection();
			statement = connection.prepareStatement("SELECT * FROM " + plugin.getTablePrefix() + "bans WHERE uuid = ? ORDER BY -(expiration IS NULL), expiration DESC LIMIT 1");
			statement.setString(1, uuid);
			resultSet = statement.executeQuery();
        	
            if (resultSet.next()) {
                Date expiration = resultSet.getTimestamp("expiration");
                
                if (resultSet.wasNull() || expiration.after(new Date())) {
                    ev.setCancelled(true);
                    String reason = "banned";
                    
                    // if (rs.wasNull())
                    //     reason = main.getMessageHandler().getMessage("join.banned").replace("%reason%", rs.getString("reason"));
                    // else
                    //     reason = main.getMessageHandler().getMessage("join.banneduntil").replace("%reason%", rs.getString("reason")).replace("%expiration%", main.getDateFormat().format(expiration));
                    
                    ev.setCancelReason(new TextComponent(reason));
                }
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
        
        //String ip = ev.getConnection().getAddress().getAddress().toString();
        /*
        if (!ev.isCancelled() && main.getConfig().getInt("CTSuite.Security.MaxPlayersWithSameIP") > 0) {
            if (main.getPlayerHandler().getIps().containsKey(ip) && main.getPlayerHandler().getIps().get(ip) >= main
                    .getConfig().getInt("CTSuite.Security.MaxPlayersWithSameIP")) {
                ev.setCancelled(true);
                ev.setCancelReason(new TextComponent(main.getMessageHandler().getMessage("security.join.denied")));
            }
        }
        */

        if (!ev.isCancelled()) {
        	plugin.getPlayerHandler().registerLogin(ev.getConnection());
        	
        	// Broadcast Login
            plugin.getProxy().getScheduler().schedule(plugin, new Runnable() {
                public void run() {
                	HashMap<String, String> placeHolder = new HashMap<String, String>();
                	placeHolder.put("player", ev.getConnection().getName());        	
                	plugin.getMessageHandler().broadcast(plugin.getMessageHandler().getMessage("join.broadcast", placeHolder));
                }
            }, 1, TimeUnit.SECONDS);
        	
            /*
            if (main.getPlayerHandler().getIps().containsKey(ip))
                main.getPlayerHandler().getIps().put(ip, main.getPlayerHandler().getIps().get(ip) + 1);
            else
                main.getPlayerHandler().getIps().put(ip, 1);
        	*/

        	NetworkMessage nm = new NetworkMessage("player.update.joined.network");
        	nm.put("uuid", ev.getConnection().getUniqueId());
        	nm.send("all");
        }
        
        ev.completeIntent(plugin);
    }
}
