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
    
    public static String defaultCuboidPicker = "SEEDS";
        
    public void onDisable() {
        logger.info("WorldSchematics Disabled");
        saveAllConfigs();
    }

    public void onEnable() {
    	instance = this;
    	new File("plugins/WorldFeatures/Created").mkdirs();
        new File("plugins/WorldFeatures/ToUse").mkdirs();
        getServer().getPluginManager().registerEvents(new ChunkListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				saveAllConfigs();
			}
        }, 20 * 60 * 15, 20 * 60 * 15);
        getSettingsConfig().save();
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
    
    public static BetterConfiguration getSettingsConfig() {
    	return getConfig("plugins/WorldSchematics/Settings");
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
    			.append(getSettingsConfig().getString("wandmaterial", defaultCuboidPicker).toLowerCase().replaceAll("_", " ")).toString();
    	error += left ? " and left click." : " and right click.";
    	return error;
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String args[]) {
        Player playa = (Player)sender;
        if(cmd.getName().equalsIgnoreCase("wf") || cmd.getName().equalsIgnoreCase("WorldSchematics")) {
            if(playa.hasPermission("WorldSchematics.commands") || playa.isOp()) {
                if(args.length >= 1 && !args[0].equalsIgnoreCase("copy")) {
                	String saveName = "";
                	for(String s : args) {
                		saveName += s + " ";
                	}
                	saveName = saveName.substring(0, saveName.length() - 1);
                	Location point1 = getPlayerInfo(playa).point1;
                	Location point2 = getPlayerInfo(playa).point2;
                    if(point1 != null) {
                        if(point2 != null) {
                        	BetterConfiguration cusLoad = WorldFeatures.getConfig("plugins/WorldSchematics/Created/" + saveName);
                            int loc1X = point1.getBlockX();
                            int loc1Y = point1.getBlockY();
                            int loc1Z = point1.getBlockZ();
                            int loc2X = point2.getBlockX();
                            int loc2Y = point2.getBlockY();
                            int loc2Z = point2.getBlockZ();
                            int minx = Math.min(loc1X, loc2X);
                            int miny = Math.min(loc1Y, loc2Y);
                            int minz = Math.min(loc1Z, loc2Z);
                            int maxx = Math.max(loc1X, loc2X);
                            int maxy = Math.max(loc1Y, loc2Y);
                            int maxz = Math.max(loc1Z, loc2Z);
                            cusLoad.set("place", "ground");
                            cusLoad.set("maxspawns", 0);
                            cusLoad.set("chance", 50);
                            cusLoad.set("basementdepth", 0);
                            cusLoad.set("anywhereminY", 1);
                            cusLoad.set("anywheremaxY", 126);
                            cusLoad.set("randomrotate", true);
                            cusLoad.set("biome", "none");
                            cusLoad.set("pasteschematicair", false);
                            cusLoad.save();
                            Vector min = new Vector(minx, miny, minz);
                            Vector max = new Vector(maxx, maxy, maxz);
                            File schFile = new File((new StringBuilder("plugins/WorldSchematics/Created/")).append(saveName).append(".schematic").toString());
                            saveArea(playa.getWorld(), max.subtract(min).add(new Vector(1, 1, 1)), min, schFile);
                            playa.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("You saved this in the Created folder as the file: ").append(saveName).toString());
                            return true;
                        } else {
                            playa.sendMessage(getErrorMessage(false));
                            return true;
                        }
                    } else {
                        playa.sendMessage(getErrorMessage(true));
                        return true;
                    }
                }
                if(args.length >= 2 && args[0].equalsIgnoreCase("copy")) {
                    try {
                    	String name = "";
                    	for(int i = 1; i < args.length; i++) {
                    		String s = args[i];
                    		name += s + " ";
                    	}
                    	name = name.substring(0, name.length() - 1);
                    	String worldName = playa.getWorld().getName();
                        new File("plugins/WorldSchematics/ToUse/" + worldName).mkdir();
                        String fromName = "plugins/WorldSchematics/Created/" + name;
                        String toName = "plugins/WorldSchematics/ToUse/" + worldName + "/"  + name;
                        Files.copy(new File(fromName + ".yml"), new File(toName + ".yml"));
                        Files.copy(new File(fromName + ".schematic"), new File(toName + ".schematic"));
                        playa.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Successfully copied ").append(name).append(".").toString());
                    } catch(IOException e) {
                        playa.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Something went wrong!").toString());
                        e.printStackTrace();
                    }
                    return true;
                } else {
                    playa.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("/wf <name>:").append(ChatColor.WHITE).append(" Creates a schematic named <name> of the selected cuboid.").toString());
                    playa.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("/wf copy <name>:").append(ChatColor.WHITE).append(" Copies and pastes this schematic from your Created folder into your ToUse folder.").toString());
                    return true;
                }
            } else {
                playa.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("You do not have permission to do that.").toString());
                return true;
            }
        } else {
            return false;
        }
    }
}