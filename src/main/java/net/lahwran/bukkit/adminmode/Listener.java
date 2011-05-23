package net.lahwran.bukkit.adminmode;
import java.lang.reflect.Field;
import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nijiko.permissions.Control;

public class Listener implements CommandExecutor 
{

    public final Main plugin;

    public Listener(final Main plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (!(sender instanceof Player))
        {
            sender.sendMessage("You're not a player");
            return true;
        }
        Control control = this.plugin.control;
        synchronized(control)
        {
            Player player = (Player)sender;
            String playername = player.getName();
            String world = player.getWorld().getName();
            
            Field field_WorldCache = null;
            Field field_WorldUserGroups = null;
            Map<String, Map<String, Boolean>> WorldCache = null;
            Map<String, Map<String, String>> WorldUserGroups = null;
            try
            {
                field_WorldCache = Control.class.getDeclaredField("WorldCache");
                field_WorldCache.setAccessible(true);
                
                field_WorldUserGroups = Control.class.getDeclaredField("WorldUserGroups");
                field_WorldUserGroups.setAccessible(true);
                
            
                WorldCache = (Map<String, Map<String, Boolean>>)field_WorldCache.get(control);
                WorldUserGroups = (Map<String, Map<String, String>>)field_WorldUserGroups.get(control);
            }
            catch (Throwable t)
            {
                t.printStackTrace();
            }
            Map<String, Boolean> cache = WorldCache.get(world);
            Map<String, String> worldgroups = WorldUserGroups.get(world);
            
            for(String key:cache.keySet())
            {
                if (key.startsWith(playername+",")) 
                {
                    cache.remove(key);
                } 
            }
            
            String prevGroup = worldgroups.get(playername);
            String newGroup = null;
            if(prevGroup == null || prevGroup.equals("group1"))
                newGroup = "group2";
            else if(prevGroup.equals("group2"))
                newGroup = "group1";
            else 
                return true;
            worldgroups.put(playername, newGroup);
            return true;
        }
    }
}
