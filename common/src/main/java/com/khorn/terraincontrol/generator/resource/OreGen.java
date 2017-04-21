package com.khorn.terraincontrol.generator.resource;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.util.MaterialSet;
import com.khorn.terraincontrol.util.helpers.MathHelper;
import com.khorn.terraincontrol.util.helpers.RandomHelper;

import java.util.List;
import java.util.Random;

public class OreGen extends Resource
{
    private final int maxAltitude;
    private final int maxSize;
    private final int minAltitude;
    private final MaterialSet sourceBlocks;

    public OreGen(BiomeConfig biomeConfig, List<String> args) throws InvalidConfigException
    {
        super(biomeConfig);
        assureSize(7, args);

        this.material = readMaterial(args.get(0));
        this.maxSize = readInt(args.get(1), 1, 128);
        this.frequency = readInt(args.get(2), 1, 100);
        this.rarity = readRarity(args.get(3));
        this.minAltitude = readInt(args.get(4), TerrainControl.WORLD_DEPTH, TerrainControl.WORLD_HEIGHT);
        this.maxAltitude = readInt(args.get(5), minAltitude, TerrainControl.WORLD_HEIGHT);
        this.sourceBlocks = readMaterials(args, 6);
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
        final OreGen compare = (OreGen) other;
        return this.maxSize == compare.maxSize
               && this.minAltitude == compare.minAltitude
               && this.maxAltitude == compare.maxAltitude
               && (this.sourceBlocks == null ? this.sourceBlocks == compare.sourceBlocks
                   : this.sourceBlocks.equals(compare.sourceBlocks));
    }

    @Override
    public int getPriority()
    {
        return 10;
    }

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 11 * hash + super.hashCode();
        hash = 11 * hash + this.minAltitude;
        hash = 11 * hash + this.maxAltitude;
        hash = 11 * hash + this.maxSize;
        hash = 11 * hash + (this.sourceBlocks != null ? this.sourceBlocks.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString()
    {
        return "Ore(" + this.material + "," + this.maxSize + "," + this.frequency + "," + this.rarity + "," + this.minAltitude + "," +
            this.maxAltitude + makeMaterials(this.sourceBlocks) + ")";
    }

    @Override
    public void spawn(LocalWorld world, Random rand, boolean villageInChunk, int x, int z)
    {
        int y = RandomHelper.numberInRange(rand, this.minAltitude, this.maxAltitude);

        float f = rand.nextFloat() * (float) Math.PI;

        double d1 = x + 8 + MathHelper.sin(f) * this.maxSize / 8.0F;
        double d2 = x + 8 - MathHelper.sin(f) * this.maxSize / 8.0F;
        double d3 = z + 8 + MathHelper.cos(f) * this.maxSize / 8.0F;
        double d4 = z + 8 - MathHelper.cos(f) * this.maxSize / 8.0F;

        double d5 = y + rand.nextInt(3) - 2;
        double d6 = y + rand.nextInt(3) - 2;

        for (int i = 0; i < this.maxSize; i++)
        {
            float iFactor = (float) i / (float) this.maxSize;
            double d7 = d1 + (d2 - d1) * iFactor;
            double d8 = d5 + (d6 - d5) * iFactor;
            double d9 = d3 + (d4 - d3) * iFactor;

            double d10 = rand.nextDouble() * this.maxSize / 16.0D;
            double d11 = (MathHelper.sin((float) Math.PI * iFactor) + 1.0) * d10 + 1.0;
            double d12 = (MathHelper.sin((float) Math.PI * iFactor) + 1.0) * d10 + 1.0;

            int j = MathHelper.floor(d7 - d11 / 2.0D);
            int k = MathHelper.floor(d8 - d12 / 2.0D);
            int m = MathHelper.floor(d9 - d11 / 2.0D);

            int n = MathHelper.floor(d7 + d11 / 2.0D);
            int i1 = MathHelper.floor(d8 + d12 / 2.0D);
            int i2 = MathHelper.floor(d9 + d11 / 2.0D);

            for (int i3 = j; i3 <= n; i3++)
            {
                double d13 = (i3 + 0.5D - d7) / (d11 / 2.0D);
                if (d13 * d13 < 1.0D)
                {
                    for (int i4 = k; i4 <= i1; i4++)
                    {
                        double d14 = (i4 + 0.5D - d8) / (d12 / 2.0D);
                        if (d13 * d13 + d14 * d14 < 1.0D)
                        {
                            for (int i5 = m; i5 <= i2; i5++)
                            {
                                double d15 = (i5 + 0.5D - d9) / (d11 / 2.0D);
                                if ((d13 * d13 + d14 * d14 + d15 * d15 < 1.0D) && this.sourceBlocks.contains(world.getMaterial(i3, i4, i5)))
                                {
                                    world.setBlock(i3, i4, i5, this.material);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
