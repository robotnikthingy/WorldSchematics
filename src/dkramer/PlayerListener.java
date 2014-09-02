package dkramer;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerListener implements Listener {
	
    public static WorldFeatures plugin;
    
    public PlayerListener(WorldFeatures main) {
    	ChunkListener.plugin = main;
    }
	
    public PlayerListener() {
    }
    
	@EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        if(!p.getItemInHand().getType().toString().equals(plugin.getConfig().getString("wandmaterial"))) {
            return;
        }
        if(!p.hasPermission("WorldSchematics.commands") && !p.isOp()) {
            return;
        }
        Location blockLocation = event.getClickedBlock().getLocation();
        if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
        	WorldFeatures.getPlayerInfo(p).point2 = blockLocation;
            p.sendMessage(getMessage(event.getPlayer(), false, blockLocation));
        } else if(event.getAction() == Action.LEFT_CLICK_BLOCK) {
        	WorldFeatures.getPlayerInfo(p).point1 = blockLocation;
            p.sendMessage(getMessage(event.getPlayer(), true, blockLocation));
        }
    }
    
    private String getMessage(Player player, boolean first, Location loc) {
    	String message = ChatColor.YELLOW + (first ? "First" : "Second");
    	
    	return new StringBuilder(message)
	    	.append(" corner set: ")
	    	.append(loc.getBlockX())
	    	.append(", ")
	    	.append(loc.getBlockY())
	    	.append(", ")
	    	.append(loc.getBlockZ())
	    	.toString();
    }
}