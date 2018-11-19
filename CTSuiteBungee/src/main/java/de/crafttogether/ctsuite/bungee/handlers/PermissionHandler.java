package de.crafttogether.ctsuite.bungee.handlers;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.crafttogether.ctsuite.bungee.CTSuite;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class PermissionHandler {

    private CTSuite main;
    private List<String> availablePermissions;
    private HashMap<String, List<String>> permissions;

    public PermissionHandler(CTSuite main) {
        this.main = main;
        permissions = new HashMap<String, List<String>>();
        availablePermissions = new ArrayList<String>();
    }

    public void updatePermissions() {
        for (ProxiedPlayer p : main.getProxy().getPlayers())
            updatePermissions(p);
    }

    public void updatePermissions(ProxiedPlayer p) {
        //PluginMessage pm = new PluginMessage(this)
    	ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);
        
        
        try {
            out.writeUTF("GetPermissions");
            out.writeUTF(p.getName());
            for (String perm : availablePermissions)
                out.writeUTF(perm);
        } catch (IOException e) {
            e.printStackTrace();
        }
        p.getServer().sendData("ProxySuite", b.toByteArray());
    }

    public void readAvailablePermissionsFromFile() {
        try {
            BufferedReader read = new BufferedReader(new InputStreamReader(main.getClass().getResourceAsStream
                    ("/availablePermissions.yml")));
            String line;
            while ((line = read.readLine()) != null) {
                line = line.trim();
                if (!line.trim().equals("") && !line.startsWith("#"))
                    availablePermissions.add(line.trim());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void resetPermissions() {
        for (ProxiedPlayer p : main.getProxy().getPlayers())
            resetPermissions(p);
    }

    public void resetPermissions(CommandSender player) {
        permissions.remove(player.getName());
    }

    public void resetPermissions(String player) {
        permissions.remove(player);
    }

    public boolean hasPermission(CommandSender sender, String permission) {
        return hasPermission(sender, permission, false);
    }

    public boolean hasPermission(CommandSender sender, String permission, boolean ignoreWildcard) {
        if (sender instanceof ProxiedPlayer) {
            ProxiedPlayer p = (ProxiedPlayer) sender;
            if (permissions.containsKey(p.getName())) {
                if (permissions.get(p.getName()).contains(permission.toLowerCase()) || (!ignoreWildcard && permissions.get(p.getName()).contains("*")) || (!ignoreWildcard && permissions.get(p.getName()).contains(permission.toLowerCase() + ".*")))
                    return true;
                else if (!ignoreWildcard) {
                    String check = "";
                    for (String s : permission.toLowerCase().split("\\.")) {
                        check = check + s + ".";
                        if (permissions.get(p.getName()).contains(check + "*")) {
                            main.getLogger().info(sender.getName() + " has '" + check + "*'");
                            return true;
                        }
                    }
                }
            }
        } else
            return true;
        return false;
    }

    public boolean hasPermission(String player, String permission) {
        if (permissions.containsKey(player)) {
            if (permissions.get(player).contains(permission.toLowerCase()) || permissions.get(player)
                    .contains("*"))
                return true;
            else {
                String check = "";
                for (String s : permission.toLowerCase().split("\\.")) {
                    check = check + s + ".";
                    if (permissions.get(player).contains(check + "*"))
                        return true;
                }
            }
        }
        return false;
    }

    public void sendMissingPermissionInfo(CommandSender sender) {
        main.getMessageHandler().sendMessage(sender, main.getMessageHandler().getMessage("command.nopermission"));
    }

    public HashMap<String, List<String>> getPermissions() {
        return permissions;
    }

    public List<String> getAvailablePermissions() {
        return availablePermissions;
    }
}
