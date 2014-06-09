package com.khorn.terraincontrol.generator.resource;

import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.util.MaterialSet;

import java.util.List;
import java.util.Random;

public class CactusGen extends Resource
{

    private int minAltitude;
    private int maxAltitude;
    private MaterialSet sourceBlocks;

    @Override
    public void spawn(LocalWorld world, Random rand, boolean villageInChunk, int x, int z)
    {
        int y = rand.nextInt(maxAltitude - minAltitude) + minAltitude;

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
    public String makeString()
    {
        return "Cactus(" + material + "," + frequency + "," + rarity + "," + minAltitude + "," + maxAltitude + makeMaterials(sourceBlocks) + ")";
    }

    @Override
    public void load(List<String> args) throws InvalidConfigException
    {
        assureSize(6, args);

        material = readMaterial(args.get(0));
        frequency = readInt(args.get(1), 1, 100);
        rarity = readRarity(args.get(2));
        minAltitude = readInt(args.get(3), TerrainControl.WORLD_DEPTH, TerrainControl.WORLD_HEIGHT);
        maxAltitude = readInt(args.get(4), minAltitude + 1, TerrainControl.WORLD_HEIGHT);
        sourceBlocks = readMaterials(args, 5);
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

}
