package de.crafttogether.ctsuite.bungee.util;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;

import de.crafttogether.ctsuite.bungee.CTSuite;
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
            
            switch(messageName) {
            	
            	case "bungee.player.updatePrefixSuffix":
            		// 0 = uuid, 1 = prefix, 2 = suffix
            		main.getPlayerHandler().setPrefix(values.get(0), values.get(1));
            		main.getPlayerHandler().setSuffix(values.get(0), values.get(2));
            		PMessage pm = new PMessage(main, "bukkit.player.setGamemode");
            		pm.put(values.get(0));
            		pm.send(serverName);
            		break;
            	
            		
            	case "bungee.player.setGamemode":
            		
            		break;
            }
    	}
    }
}