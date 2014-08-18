package com.khorn.terraincontrol.generator.biome;

import com.khorn.terraincontrol.LocalWorld;

/**
 * Represents the vanilla Minecraft generator.
 * 
 * Unlike other biome generators this biome generator isn't automatically
 * registered. The reason for this is simple: it has a dependency on Minecraft.
 * Implementations should register a subclass of this class using
 * {@link BiomeModeManager#register(String, Class)} with the name set to
 * {@link #GENERATOR_NAME}.
 *
 */
public abstract class VanillaBiomeGenerator extends BiomeGenerator {

    /**
     * Name the vanilla generator should register itself with.
     */
    public static final String GENERATOR_NAME = "Default";

    public VanillaBiomeGenerator(LocalWorld world)
    {
        super(world);
    }

    @Override
    public boolean isCached()
    {
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * Redeclared as abstract to force the vanilla generator to override
     * this method.
     */
    @Override
    public abstract void cleanupCache();

}
