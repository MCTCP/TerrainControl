package com.pg85.otg.generator.biome;

import com.pg85.otg.LocalWorld;
import com.pg85.otg.generator.biome.layers.Layer;
import com.pg85.otg.generator.biome.layers.LayerFactory;

/**
 * This is the normal biome mode, which has all of Open Terrain Generator's features.
 */
public class BeforeGroupsBiomeGenerator extends LayeredBiomeGenerator
{

    public BeforeGroupsBiomeGenerator(LocalWorld world)
    {
        super(world);
    }

    @Override
    protected Layer[] initLayers()
    {
        return LayerFactory.createBeforeGroups(world);
    }

}
