package com.khorn.terraincontrol.forge.generator;

import net.minecraft.util.math.BlockPos;

import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.forge.ForgeWorld;
import com.khorn.terraincontrol.util.ChunkCoordinate;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultMaterial;

public class Cartographer
{ 
	/**
	 * Spawns a miniature of the known world at 1/16 scale (so one block per chunk) at spawn, made of 
	 * colored clay, wool and glass. Work in progress, will add more features for spawning location, 
	 * updating blocks, console commands etc.
	 */
    public static void CreateBlockWorldMapAtSpawn(ForgeWorld world, ChunkCoordinate chunkCoord)
    {
    	String replaceByMaterial = "159"; // Glass = 95, Clay = 159, Wool = 35 
    	
    	BlockPos spawnPoint =  world.getSpawnPoint();
    	ChunkCoordinate spawnChunk = ChunkCoordinate.fromBlockCoords(spawnPoint.getX(), spawnPoint.getZ());
    	
    	// Draw a map of the world at spawn using blocks
    	int highestBlockY = world.getHighestBlockYAt(chunkCoord.getBlockXCenter(), chunkCoord.getBlockZCenter());
    	LocalMaterialData material = null;
    	LocalMaterialData topMaterial = null;
    	// Find the appropriate blocks to place on the map for the given material.
    	// If a block cannot be found (because the material is unknown or has no replace-to block)
    	// try lower blocks until a block is found or the world depth is reached.
    	while(material == null && highestBlockY >= 0)
    	{
    		LocalMaterialData materialToReplace = world.getMaterial(chunkCoord.getBlockXCenter(), highestBlockY, chunkCoord.getBlockZCenter());
    		
    		// For trees/water/lava/ice/wood the top block is wool or glass, for everything else it's clay
        	String replaceByMaterialTop = replaceByMaterial;
        	if(materialToReplace != null)
        	{
    	    	DefaultMaterial defaultMaterialToReplace = materialToReplace.toDefaultMaterial();
    	    	if(
    				defaultMaterialToReplace.equals(DefaultMaterial.WATER) || 
    				defaultMaterialToReplace.equals(DefaultMaterial.STATIONARY_WATER) ||
    				defaultMaterialToReplace.equals(DefaultMaterial.LAVA) || 
    				defaultMaterialToReplace.equals(DefaultMaterial.STATIONARY_LAVA) ||
    				defaultMaterialToReplace.equals(DefaultMaterial.MAGMA) ||
    				defaultMaterialToReplace.equals(DefaultMaterial.ICE)
    			)
    	    	{
    	    		replaceByMaterialTop = "95";
    	    	}
    	    	if(
    				defaultMaterialToReplace.equals(DefaultMaterial.LEAVES) ||
    				defaultMaterialToReplace.equals(DefaultMaterial.LEAVES_2) ||
    				defaultMaterialToReplace.equals(DefaultMaterial.LOG) ||
    				defaultMaterialToReplace.equals(DefaultMaterial.LOG_2) ||
    	    		defaultMaterialToReplace.equals(DefaultMaterial.WOOD) ||
    				defaultMaterialToReplace.equals(DefaultMaterial.WOOD_DOUBLE_STEP) ||
					defaultMaterialToReplace.equals(DefaultMaterial.WOOD_STEP) ||
					defaultMaterialToReplace.equals(DefaultMaterial.SPRUCE_WOOD_STAIRS) ||
					defaultMaterialToReplace.equals(DefaultMaterial.BIRCH_WOOD_STAIRS) ||
					defaultMaterialToReplace.equals(DefaultMaterial.JUNGLE_WOOD_STAIRS) ||
					defaultMaterialToReplace.equals(DefaultMaterial.ACACIA_STAIRS) ||
					defaultMaterialToReplace.equals(DefaultMaterial.DARK_OAK_STAIRS)
    			)
    	    	{
    	    		replaceByMaterialTop = "35";
    	    	}    	    	
        	}
    		
    		topMaterial = GetReplaceByMaterial(materialToReplace, replaceByMaterialTop);
    		material = GetReplaceByMaterial(materialToReplace, replaceByMaterial);
    		if(material != null && topMaterial == null)
    		{
    			topMaterial = GetReplaceByMaterial(materialToReplace, replaceByMaterialTop);
    		}
    		highestBlockY--;
    	}    	
    	
    	// Couldn't find a block, use black.
    	if(highestBlockY < 0)
    	{
    		highestBlockY = world.getHighestBlockYAt(chunkCoord.getBlockXCenter(), chunkCoord.getBlockZCenter()) - 1;
			try {
				material = TerrainControl.readMaterial(replaceByMaterial + ":15");
				topMaterial = material;
			} catch (InvalidConfigException e) {
				e.printStackTrace();
				return;
			}
    	}

    	// Apply 1/16 to height and get Y, take into account height of spawn location
    	int baseHeight = spawnPoint.getY();
    	int heightDiff = (int) Math.floor(((highestBlockY + 1) - baseHeight) / 16);
    	
    	// Set top block
    	int newY = baseHeight + heightDiff + TerrainControl.getPluginConfig().CartographerHeightOffset;
    	world.setBlock(spawnChunk.getBlockXCenter() + chunkCoord.getChunkX(), newY, spawnChunk.getBlockZCenter() + chunkCoord.getChunkZ(), topMaterial);
    	    	
    	// Set lower blocks
    	while(newY >= (baseHeight + TerrainControl.getPluginConfig().CartographerHeightOffset - (int) Math.floor(baseHeight / 16)))
    	{
    		newY--;
   			world.setBlock(spawnChunk.getBlockXCenter() + chunkCoord.getChunkX(), newY, spawnChunk.getBlockZCenter() + chunkCoord.getChunkZ(), material);
    	}
    
    	// Put a glowstone block at spawn
    	if(chunkCoord.equals(spawnChunk))
    	{
    		try {
    			world.setBlock(spawnChunk.getBlockXCenter() + chunkCoord.getChunkX(), baseHeight + heightDiff + TerrainControl.getPluginConfig().CartographerHeightOffset + 1, spawnChunk.getBlockZCenter() + chunkCoord.getChunkZ(), TerrainControl.readMaterial("GLOWSTONE"));
			} catch (InvalidConfigException e) {
				e.printStackTrace();
			}
    	}
    }
    
    /**
     * Gets the block to place on the map based on the given material.
     */
    private static LocalMaterialData GetReplaceByMaterial(LocalMaterialData materialToReplace, String replaceByMaterial)
    {    	
		DefaultMaterial[] TransparentBlocks = { 
		};

		DefaultMaterial[] WhiteBlocks = {
    		DefaultMaterial.SNOW,
    		DefaultMaterial.SNOW_BLOCK
		};
		
		DefaultMaterial[] OrangeBlocks = {
    		DefaultMaterial.SAND,
    	    DefaultMaterial.RED_SANDSTONE,
    	    DefaultMaterial.HARD_CLAY,
    	    DefaultMaterial.RED_SANDSTONE_STAIRS
		};
		
	    DefaultMaterial[] MagentaBlocks = {
    	    DefaultMaterial.PURPUR_BLOCK,
    	    DefaultMaterial.PURPUR_PILLAR,
    	    DefaultMaterial.PURPUR_STAIRS,
    	    DefaultMaterial.PURPUR_DOUBLE_SLAB,
    	    DefaultMaterial.PURPUR_SLAB
	    };
		
	    DefaultMaterial[] LightBlueBlocks = {
	    		DefaultMaterial.PACKED_ICE
	    };
		
		DefaultMaterial[] YellowBlocks = {
    		DefaultMaterial.SAND,
    		DefaultMaterial.SANDSTONE,
    		DefaultMaterial.SANDSTONE_STAIRS
		};
		
		DefaultMaterial[] LimeBlocks = {
			DefaultMaterial.LEAVES,
			DefaultMaterial.LEAVES_2
		};
		
		DefaultMaterial[] PinkBlocks = {
    		DefaultMaterial.NETHERRACK,
    		DefaultMaterial.MYCEL
		};
		
		DefaultMaterial[] GrayBlocks = {
    		DefaultMaterial.COBBLESTONE,
    		DefaultMaterial.COBBLESTONE_STAIRS,
    		DefaultMaterial.MOSSY_COBBLESTONE,
    	    DefaultMaterial.STONE_SLAB2,
    	    DefaultMaterial.DOUBLE_STONE_SLAB2,
    	    DefaultMaterial.STEP,
    		DefaultMaterial.DOUBLE_STEP,
    		DefaultMaterial.BRICK,
    	    DefaultMaterial.BRICK_STAIRS,
    		DefaultMaterial.SMOOTH_BRICK,
    	    DefaultMaterial.SMOOTH_STAIRS,
		};
		
		DefaultMaterial[] LightGrayBlocks = {
    		DefaultMaterial.STONE,
    		DefaultMaterial.GRAVEL,
    	    DefaultMaterial.CLAY,
    	    DefaultMaterial.EMERALD_ORE,
    	    DefaultMaterial.EMERALD_BLOCK,
    	    DefaultMaterial.REDSTONE_ORE,
    	    DefaultMaterial.GLOWING_REDSTONE_ORE,
    	    DefaultMaterial.DIAMOND_ORE,
    	    DefaultMaterial.DIAMOND_BLOCK,
    		DefaultMaterial.GOLD_ORE,
    	    DefaultMaterial.IRON_ORE,
    	    DefaultMaterial.COAL_ORE,
    	    DefaultMaterial.COAL_BLOCK,
    	    DefaultMaterial.LAPIS_ORE,
    	    DefaultMaterial.LAPIS_BLOCK,
    	    DefaultMaterial.GOLD_BLOCK,
    	    DefaultMaterial.IRON_BLOCK,
    	    DefaultMaterial.REDSTONE_BLOCK,
    	    DefaultMaterial.QUARTZ_ORE,
    	    DefaultMaterial.QUARTZ_BLOCK,
    	    DefaultMaterial.QUARTZ_STAIRS,    	    
		};
		
		DefaultMaterial[] CyanBlocks = {
    		DefaultMaterial.FROSTED_ICE,
    		DefaultMaterial.ICE
		};
		
		DefaultMaterial[] PurpleBlocks = {
    	    DefaultMaterial.NETHER_BRICK,
    	    DefaultMaterial.NETHER_BRICK_STAIRS
		};
		
	    DefaultMaterial[] BlueBlocks = {    		
    	    DefaultMaterial.WATER,
    	    DefaultMaterial.STATIONARY_WATER
	    };
	    
	    DefaultMaterial[] BrownBlocks = {
    		DefaultMaterial.DIRT,
    		DefaultMaterial.WOOD,
    	    DefaultMaterial.WOOD_DOUBLE_STEP,
    	    DefaultMaterial.WOOD_STEP,
    	    DefaultMaterial.SPRUCE_WOOD_STAIRS,
    	    DefaultMaterial.BIRCH_WOOD_STAIRS,
    	    DefaultMaterial.JUNGLE_WOOD_STAIRS,
    	    DefaultMaterial.ACACIA_STAIRS,
    	    DefaultMaterial.DARK_OAK_STAIRS,
    		DefaultMaterial.LOG,
    		DefaultMaterial.LOG_2,
    		DefaultMaterial.SOIL,
    		DefaultMaterial.SOUL_SAND,
    	    DefaultMaterial.HUGE_MUSHROOM_1,
    	    DefaultMaterial.HUGE_MUSHROOM_2    	    
	    };
	    
	    DefaultMaterial[] GreenBlocks = {
    		DefaultMaterial.GRASS,
    		DefaultMaterial.GRASS_PATH,
	    };
		
		DefaultMaterial[] RedBlocks = {
    	    DefaultMaterial.LAVA,
    	    DefaultMaterial.STATIONARY_LAVA,
    	    DefaultMaterial.MAGMA
		};
	    
	    DefaultMaterial[] BlackBlocks = {
    		DefaultMaterial.BEDROCK,
    		DefaultMaterial.OBSIDIAN
	    };
		
		DefaultMaterial[] ColoredBlocks = {
    	    DefaultMaterial.STAINED_CLAY
		};
		
	    // Ignored
	    /*
		DefaultMaterial.GLASS
		DefaultMaterial.WOOL,
		DefaultMaterial.AIR,

	    DefaultMaterial.SPONGE
	    DefaultMaterial.POWERED_RAIL
	    DefaultMaterial.DETECTOR_RAIL
	    DefaultMaterial.PISTON_STICKY_BASE
	    DefaultMaterial.WEB
	    DefaultMaterial.DEAD_BUSH
	    DefaultMaterial.PISTON_BASE
	    DefaultMaterial.BED_BLOCK
	    DefaultMaterial.NOTE_BLOCK
	    DefaultMaterial.DISPENSER
	    DefaultMaterial.PISTON_EXTENSION
	    DefaultMaterial.PISTON_MOVING_PIECE
	    DefaultMaterial.BROWN_MUSHROOM
		DefaultMaterial.RED_ROSE
		DefaultMaterial.YELLOW_FLOWER
		DefaultMaterial.LONG_GRASS
		DefaultMaterial.SAPLING
		DefaultMaterial.RED_MUSHROOM
	    DefaultMaterial.TNT
	    DefaultMaterial.BOOKSHELF
	    DefaultMaterial.TORCH
	    DefaultMaterial.FIRE
	    DefaultMaterial.MOB_SPAWNER
	    DefaultMaterial.WOOD_STAIRS
	    DefaultMaterial.CHEST
	    DefaultMaterial.REDSTONE_WIRE
	    DefaultMaterial.WORKBENCH
	    DefaultMaterial.CROPS        	    
	    DefaultMaterial.FURNACE
	    DefaultMaterial.BURNING_FURNACE
	    DefaultMaterial.SIGN_POST
	    DefaultMaterial.WOODEN_DOOR
	    DefaultMaterial.LADDER
	    DefaultMaterial.RAILS
	    DefaultMaterial.WALL_SIGN
	    DefaultMaterial.LEVER
	    DefaultMaterial.STONE_PLATE
	    DefaultMaterial.IRON_DOOR_BLOCK
	    DefaultMaterial.WOOD_PLATE
	    DefaultMaterial.REDSTONE_TORCH_OFF
	    DefaultMaterial.REDSTONE_TORCH_ON
	    DefaultMaterial.STONE_BUTTON
	    DefaultMaterial.CACTUS       	    
	    DefaultMaterial.SUGAR_CANE_BLOCK
	    DefaultMaterial.JUKEBOX
	    DefaultMaterial.FENCE
	    DefaultMaterial.PUMPKIN
	    DefaultMaterial.GLOWSTONE
	    DefaultMaterial.PORTAL
	    DefaultMaterial.JACK_O_LANTERN
	    DefaultMaterial.CAKE_BLOCK
	    DefaultMaterial.DIODE_BLOCK_OFF
	    DefaultMaterial.DIODE_BLOCK_ON
	    DefaultMaterial.STAINED_GLASS
	    DefaultMaterial.TRAP_DOOR
	    DefaultMaterial.MONSTER_EGGS
	    DefaultMaterial.IRON_FENCE
	    DefaultMaterial.THIN_GLASS
	    DefaultMaterial.MELON_BLOCK
	    DefaultMaterial.PUMPKIN_STEM
	    DefaultMaterial.MELON_STEM
	    DefaultMaterial.VINE
	    DefaultMaterial.FENCE_GATE
	    DefaultMaterial.WATER_LILY
	    DefaultMaterial.NETHER_FENCE
	    DefaultMaterial.COCOA
	    DefaultMaterial.NETHER_WARTS
	    DefaultMaterial.ENCHANTMENT_TABLE
	    DefaultMaterial.BREWING_STAND
	    DefaultMaterial.CAULDRON
	    DefaultMaterial.ENDER_PORTAL
	    DefaultMaterial.ENDER_PORTAL_FRAME
	    DefaultMaterial.ENDER_STONE
	    DefaultMaterial.DRAGON_EGG
	    DefaultMaterial.REDSTONE_LAMP_OFF
	    DefaultMaterial.REDSTONE_LAMP_ON
	    DefaultMaterial.ENDER_CHEST
	    DefaultMaterial.TRIPWIRE_HOOK
	    DefaultMaterial.TRIPWIRE
	    DefaultMaterial.COMMAND
	    DefaultMaterial.BEACON
	    DefaultMaterial.COBBLE_WALL
	    DefaultMaterial.FLOWER_POT
	    DefaultMaterial.CARROT
	    DefaultMaterial.POTATO
	    DefaultMaterial.WOOD_BUTTON
	    DefaultMaterial.SKULL
	    DefaultMaterial.ANVIL
	    DefaultMaterial.TRAPPED_CHEST
	    DefaultMaterial.GOLD_PLATE
	    DefaultMaterial.IRON_PLATE
	    DefaultMaterial.REDSTONE_COMPARATOR_OFF
	    DefaultMaterial.REDSTONE_COMPARATOR_ON
	    DefaultMaterial.DAYLIGHT_DETECTOR
	    DefaultMaterial.HOPPER
	    DefaultMaterial.ACTIVATOR_RAIL
	    DefaultMaterial.DROPPER
	    DefaultMaterial.STAINED_GLASS_PANE
	    DefaultMaterial.SLIME_BLOCK
	    DefaultMaterial.BARRIER
	    DefaultMaterial.IRON_TRAPDOOR       	    
	    DefaultMaterial.PRISMARINE
	    DefaultMaterial.SEA_LANTERN
	    DefaultMaterial.HAY_BLOCK(170),
	    DefaultMaterial.CARPET
	    DefaultMaterial.DOUBLE_PLANT
	    DefaultMaterial.STANDING_BANNER
	    DefaultMaterial.WALL_BANNER
	    DefaultMaterial.DAYLIGHT_DETECTOR_INVERTED
	    DefaultMaterial.SPRUCE_FENCE_GATE
	    DefaultMaterial.BIRCH_FENCE_GATE
	    DefaultMaterial.JUNGLE_FENCE_GATE
	    DefaultMaterial.DARK_OAK_FENCE_GATE
	    DefaultMaterial.ACACIA_FENCE_GATE
	    DefaultMaterial.SPRUCE_FENCE
	    DefaultMaterial.BIRCH_FENCE
	    DefaultMaterial.JUNGLE_FENCE
	    DefaultMaterial.DARK_OAK_FENCE
	    DefaultMaterial.ACACIA_FENCE
	    DefaultMaterial.SPRUCE_DOOR
	    DefaultMaterial.BIRCH_DOOR
	    DefaultMaterial.JUNGLE_DOOR
	    DefaultMaterial.ACACIA_DOOR
	    DefaultMaterial.DARK_OAK_DOOR
	    DefaultMaterial.END_ROD
	    DefaultMaterial.CHORUS_PLANT
	    DefaultMaterial.CHORUS_FLOWER        	    
	    DefaultMaterial.END_BRICKS        	    
	    DefaultMaterial.BEETROOT_BLOCK        	    
	    DefaultMaterial.END_GATEWAY
	    DefaultMaterial.COMMAND_REPEATING
	    DefaultMaterial.COMMAND_CHAIN
	    DefaultMaterial.NETHER_WART_BLOCK
	    DefaultMaterial.RED_NETHER_BRICK
	    DefaultMaterial.BONE_BLOCK
	    DefaultMaterial.STRUCTURE_VOID
	    DefaultMaterial.STRUCTURE_BLOCK
	    */    
		
    	for(DefaultMaterial replacematerial : TransparentBlocks)
    	{
    		if(replacematerial.equals(materialToReplace.toDefaultMaterial()))
    		{
    			try {
					return TerrainControl.readMaterial("GLASS");
				} catch (InvalidConfigException e) {
					e.printStackTrace();
					return null;
				}
    		}
    	}
    	for(DefaultMaterial replacematerial : WhiteBlocks)
    	{
    		if(replacematerial.equals(materialToReplace.toDefaultMaterial()))
    		{
    			try {
					return TerrainControl.readMaterial(replaceByMaterial);
				} catch (InvalidConfigException e) {
					e.printStackTrace();
					return null;
				}
    		}
    	}
    	for(DefaultMaterial replacematerial : OrangeBlocks)
    	{
    		if(replacematerial.equals(materialToReplace.toDefaultMaterial()))
    		{
    			if(replacematerial.equals(DefaultMaterial.SAND))
    			{
    				if(materialToReplace.getBlockData() != 1) { continue; }
    			}    			
    			try {
					return TerrainControl.readMaterial(replaceByMaterial + ":1");
				} catch (InvalidConfigException e) {
					e.printStackTrace();
					return null;
				}
    		}
    	}
    	for(DefaultMaterial replacematerial : MagentaBlocks)
    	{
    		if(replacematerial.equals(materialToReplace.toDefaultMaterial()))
    		{
    			try {
					return TerrainControl.readMaterial(replaceByMaterial + ":2");
				} catch (InvalidConfigException e) {
					e.printStackTrace();
					return null;
				}
    		}
    	}
    	for(DefaultMaterial replacematerial : LightBlueBlocks)
    	{
    		if(replacematerial.equals(materialToReplace.toDefaultMaterial()))
    		{
    			try {
					return TerrainControl.readMaterial(replaceByMaterial + ":3");
				} catch (InvalidConfigException e) {
					e.printStackTrace();
					return null;
				}
    		}
    	}
    	for(DefaultMaterial replacematerial : YellowBlocks)
    	{
    		if(replacematerial.equals(materialToReplace.toDefaultMaterial()))
    		{
    			try {
					return TerrainControl.readMaterial(replaceByMaterial + ":4");
				} catch (InvalidConfigException e) {
					e.printStackTrace();
					return null;
				}
    		}
    	}
    	for(DefaultMaterial replacematerial : LimeBlocks)
    	{
    		if(replacematerial.equals(materialToReplace.toDefaultMaterial()))
    		{
    			if(replaceByMaterial == "35") // Use green glass instead of lime for spawning trees on top of clay
    			{
    				continue;
    			}
    			
    			try {
					return TerrainControl.readMaterial(replaceByMaterial + ":5");
				} catch (InvalidConfigException e) {
					e.printStackTrace();
					return null;
				}
    		}
    	}
    	for(DefaultMaterial replacematerial : PinkBlocks)
    	{
    		if(replacematerial.equals(materialToReplace.toDefaultMaterial()))
    		{
    			try {
					return TerrainControl.readMaterial(replaceByMaterial + ":6");
				} catch (InvalidConfigException e) {
					e.printStackTrace();
					return null;
				}
    		}
    	}
    	for(DefaultMaterial replacematerial : GrayBlocks)
    	{
    		if(replacematerial.equals(materialToReplace.toDefaultMaterial()))
    		{
    			try {
					return TerrainControl.readMaterial(replaceByMaterial + ":7");
				} catch (InvalidConfigException e) {
					e.printStackTrace();
					return null;
				}
    		}
    	}
    	for(DefaultMaterial replacematerial : LightGrayBlocks)
    	{
    		if(replacematerial.equals(materialToReplace.toDefaultMaterial()))
    		{
    			try {
					return TerrainControl.readMaterial(replaceByMaterial + ":8");
				} catch (InvalidConfigException e) {
					e.printStackTrace();
					return null;
				}
    		}
    	}
    	for(DefaultMaterial replacematerial : CyanBlocks)
    	{
    		if(replacematerial.equals(materialToReplace.toDefaultMaterial()))
    		{
    			try {
					return TerrainControl.readMaterial(replaceByMaterial + ":9");
				} catch (InvalidConfigException e) {
					e.printStackTrace();
					return null;
				}
    		}
    	}
    	for(DefaultMaterial replacematerial : PurpleBlocks)
    	{
    		if(replacematerial.equals(materialToReplace.toDefaultMaterial()))
    		{
    			try {
					return TerrainControl.readMaterial(replaceByMaterial + ":10");
				} catch (InvalidConfigException e) {
					e.printStackTrace();
					return null;
				}
    		}
    	}
    	for(DefaultMaterial replacematerial : BlueBlocks)
    	{
    		if(replacematerial.equals(materialToReplace.toDefaultMaterial()))
    		{
    			try {
					return TerrainControl.readMaterial(replaceByMaterial + ":11");
				} catch (InvalidConfigException e) {
					e.printStackTrace();
					return null;
				}
    		}
    	}
    	for(DefaultMaterial replacematerial : BrownBlocks)
    	{
    		if(replacematerial.equals(materialToReplace.toDefaultMaterial()))
    		{
    			try {
					return TerrainControl.readMaterial(replaceByMaterial + ":12");
				} catch (InvalidConfigException e) {
					e.printStackTrace();
					return null;
				}
    		}
    	}
    	for(DefaultMaterial replacematerial : GreenBlocks)
    	{
    		if(
				replacematerial.equals(materialToReplace.toDefaultMaterial()) ||
    			(replaceByMaterial == "35" && (materialToReplace.toDefaultMaterial().equals(DefaultMaterial.LEAVES) || materialToReplace.toDefaultMaterial().equals(DefaultMaterial.LEAVES_2))) // Use green glass instead of lime for spawning trees on top of clay
			)
    		{
    			try {
					return TerrainControl.readMaterial(replaceByMaterial + ":13");
				} catch (InvalidConfigException e) {
					e.printStackTrace();
					return null;
				}
    		}
    	}
    	for(DefaultMaterial replacematerial : RedBlocks)
    	{
    		if(replacematerial.equals(materialToReplace.toDefaultMaterial()))
    		{
    			try {
					return TerrainControl.readMaterial(replaceByMaterial + ":14");
				} catch (InvalidConfigException e) {
					e.printStackTrace();
					return null;
				}
    		}
    	}
    	for(DefaultMaterial replacematerial : BlackBlocks)
    	{
    		if(replacematerial.equals(materialToReplace.toDefaultMaterial()))
    		{
    			try {
					return TerrainControl.readMaterial(replaceByMaterial + ":15");
				} catch (InvalidConfigException e) {
					e.printStackTrace();
					return null;
				}
    		}
    	}
    	for(DefaultMaterial replacematerial : ColoredBlocks)
    	{
    		if(replacematerial.equals(materialToReplace.toDefaultMaterial()))
    		{
    			try {
					return TerrainControl.readMaterial(replaceByMaterial + ":" + materialToReplace.getBlockData());
				} catch (InvalidConfigException e) {
					e.printStackTrace();
					return null;
				}
    		}
    	}
    	
    	return null;
    }	
}
