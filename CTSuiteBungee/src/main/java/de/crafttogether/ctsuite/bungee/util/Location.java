package de.crafttogether.ctsuite.bungee.util;

public class Location {
	private String server;
	private String world;
	private int x;
	private int y;
	private int z;
	private float yaw;
	private float pitch;
	
	public Location () {
		
	}
	
	public Location (String server, String world, int x, int y, int z, float yaw, float pitch) {
		this.server = server;
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
		this.yaw = yaw;
		this.pitch = pitch;
	}
	
	public Location fromString(String strLocation) {
		String[] str = strLocation.split(":");
		this.server = str[0];
		this.world = str[1];
		this.x = Integer.parseInt(str[2]);
		this.y = Integer.parseInt(str[3]);
		this.z = Integer.parseInt(str[4]);
		this.yaw = Float.parseFloat(str[5]);
		this.pitch = Float.parseFloat(str[6]);
		return this;
	}
	
	public String getString() {
		return 
		  this.server + ":" +
		  this.world + ":" +
		  this.x + ":" +
		  this.y + ":" +
		  this.z + ":" +
		  this.yaw + ":" +
		  this.pitch;
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
	
	public int getX() {
		return this.x;
	}
	
	public int getY() {
		return this.y;
	}
	
	public int getZ() {
		return this.z;
	}
	
	public float getYaw() {
		return this.yaw;
	}
	
	public float getPitch() {
		return this.pitch;
	}
}
