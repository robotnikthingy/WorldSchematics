package dkramer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkPopulateEvent;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.schematic.MCEditSchematicFormat;

public class ChunkListener implements Listener {
	Random rand;
    private Chunk chunk;
    private int bHeight;
    private World wrld;
    private int chunkX;
    private int chunkZ;
    private int randX;
    private int randZ;
    private int width;
    private int bredth;
    private int height;
    private int rotation;
    public static WorldFeatures plugin;
    CuboidClipboard cc;

    public ChunkListener(WorldFeatures main) {
    	ChunkListener.plugin = main;
    }

    public ChunkListener() {
        rand = new Random();
        width = 0;
        bredth = 0;
        height = 0;
    }
    private void loadArea(World world, Vector origin, int[] pasteNoneOfThese) {
        EditSession es = new EditSession(new BukkitWorld(world), 1000000);
        try  {
            cc.paste(es, origin, true);
        } catch(MaxChangedBlocksException e) {
            e.printStackTrace();
        }
    }

    public Block loadBlockChunk(int x, int y, int z) {
        return wrld.getBlockAt(chunkX + x, y, chunkZ + z);
    }

    public boolean cornerBlocksOr(Material mat) {
        return loadBlockChunk(randX, bHeight, randZ).getType() == mat 
        	|| loadBlockChunk(randX + width, bHeight, randZ).getType() == mat 
        	|| loadBlockChunk(randX, bHeight, randZ + bredth).getType() == mat 
        	|| loadBlockChunk(randX + width, bHeight, randZ + bredth).getType() == mat;
    }

    public boolean cornerBlocksAnd(Material mat) {
        return loadBlockChunk(randX, bHeight, randZ).getType() == mat
        	&& loadBlockChunk(randX + width, bHeight, randZ).getType() == mat
        	&& loadBlockChunk(randX, bHeight, randZ + bredth).getType() == mat 
        	&& loadBlockChunk(randX + width, bHeight, randZ + bredth).getType() == mat;
    }

    public boolean cornerBlocksBiome(Biome biome) {
        return loadBlockChunk(randX, bHeight, randZ).getBiome().toString().equals(biome) 
        	&& loadBlockChunk(randX + width, bHeight, randZ).getBiome().toString().equals(biome) 
        	&& loadBlockChunk(randX, bHeight, randZ + bredth).getBiome().toString().equals(biome) 
        	&& loadBlockChunk(randX + width, bHeight, randZ + bredth).getBiome().toString().equals(biome);
    }

    @EventHandler
    public void onChunkPopulate(ChunkPopulateEvent event) {
    	System.out.println("[WorldSchematics] New chunk created");
    	chunk = event.getChunk();
        chunkX = chunk.getX() * 16;
        chunkZ = chunk.getZ() * 16;
        wrld = chunk.getWorld();
        if(rand.nextInt(100) + 1 > plugin.getConfig().getInt("chunkchance")) {
        	System.out.println("[WorldSchematics] Not going to load schematics in newly created chunk");
            return;
        }
        
        String worldPath = "plugins/WorldSchematics/ToUse/" + wrld.getName();
        ArrayList<String> schemeNames = new ArrayList<String>();
        String children[] = new File(worldPath).list();
        if(children != null) {
        	System.out.println("[WorldSchematics] Found schematics in folder: " + wrld.getName());
            for(int ab = 0; ab < children.length; ab++) {
            	String fileType = children[ab].substring(children[ab].indexOf('.') + 1);
                if(fileType.equals("schematic")) {
                	schemeNames.add(children[ab]);
                }
            }
        }
        
        
        if(schemeNames.size() == 0) {
        	System.out.println("[WorldSchematics] Did not find any schematics in folder: " + wrld.getName() + "!");
            return;
        }
        
        List<String> chosenSchemeNames = new ArrayList<String>();
        for(int x = 0; x < schemeNames.size(); x++) {
        	String name = schemeNames.get(x);
            BetterConfiguration config = 
            		WorldFeatures.getConfig(new StringBuilder(worldPath).append("/").append(name.substring(0, name.indexOf("."))).toString());
            if(rand.nextInt(100) + 1 <= config.getInt("chance", 50)) {
            	chosenSchemeNames.add(name);
            }
        }

        if(chosenSchemeNames.isEmpty()) {
            return;
        }
        
        String chosenSchemeName = chosenSchemeNames.get(rand.nextInt(chosenSchemeNames.size()));
        String schemeName = chosenSchemeName;
        String configName = schemeName.substring(0, schemeName.indexOf('.'));
        BetterConfiguration schemeConfig = WorldFeatures.getConfig(new StringBuilder(worldPath).append("/").append(configName).toString());
        randX = rand.nextInt(16);
        randZ = rand.nextInt(16);
        boolean canSpawn = true;
        int maxSpawns = schemeConfig.getInt("maxspawns", 0);
        if(maxSpawns != 0 && schemeConfig.getInt((new StringBuilder("spawns.")).append(wrld.getName()).toString(), 0) >= maxSpawns) {
            return;
        }
        
        try {
            File file = new File(new StringBuilder(worldPath).append("/").append(schemeName).toString());
            cc = MCEditSchematicFormat.MCEDIT.load(file);
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        width = cc.getWidth();
        bredth = cc.getLength();
        height = cc.getHeight();
        
        if(schemeConfig.getBoolean("randomrotate", true)) {
            rotation = rand.nextInt(4) * 90;
            cc.rotate2D(rotation);
            switch(rotation) {
	            case 90:
	                width = - cc.getWidth();
	                bredth = cc.getLength();
	                break;
	
	            case 180: 
	                width = - cc.getWidth();
	                bredth = - cc.getLength();
	                break;
	
	            case 270: 
	                width = cc.getWidth();
	                bredth = - cc.getLength();
	                break;
            }
        }

/*        if(!schemeConfig.getString("biome", "none").equals("none")) {
        	System.out.println("[WorldSchematics] Biome for schematic = none. Will not create" + wrld.getName() + "!");        	
            return;
        }*/
        
        int maxHeight = wrld.getMaxHeight() - 1;
        
        String place = schemeConfig.getString("place", "ground");
        if(place.equals("anywhere")) {
            int minY = schemeConfig.getInt("anywhereminY", 1);
            int maxY = schemeConfig.getInt("anywheremaxY", maxHeight);
            bHeight = rand.nextInt(maxY - minY) + 1 + minY;
            if(bHeight > maxHeight - height) {
                canSpawn = false;
            }
        } else if(place.equals("ground")) {
            bHeight = maxHeight;
            int base = schemeConfig.getInt("basementdepth", 0);
            while(cornerBlocksOr(Material.AIR))  {
                bHeight--;
            }
            while(cornerBlocksOr(Material.LEAVES))  {
                bHeight--;
            }
            while(cornerBlocksOr(Material.SNOW))  {
                bHeight--;
            }
            if(bHeight > (maxHeight - height) + base) {
                canSpawn = false;
            }
            if(cornerBlocksOr(Material.STATIONARY_WATER)) {
                canSpawn = false;
            }
        } else if(place.equals("air")) {
            for(bHeight = maxHeight; cornerBlocksOr(Material.AIR); bHeight--) { }
            for(bHeight = rand.nextInt(maxHeight - bHeight) + 1 + bHeight; bHeight > maxHeight - height; bHeight--) { }
            canSpawn = false;
            if(cornerBlocksAnd(Material.AIR)) {
                canSpawn = true;
            }
        } else if(place.equals("underground")) {
            for(bHeight = 1; loadBlockChunk(randX, bHeight + 1 + height, randZ).getType() != Material.AIR; bHeight++) { }
            for(; loadBlockChunk(randX + width, bHeight + 1 + height, randZ).getType() != Material.AIR; bHeight++) { }
            for(; loadBlockChunk(randX, bHeight + 1 + height, randZ + bredth).getType() != Material.AIR; bHeight++) { }
            for(; loadBlockChunk(randX + width, bHeight + 1 + height, randZ + bredth).getType() != Material.AIR; bHeight++) { }
            for(bHeight = rand.nextInt(bHeight) + 1; bHeight > maxHeight - height; bHeight--) { }
            canSpawn = false;
            if(loadBlockChunk(randX, bHeight + 1 + height, randZ).getType() != Material.AIR 
            		&& loadBlockChunk(randX + width, bHeight + 1 + height, randZ).getType() != Material.AIR  
            		&& loadBlockChunk(randX, bHeight + 1 + height, randZ + bredth).getType() != Material.AIR  
            		&& loadBlockChunk(randX + width, bHeight + 1 + height, randZ + bredth).getType() != Material.AIR ) {
                canSpawn = true;
            }
        }
        if(canSpawn) {
        	System.out.println("[WorldSchematics] spawning schematic at chunk (x,z)" + chunkX + "," + chunkZ );
        	String[] stringNone = schemeConfig.getString("dontpaste", "0").replaceAll(" ", "").split(",");
        	int[] pasteNone = new int[stringNone.length];
        	int i = 0;
        	for(String s : stringNone) {
        		pasteNone[i] = Integer.parseInt(s);
        		i++;
        	}
            loadArea(wrld, new Vector(chunkX + randX, (bHeight + 1) - schemeConfig.getInt("basementdepth", 0), chunkZ + randZ), pasteNone);
            schemeConfig.set((new StringBuilder("spawns.")).append(wrld.getName()).toString(), schemeConfig.getInt((new StringBuilder("spawns.")).append(wrld.getName()).toString(), 0) + 1);
            return;
        }
    }
}