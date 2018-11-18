package de.crafttogether.ctsuite.bungee.util;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;

import de.crafttogether.ctsuite.bungee.CTSuite;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PluginMessageListener implements Listener {
    private CTSuite main;

    public PluginMessageListener(CTSuite main) {
        this.main = main;
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent ev) {
    	
    	if (ev.getTag().equals("CTSuite")) {
    		
    		String messageName = null;
    		ArrayList<String> values = new ArrayList<String>();
            ByteArrayInputStream stream = new ByteArrayInputStream(ev.getData());
            DataInputStream in = new DataInputStream(stream);
            
            try {
                messageName = in.readUTF();
				while (in.available() > 0) {
					values.add(in.readUTF());
				}
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

            switch(messageName) {
            	
            	case "bungee.player.updatePrefixSuffix":
            		// 0 = uuid, 1 = prefix, 2 = suffix
            		
            		main.getPlayerHandler().updatePrefixSuffix(values.get(0), values.get(1), values.get(2));
            		break;
            	
            		
            	case "bungee.player.setGamemode":
            		
            		break;
            }
    	}
    }
}