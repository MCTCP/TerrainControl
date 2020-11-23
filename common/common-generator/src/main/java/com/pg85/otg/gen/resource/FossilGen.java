package com.pg85.otg.gen.resource;

import com.pg85.otg.config.ConfigFunction;
import com.pg85.otg.config.biome.Resource;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.logging.ILogger;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.interfaces.IBiomeConfig;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.interfaces.IWorldGenRegion;

import java.util.List;
import java.util.Random;

public final class FossilGen extends Resource
{
    public FossilGen(IBiomeConfig biomeConfig, List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
    {
        super(biomeConfig, args, logger, materialReader);
        assureSize(1, args);

        rarity = readRarity(args.get(0));
    }

    @Override
    public void spawn(IWorldGenRegion worldGenRegion, Random random, boolean villageInChunk, int x, int z, ChunkCoordinate chunkBeingPopulated)
    {
        // Left blank, as spawnInChunk already handles this.
    }

    @Override
    protected void spawnInChunk(IWorldGenRegion worldGenRegion, Random random, boolean villageInChunk, ChunkCoordinate chunkBeingPopulated, ILogger logger, IMaterialReader materialReader)
    {
        if (random.nextDouble() * 100.0 > rarity)
        {
            return;
        }
        
        // Unfortunately, Minecraft ignores the passed random instance, and
        // creates one based on the chunk coords and world seed. This means
        // that spawning the object multiple times in a chunk will just
        // spawn exactly the same object at exactly the same location. In
        // other words: don't bother adding a frequency parameter, unless
        // you are going to rewrite the fossil code.
        worldGenRegion.placeFossil(random, chunkBeingPopulated);
    }

    @Override
    public boolean isAnalogousTo(ConfigFunction<IBiomeConfig> other, ILogger logger)
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
