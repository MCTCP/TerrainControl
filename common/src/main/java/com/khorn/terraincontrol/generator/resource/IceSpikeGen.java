package com.khorn.terraincontrol.generator.resource;

import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.ConfigFunction;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.util.MaterialSet;
import com.khorn.terraincontrol.util.helpers.MathHelper;
import com.khorn.terraincontrol.util.helpers.RandomHelper;

import java.util.List;
import java.util.Random;

public class IceSpikeGen extends Resource
{
    public enum SpikeType {
        Basement,
        HugeSpike,
        SmallSpike;
    }

    private final int maxAltitude;
    private final int minAltitude;
    private final MaterialSet sourceBlocks;
    private SpikeType type;

    public IceSpikeGen(BiomeConfig biomeConfig, List<String> args) throws InvalidConfigException
    {
        super(biomeConfig);
        assureSize(2, args);

        this.material = readMaterial(args.get(0));

        // Read type
        String typeString = args.get(1);
        this.type = null;
        for(SpikeType possibleType : SpikeType.values())
        {
            if (possibleType.toString().equalsIgnoreCase(typeString)) {
                this.type = possibleType;
                break;
            }
        }
        if (this.type == null)
        {
            throw new InvalidConfigException("Unknown spike type " + typeString);
        }

        this.frequency = readInt(args.get(2), 1, 30);
        this.rarity = readRarity(args.get(3));
        this.minAltitude = readInt(args.get(4), TerrainControl.WORLD_DEPTH, TerrainControl.WORLD_HEIGHT);
        this.maxAltitude = readInt(args.get(5), this.minAltitude, TerrainControl.WORLD_HEIGHT);

        this.sourceBlocks = readMaterials(args, 6);
    }

    @Override
    public int getPriority()
    {
        return -21;
    }

    @Override
    public boolean isAnalogousTo(ConfigFunction<BiomeConfig> other)
    {
        // Enforces shape as well
        return super.isAnalogousTo(other) && ((IceSpikeGen) other).type == this.type;
    }

    @Override
    public String toString()
    {
        return "IceSpike(" + this.material + "," + this.type + "," + this.frequency + "," + this.rarity
            + "," + this.minAltitude + "," + this.maxAltitude + makeMaterials(this.sourceBlocks) + ")";
    }

    @Override
    public void spawn(LocalWorld world, Random random, boolean villageInChunk, int x, int z)
    {
        switch(this.type) {
            case Basement:
                spawnBasement(world, random, x, z);
                break;
            case HugeSpike:
                spawnSpike(world, random, x, z, true);
                break;
            case SmallSpike:
                spawnSpike(world, random, x, z, false);
                break;
        }
    }

    public void spawnBasement(LocalWorld world, Random random,int x, int z)
    {
        int y = RandomHelper.numberInRange(random, this.minAltitude, this.maxAltitude);

        while ((world.isEmpty(x, y, z)) && (y > 2))
        {
            y--;
        }
        if (!this.sourceBlocks.contains(world.getMaterial(x, y, z)))
        {
            return;
        }
        int radius = random.nextInt(2) + 2;
        int one = 1;
        for (int actualX = x - radius; actualX <= x + radius; actualX++)
        {
            for (int actualZ = z - radius; actualZ <= z + radius; actualZ++)
            {
                int deltaX = actualX - x;
                int deltaZ = actualZ - z;
                if (deltaX * deltaX + deltaZ * deltaZ <= radius * radius)
                {
                    for (int deltaY = y - one; deltaY <= y + one; deltaY++)
                    {
                        LocalMaterialData localBlock = world.getMaterial(actualX, deltaY, actualZ);
                        if (this.sourceBlocks.contains(localBlock))
                        {
                            world.setBlock(actualX, deltaY, actualZ, this.material);
                        }
                    }
                }
            }
        }
    }

    public void spawnSpike(LocalWorld par1World, Random random, int x, int z, boolean hugeSpike)
    {
        int y = RandomHelper.numberInRange(random, this.minAltitude, this.maxAltitude);
        while (par1World.isEmpty(x, y, z) && y > 2)
        {
            --y;
        }

        if (!this.sourceBlocks.contains(par1World.getMaterial(x, y, z)))
        {
            return;
        }

        y += random.nextInt(4);
        int var6 = random.nextInt(4) + 7;
        int var7 = var6 / 4 + random.nextInt(2);

        if (var7 > 1 && hugeSpike)
        {
            y += 10 + random.nextInt(30);
        }

        int var8;
        int var10;
        int var11;

        for (var8 = 0; var8 < var6; ++var8)
        {
            float var9 = (1.0F - (float) var8 / (float) var6) * var7;
            var10 = MathHelper.ceil(var9);

            for (var11 = -var10; var11 <= var10; ++var11)
            {
                float var12 = MathHelper.abs(var11) - 0.25F;

                for (int var13 = -var10; var13 <= var10; ++var13)
                {
                    float var14 = MathHelper.abs(var13) - 0.25F;

                    if ((var11 == 0 && var13 == 0 || var12 * var12 + var14 * var14 <= var9 * var9) && (var11 != -var10 && var11 != var10 && var13 != -var10 && var13 != var10 || random.nextFloat() <= 0.75F))
                    {
                        LocalMaterialData sourceBlock = par1World.getMaterial(x + var11, y + var8, z + var13);

                        if (sourceBlock.isAir() || this.sourceBlocks.contains(sourceBlock))
                        {
                            par1World.setBlock(x + var11, y + var8, z + var13, this.material);
                        }

                        if (var8 != 0 && var10 > 1)
                        {
                            sourceBlock = par1World.getMaterial(x + var11, y - var8, z + var13);

                            if (sourceBlock.isAir() || this.sourceBlocks.contains(sourceBlock))
                            {
                                par1World.setBlock(x + var11, y - var8, z + var13, this.material);
                            }
                        }
                    }
                }
            }
        }

        var8 = var7 - 1;

        if (var8 < 0)
        {
            var8 = 0;
        } else if (var8 > 1)
        {
            var8 = 1;
        }

        for (int var16 = -var8; var16 <= var8; ++var16)
        {
            var10 = -var8;

            while (var10 <= var8)
            {
                var11 = y - 1;
                int var17 = 50;

                if (Math.abs(var16) == 1 && Math.abs(var10) == 1)
                {
                    var17 = random.nextInt(5);
                }

                while (true)
                {
                    if (var11 > 50)
                    {
                        LocalMaterialData var18 = par1World.getMaterial(x + var16, var11, z + var10);

                        if (var18.isAir() || this.sourceBlocks.contains(var18) || var18.equals(this.material))
                        {
                            par1World.setBlock(x + var16, var11, z + var10, this.material);
                            --var11;
                            --var17;

                            if (var17 <= 0)
                            {
                                var11 -= random.nextInt(5) + 1;
                                var17 = random.nextInt(5);
                            }

                            continue;
                        }
                    }

                    ++var10;
                    break;
                }
            }
        }
    }

}
