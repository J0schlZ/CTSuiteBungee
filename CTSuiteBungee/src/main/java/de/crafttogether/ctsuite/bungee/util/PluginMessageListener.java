package de.crafttogether.ctsuite.bungee.util;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;

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

    	if (ev.getTag().equals("ctsuite:bungee")) {
        	System.out.println("Receive PluginMessage [" + ev.getTag() + "]");
    		
    		String messageName = null;
    		String serverName = null;
    		ArrayList<String> values = new ArrayList<String>();
            ByteArrayInputStream stream = new ByteArrayInputStream(ev.getData());
            DataInputStream in = new DataInputStream(stream);
                      
            try {
                messageName = in.readUTF();                
                serverName = in.readUTF();
                String value;
                
                try {
                    while ((value = in.readUTF()) != null) {
                    	System.out.println(value);
                        values.add(value);
                    }
                } catch (EOFException ex) { }
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

            System.out.println("Receive PluginMessage from " + serverName + " [" + messageName + "]");
            
            switch(messageName) {
            	
            	case "bungee.player.updatePrefixSuffix":
            		// 0 = uuid, 1 = prefix, 2 = suffix
            		System.out.println("Prefix: " + values.get(1) + " Suffix: " + values.get(2));
            		//main.getPlayerHandler().setPrefix(values.get(0), values.get(1));
            		//main.getPlayerHandler().setSuffix(values.get(0), values.get(2));
            		break;
            	
            		
            	case "bungee.player.setGamemode":
            		
            		break;
            }
    	}
    }
}