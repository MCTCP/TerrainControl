package com.khorn.terraincontrol.generator.resource;

import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.util.MaterialSet;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultMaterial;

import java.util.List;
import java.util.Random;

public class LiquidGen extends Resource
{
    private MaterialSet sourceBlocks;
    private int minAltitude;
    private int maxAltitude;

    @Override
    public void spawn(LocalWorld world, Random rand, boolean villageInChunk, int x, int z)
    {
        int y = rand.nextInt(maxAltitude - minAltitude) + minAltitude;

        if (!sourceBlocks.contains(world.getMaterial(x, y + 1, z)))
            return;
        if (!sourceBlocks.contains(world.getMaterial(x, y - 1, z)))
            return;

        if (!world.isEmpty(x, y, z) && (!sourceBlocks.contains(world.getMaterial(x, y, z))))
            return;

        int i = 0;
        int j = 0;

        LocalMaterialData tempBlock = world.getMaterial(x - 1, y, z);

        i = (sourceBlocks.contains(tempBlock)) ? i + 1 : i;
        j = (tempBlock.isMaterial(DefaultMaterial.AIR)) ? j + 1 : j;

        tempBlock = world.getMaterial(x + 1, y, z);

        i = (sourceBlocks.contains(tempBlock)) ? i + 1 : i;
        j = (tempBlock.isMaterial(DefaultMaterial.AIR)) ? j + 1 : j;

        tempBlock = world.getMaterial(x, y, z - 1);

        i = (sourceBlocks.contains(tempBlock)) ? i + 1 : i;
        j = (tempBlock.isMaterial(DefaultMaterial.AIR)) ? j + 1 : j;

        tempBlock = world.getMaterial(x, y, z + 1);

        i = (sourceBlocks.contains(tempBlock)) ? i + 1 : i;
        j = (tempBlock.isMaterial(DefaultMaterial.AIR)) ? j + 1 : j;

        if ((i == 3) && (j == 1))
        {
            world.setBlock(x, y, z, material);
        }
    }

    @Override
    public void load(List<String> args) throws InvalidConfigException
    {
        assureSize(6, args);

        material = readMaterial(args.get(0));
        frequency = readInt(args.get(1), 1, 5000);
        rarity = readRarity(args.get(2));
        minAltitude = readInt(args.get(3), TerrainControl.WORLD_DEPTH, TerrainControl.WORLD_HEIGHT);
        maxAltitude = readInt(args.get(4), minAltitude + 1, TerrainControl.WORLD_HEIGHT);
        sourceBlocks = readMaterials(args, 5);
    }

    @Override
    public String makeString()
    {
        return "Liquid(" + material + "," + frequency + "," + rarity + "," + minAltitude + "," + maxAltitude + makeMaterials(sourceBlocks) + ")";
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 17 * hash + super.hashCode();
        hash = 17 * hash + this.minAltitude;
        hash = 17 * hash + this.maxAltitude;
        hash = 17 * hash + (this.sourceBlocks != null ? this.sourceBlocks.hashCode() : 0);
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
        final LiquidGen compare = (LiquidGen) other;
        return this.minAltitude == compare.minAltitude
               && this.maxAltitude == compare.maxAltitude
               && (this.sourceBlocks == null ? this.sourceBlocks == compare.sourceBlocks
                   : this.sourceBlocks.equals(compare.sourceBlocks));
    }

}
