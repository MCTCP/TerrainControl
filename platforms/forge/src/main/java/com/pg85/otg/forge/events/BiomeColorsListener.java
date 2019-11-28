package com.pg85.otg.forge.events;

import com.google.common.base.Function;
import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalBiome;
import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.exception.BiomeNotFoundException;

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
    private final Function<Biome, BiomeConfig> getBiomeConfig;
    private ResourceLocation lastBiome = null;
    private BiomeConfig lastBiomeConfig;
    
    public BiomeColorsListener()
    {
        // Register colorizer, for biome colors
        this.getBiomeConfig = new Function<Biome, BiomeConfig>()
        {
            @Override
            public BiomeConfig apply(Biome input)
            {
                LocalBiome biome = null;
                try
                {
                	// Get world name from resourcelocation
                	// TODO: Get world name from somewhere sensical...
                	biome = OTG.getBiome(input.getBiomeName(), input.getRegistryName().getPath().split("_")[0]);
                }
                catch (BiomeNotFoundException e)
                {
                    // Ignored, try in next world
                }
                catch (NoSuchMethodError e)
                {
                    // Thrown when a mod biome doesn't have the method getBiomeName, making it fail to get the biomeconfig
                    // Ignored, as biomes from OTG should never throw this
                }

                if (biome == null)
                {
                    return null;
                }

                return biome.getBiomeConfig();
            }
        };
    }
    
    @SubscribeEvent
    public void grassColor(BiomeEvent.GetGrassColor grassColorEvent)
    {
    	BiomeConfig biomeConfig = lastBiomeConfig;
    	
    	if(lastBiome == null || !grassColorEvent.getBiome().getRegistryName().equals(lastBiome))
    	{
            biomeConfig = this.getBiomeConfig.apply(grassColorEvent.getBiome());	
    	}
    	
    	lastBiome = grassColorEvent.getBiome().getRegistryName();
    	lastBiomeConfig = biomeConfig;
        
    	if (biomeConfig == null)
        {
        	return;
        }
        
        if (biomeConfig.grassColorIsMultiplier)
        {
            if (biomeConfig.grassColor != 0xffffff)
            {
                // ^ This ignores the default grass color
                grassColorEvent.setNewColor((grassColorEvent.getOriginalColor() + biomeConfig.grassColor) / 2);
            }
        } else {
            grassColorEvent.setNewColor(biomeConfig.grassColor);
        }
    }

    @SubscribeEvent
    public void foliageColor(BiomeEvent.GetFoliageColor foliageColorEvent)
    {
    	BiomeConfig biomeConfig = lastBiomeConfig;
    	
    	if(lastBiome == null || !foliageColorEvent.getBiome().getRegistryName().equals(lastBiome))
    	{
            biomeConfig = this.getBiomeConfig.apply(foliageColorEvent.getBiome());	
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
        } else {
            foliageColorEvent.setNewColor(biomeConfig.foliageColor);
        }
    }

    @SubscribeEvent
    public void waterColor(BiomeEvent.GetWaterColor waterColorEvent)
    {
    	BiomeConfig biomeConfig = lastBiomeConfig;
    	
    	if(lastBiome == null || !waterColorEvent.getBiome().getRegistryName().equals(lastBiome))
    	{
            biomeConfig = this.getBiomeConfig.apply(waterColorEvent.getBiome());	
    	}
    	
    	lastBiome = waterColorEvent.getBiome().getRegistryName();
    	lastBiomeConfig = biomeConfig;
    	
        if (biomeConfig == null)
        {
        	return;
        }
        	
        waterColorEvent.setNewColor(biomeConfig.waterColor);
    }
}
