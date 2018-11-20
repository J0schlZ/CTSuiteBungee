package de.crafttogether.ctsuite.bungee.events;

import de.crafttogether.ctsuite.bungee.CTSuite;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PlayerSwitchedServerListener implements Listener {
    private CTSuite main;

    public PlayerSwitchedServerListener(CTSuite main) {
        this.main = main;
    }

    @EventHandler
    public void onServerSwitch(ServerSwitchEvent ev) {
    	main.getPlayerHandler().updatePlayer(ev.getPlayer());
    }
}