package com.khorn.terraincontrol.generator.biome;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.generator.biome.layers.Layer;
import com.khorn.terraincontrol.generator.biome.layers.LayerFactory;

/**
 * Generates biomes from the image specified by the WorldConfig.
 *
 */
public class FromImageBiomeGenerator extends LayeredBiomeGenerator
{
    public FromImageBiomeGenerator(LocalWorld world)
    {
        super(world);
    }

    @Override
    protected Layer[] initLayers()
    {
        return LayerFactory.createFromImage(world);
    }
}
