package de.crafttogether.ctsuite.bungee.util;

public class Location {
	private String server;
	private String world;
	private float x;
	private float y;
	private float z;
	private float yaw;
	private float pitch;
	
	public Location () {
		
	}
	
	public Location (String server, String world, int x, int y, int z, float pitch, float yaw) {
		this.server = server;
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
		this.pitch = pitch;
		this.yaw = yaw;
	}
	
	public Location fromString(String strLocation) {
		String[] str = strLocation.split(":");
		this.server = str[0];
		this.world = str[1];
		this.x = Float.parseFloat(str[2]);
		this.y = Float.parseFloat(str[3]);
		this.z = Float.parseFloat(str[4]);
		this.pitch = Float.parseFloat(str[5]);
		this.yaw = Float.parseFloat(str[4]);
		return this;
	}
	
	public String toString() {
		return 
		  this.server + ":" +
		  this.world + ":" +
		  this.x + ":" +
		  this.y + ":" +
		  this.z + ":" +
		  this.pitch + ":" +
		  this.yaw;
	}
	
	public void setServer(String server) {
		this.server = server;
	}
	
	public void setWorld(String world) {
		this.server = world;
	}
	
	public void setX(String x) {
		this.server = x;
	}
	
	public void setY(String y) {
		this.server = y;
	}
	
	public void setZ(String z) {
		this.server = z;
	}
	
	public void setYaw(String yaw) {
		this.server = yaw;
	}
	
	public void setPitch(String pitch) {
		this.server = pitch;
	}
	
	public String getServer() {
		return this.server;
	}
	
	public String getWorld() {
		return this.world;
	}
	
	public Float getX() {
		return this.x;
	}
	
	public Float getY() {
		return this.y;
	}
	
	public Float getZ() {
		return this.z;
	}
	
	public float getYaw() {
		return this.yaw;
	}
	
	public float getPitch() {
		return this.pitch;
	}
}
