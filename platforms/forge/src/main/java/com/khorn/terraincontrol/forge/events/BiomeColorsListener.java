package com.khorn.terraincontrol.forge.events;

import com.google.common.base.Function;
import com.khorn.terraincontrol.configuration.BiomeConfig;
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

    public BiomeColorsListener(Function<Biome, BiomeConfig> getBiomeConfig)
    {
        this.getBiomeConfig = getBiomeConfig;
    }

    @SubscribeEvent
    public void grassColor(BiomeEvent.GetGrassColor grassColorEvent)
    {
        BiomeConfig biomeConfig = this.getBiomeConfig.apply(grassColorEvent.getBiome());
        if (biomeConfig == null)
            return;

        if (biomeConfig.grassColorIsMultiplier)
        {
            if (biomeConfig.grassColor != 0xffffff)
            {
                // ^ This ignores the default grass color
                grassColorEvent.setNewColor((grassColorEvent.getOriginalColor() + biomeConfig.grassColor) / 2);
            }
        } else
        {
            grassColorEvent.setNewColor(biomeConfig.grassColor);
        }
    }

    @SubscribeEvent
    public void foliageColor(BiomeEvent.GetFoliageColor foliageColorEvent)
    {
        BiomeConfig biomeConfig = this.getBiomeConfig.apply(foliageColorEvent.getBiome());
        if (biomeConfig == null)
            return;
        if (biomeConfig.foliageColor == 0xffffff)
            return;

        if (biomeConfig.foliageColorIsMultiplier)
        {
            foliageColorEvent.setNewColor((foliageColorEvent.getOriginalColor() + biomeConfig.foliageColor) / 2);
        } else
        {
            foliageColorEvent.setNewColor(biomeConfig.foliageColor);
        }
    }

    @SubscribeEvent
    public void waterColor(BiomeEvent.GetWaterColor waterColorEvent)
    {
        BiomeConfig biomeConfig = this.getBiomeConfig.apply(waterColorEvent.getBiome());
        if (biomeConfig == null)
            return;

        waterColorEvent.setNewColor(biomeConfig.waterColor);
    }

}
