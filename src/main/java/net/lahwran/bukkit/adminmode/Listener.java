package net.lahwran.bukkit.adminmode;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
        Player player = (Player)sender;
        String playername = player.getName();
        
        
        
        boolean currentlyadmin = false;

        String newname;
        if (playername.startsWith("\u00a74"))
        {
            currentlyadmin=true;
            newname = playername.substring(2);
        }
        else
        {
            currentlyadmin=false;
            newname = "\u00a74" + playername;
        }
        

        if (! plugin.control.has(player, String.format("adminmode.%s", (!currentlyadmin ? "admin" : "play"))))
        {
            sender.sendMessage(String.format("you don't have permission to enter %s mode.",(!currentlyadmin ? "admin" : "play")));
            return true;
        }
        
        String message = null;
        if (args.length != 1 || args[0].equals("help"))
        {
            sender.sendMessage("usage: /mode <play|admin|cur|help>");
            return true;
        }
        if(args[0].equals("play"))
        {
            if(!currentlyadmin)
            {
                player.sendMessage("-- you are in play mode.");
                return true;
            }
            message="\u00a7e * %s has entered play mode";
        }
        else if (args[0].equals("admin"))
        {
            if(currentlyadmin)
            {
                player.sendMessage("-- you are in admin mode.");
                return true;
            }
            message="\u00a7e * %s has entered admin mode";
        }
        else if(args[0].equals("cur"))
        {
            player.sendMessage(String.format("-- you are in %s mode",(currentlyadmin ? "admin" : "play")));
            return true;
        }
        else
        {
            
            player.sendMessage("-- available modes are admin and play. see /mode help.");
            return true;
        }


        plugin.hide(player);
        plugin.spyrename(player, newname);
        try
        {
            Class CraftPlayer = Class.forName("org.bukkit.craftbukkit.entity.CraftPlayer");
            Class EntityHuman = Class.forName("net.minecraft.server.EntityHuman");
            Method getHandle = CraftPlayer.getMethod("getHandle");
            Object entityplayer = getHandle.invoke(player);
            Field name = EntityHuman.getField("name");
            
            name.setAccessible(true);
            name.set(entityplayer, newname);
            
            currentlyadmin = !currentlyadmin;
        }
        catch (ClassNotFoundException e)
        {
            sender.sendMessage("-- you don't seem to be on craftbukkit. This plugin cheats, so it only works on craftbukkit.");
            e.printStackTrace();
            return true;
        }
        catch (SecurityException e)
        {
            sender.sendMessage("-- the plugin doesn't seem to be allowed to do that.");
            e.printStackTrace();
            return true;
        }
        catch (NoSuchMethodException e)
        {
            sender.sendMessage("-- craftplayer is insane");
            e.printStackTrace();
            return true;
        }
        catch (IllegalArgumentException e)
        {
            sender.sendMessage("-- craftplayer is insane");
            e.printStackTrace();
            return true;
        }
        catch (IllegalAccessException e)
        {
            sender.sendMessage("-- craftplayer is insane");
            e.printStackTrace();
            return true;
        }
        catch (InvocationTargetException e)
        {
            sender.sendMessage("-- craftplayer is insane");
            e.printStackTrace();
            return true;
        }
        catch (NoSuchFieldException e)
        {
            sender.sendMessage("-- EntityHuman is insane");
            e.printStackTrace();
            return true;
        }
        try
        {
            Thread.sleep(250);
        }
        catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (!currentlyadmin) plugin.show(player);
        player.getServer().broadcastMessage(String.format(message, player.getDisplayName()));
        return true;
    }
}
