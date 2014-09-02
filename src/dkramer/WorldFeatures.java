package dkramer;

import java.io.File;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.io.Files;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.schematic.MCEditSchematicFormat;

public class WorldFeatures extends JavaPlugin {
	public static WorldFeatures instance;

    public static final Logger logger = Logger.getLogger("Minecraft");
    private static final HashMap<Player, PlayerInfo> playerInfos = new HashMap<Player, PlayerInfo>();
    private static final HashMap<String, BetterConfiguration> configs = new HashMap<String, BetterConfiguration>();

    public ChunkListener c1;
    public PlayerListener p1;
    
    
    public static String defaultCuboidPicker = "SEEDS";
        
    public void onDisable() {
        logger.info("WorldSchematics Disabled");
    }

    public void onEnable() {
    	instance = this;
        new File("plugins/WorldSchematics/Schematics").mkdirs();
        new File("plugins/WorldSchematics/Schematics/world").mkdirs();
        //Confusing stuff to pass this to that to this and back
        c1 = new ChunkListener(this);
        p1 = new PlayerListener(this);
        
        getServer().getPluginManager().registerEvents(new ChunkListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				saveAllConfigs();
			}
        }, 20 * 60 * 15, 20 * 60 * 15);
        this.saveDefaultConfig();
    }
    
    private void saveAllConfigs() {
    	System.out.println("[WorldSchematics] Saving Information...");
    	for(BetterConfiguration config : configs.values()) {
        	config.save();
        }
    	System.out.println("[WorldSchematics] Done Saving!");
    }
    
    public static BetterConfiguration getConfig(String path) {
    	if(!configs.containsKey(path)) {
    		BetterConfiguration config = new BetterConfiguration(path + ".yml");
    		config.load();
    		configs.put(path, config);
    	}
    	return configs.get(path);
    }
    
    
    public static PlayerInfo getPlayerInfo(Player player) {
    	if(!playerInfos.containsKey(player)) {
    		playerInfos.put(player, new PlayerInfo());
    	}
    	return playerInfos.get(player);
    }

    private void saveArea(World world, Vector origin, Vector size, File file) {
        EditSession es = new EditSession(new BukkitWorld(world), 0x30d40);
        CuboidClipboard cc = new CuboidClipboard(origin, size);
        cc.copy(es);
        try {
        	MCEditSchematicFormat.MCEDIT.save(cc, file);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private String getErrorMessage(boolean left) {
    	String error = (new StringBuilder())
    			.append(ChatColor.YELLOW)
    			.append("You need a corner! To select it, wield ")
    			.append(this.getConfig().getString("wandmaterial").toLowerCase().replaceAll("_", " ")).toString();
    	error += left ? " and left click." : " and right click.";
    	return error;
    }
    
 
}