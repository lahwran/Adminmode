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

    @SuppressWarnings("unchecked")
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (!(sender instanceof Player))
        {
            sender.sendMessage("You're not a player");
            return true;
        }
        String from;
        String message;
        String to;
        boolean cur = false;
        if (args.length != 1)
        {
            sender.sendMessage("usage: /mode <play|admin|cur>");
            return true;
        }
        if(args[0].equals("play"))
        {
            from="admin";
            to="play";
            message="\u00a7e * %s has entered play mode";
        }
        else if (args[0].equals("admin"))
        {
            from="play";
            to="admin";
            message="\u00a7e * %s has entered admin mode";
        }
        else if(args[0].equals("cur"))
        {
            message = to = from = null;
            cur = true;
        }
        else return true;
        Control control = this.plugin.control;
        synchronized(control)
        {
            do {
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
                Map<String, Boolean> cache = null;
                Map<String, String> worldgroups = null;
                //synchronized(WorldCache)
                //{
                    cache = WorldCache.get(world);
                //}
                //synchronized(WorldUserGroups)
                //{
                    worldgroups = WorldUserGroups.get(world);
                //}
                
                //synchronized(cache)
                //{
                    for(String key:cache.keySet())
                    {
                        if (key.startsWith(playername+",")) 
                        {
                            cache.remove(key);
                        } 
                    }
                //}
                String prevGroup = worldgroups.get(playername);
                if (cur)
                {
                    String m;
                    if(prevGroup.endsWith("-admin"))
                        m="admin";
                    else if (prevGroup.endsWith("-play"))
                        m="play";
                    else
                    {
                        player.sendMessage("you are not admin.");
                        break;
                    }
                    player.sendMessage("you are in "+m+" mode.");
                    break;
                }
                String newGroup;
                if(prevGroup.endsWith("-"+from))
                {
                    newGroup = prevGroup.replace("-"+from, "-"+to);
                }
                else 
                {
                    player.sendMessage("-- available modes are 'admin' and 'play'.");
                    break;
                }
                player.getServer().broadcastMessage(String.format(message, player.getDisplayName()));
                worldgroups.put(playername, newGroup);
                
            }while(false);
        }
        return true;
    }
}
