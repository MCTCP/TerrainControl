package com.pg85.otg.forge.events;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalBiome;
import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.exception.BiomeNotFoundException;
import com.pg85.otg.forge.OTGPlugin;
import com.pg85.otg.logging.LogMarker;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.event.terraingen.BiomeEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Listens to various events in order to change the grass color, water color and
 * foliage color.
 */
public final class BiomeColorsListener
{
    private ResourceLocation lastBiome = null;
    private BiomeConfig lastBiomeConfig;
       
    public BiomeColorsListener() { }
    
    @SubscribeEvent
    public void grassColor(BiomeEvent.GetGrassColor grassColorEvent)
    {
    	// This is also called on empty chunks that don't have OTG biome information yet,
    	// so they'll call this on vanilla biomes, even though those don't exist in the world.
    	// Unfortunately, we can't get the coords to get the proper biome..
		if(!grassColorEvent.getBiome().getRegistryName().getNamespace().equals(PluginStandardValues.MOD_ID))
    	{
    		return;
    	}
   	
    	BiomeConfig biomeConfig = lastBiomeConfig;    
    	if(lastBiome == null || lastBiomeConfig == null || !grassColorEvent.getBiome().getRegistryName().equals(lastBiome))
    	{
            biomeConfig = getBiomeConfig(grassColorEvent.getBiome());	
    	}
    	
    	lastBiome = grassColorEvent.getBiome().getRegistryName();
    	lastBiomeConfig = biomeConfig;
        
    	if (biomeConfig == null)
        {
        	return;
        }
        
    	if (biomeConfig.grassColor == 0xffffff)
    	{
    		return;
    	}
    	
        if (biomeConfig.grassColorIsMultiplier)
        {
        	grassColorEvent.setNewColor((grassColorEvent.getOriginalColor() + biomeConfig.grassColor) / 2);
        }
    }

    @SubscribeEvent
    public void foliageColor(BiomeEvent.GetFoliageColor foliageColorEvent)
    {
    	// This is also called on empty chunks that don't have OTG biome information yet,
    	// so they'll call this on vanilla biomes, even though those don't exist in the world.
    	// Unfortunately, we can't get the coords to get the proper biome..
		if(!foliageColorEvent.getBiome().getRegistryName().getNamespace().equals(PluginStandardValues.MOD_ID))
    	{
    		return;
    	}
    	
    	BiomeConfig biomeConfig = lastBiomeConfig;    	
    	if(lastBiome == null || lastBiomeConfig == null || !foliageColorEvent.getBiome().getRegistryName().equals(lastBiome))
    	{
            biomeConfig = getBiomeConfig(foliageColorEvent.getBiome());	
    	}
    	
    	lastBiome = foliageColorEvent.getBiome().getRegistryName();
    	lastBiomeConfig = biomeConfig;
    	
        if (biomeConfig == null)
        {
        	return;
        }
        
        if (biomeConfig.foliageColor == 0xffffff)
        {
        	return;
        }

        if (biomeConfig.foliageColorIsMultiplier)
        {
            foliageColorEvent.setNewColor((foliageColorEvent.getOriginalColor() + biomeConfig.foliageColor) / 2);
        }
    }

    @SubscribeEvent
    public void waterColor(BiomeEvent.GetWaterColor waterColorEvent)
    {
    	// This is also called on empty chunks that don't have OTG biome information yet,
    	// so they'll call this on vanilla biomes, even though those don't exist in the world.
    	// Unfortunately, we can't get the coords to get the proper biome..
    	if(!waterColorEvent.getBiome().getRegistryName().getNamespace().equals(PluginStandardValues.MOD_ID))
    	{
    		return;
    	}
    	
    	BiomeConfig biomeConfig = lastBiomeConfig;
    	if(lastBiome == null || lastBiomeConfig == null || !waterColorEvent.getBiome().getRegistryName().equals(lastBiome))
    	{
            biomeConfig = getBiomeConfig(waterColorEvent.getBiome());
    	}
    	
    	lastBiome = waterColorEvent.getBiome().getRegistryName();
    	lastBiomeConfig = biomeConfig;
    	
        if (biomeConfig == null)
        {
        	return;
        }
        
        // Base watercolor is applied by OTG when the biome is created if !waterColorIsMultiplier.
        if (biomeConfig.waterColor == 0xffffff)
        {
        	return;
        }
        
        waterColorEvent.setNewColor(biomeConfig.waterColor);
    }
    
    public BiomeConfig getBiomeConfig(Biome input)
    {
        LocalBiome biome = null;
        try
        {
        	// Get world name from resourcelocation
        	// TODO: Get world name from somewhere sensical...
        	// TODO: This forbids worldnames with an underscore
        	biome = OTG.getBiome(input.getBiomeName(), input.getRegistryName().getPath().split("_")[0]);
        	if(biome == null)
        	{
        		OTG.log(LogMarker.INFO, "Could not find biome " + input.getBiomeName() + " - " + input.getRegistryName().toString());
        		return null;
        	}
        }
        catch (BiomeNotFoundException e)
        {
            // Ignored, try in next world
            return null;
        }
        catch (NoSuchMethodError e)
        {
            // Thrown when a mod biome doesn't have the method getBiomeName, making it fail to get the biomeconfig
            // Ignored, as biomes from OTG should never throw this
            return null;
        }

        return biome.getBiomeConfig();
    }
    
    public void reload()
    {
    	lastBiomeConfig = null;	
    }
}
