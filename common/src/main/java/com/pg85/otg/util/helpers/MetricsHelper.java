package com.pg85.otg.util.helpers;

import com.pg85.otg.LocalWorld;
import com.pg85.otg.configuration.WorldConfig;
import com.pg85.otg.generator.biome.*;

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
    protected int beforeGroupsBiomeMode = 0;
    protected int customMode = 0;

    /**
     * Calculates how much each biome mode is used.
     * @param worlds The loaded worlds.
     */
    protected void calculateBiomeModes(Iterable<? extends LocalWorld> worlds)
    {
        for (LocalWorld world : worlds)
        {
            WorldConfig config = world.getConfigs().getWorldConfig();
            if (config != null)
            {
                Class<? extends BiomeGenerator> clazz = config.biomeMode;
                if (clazz.equals(NormalBiomeGenerator.class))
                {
                    normalMode++;
                } else if (clazz.equals(FromImageBiomeGenerator.class))
                {
                    fromImageMode++;
                } else if (VanillaBiomeGenerator.class.isAssignableFrom(clazz))
                {
                    vanillaMode++;
                } else if (clazz.equals(OldBiomeGenerator.class))
                {
                    oldBiomeMode++;
                } else if (clazz.equals(BeforeGroupsBiomeGenerator.class))
                {
                    beforeGroupsBiomeMode++;
                } else
                {
                    customMode++;
                }
            }
        }
    }

}
