package de.crafttogether.ctsuite.bungee.events;

import de.crafttogether.ctsuite.bungee.CTSuite;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class PlayerSwitchedServerListener implements Listener {
    private CTSuite main;

    public PlayerSwitchedServerListener(CTSuite main) {
        this.main = main;
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onServerSwitch(ServerSwitchEvent ev) {
    	main.getPlayerHandler().updatePlayerServer(ev.getPlayer());
    }
}