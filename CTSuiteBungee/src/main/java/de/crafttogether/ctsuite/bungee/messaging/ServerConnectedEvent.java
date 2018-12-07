package de.crafttogether.ctsuite.bungee.messaging;

import net.md_5.bungee.api.plugin.Event;

public final class ServerConnectedEvent extends Event {
    private String serverName;

    public ServerConnectedEvent(String serverName) {
        this.serverName = serverName;
    }

    public String getName() {
        return this.serverName;
    }
}