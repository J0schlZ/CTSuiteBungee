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
            	
            	case "bungee.player.update.prefixSuffix":
            		/*
            		 *  0 => (str)	uuid
            		 *	1 => (str)	prefix
            		 *  2 => (str)	suffix
            		 */
            		main.getPlayerHandler().setPrefix(values.get(0), values.get(1));
            		main.getPlayerHandler().setSuffix(values.get(0), values.get(2));
            		break;
            	
            	case "bungee.player.update.isAllowedFlight":
            		/*
            		 * 0 => (str)	uuid
            		 * 1 => (str)	senderUUID
            		 * 2 => (bool)	isAllowedFlight
            		 * 3 => (bool)	apply
            		 */
            		main.getPlayerHandler().updateIsAllowedFlight(values.get(0), values.get(1), ((values.get(2).equals("true")) ? true : false), (values.get(3).equals("true") ? true : false));
            		break;
            	
            }
    	}
    }
}