package com.pg85.otg.util.materials;

import com.pg85.otg.OTG;
import com.pg85.otg.OTGEngine;
import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.util.FifoMap;
import com.pg85.otg.util.minecraft.defaults.DefaultMaterial;

//TODO: Clean up and optimise ForgeMaterialData/BukkitMaterialData/LocalMaterialData/MaterialHelper/OTGEngine.readMaterial
public class MaterialHelper
{
    private static FifoMap<String, LocalMaterialData> CachedMaterials = new FifoMap<String, LocalMaterialData>(4096);
    
    public static final LocalMaterialData AIR = MaterialHelper.toLocalMaterialData(DefaultMaterial.AIR, 0);
    public static final LocalMaterialData SANDSTONE = MaterialHelper.toLocalMaterialData(DefaultMaterial.SANDSTONE, 0);
    public static final LocalMaterialData RED_SANDSTONE = MaterialHelper.toLocalMaterialData(DefaultMaterial.RED_SANDSTONE, 0);    
    public static final LocalMaterialData LAVA = MaterialHelper.toLocalMaterialData(DefaultMaterial.STATIONARY_LAVA, 0);
    public static final LocalMaterialData WATER = MaterialHelper.toLocalMaterialData(DefaultMaterial.STATIONARY_WATER, 0);
    public static final LocalMaterialData HARDENED_CLAY = MaterialHelper.toLocalMaterialData(DefaultMaterial.HARD_CLAY, 0);
    public static final LocalMaterialData RED_SAND = MaterialHelper.toLocalMaterialData(DefaultMaterial.SAND, 1);
    public static final LocalMaterialData COARSE_DIRT = MaterialHelper.toLocalMaterialData(DefaultMaterial.DIRT, 1);
    public static final LocalMaterialData WHITE_STAINED_CLAY = MaterialHelper.toLocalMaterialData(DefaultMaterial.STAINED_CLAY, 0);
    public static final LocalMaterialData ORANGE_STAINED_CLAY = MaterialHelper.toLocalMaterialData(DefaultMaterial.STAINED_CLAY, 1);
    public static final LocalMaterialData YELLOW_STAINED_CLAY = MaterialHelper.toLocalMaterialData(DefaultMaterial.STAINED_CLAY, 4);
    public static final LocalMaterialData BROWN_STAINED_CLAY = MaterialHelper.toLocalMaterialData(DefaultMaterial.STAINED_CLAY, 12);
    public static final LocalMaterialData RED_STAINED_CLAY = MaterialHelper.toLocalMaterialData(DefaultMaterial.STAINED_CLAY, 14);
    public static final LocalMaterialData SILVER_STAINED_CLAY = MaterialHelper.toLocalMaterialData(DefaultMaterial.STAINED_CLAY, 8);
    public static final LocalMaterialData GLOWSTONE = MaterialHelper.toLocalMaterialData(DefaultMaterial.GLOWSTONE, 0);
    public static final LocalMaterialData GRASS = MaterialHelper.toLocalMaterialData(DefaultMaterial.GRASS, 0);
    public static final LocalMaterialData DIRT = MaterialHelper.toLocalMaterialData(DefaultMaterial.DIRT, 0);
    public static final LocalMaterialData SNOW_BLOCK = MaterialHelper.toLocalMaterialData(DefaultMaterial.SNOW_BLOCK, 0);
    public static final LocalMaterialData VINE = MaterialHelper.toLocalMaterialData(DefaultMaterial.VINE, 0);
    
    /**
     * @throws InvalidConfigException 
     * @see OTGEngine#readMaterial(String)
     */
    public static LocalMaterialData readMaterial(String name) throws InvalidConfigException
    {
    	if(name == null)
    	{
    		return null;
    	}
    	
    	// TODO: Make sure it won't cause problems to return the same material object multiple times, is it not changed anywhere?
    	LocalMaterialData material = CachedMaterials.get(name);
    	if(material != null)
    	{
    		return material;
    	}
    	else if(CachedMaterials.containsKey(name))
    	{
    		throw new InvalidConfigException("Cannot read block: " + name);
    	}

    	String originalName = name;
    	
    	// Spigot interprets snow as SNOW_LAYER and that's how TC has always seen it too so keep it that way (even though minecraft:snow is actually a snow block).
    	if(name.toLowerCase().equals("snow"))
    	{
    		name = "SNOW_LAYER:0";
    	}
    	if(name.toLowerCase().startsWith("snow:"))
    	{
    		name = name.toUpperCase().replace("SNOW:", "SNOW_LAYER:");
    	}
    	// Spigot interprets water as FLOWING_WATER and that's how TC has always seen it too so keep it that way (even though minecraft:water is actually stationary water).
    	else if(name.toLowerCase().equals("water"))
    	{
    		name = "FLOWING_WATER";
    	}
    	// Spigot interprets lava as FLOWING_LAVA and that's how TC has always seen it too so keep it that way (even though minecraft:lava is actually stationary lava).
    	else if(name.toLowerCase().equals("lava"))
    	{
    		name = "FLOWING_LAVA";
    	}
    	
    	try
    	{
    		material = OTG.getEngine().readMaterial(name);
    	}
    	catch(InvalidConfigException ex)
    	{
    		// Happens when a non existing block name is used.
    		String breakpoint = "";
    	}

    	CachedMaterials.put(originalName, material);

        return material;
    }
    
    /**
     * @see OTGEngine#toLocalMaterialData(DefaultMaterial, int)
     */
    public static LocalMaterialData toLocalMaterialData(DefaultMaterial defaultMaterial, int blockData)
    {
        return OTG.getEngine().toLocalMaterialData(defaultMaterial, blockData);
    }

	public static boolean isOre(LocalMaterialData material)
	{
    	return
			material.isMaterial(DefaultMaterial.COAL_ORE) ||
			material.isMaterial(DefaultMaterial.DIAMOND_ORE) ||
			material.isMaterial(DefaultMaterial.EMERALD_ORE) ||
			material.isMaterial(DefaultMaterial.GLOWING_REDSTONE_ORE) ||
			material.isMaterial(DefaultMaterial.GOLD_ORE) ||
			material.isMaterial(DefaultMaterial.IRON_ORE) ||
			material.isMaterial(DefaultMaterial.LAPIS_ORE) ||
			material.isMaterial(DefaultMaterial.QUARTZ_ORE) ||
			material.isMaterial(DefaultMaterial.REDSTONE_ORE)
		;
	}
}
