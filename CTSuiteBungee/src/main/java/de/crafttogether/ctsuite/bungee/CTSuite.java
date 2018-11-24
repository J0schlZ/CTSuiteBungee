package de.crafttogether.ctsuite.bungee;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import com.google.common.io.ByteStreams;
import com.zaxxer.hikari.HikariDataSource;

import de.crafttogether.ctsuite.bungee.events.PlayerLeaveListener;
import de.crafttogether.ctsuite.bungee.events.PlayerLoginListener;
import de.crafttogether.ctsuite.bungee.events.PlayerPostLoginListener;
import de.crafttogether.ctsuite.bungee.events.PlayerSwitchedServerListener;
import de.crafttogether.ctsuite.bungee.handlers.PlayerHandler;
import de.crafttogether.ctsuite.bungee.util.PMessage;
import de.crafttogether.ctsuite.bungee.util.PMessageListener;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class CTSuite extends Plugin {
	
    private static CTSuite instance;
    private HikariDataSource hikari;
    private Configuration config;
    private String tablePrefix;
    private SimpleDateFormat dateFormat;
    private PlayerHandler playerHandler;
    
    public void onEnable() {
        instance = this;
        
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }
        
        File configFile = new File(getDataFolder(), "config.yml");
        File messagesFile = new File(getDataFolder(), "messages.yml");
        
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
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new InputStreamReader(new FileInputStream(configFile), "UTF8"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        hikari = new HikariDataSource();
        hikari.setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
        hikari.addDataSourceProperty("serverName", this.config.get("MySQL.host"));
        hikari.addDataSourceProperty("port", this.config.get("MySQL.port"));
        hikari.addDataSourceProperty("databaseName", this.config.get("MySQL.database"));
        hikari.addDataSourceProperty("user", this.config.get("MySQL.user"));
        hikari.addDataSourceProperty("password", this.config.get("MySQL.password"));
        
        tablePrefix = this.config.getString("MySQL.prefix");
        dateFormat = new SimpleDateFormat(config.getString("CTSuite.Messages.TimeFormat"));

        playerHandler = new PlayerHandler(this);
        
        getProxy().getPluginManager().registerListener(this, new PlayerLoginListener(this));
        getProxy().getPluginManager().registerListener(this, new PlayerPostLoginListener(this));
        getProxy().getPluginManager().registerListener(this, new PlayerSwitchedServerListener(this));
        getProxy().getPluginManager().registerListener(this, new PlayerLeaveListener(this));

        getProxy().registerChannel("ctsuite:bukkit");
        getProxy().registerChannel("ctsuite:bungee");
        getProxy().getPluginManager().registerListener(this, new PMessageListener(this));

        playerHandler.readPlayersFromDB();
    }

    public void onDisable() {
        if (hikari != null) {
            try {
                hikari.close();
            } catch (Exception e) { }
        }
    }
    
    public Configuration getConfig() {
        return config;
    }
    
    public String getTablePrefix() {
    	return tablePrefix;
    }

    public PlayerHandler getPlayerHandler() {
        return playerHandler;
    }
    
    public Connection getConnection() {
        try {
			return this.hikari.getConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
    }
    
    public SimpleDateFormat getDateFormat() {
        return dateFormat;
    }
    
    public static CTSuite getInstance() {
        return instance;
    }
}