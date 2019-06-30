package com.pg85.otg.util.helpers;

import com.pg85.otg.OTG;
import com.pg85.otg.OTGEngine;
import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.util.FifoMap;
import com.pg85.otg.util.minecraftTypes.DefaultMaterial;

public class MaterialHelper
{
    static FifoMap<String, LocalMaterialData> CachedMaterials = new FifoMap<String, LocalMaterialData>(4096);
    /**
     * @see OTGEngine#readMaterial(String)
     */
    public static LocalMaterialData readMaterial(String name) throws InvalidConfigException
    {
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
    		name = "SNOW_LAYER";
    	}
    	// Spigot interprets water as FLOWING_WATER and that's how TC has always seen it too so keep it that way (even though minecraft:water is actually stationary water).
    	if(name.toLowerCase().equals("water"))
    	{
    		name = "FLOWING_WATER";
    	}
    	// Spigot interprets lava as FLOWING_LAVA and that's how TC has always seen it too so keep it that way (even though minecraft:lava is actually stationary lava).
    	if(name.toLowerCase().equals("lava"))
    	{
    		name = "FLOWING_LAVA";
    	}

    	try
    	{
    		material = OTG.getEngine().readMaterial(name);
    	}
    	catch(InvalidConfigException ex)
    	{
    		CachedMaterials.put(originalName, null);
    		throw ex;
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
}
