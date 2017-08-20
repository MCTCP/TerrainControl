package com.pg85.otg.generator.biome;

import com.pg85.otg.LocalWorld;
import com.pg85.otg.generator.biome.layers.Layer;
import com.pg85.otg.generator.biome.layers.LayerFactory;

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
