package com.pg85.otg.gen.resource;

import com.pg85.otg.config.ConfigFunction;
import com.pg85.otg.config.biome.Resource;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.logging.ILogger;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.helpers.RandomHelper;
import com.pg85.otg.util.interfaces.IBiomeConfig;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.interfaces.IWorldGenRegion;

import java.util.List;
import java.util.Random;

public class DungeonGen extends Resource
{
    private final int maxAltitude;
    private final int minAltitude;

    public DungeonGen(IBiomeConfig biomeConfig, List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
    {
        super(biomeConfig, args, logger, materialReader);
        assureSize(4, args);

        frequency = readInt(args.get(0), 1, 100);
        rarity = readRarity(args.get(1));
        minAltitude = readInt(args.get(2), Constants.WORLD_DEPTH, Constants.WORLD_HEIGHT - 1);
        maxAltitude = readInt(args.get(3), minAltitude, Constants.WORLD_HEIGHT - 1);
    }

    @Override
    public boolean equals(Object other)
    {
        if (!super.equals(other))
            return false;
        if (other == null)
            return false;
        if (other == this)
            return true;
        if (getClass() != other.getClass())
            return false;
        final DungeonGen compare = (DungeonGen) other;
        return this.minAltitude == compare.minAltitude
               && this.maxAltitude == compare.maxAltitude;
    }

    @Override
    public int getPriority()
    {
        return -20;
    }

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 61 * hash + super.hashCode();
        hash = 61 * hash + this.minAltitude;
        hash = 61 * hash + this.maxAltitude;
        return hash;
    }

    @Override
    public boolean isAnalogousTo(ConfigFunction<IBiomeConfig> other, ILogger logger)
    {
        return getClass() == other.getClass();
    }

    @Override
    public String toString()
    {
        return "Dungeon(" + frequency + "," + rarity + "," + minAltitude + "," + maxAltitude + ")";
    }

    @Override
    public void spawn(IWorldGenRegion worldGenRegion, Random random, boolean villageInChunk, int x, int z, ChunkCoordinate chunkBeingPopulated)
    {
    	// TOOO: Make sure we stay within population bounds, anything outside won't be spawned (unless it's in an existing chunk).
        int y = RandomHelper.numberInRange(random, minAltitude, maxAltitude);
        worldGenRegion.placeDungeon(random, x, y, z);
    }
}
