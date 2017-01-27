package com.khorn.terraincontrol.generator.resource;

import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.util.MaterialSet;
import com.khorn.terraincontrol.util.helpers.RandomHelper;

import java.util.List;
import java.util.Random;

public class CactusGen extends Resource
{

    private int minAltitude;
    private int maxAltitude;
    private MaterialSet sourceBlocks;

    public CactusGen(BiomeConfig biomeConfig, List<String> args) throws InvalidConfigException
    {
        super(biomeConfig);
        assureSize(6, args);

        this.material = readMaterial(args.get(0));
        this.frequency = readInt(args.get(1), 1, 100);
        this.rarity = readRarity(args.get(2));
        this.minAltitude = readInt(args.get(3), TerrainControl.WORLD_DEPTH, TerrainControl.WORLD_HEIGHT);
        this.maxAltitude = readInt(args.get(4), this.minAltitude, TerrainControl.WORLD_HEIGHT);
        this.sourceBlocks = readMaterials(args, 5);
    }

    @Override
    public void spawn(LocalWorld world, Random rand, boolean villageInChunk, int x, int z)
    {
        int y = RandomHelper.numberInRange(rand, this.minAltitude, this.maxAltitude);

        for (int i = 0; i < 10; i++)
        {
            int cactusX = x + rand.nextInt(8) - rand.nextInt(8);
            int cactusBaseY = y + rand.nextInt(4) - rand.nextInt(4);
            int cactusZ = z + rand.nextInt(8) - rand.nextInt(8);

            // Check position
            if (!world.isEmpty(cactusX, cactusBaseY, cactusZ))
                continue;

            // Check foundation
            LocalMaterialData foundationMaterial = world.getMaterial(cactusX, cactusBaseY - 1, cactusZ);
            if (!sourceBlocks.contains(foundationMaterial))
                continue;

            // Check neighbors
            if (!world.isEmpty(cactusX - 1, cactusBaseY, cactusZ))
                continue;
            if (!world.isEmpty(cactusX + 1, cactusBaseY, cactusZ))
                continue;
            if (!world.isEmpty(cactusX, cactusBaseY, cactusZ + 1))
                continue;
            if (!world.isEmpty(cactusX, cactusBaseY, cactusZ + 1))
                continue;

            // Spawn cactus
            int cactusHeight = 1 + rand.nextInt(rand.nextInt(3) + 1);
            for (int dY = 0; dY < cactusHeight; dY++)
            {
                world.setBlock(cactusX, cactusBaseY + dY, cactusZ, material);
            }
        }
    }

    @Override
    public String toString()
    {
        return "Cactus(" + this.material + "," + this.frequency + "," + this.rarity + "," + this.minAltitude + ","
            + this.maxAltitude + makeMaterials(sourceBlocks) + ")";
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 79 * hash + super.hashCode();
        hash = 79 * hash + this.minAltitude;
        hash = 79 * hash + this.maxAltitude;
        hash = 79 * hash + (this.sourceBlocks != null ? this.sourceBlocks.hashCode() : 0);
        return hash;
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
        final CactusGen compare = (CactusGen) other;
        return this.minAltitude == compare.minAltitude
               && this.maxAltitude == compare.maxAltitude
               && (this.sourceBlocks == null ? this.sourceBlocks == compare.sourceBlocks
                   : this.sourceBlocks.equals(compare.sourceBlocks));
    }

    @Override
    public int getPriority()
    {
        return -35;
    }

}
