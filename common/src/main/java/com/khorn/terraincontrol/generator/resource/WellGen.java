package com.khorn.terraincontrol.generator.resource;

import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.util.MaterialSet;

import java.util.List;
import java.util.Random;

public class WellGen extends Resource
{

    private final int maxAltitude;
    private final int minAltitude;

    private final LocalMaterialData slab;
    private final LocalMaterialData water;

    private final MaterialSet sourceBlocks;

    public WellGen(BiomeConfig biomeConfig, List<String> args) throws InvalidConfigException
    {
        super(biomeConfig);
        assureSize(8, args);

        this.material = readMaterial(args.get(0));
        this.slab = readMaterial(args.get(1));
        this.water = readMaterial(args.get(2));
        this.frequency = readInt(args.get(3), 1, 100);
        this.rarity = readRarity(args.get(4));
        this.minAltitude = readInt(args.get(5), TerrainControl.WORLD_DEPTH, TerrainControl.WORLD_HEIGHT);
        this.maxAltitude = readInt(args.get(6), minAltitude + 1, TerrainControl.WORLD_HEIGHT);
        this.sourceBlocks = readMaterials(args, 7);
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
        final WellGen compare = (WellGen) other;
        return this.maxAltitude == compare.maxAltitude
               && this.minAltitude == compare.minAltitude
               && this.slab.equals(compare.slab)
               && this.sourceBlocks.equals(compare.sourceBlocks)
               && this.water.equals(compare.water);
    }

    @Override
    public int getPriority()
    {
        return 0;
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 17 * hash + super.hashCode();
        hash = 17 * hash + this.minAltitude;
        hash = 17 * hash + this.maxAltitude;
        hash = 17 * hash + this.slab.hashCode();
        hash = 17 * hash + this.water.hashCode();
        hash = 17 * hash + this.sourceBlocks.hashCode();
        return hash;
    }

    @Override
    public String toString()
    {

        return "Well(" + this.material + "," + this.slab + "," + this.water + "," + this.frequency + "," + this.rarity + "," +
            this.minAltitude + "," + this.maxAltitude + this.makeMaterials(this.sourceBlocks) + ")";
    }

    @Override
    public void spawn(LocalWorld world, Random random, boolean villageInChunk, int x, int z)
    {
        int y = random.nextInt(this.maxAltitude - this.minAltitude) + this.minAltitude;

        while (world.isEmpty(x, y, z) && y > this.minAltitude)
        {
            --y;
        }

        LocalMaterialData sourceBlock = world.getMaterial(x, y, z);

        if (!this.sourceBlocks.contains(sourceBlock))
        {
            return;
        }
        int i;
        int j;

        for (i = -2; i <= 2; ++i)
        {
            for (j = -2; j <= 2; ++j)
            {
                if (world.isEmpty(x + i, y - 1, z + j) && world.isEmpty(x + i, y - 2, z + j))
                {
                    return;
                }
            }
        }

        for (i = -1; i <= 0; ++i)
        {
            for (j = -2; j <= 2; ++j)
            {
                for (int var9 = -2; var9 <= 2; ++var9)
                {
                    world.setBlock(x + j, y + i, z + var9, this.material);
                }
            }
        }

        world.setBlock(x, y, z, this.water);
        world.setBlock(x - 1, y, z, this.water);
        world.setBlock(x + 1, y, z, this.water);
        world.setBlock(x, y, z - 1, this.water);
        world.setBlock(x, y, z + 1, this.water);

        for (i = -2; i <= 2; ++i)
        {
            for (j = -2; j <= 2; ++j)
            {
                if (i == -2 || i == 2 || j == -2 || j == 2)
                {
                    world.setBlock(x + i, y + 1, z + j, this.material);
                }
            }
        }

        world.setBlock(x + 2, y + 1, z, this.slab);
        world.setBlock(x - 2, y + 1, z, this.slab);
        world.setBlock(x, y + 1, z + 2, this.slab);
        world.setBlock(x, y + 1, z - 2, this.slab);

        for (i = -1; i <= 1; ++i)
        {
            for (j = -1; j <= 1; ++j)
            {
                if (i == 0 && j == 0)
                {
                    world.setBlock(x + i, y + 4, z + j, this.material);
                } else
                {
                    world.setBlock(x + i, y + 4, z + j, this.slab);
                }
            }
        }

        for (i = 1; i <= 3; ++i)
        {
            world.setBlock(x - 1, y + i, z - 1, this.material);
            world.setBlock(x - 1, y + i, z + 1, this.material);
            world.setBlock(x + 1, y + i, z - 1, this.material);
            world.setBlock(x + 1, y + i, z + 1, this.material);
        }
    }

}