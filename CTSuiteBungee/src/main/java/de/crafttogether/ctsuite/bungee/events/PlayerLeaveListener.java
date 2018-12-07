package de.crafttogether.ctsuite.bungee.events;

import java.util.HashMap;

import de.crafttogether.ctsuite.bungee.CTSuite;
import de.crafttogether.ctsuite.bungee.messaging.NetworkMessage;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PlayerLeaveListener implements Listener {
    private CTSuite plugin;

    public PlayerLeaveListener() {
        this.plugin = CTSuite.getInstance();
    }

    @EventHandler
    public void onPlayerLeave(PlayerDisconnectEvent ev) {
    	plugin.getPlayerHandler().registerLogout(ev.getPlayer());
    	
    	// Broadcast Logout
    	HashMap<String, String> placeHolder = new HashMap<String, String>();
    	placeHolder.put("player", ev.getPlayer().getName());        	
    	plugin.getMessageHandler().broadcast(plugin.getMessageHandler().getMessage("leave.broadcast", placeHolder));
    	
    	NetworkMessage nm = new NetworkMessage("player.update.leaved.network");
    	nm.put("uuid", ev.getPlayer().getUniqueId());
    	nm.send("all");
    }
}
