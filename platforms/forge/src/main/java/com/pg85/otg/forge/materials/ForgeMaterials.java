package com.pg85.otg.forge.materials;

import com.pg85.otg.OTG;
import com.pg85.otg.common.materials.LocalMaterialData;
import com.pg85.otg.common.materials.LocalMaterials;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.FifoMap;

public class ForgeMaterials extends LocalMaterials
{
    private static final FifoMap<String, LocalMaterialData> CachedMaterials = new FifoMap<String, LocalMaterialData>(4096); // TODO: Smaller cache should be ok, only most frequently used should be cached?
    
	private static ForgeMaterials forgeMaterials = new ForgeMaterials();
	private ForgeMaterials()
	{
	    // Blocks used in OTG code
		try
		{
			AIR = readMaterial(ForgeMaterials.AIR_NAME);
		    GRASS = readMaterial(ForgeMaterials.GRASS_NAME);
		    DIRT = readMaterial(ForgeMaterials.DIRT_NAME);
		    COARSE_DIRT = readMaterial(ForgeMaterials.COARSE_DIRT_NAME);
		    CLAY = readMaterial(ForgeMaterials.CLAY_NAME);
		    TERRACOTTA = readMaterial(ForgeMaterials.TERRACOTTA_NAME);
		    STAINED_CLAY = readMaterial(ForgeMaterials.STAINED_CLAY_NAME);
		    WHITE_STAINED_CLAY = readMaterial(ForgeMaterials.WHITE_STAINED_CLAY_NAME);
		    ORANGE_STAINED_CLAY = readMaterial(ForgeMaterials.ORANGE_STAINED_CLAY_NAME);
		    YELLOW_STAINED_CLAY = readMaterial(ForgeMaterials.YELLOW_STAINED_CLAY_NAME);
		    BROWN_STAINED_CLAY = readMaterial(ForgeMaterials.BROWN_STAINED_CLAY_NAME);
		    RED_STAINED_CLAY = readMaterial(ForgeMaterials.RED_STAINED_CLAY_NAME);
		    SILVER_STAINED_CLAY = readMaterial(ForgeMaterials.SILVER_STAINED_CLAY_NAME);
		    STONE = readMaterial(ForgeMaterials.STONE_NAME);
		    SAND = readMaterial(ForgeMaterials.SAND_NAME);
		    RED_SAND = readMaterial(ForgeMaterials.RED_SAND_NAME);
		    SANDSTONE = readMaterial(ForgeMaterials.SANDSTONE_NAME);
		    RED_SANDSTONE = readMaterial(ForgeMaterials.RED_SANDSTONE_NAME);
		    GRAVEL = readMaterial(ForgeMaterials.GRAVEL_NAME);
		    MOSSY_COBBLESTONE = readMaterial(ForgeMaterials.MOSSY_COBBLESTONE_NAME);
		    SNOW = readMaterial(ForgeMaterials.SNOW_NAME);
		    SNOW_BLOCK = readMaterial(ForgeMaterials.SNOW_BLOCK_NAME);
		    TORCH = readMaterial(ForgeMaterials.TORCH_NAME);
		    BEDROCK = readMaterial(ForgeMaterials.BEDROCK_NAME);
		    MAGMA = readMaterial(ForgeMaterials.MAGMA_NAME);
		    ICE = readMaterial(ForgeMaterials.ICE_NAME);
		    PACKED_ICE = readMaterial(ForgeMaterials.PACKED_ICE_NAME);
		    FROSTED_ICE = readMaterial(ForgeMaterials.FROSTED_ICE_NAME);
		    GLOWSTONE = readMaterial(ForgeMaterials.GLOWSTONE_NAME);
		    MYCELIUM = readMaterial(ForgeMaterials.MYCELIUM_NAME);
		    STONE_SLAB = readMaterial(ForgeMaterials.STONE_SLAB_NAME);
	
		    // Liquids
		    WATER = readMaterial(ForgeMaterials.WATER_NAME);
		    LAVA = readMaterial(ForgeMaterials.LAVA_NAME);
	
		    // Trees    
		    LOG = readMaterial(ForgeMaterials.LOG_NAME);
		    LOG_2 = readMaterial(ForgeMaterials.LOG_2_NAME);
		    LEAVES = readMaterial(ForgeMaterials.LEAVES_NAME);
		    LEAVES_2 = readMaterial(ForgeMaterials.LEAVES_2_NAME);
	
		    // Plants
		    RED_ROSE = readMaterial(ForgeMaterials.RED_ROSE_NAME);
			BROWN_MUSHROOM = readMaterial(ForgeMaterials.BROWN_MUSHROOM_NAME);
			YELLOW_FLOWER = readMaterial(ForgeMaterials.YELLOW_FLOWER_NAME);
			DEAD_BUSH = readMaterial(ForgeMaterials.DEAD_BUSH_NAME);
			LONG_GRASS = readMaterial(ForgeMaterials.LONG_GRASS_NAME);
			DOUBLE_PLANT = readMaterial(ForgeMaterials.DOUBLE_PLANT_NAME);
			RED_MUSHROOM = readMaterial(ForgeMaterials.RED_MUSHROOM_NAME);
	
			PUMPKIN = readMaterial(ForgeMaterials.PUMPKIN_NAME);
		    CACTUS = readMaterial(ForgeMaterials.CACTUS_NAME);
		    MELON_BLOCK = readMaterial(ForgeMaterials.MELON_BLOCK_NAME);
		    VINE = readMaterial(ForgeMaterials.VINE_NAME);
		    SAPLING = readMaterial(ForgeMaterials.SAPLING_NAME);
		    WATER_LILY = readMaterial(ForgeMaterials.WATER_LILY_NAME);
		    SUGAR_CANE_BLOCK = readMaterial(ForgeMaterials.SUGAR_CANE_BLOCK_NAME);
	
		    // Ores
			COAL_ORE = readMaterial(ForgeMaterials.COAL_ORE_NAME);
			DIAMOND_ORE = readMaterial(ForgeMaterials.DIAMOND_ORE_NAME);
			EMERALD_ORE = readMaterial(ForgeMaterials.EMERALD_ORE_NAME);
			GLOWING_REDSTONE_ORE = readMaterial(ForgeMaterials.GLOWING_REDSTONE_ORE_NAME);
			GOLD_ORE = readMaterial(ForgeMaterials.GOLD_ORE_NAME);
			IRON_ORE = readMaterial(ForgeMaterials.IRON_ORE_NAME);
			LAPIS_ORE = readMaterial(ForgeMaterials.LAPIS_ORE_NAME);
			QUARTZ_ORE = readMaterial(ForgeMaterials.QUARTZ_ORE_NAME);
			REDSTONE_ORE = readMaterial(ForgeMaterials.REDSTONE_ORE_NAME);
	
			// Ore blocks
			GOLD_BLOCK = readMaterial(ForgeMaterials.GOLD_BLOCK_NAME);
			IRON_BLOCK = readMaterial(ForgeMaterials.IRON_BLOCK_NAME);
			REDSTONE_BLOCK = readMaterial(ForgeMaterials.REDSTONE_BLOCK_NAME);
			DIAMOND_BLOCK = readMaterial(ForgeMaterials.DIAMOND_BLOCK_NAME);
			LAPIS_BLOCK = readMaterial(ForgeMaterials.LAPIS_BLOCK_NAME);
			COAL_BLOCK = readMaterial(ForgeMaterials.COAL_BLOCK_NAME);
			QUARTZ_BLOCK = readMaterial(ForgeMaterials.QUARTZ_BLOCK_NAME);
			EMERALD_BLOCK = readMaterial(ForgeMaterials.EMERALD_BLOCK_NAME);
		} catch(InvalidConfigException ex) {
			OTG.log(LogMarker.ERROR, "Could not load default blocks for this version of minecraft, exiting.");
			throw new RuntimeException("Could not load default blocks for this version of minecraft, exiting.");
		}
	};

    public static LocalMaterialData readMaterial(String name) throws InvalidConfigException
	{
    	if(name == null)
    	{
    		return null;
    	}

    	LocalMaterialData material = CachedMaterials.get(name);
    	if(material != null)
    	{
    		return material;
    	}
    	else if(CachedMaterials.containsKey(name))
    	{
    		throw new InvalidConfigException("Cannot read block: " + name);
    	}
    	
    	try
    	{
    		material = ForgeMaterialData.ofString(name);
    	}
    	catch(InvalidConfigException ex)
    	{
    		// Happens when a non existing block name is used.
    		String breakpoint = "";
    	}

    	CachedMaterials.put(name, material);

        return material;
	}
}