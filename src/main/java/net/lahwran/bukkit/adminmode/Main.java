
package net.lahwran.bukkit.adminmode;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Timer;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.nijiko.permissions.Control;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

public class Main extends JavaPlugin {
    public final Listener listener = new Listener(this);
    public PermissionHandler control;
    public Plugin spyer;
    public Method reappear;
    public Method vanish;
    public ArrayList<String> spying;
    public Object spyctl;
    public ArrayList<String> antigrief;
    public HashMap<String, Timer> timers;
    public HashMap<String, Integer> schedulers;
    public HashSet<String> commonPlayers;
    public HashMap<String, ArrayList<String>> playerHideTree;

    public Plugin worldguard;
    public Object worldguard_config;
    public Method worldguard_enableGodMode;
    public Method worldguard_disableGodMode;
    
    public void onEnable() {
        getCommand("mode").setExecutor(listener);

        this.control = ((Permissions) this.getServer().getPluginManager().getPlugin("Permissions")).getHandler();
        
    }
    public void onDisable() {}
    
    @SuppressWarnings("unchecked")
    public boolean isSpyerReady()
    {
        if(spyer == null)
        {
            try{
                spyer = this.getServer().getPluginManager().getPlugin("SpyerAdmin");
                
            } catch (Throwable t) {
                t.printStackTrace();
                System.err.println("SpyerAdmin not found, not good!");
                spyer = null;
                return false;
            }
        }
        try{
            Class SpyerAdmin = Class.forName("nickguletskii200.SpyerAdmin.SpyerAdmin");
            Class SpyerListener = Class.forName("nickguletskii200.SpyerAdmin.SpyerAdminPlayerListener");
            Field SpyerAdmin_spying = SpyerAdmin.getDeclaredField("spying");
            Field SpyerAdmin_antigrief = SpyerAdmin.getDeclaredField("antigrief");
            
            Field Listener_timers = SpyerListener.getDeclaredField("timers");
            Field Listener_schedulers = SpyerListener.getDeclaredField("schedulers");
            Field Listener_commonPlayers = SpyerListener.getDeclaredField("commonPlayers");
            Field Listener_playerHideTree = SpyerListener.getDeclaredField("playerHideTree");
            
            Method getPlayerListener = SpyerAdmin.getMethod("getPlayerListener");
            
            SpyerAdmin_spying.setAccessible(true);
            SpyerAdmin_antigrief.setAccessible(true);
            
            Listener_timers.setAccessible(true);
            Listener_schedulers.setAccessible(true);
            Listener_commonPlayers.setAccessible(true);
            Listener_playerHideTree.setAccessible(true);
            
            Object spyerlistener = getPlayerListener.invoke(spyer);
            if(spyerlistener == null) return false;
            ArrayList<String> spying = (ArrayList<String>)SpyerAdmin_spying.get(spyer);
            ArrayList<String> antigrief = (ArrayList<String>)SpyerAdmin_antigrief.get(spyer);
            
            HashMap<String, Timer> timers = (HashMap<String, Timer>)Listener_timers.get(spyerlistener);
            HashMap<String, Integer> schedulers = (HashMap<String, Integer>)Listener_schedulers.get(spyerlistener);
            HashSet<String> commonPlayers = (HashSet<String>)Listener_commonPlayers.get(spyerlistener);
            HashMap<String, ArrayList<String>> playerHideTree = (HashMap<String, ArrayList<String>>)Listener_playerHideTree.get(spyerlistener);
            
            if(spying == null || antigrief == null) return false;
            if(this.vanish != null) return true;
            
            Method vanish = SpyerListener.getMethod("vanish", Player.class);
            Method reappear = SpyerListener.getMethod("reappear", Player.class);
            
            
            this.vanish = vanish;
            this.reappear = reappear;
            this.spying = spying;
            this.antigrief = antigrief;
            this.spyctl = spyerlistener;
            this.timers = timers;
            this.schedulers = schedulers;
            this.commonPlayers = commonPlayers;
            this.playerHideTree = playerHideTree;
            
            return true;
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }
    
    public void hide(Player p)
    {
        if (!isSpyerReady())
        {
            System.err.println("WARNING: TRIED TO HIDE PLAYER '"+p.getDisplayName()+"' WHILE SPYER WAS NOT READY!");
            p.sendMessage("-- WARNING: THE SPYER PLUGIN IS NOT READY! YOUR NAME IS NOT UPDATED! try /spy in a moment.");
            return;
        }
        try
        {
            vanish.invoke(spyctl, p);
            spying.add(p.getName());
        }
        catch (Throwable e)
        {
            System.err.println("WARNING: TRIED TO HIDE PLAYER '"+p.getDisplayName()+"', THREW EXCEPTION");
            p.sendMessage("-- WARNING: THE SPYER PLUGIN FAILED TO HIDE YOU! YOUR NAME IS NOT UPDATED! try /spy in a moment.");
            e.printStackTrace();
        }
    }
    public void show(Player p)
    {
        if (!isSpyerReady())
        {
            System.err.println("attempted to unvanish player "+p.getDisplayName()+" while spyer was not ready");
            p.sendMessage("-- attempted to unvanish you while spyer was not ready - try again in a moment.");
            return;
        }
        try
        {
            reappear.invoke(spyctl, p);
            spying.remove(p.getName());
        }
        catch (Throwable e)
        {
            p.sendMessage("-- warning: spyer failed to unvanish you");
            e.printStackTrace();
        }
    }
    public void spyrename(Player p, String newname)
    {
        if (!isSpyerReady())
        {
            System.err.println("FAILED TO NOTIFY SPYERADMIN OF RENAME - BAD THINGS WILL HAPPEN!");
            p.sendMessage("-- SpyerAdmin failed to rename you. This is very bad.");
            return;
        }
        String oldname = p.getName();
        if(oldname.equals(newname)) return;
        try{
            if(timers.containsKey(oldname))
            {
                timers.put(newname, timers.get(oldname));
                timers.remove(oldname);
            }
            if(schedulers.containsKey(oldname))
            {
                schedulers.put(newname, schedulers.get(oldname));
                schedulers.remove(oldname);
            }
            
            if(commonPlayers.contains(oldname))
            {
                commonPlayers.remove(oldname);
                commonPlayers.add(newname);
            }
            if(playerHideTree.containsKey(oldname))
            {
                playerHideTree.put(newname, playerHideTree.get(oldname));
                playerHideTree.remove(oldname);
            }
            for(ArrayList<String> list:playerHideTree.values())
            {
                while(list.contains(oldname))
                {
                    int idx = list.indexOf(oldname);
                    list.set(idx, newname);
                }
            }
        }
        catch (Throwable e) {
            p.sendMessage("-- SpyerAdmin failed to rename you. This is very bad.");
            e.printStackTrace();
        }
    }

    public void god(Player player) {
        if (!isWorldGuardReady())
        {
        //ConfigurationManager config = plugin.getGlobalStateManager();
        //} catch(Throwable t) {
            player.sendMessage("-- Failed to tell worldedit to god you! check console.");
            return;
        }
        try
        {
            worldguard_enableGodMode.invoke(worldguard_config, player);
            player.setFireTicks(0);
            player.sendMessage(ChatColor.YELLOW + "God mode enabled! Use /ungod to disable.");
        }
        catch (Throwable e)
        {
            player.sendMessage("-- Failed to tell worldedit to god you! check console.");
            e.printStackTrace();
        }
    }
    
    public void ungod(Player player) {
        if (!isWorldGuardReady())
        {
        //ConfigurationManager config = plugin.getGlobalStateManager();
        //} catch(Throwable t) {
            player.sendMessage("-- Failed to tell worldedit to ungod you! check console.");
            return;
        }
        try
        {
            worldguard_disableGodMode.invoke(worldguard_config, player);
            player.sendMessage(ChatColor.YELLOW + "God mode disabled!");
        }
        catch (Throwable e)
        {
            player.sendMessage("-- Failed to tell worldedit to ungod you! check console.");
            e.printStackTrace();
        }
    }
    /**
     * @return
     */
    private boolean isWorldGuardReady()
    {
        if(worldguard == null)
        {
            try{
                worldguard = this.getServer().getPluginManager().getPlugin("WorldGuard");
                
            } catch (Throwable t) {
                t.printStackTrace();
                System.err.println("WorldGuard not found, not good!");
                worldguard = null;
                return false;
            }
        }
        if(this.worldguard_config != null && this.worldguard_enableGodMode != null
                && this.worldguard_disableGodMode != null) return true;
        
        try{
            Class WorldGuardPlugin = Class.forName("com.sk89q.worldguard.bukkit.WorldGuardPlugin");
            Class WorldGuardConfig = Class.forName("com.sk89q.worldguard.bukkit.ConfigurationManager");
            
            Method getGlobalStateManager = WorldGuardPlugin.getDeclaredMethod("getGlobalStateManager");
            Method enableGodMode = WorldGuardConfig.getDeclaredMethod("enableGodMode");
            Method disableGodMode = WorldGuardConfig.getDeclaredMethod("disableGodMode");
            
            Object config = getGlobalStateManager.invoke(worldguard);
            
            this.worldguard_config = config;
            this.worldguard_enableGodMode = enableGodMode;
            this.worldguard_disableGodMode = disableGodMode;
            
            return true;
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }

}
