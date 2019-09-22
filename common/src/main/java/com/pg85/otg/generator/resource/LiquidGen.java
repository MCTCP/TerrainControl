package com.pg85.otg.generator.resource;

import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.util.helpers.RandomHelper;
import com.pg85.otg.util.materials.MaterialSet;

import java.util.List;
import java.util.Random;

public class LiquidGen extends Resource
{
    private final int maxAltitude;
    private final int minAltitude;
    private final MaterialSet sourceBlocks;
    private boolean bLoaded = false;

    public LiquidGen(BiomeConfig biomeConfig, List<String> args) throws InvalidConfigException
    {
        super(biomeConfig);
        assureSize(6, args);

        material = readMaterial(args.get(0));
        frequency = readInt(args.get(1), 1, 5000);
        rarity = readRarity(args.get(2));
        minAltitude = readInt(args.get(3), PluginStandardValues.WORLD_DEPTH,
                PluginStandardValues.WORLD_HEIGHT);
        maxAltitude = readInt(args.get(4), minAltitude,
                PluginStandardValues.WORLD_HEIGHT);
        sourceBlocks = readMaterials(args, 5);
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
        return "Liquid(" + material + "," + frequency + "," + rarity + "," + minAltitude + "," + maxAltitude + makeMaterials(sourceBlocks) + ")";
    }

    @Override
    public void spawn(LocalWorld world, Random rand, boolean villageInChunk, int x, int z)
    {
        int y = RandomHelper.numberInRange(rand, minAltitude, maxAltitude);

        if (!sourceBlocks.contains(world.getMaterial(x, y + 1, z, false)))
            return;
        if (!sourceBlocks.contains(world.getMaterial(x, y - 1, z, false)))
            return;

        if (!world.isNullOrAir(x, y, z, false) && (!sourceBlocks.contains(world.getMaterial(x, y, z, false))))
            return;
        
        if (!bLoaded ) {
            parseMaterials(world, material, sourceBlocks);
            bLoaded = true;
        }

        int i = 0;
        int j = 0;

        LocalMaterialData tempBlock = world.getMaterial(x - 1, y, z, false);

        i = (sourceBlocks.contains(tempBlock)) ? i + 1 : i;
        j = (tempBlock.isAir()) ? j + 1 : j;

        tempBlock = world.getMaterial(x + 1, y, z, false);

        i = (sourceBlocks.contains(tempBlock)) ? i + 1 : i;
        j = (tempBlock.isAir()) ? j + 1 : j;

        tempBlock = world.getMaterial(x, y, z - 1, false);

        i = (sourceBlocks.contains(tempBlock)) ? i + 1 : i;
        j = (tempBlock.isAir()) ? j + 1 : j;

        tempBlock = world.getMaterial(x, y, z + 1, false);

        i = (sourceBlocks.contains(tempBlock)) ? i + 1 : i;
        j = (tempBlock.isAir()) ? j + 1 : j;

        if ((i == 3) && (j == 1))
        {
            world.setBlock(x, y, z, material, null, false);
        }
    }

}
