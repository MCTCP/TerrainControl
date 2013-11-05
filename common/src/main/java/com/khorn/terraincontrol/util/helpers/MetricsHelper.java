package com.khorn.terraincontrol.util.helpers;

import com.khorn.terraincontrol.generator.biome.OldBiomeGenerator;
import com.khorn.terraincontrol.generator.biome.FromImageBiomeGenerator;
import com.khorn.terraincontrol.generator.biome.BiomeGenerator;
import com.khorn.terraincontrol.generator.biome.VanillaBiomeGenerator;
import com.khorn.terraincontrol.generator.biome.NormalBiomeGenerator;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.configuration.WorldConfigFile;

/**
 * Counts how much each biome mode is used.
 *
 */
public abstract class MetricsHelper
{
    protected int normalMode = 0;
    protected int fromImageMode = 0;
    protected int vanillaMode = 0;
    protected int oldBiomeMode = 0;
    protected int customMode = 0;

    /**
     * Calculates how much each biome mode is used.
     * @param worlds The loaded worlds.
     */
    protected void calculateBiomeModes(Iterable<? extends LocalWorld> worlds)
    {
        for (LocalWorld world : worlds)
        {
            WorldConfigFile config = world.getSettings();
            if (config != null)
            {
                Class<? extends BiomeGenerator> clazz = config.biomeMode;
                if (clazz.equals(NormalBiomeGenerator.class))
                {
                    normalMode++;
                } else if (clazz.equals(FromImageBiomeGenerator.class))
                {
                    fromImageMode++;
                } else if (clazz.equals(VanillaBiomeGenerator.class))
                {
                    vanillaMode++;
                } else if (clazz.equals(OldBiomeGenerator.class))
                {
                    oldBiomeMode++;
                } else
                {
                    customMode++;
                }
            }
        }
    }

}
