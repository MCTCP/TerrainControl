package com.khorn.terraincontrol.generator.biome;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.generator.biome.layers.Layer;
import com.khorn.terraincontrol.generator.biome.layers.LayerFactory;

/**
 * This is the normal biome mode, which has all of Terrain Control's features.
 */
public class NormalBiomeGenerator extends LayeredBiomeGenerator
{

    public NormalBiomeGenerator(LocalWorld world)
    {
        super(world);
    }

    @Override
    protected Layer[] initLayers()
    {
        return LayerFactory.createNormal(world);
    }

}
