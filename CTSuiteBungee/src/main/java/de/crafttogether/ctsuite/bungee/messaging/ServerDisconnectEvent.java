package de.crafttogether.ctsuite.bungee.messaging;

import net.md_5.bungee.api.plugin.Event;

public final class ServerDisconnectEvent extends Event {
    private String serverName;

    public ServerDisconnectEvent(String serverName) {
        this.serverName = serverName;
    }

    public String getName() {
        return this.serverName;
    }
}