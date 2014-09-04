package com.khorn.terraincontrol.generator.surface;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.configuration.BiomeConfig;

/**
 * Implementation of {@link SurfaceGenerator} that does absolutely nothing.
 *
 */
public class NullSurfaceGenerator implements SurfaceGenerator
{

    @Override
    public void spawn(LocalWorld world, BiomeConfig biomeConfig, double noise, int x, int z)
    {
        // Empty!
    }

    @Override
    public String toString()
    {
        // Make sure that empty name is written to the config files
        return "";
    }
}
