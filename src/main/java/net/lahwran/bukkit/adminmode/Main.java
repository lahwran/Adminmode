
package net.lahwran.bukkit.adminmode;

import org.bukkit.plugin.java.JavaPlugin;

import com.nijiko.permissions.Control;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

public class Main extends JavaPlugin {
	public final Listener listener = new Listener(this);
	public Control control;
	
	public void onEnable() {
        getCommand("mode").setExecutor(listener);

        this.control = (Control)((Permissions) this.getServer().getPluginManager().getPlugin("Permissions")).getHandler();

	}
	public void onDisable() {}

}
