package de.crafttogether.ctsuite.bungee;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.google.common.io.ByteStreams;
import com.zaxxer.hikari.HikariDataSource;

import de.crafttogether.ctsuite.bungee.events.PlayerLeaveListener;
import de.crafttogether.ctsuite.bungee.events.PlayerLoginListener;
import de.crafttogether.ctsuite.bungee.handlers.MessageHandler;
import de.crafttogether.ctsuite.bungee.handlers.PlayerHandler;
import de.crafttogether.ctsuite.bungee.handlers.TeleportHandler;
import de.crafttogether.ctsuite.bungee.handlers.WorldHandler;
import de.crafttogether.ctsuite.bungee.messaging.adapter.Sockets4MC;
import de.crafttogether.ctsuite.bungee.util.CTLocation;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class CTSuite extends Plugin {
    private static CTSuite plugin;
    private Configuration config;
    private Configuration messages;
    private HikariDataSource hikari;
    
    private String tablePrefix;
    private String messagingService;

    private MessageHandler messageHandler;
    private PlayerHandler playerHandler;
    private WorldHandler worldHandler;
    private TeleportHandler teleportHandler;
    
    public void onEnable() {
    	plugin = this;
    	
        if (!getDataFolder().exists()) 
        	this.getDataFolder().mkdir();
        
        File configFile = new File(getDataFolder(), "config.yml");
        File messagesFile = new File(getDataFolder(), "messages.yml");
        
        // For development
        if (messagesFile.exists())
        	messagesFile.delete();
        
        try {
            if (!configFile.exists()) {
                configFile.createNewFile();
                InputStream is = getResourceAsStream("config.yml");
                OutputStream os = new FileOutputStream(configFile);
                ByteStreams.copy(is, os);
            }
            if (!messagesFile.exists()) {
                messagesFile.createNewFile();
                InputStream is = getResourceAsStream("messages.yml");
                OutputStream os = new FileOutputStream(messagesFile);
                ByteStreams.copy(is, os);
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to create file", e);
        }
        try {
        	this.config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new InputStreamReader(new FileInputStream(configFile), "UTF8"));
        	this.messages = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new InputStreamReader(new FileInputStream(messagesFile), "UTF8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    	
        this.hikari = new HikariDataSource();
        this.hikari.setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
        this.hikari.addDataSourceProperty("serverName", this.config.get("MySQL.host"));
        this.hikari.addDataSourceProperty("port", this.config.get("MySQL.port"));
        this.hikari.addDataSourceProperty("databaseName", this.config.get("MySQL.database"));
        this.hikari.addDataSourceProperty("user", this.config.get("MySQL.user"));
        this.hikari.addDataSourceProperty("password", this.config.get("MySQL.password"));
        
        this.messagingService = "Sockets4MC"; //config.getString("?");
        this.tablePrefix = config.getString("MySQL.prefix");
        
        // Choose Messaging Adapter
        switch (this.messagingService) {
        	case "Sockets4MC": new Sockets4MC(); break;
        }

        this.messageHandler = new MessageHandler();
        this.playerHandler = new PlayerHandler();
        this.worldHandler = new WorldHandler();
        this.teleportHandler = new TeleportHandler();

    	this.getProxy().getPluginManager().registerListener(this, new PlayerLoginListener());
    	this.getProxy().getPluginManager().registerListener(this, new PlayerLeaveListener());

    	this.playerHandler.readPlayersFromDB();
    }
    
    public void onDisable() {
        plugin.getProxy().getScheduler().runAsync(plugin, new Runnable() {
		    public void run() {
		        PreparedStatement statement = null;
		        Connection connection = null;
		        try {
		        	connection = plugin.getMySQLConnection();
		            statement = connection.prepareStatement("UPDATE " + plugin.getTablePrefix() + "players SET online = 0;");
					statement.execute();
		        } catch (SQLException e) {
		            e.printStackTrace();
		        } finally {
		           if (statement != null) {
		               try { statement.close(); }
		               catch (SQLException e) { e.printStackTrace(); }
		           }
		           if (connection != null) {
		               try { connection.close(); }
		               catch (SQLException e) { e.printStackTrace(); }
		           }
		        }
		        
		        if (plugin.hikari != null) {
		            try {
		            	plugin.hikari.close();
		            } catch (Exception e) { }
		        }
		    }
		});
    }

    public MessageHandler getMessageHandler() {
    	return this.messageHandler;
    }

    public PlayerHandler getPlayerHandler() {
    	return this.playerHandler;
    }
    
    public WorldHandler getWorldHandler() {
    	return this.worldHandler;
    }
    
    public TeleportHandler getTeleportHandler() {
    	return this.teleportHandler;
    }
    
    public Connection getMySQLConnection() {
    	try {
			return this.hikari.getConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	return null;
    }
    
    public String getTablePrefix() {
    	return this.tablePrefix;
    }

	public String getMessagingService() {
		return this.messagingService;
	}

	public Configuration getMessages() {
		return this.messages;
	}

	public Configuration getConfig() {
		return this.config;
	}
    
    public static CTSuite getInstance() {
        return plugin;
    }
}