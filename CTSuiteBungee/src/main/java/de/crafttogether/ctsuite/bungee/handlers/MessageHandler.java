package de.crafttogether.ctsuite.bungee.handlers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import de.crafttogether.ctsuite.bungee.CTSuite;
import de.crafttogether.ctsuite.bungee.messaging.NetworkMessageEvent;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class MessageHandler implements Listener {
	private CTSuite plugin;
	private HashMap<String, String> messages;
	
	public MessageHandler() {
		this.plugin = CTSuite.getInstance();
		this.messages = new HashMap<String, String>();
	}
	
	@EventHandler
    public void onNetworkMessage(NetworkMessageEvent ev) {
		switch(ev.getMessageKey())
		{
			case "player.inform.permission.denied":
				UUID uuid = (UUID) ev.getValue("uuid");
				String playerName = CTSuite.getInstance().getPlayerHandler().getName(uuid);
				if (playerName == null) break;
				
				HashMap<String, String> placeHolder = new HashMap<String, String>();
	        	placeHolder.put("player", playerName);
	        	placeHolder.put("permission", (String) ev.getValue("permission"));
	        	send(uuid, getMessage("permission.denied", placeHolder));
				break;
				
			case "player.inform.message.send":
				send((UUID) ev.getValue("uuid"), (String) ev.getValue("message"));
				break;
		}
    }
	
	public void readMessages() {
		BufferedReader read = null;
        this.messages = new HashMap<String, String>();
        
        File f = new File(plugin.getDataFolder(), "messages.yml");
        try {
            read = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF8"));
            String line;
            while ((line = read.readLine().toString()) != null) {
                line = line.trim();
                if (!line.equals("") && !line.startsWith("#")) {
                    String[] split = line.split(": ");
                    String msg = "";
                    for (int i = 1; i < split.length; i++)
                        msg += split[i] + ": ";
                    msg = msg.substring(1, msg.length() - 3);
                    messages.put(split[0], msg);
                    System.out.println(split[0] + " -> " + msg);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
			try {
				read.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
    }

	//
    public void send(UUID uuid, TextComponent message) {
    	ProxiedPlayer p = plugin.getProxy().getPlayer(uuid);
    	
    	if (p != null)
    		p.sendMessage(message);
    }
    
    //
    public void send(UUID uuid, String message) {
    	ProxiedPlayer p = plugin.getProxy().getPlayer(uuid);
    	
    	if (p != null)
    		p.sendMessage(translateColorCodes(message));
    }
    
    public void send(ProxiedPlayer p, TextComponent message) {
    	send(p.getUniqueId(), message);
    }
    
    public void send(ProxiedPlayer p, String message) {
    	send(p.getUniqueId(), message);
    }

    public void send(String playerName, TextComponent message) {
    	UUID uuid = CTSuite.getInstance().getPlayerHandler().getUUID(playerName);
    	
    	if (uuid != null)
    		send(uuid, message);
    }
    
    public void send(String playerName, String message) {
    	UUID uuid = CTSuite.getInstance().getPlayerHandler().getUUID(playerName);
    	
    	if (uuid != null)
    		send(uuid, message);
    }

    //
    public void broadcast(TextComponent message) {
        if (message != null)
           for (ProxiedPlayer p : plugin.getProxy().getPlayers())
            	send(p, message);
    }
    
    public void broadcast(String message) {
        TextComponent text = new TextComponent();
        text.setText(message);
        broadcast(text);
    }

    // ##
    public TextComponent getMessage(String identifier, HashMap<String, String> placeHolders) {
    	String message = messages.get(identifier).toString();
    	
    	if (message == null) {
         	plugin.getLogger().info("Message '" + identifier + "' could not be found. Please update your messages.yml");
         	return null;
    	}
        
    	if (placeHolders != null)
    		message = applyPlaceHolders(message, placeHolders);
    	
    	TextComponent messageComponent = translateColorCodes(message);
        
        if (messages.containsKey(identifier + ".hover")) {
        	HoverEvent.Action hoverAction = HoverEvent.Action.SHOW_TEXT;
        	String hoverMessage = applyPlaceHolders(messages.get(identifier + ".hover"), placeHolders);
        	TextComponent hoverComponent = translateColorCodes(hoverMessage);
        	HoverEvent hover = new HoverEvent(hoverAction, new ComponentBuilder(hoverComponent.toLegacyText()).create());
        	messageComponent.setHoverEvent(hover);
        }
        
        return messageComponent;
    }
    
    public TextComponent getMessage(String identifier) {
        return getMessage(identifier, null);
    }

    private String applyPlaceHolders(String str, HashMap<String, String> placeHolders) {
    	if (placeHolders != null) {
	    	for (Entry<String, String> entry : placeHolders.entrySet())
	    		str = str.replace("{" + entry.getKey() + "}", entry.getValue());
    	}
    	return str;
    }
    
    public TextComponent translateColorCodes(String text) {
        TextComponent textComponent = new TextComponent("");
        for (BaseComponent baseComponent : TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', text)))
            textComponent.addExtra(baseComponent);
        return textComponent;
    }
}
