package de.crafttogether.ctsuite.bungee.util;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;

import de.crafttogether.ctsuite.bungee.CTSuite;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PMessageListener implements Listener {
    private CTSuite main;

    public PMessageListener(CTSuite main) {
        this.main = main;
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent ev) {

    	if (ev.getTag().equals("ctsuite:bungee")) {
    		ArrayList<String> values = new ArrayList<String>();
            ByteArrayInputStream stream = new ByteArrayInputStream(ev.getData());
            DataInputStream in = new DataInputStream(stream);
                    

            String value;
    		String messageName = null;
    		String serverName = null;
            
            try {
                messageName = in.readUTF();                
                serverName = in.readUTF();
                
                try {
                	while ((value = in.readUTF()) != null)
                		values.add(value);
                }
                catch (EOFException e) { }
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					stream.close();
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

            System.out.println("[PMessage][" + serverName + "->Bungee]: " + messageName);
            TextComponent text;
            switch(messageName) {
            	
            	case "bungee.player.update.joined":
            		/*
            		 *  0 => (str)	uuid		
            		 *  1 => (str)	server			
            		 *  2 => (str)	world				
            		 *  3 => (str)	prefix			
            		 *  4 => (str)	suffix
            		 */
            		main.getPlayerHandler().setServer(values.get(0), values.get(1));
            		main.getPlayerHandler().setWorld(values.get(0), values.get(2));
            		main.getPlayerHandler().setPrefix(values.get(0), values.get(3));
            		main.getPlayerHandler().setSuffix(values.get(0), values.get(4));
            		main.getPlayerHandler().sendPlayerListToServer("all");
            		break;
                	
            	case "bungee.player.cmd.fly":
            		/*
            		 * 0 => (str)	senderUUID
            		 * 1 => (str)	targetName
            		 * 2 => (str)	on|off|toggle
            		 * 3 => (bool)	apply
            		 */
            		main.getPlayerHandler().updateIsAllowedFlight(values.get(1), values.get(0), values.get(2), (values.get(3).equals("true") ? true : false));
            		break;
                	
            	case "bungee.player.cmd.gamemode":
            		/*
            		 * 0 => (str)	senderUUID
            		 * 1 => (str)	targetName
            		 * 2 => (str)	gameMode
            		 * 3 => (bool)	apply
            		 */
            		main.getPlayerHandler().updateGamemode(values.get(1), values.get(0), values.get(2), (values.get(3).equals("true") ? true : false));
            		break;
            		
            	case "bungee.player.cmd.tp":
            		/*
            		 * 0 => (str)	playerName
            		 * 1 => (str) 	targetName
            		 */
            		main.getTeleportHandler().playerToPlayer(values.get(0), values.get(1));
            		break;
            		
            	case "bungee.player.cmd.tppos":
            		/*
            		 * 0 => (str)	playerName
            		 * 1 => (str) 	location 'server:world:x:y:z:yaw:pitch'
            		 */
            		main.getTeleportHandler().playerToPos(values.get(0), values.get(1));
            		break;
            	
            	case "bungee.player.inform.permissionDenied":
            		/*
            		 * 0 => (str)	uuid
            		 * 1 => (str)	permission
            		 */
            		// Temp
            		text = new TextComponent("Dazu hast du keine Berechtigung");
            		text.setColor(ChatColor.RED);
            		text.setHoverEvent( new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(values.get(1)).create()));
            		main.getPlayerHandler().sendMessage(values.get(0), text);
            		break;
            		
            	case "bungee.player.inform.sendMessage":
            		/*
            		 * 0 => (str)	uuid
            		 * 1 => (str)	messageKey
            		 */
            		// Temp
            		text = new TextComponent(values.get(1));
            		text.setColor(ChatColor.WHITE);
            		main.getPlayerHandler().sendMessage(values.get(0), text);
            		break;

            	case "bungee.data.request.onlinePlayers":
            		/*
            		 * 0 => (str)	server
            		 */
            		main.getPlayerHandler().sendPlayerListToServer(values.get(0));
            		break;
            }
    	}
    }
}