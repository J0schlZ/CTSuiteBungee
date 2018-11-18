package de.crafttogether.ctsuite.bungee.events;

import de.crafttogether.ctsuite.bungee.CTSuite;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PlayerLeaveListener implements Listener {
    private CTSuite main;

    public PlayerLeaveListener(CTSuite main) {
        this.main = main;
    }

    @EventHandler
    public void onPlayerLeave(PlayerDisconnectEvent ev) {
    	main.getPlayerHandler().registerLogout(ev.getPlayer());
    }
}