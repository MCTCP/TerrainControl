package com.khorn.terraincontrol.generator.resource;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.ConfigFunction;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.util.ChunkCoordinate;

import java.util.List;
import java.util.Random;

public final class FossilGen extends Resource
{

    public FossilGen(BiomeConfig biomeConfig, List<String> args) throws InvalidConfigException
    {
        super(biomeConfig);
        assureSize(1, args);

        rarity = readRarity(args.get(0));
    }

    @Override
    public void spawn(LocalWorld world, Random random, boolean villageInChunk, int x, int z)
    {
        return;
    }

    @Override
    protected void spawnInChunk(LocalWorld world, Random random, boolean villageInChunk, ChunkCoordinate chunkCoord)
    {
        if (random.nextDouble() * 100.0 > rarity)
            return;
        // Unfortunately, Minecraft ignores the passed random instance, and
        // creates one based on the chunk coords and world seed. This means
        // that spawning the object multiple times in a chunk will just
        // spawn exactly the same object at exactly the same location. In
        // other words: don't bother adding a frequency parameter, unless
        // you are going to rewrite the fossil code.
        world.placeFossil(random, chunkCoord);
    }

    @Override
    public boolean isAnalogousTo(ConfigFunction<BiomeConfig> other)
    {
        return getClass() == other.getClass();
    }

    @Override
    public int getPriority()
    {
        return -21;
    }

    @Override
    public String toString()
    {
        return "Fossil(" + rarity + ")";
    }

    @Override
    public int hashCode()
    {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object other)
    {
        return super.equals(other);
    }

}
