package com.khorn.terraincontrol.generator.resource;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.ConfigFunction;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.util.helpers.RandomHelper;

import java.util.List;
import java.util.Random;

public class DungeonGen extends Resource
{

    private final int maxAltitude;
    private final int minAltitude;

    public DungeonGen(BiomeConfig biomeConfig, List<String> args) throws InvalidConfigException
    {
        super(biomeConfig);
        assureSize(4, args);

        frequency = readInt(args.get(0), 1, 100);
        rarity = readRarity(args.get(1));
        minAltitude = readInt(args.get(2), TerrainControl.WORLD_DEPTH, TerrainControl.WORLD_HEIGHT);
        maxAltitude = readInt(args.get(3), minAltitude, TerrainControl.WORLD_HEIGHT);
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
    public boolean isAnalogousTo(ConfigFunction<BiomeConfig> other)
    {
        return getClass() == other.getClass();
    }

    @Override
    public String toString()
    {
        return "Dungeon(" + frequency + "," + rarity + "," + minAltitude + "," + maxAltitude + ")";
    }

    @Override
    public void spawn(LocalWorld world, Random random, boolean villageInChunk, int x, int z)
    {
        int y = RandomHelper.numberInRange(random, minAltitude, maxAltitude);
        world.placeDungeon(random, x, y, z);
    }

}