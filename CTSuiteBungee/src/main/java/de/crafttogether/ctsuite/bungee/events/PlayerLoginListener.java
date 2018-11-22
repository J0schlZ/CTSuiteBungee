package de.crafttogether.ctsuite.bungee.events;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import de.crafttogether.ctsuite.bungee.CTSuite;

public class PlayerLoginListener implements Listener {
    private CTSuite main;

    public PlayerLoginListener(CTSuite main) {
        this.main = main;
    }

    @EventHandler
    public void onLogin(LoginEvent ev) {
        ev.registerIntent(main);
        
        String sql;
        ResultSet rs = null;
        Statement statement = null;
        Connection conn = null;
        
        sql = 
          "SELECT * FROM " + main.getTablePrefix() + "bans " +
          "WHERE player = '" + ev.getConnection().getUniqueId() + "' " +
          "ORDER BY -(expiration IS NULL), expiration DESC LIMIT 1";
        
        try {
        	conn = main.getConnection();
        	statement = conn.createStatement();
        	rs = statement.executeQuery(sql);
        	
            if (rs.next()) {
                Date expiration = rs.getTimestamp("expiration");
                
                if (rs.wasNull() || expiration.after(new Date())) {
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
            if (rs != null) {
                try { rs.close(); }
                catch (SQLException e) { e.printStackTrace(); }
            }
            if (statement != null) {
                try { statement.close(); }
                catch (SQLException e) { e.printStackTrace(); }
            }
            if (conn != null) {
                try { conn.close(); }
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
            main.getPlayerHandler().registerLogin(ev.getConnection());

            /*
            if (main.getPlayerHandler().getIps().containsKey(ip))
                main.getPlayerHandler().getIps().put(ip, main.getPlayerHandler().getIps().get(ip) + 1);
            else
                main.getPlayerHandler().getIps().put(ip, 1);
        	*/
        }
        
        ev.completeIntent(main);
    }
}
