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
    
}