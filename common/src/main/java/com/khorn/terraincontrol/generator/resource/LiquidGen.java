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

public class LiquidGen extends Resource
{
    private final int maxAltitude;
    private final int minAltitude;
    private final MaterialSet sourceBlocks;

    public LiquidGen(BiomeConfig biomeConfig, List<String> args) throws InvalidConfigException
    {
        super(biomeConfig);
        assureSize(6, args);

        this.material = readMaterial(args.get(0));
        this.frequency = readInt(args.get(1), 1, 5000);
        this.rarity = readRarity(args.get(2));
        this.minAltitude = readInt(args.get(3), TerrainControl.WORLD_DEPTH, TerrainControl.WORLD_HEIGHT);
        this.maxAltitude = readInt(args.get(4), this.minAltitude, TerrainControl.WORLD_HEIGHT);
        this.sourceBlocks = readMaterials(args, 5);
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

    @Override
    public int getPriority()
    {
        return 2;
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
    public String toString()
    {
        return "Liquid(" + this.material + "," + this.frequency + "," + this.rarity + "," + this.minAltitude
            + "," + this.maxAltitude + makeMaterials(this.sourceBlocks) + ")";
    }

    @Override
    public void spawn(LocalWorld world, Random rand, boolean villageInChunk, int x, int z)
    {
        int y = RandomHelper.numberInRange(rand, this.minAltitude, this.maxAltitude);

        if (!this.sourceBlocks.contains(world.getMaterial(x, y + 1, z)))
            return;
        if (!this.sourceBlocks.contains(world.getMaterial(x, y - 1, z)))
            return;

        if (!world.isEmpty(x, y, z) && (!this.sourceBlocks.contains(world.getMaterial(x, y, z))))
            return;

        int i = 0;
        int j = 0;

        LocalMaterialData tempBlock = world.getMaterial(x - 1, y, z);

        i = (this.sourceBlocks.contains(tempBlock)) ? i + 1 : i;
        j = (tempBlock.isAir()) ? j + 1 : j;

        tempBlock = world.getMaterial(x + 1, y, z);

        i = (this.sourceBlocks.contains(tempBlock)) ? i + 1 : i;
        j = (tempBlock.isAir()) ? j + 1 : j;

        tempBlock = world.getMaterial(x, y, z - 1);

        i = (this.sourceBlocks.contains(tempBlock)) ? i + 1 : i;
        j = (tempBlock.isAir()) ? j + 1 : j;

        tempBlock = world.getMaterial(x, y, z + 1);

        i = (this.sourceBlocks.contains(tempBlock)) ? i + 1 : i;
        j = (tempBlock.isAir()) ? j + 1 : j;

        if ((i == 3) && (j == 1))
        {
            world.setBlock(x, y, z, this.material);
        }
    }

}
