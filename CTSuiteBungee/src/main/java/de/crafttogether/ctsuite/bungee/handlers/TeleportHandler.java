package de.crafttogether.ctsuite.bungee.handlers;

import de.crafttogether.ctsuite.bungee.CTSuite;
import de.crafttogether.ctsuite.bungee.util.CTPlayer;
import de.crafttogether.ctsuite.bungee.util.Location;

public class TeleportHandler {
    private CTSuite main;
    
	public TeleportHandler(CTSuite main) {
		this.main = main;
	}

	public void playerToPlayer(String playerName, String targetName) {
		CTPlayer player = main.getPlayerHandler().getPlayerByName(playerName);
		CTPlayer target = main.getPlayerHandler().getPlayerByName(targetName);
		
	}

	public void playerToPos(String playerName, String strLocation) {
		Location location = new Location().fromString(strLocation);
		playerToPos(playerName, location);
	}
	
	public void playerToPos(String playerName, Location location) {
		CTPlayer ctPlayer = main.getPlayerHandler().getPlayerByName(playerName);
		
		if (ctPlayer == null)
			return;
	}
}
