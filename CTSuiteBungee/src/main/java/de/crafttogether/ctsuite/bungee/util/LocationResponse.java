package de.crafttogether.ctsuite.bungee.util;

public class LocationResponse implements Runnable {
	private CTLocation location;

	public LocationResponse() {	}
	
	public void setLocation(CTLocation loc) {
		this.location = loc;
	}
	
	public CTLocation getLocation() {
		return this.location;
	}

	public void run() { }
}
