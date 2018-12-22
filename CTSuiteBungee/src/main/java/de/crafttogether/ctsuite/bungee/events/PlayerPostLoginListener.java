package de.crafttogether.ctsuite.bungee.events;

import java.util.HashMap;

import de.crafttogether.ctsuite.bungee.CTSuite;
import de.crafttogether.ctsuite.bungee.util.CTPlayer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PlayerPostLoginListener implements Listener {
    private CTSuite plugin;

    public PlayerPostLoginListener() {
        this.plugin = CTSuite.getInstance();
    }

    @EventHandler
    public void onPostLogin(final PostLoginEvent ev) {
    	ProxiedPlayer p = ev.getPlayer();
    	CTPlayer ctPlayer = plugin.getPlayerHandler().getPlayer(p.getUniqueId());
    	
    	if (ctPlayer == null) {
    		System.out.println("[PostLoginEvent]: Spieler nicht gefunden");
    		return;
    	}
    	
        if (!ev.getPlayer().getServer().getInfo().getName().equals(ctPlayer.server)) {
        	ServerInfo server = plugin.getProxy().getServerInfo(ctPlayer.server);
        	
        	if (server != null)
        		ev.getPlayer().connect(server);
        	else {
				HashMap<String, String> placeHolders = new HashMap<String, String>();
				placeHolders.put("player", ctPlayer.name);
				placeHolders.put("server", ctPlayer.server);
				plugin.getMessageHandler().send(ev.getPlayer(), plugin.getMessageHandler().getMessage("connection.failed", placeHolders));
        	}
        }
    }
}
