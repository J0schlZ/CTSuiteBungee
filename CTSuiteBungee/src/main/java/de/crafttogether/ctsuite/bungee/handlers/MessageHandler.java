package de.crafttogether.ctsuite.bungee.handlers;

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
	
	public MessageHandler() {
		this.plugin = CTSuite.getInstance();
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
	        	send(uuid, getMessage("info.noPermission", placeHolder));
				break;
				
			case "player.inform.message.send":
				send((UUID) ev.getValue("uuid"), (String) ev.getValue("message"));
				break;
		}
    }	

	//
    public void send(UUID uuid, TextComponent message) {
    	ProxiedPlayer p = plugin.getProxy().getPlayer(uuid);
    	
    	if (p != null && p.isConnected())
    		p.sendMessage(message);
    }
    
    //
    public void send(UUID uuid, String message) {
    	ProxiedPlayer p = plugin.getProxy().getPlayer(uuid);
    	
    	if (p != null && p.isConnected())
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

    //
    public TextComponent getMessage(String path, HashMap<String, String> placeHolders) {
    	String message = plugin.getMessages().getString(path);
    	
    	if (message == null) {
         	plugin.getLogger().info("Message '" + path + "' could not be found. Please update your messages.yml");
         	return null;
    	}
        
    	if (placeHolders != null)
    		message = applyPlaceHolders(message, placeHolders);
    	
    	TextComponent messageComponent = translateColorCodes(message);
        
        if (plugin.getMessages().contains(path + "Hover")) {
        	HoverEvent.Action hoverAction = HoverEvent.Action.SHOW_TEXT;
        	String hoverMessage = applyPlaceHolders(plugin.getMessages().getString(path + "Hover"), placeHolders);
        	TextComponent hoverComponent = translateColorCodes(hoverMessage);
        	HoverEvent hover = new HoverEvent(hoverAction, new ComponentBuilder(hoverComponent.toLegacyText()).create());
        	messageComponent.setHoverEvent(hover);
        }
        
        return messageComponent;
    }
    
    public TextComponent getMessage(String path) {
        return getMessage(path, null);
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
