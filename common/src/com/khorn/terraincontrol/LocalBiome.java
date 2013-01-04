package com.khorn.terraincontrol;

import com.khorn.terraincontrol.configuration.BiomeConfig;

public interface LocalBiome
{
    public abstract boolean isCustom();

    /**
     * Sets the post generator effects. For the client it are things like
     * colors. For the server it are things like mob spawning.
     *
     * @param config The BiomeConfig of the biome.
     */
    public abstract void setEffects(BiomeConfig config);

    public abstract String getName();

    public abstract int getId();

    public abstract int getCustomId();

    public abstract float getTemperature();

    public abstract float getWetness();

    public abstract float getSurfaceHeight();

    public abstract float getSurfaceVolatility();

    public abstract byte getSurfaceBlock();

    public abstract byte getGroundBlock();
}