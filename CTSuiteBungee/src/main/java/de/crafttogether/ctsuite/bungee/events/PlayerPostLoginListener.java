package de.crafttogether.ctsuite.bungee.events;

import de.crafttogether.ctsuite.bungee.CTSuite;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PlayerPostLoginListener implements Listener {
    private CTSuite main;

    public PlayerPostLoginListener(CTSuite main) {
        this.main = main;
    }

    @EventHandler
    public void onPostLogin(PostLoginEvent ev) {
    	main.getPlayerHandler().addPlayer(ev.getPlayer());
    }
}