package com.khorn.terraincontrol.generator.resource;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.util.helpers.MathHelper;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultMaterial;

import java.util.List;
import java.util.Random;

public class IceSpikeGen extends Resource
{
    public static enum SpikeType {
        Basement,
        SmallSpike,
        HugeSpike;
    }

    private int maxAltitude;
    private int minAltitude;
    private SpikeType type;
    private List<Integer> sourceBlocks;

    @Override
    protected void load(List<String> args) throws InvalidConfigException
    {
        assureSize(2, args);

        blockId = readBlockId(args.get(0));
        blockData = readBlockData(args.get(0));

        // Read type
        String typeString = args.get(1);
        this.type = null;
        for(SpikeType possibleType : SpikeType.values())
        {
            if (possibleType.toString().equalsIgnoreCase(typeString)) {
                type = possibleType;
                break;
            }
        }
        if (this.type == null)
        {
            throw new InvalidConfigException("Unknown spike type " + typeString);
        }

        frequency = readInt(args.get(2), 1, 30);
        rarity = readRarity(args.get(3));
        minAltitude = readInt(args.get(4), TerrainControl.worldDepth, TerrainControl.worldHeight);
        maxAltitude = readInt(args.get(5), minAltitude, TerrainControl.worldHeight);

        sourceBlocks = this.readBlockIds(args, 6);
    }

    @Override
    public String makeString()
    {
        return "IceSpike(" + makeMaterial(blockId, blockData) + "," + type + "," + frequency + "," + rarity + "," + minAltitude + "," + maxAltitude + makeMaterial(sourceBlocks) + ")";
    }

    @Override
    public void spawn(LocalWorld world, Random random, boolean villageInChunk, int x, int z)
    {
        switch(type) {
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
        int y = MathHelper.getRandomNumberInRange(random, this.minAltitude, this.maxAltitude);

        while ((world.isEmpty(x, y, z)) && (y > 2))
        {
            y--;
        }
        if (!this.sourceBlocks.contains(world.getTypeId(x, y, z)))
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
                        int localBlock = world.getTypeId(actualX, deltaY, actualZ);
                        if (this.sourceBlocks.contains(localBlock))
                        {
                            world.setBlock(actualX, deltaY, actualZ, this.blockId, 0);
                        }
                    }
                }
            }
        }
    }

    public void spawnSpike(LocalWorld par1World, Random random, int x, int z, boolean hugeSpike)
    {
        int y = MathHelper.getRandomNumberInRange(random, minAltitude, maxAltitude);
        while (par1World.isEmpty(x, y, z) && y > 2)
        {
            --y;
        }

        if (!sourceBlocks.contains(par1World.getTypeId(x, y, z)))
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
            float var9 = (1.0F - (float) var8 / (float) var6) * (float) var7;
            var10 = MathHelper.ceil(var9);

            for (var11 = -var10; var11 <= var10; ++var11)
            {
                float var12 = MathHelper.abs(var11) - 0.25F;

                for (int var13 = -var10; var13 <= var10; ++var13)
                {
                    float var14 = MathHelper.abs(var13) - 0.25F;

                    if ((var11 == 0 && var13 == 0 || var12 * var12 + var14 * var14 <= var9 * var9) && (var11 != -var10 && var11 != var10 && var13 != -var10 && var13 != var10 || random.nextFloat() <= 0.75F))
                    {
                        int var15 = par1World.getTypeId(x + var11, y + var8, z + var13);

                        if (var15 == DefaultMaterial.AIR.id || sourceBlocks.contains(var15))
                        {
                            par1World.setBlock(x + var11, y + var8, z + var13, this.blockId, this.blockData);
                        }

                        if (var8 != 0 && var10 > 1)
                        {
                            var15 = par1World.getTypeId(x + var11, y - var8, z + var13);

                            if (var15 == DefaultMaterial.AIR.id || sourceBlocks.contains(var15))
                            {
                                par1World.setBlock(x + var11, y - var8, z + var13, this.blockId, this.blockData);
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
                        int var18 = par1World.getTypeId(x + var16, var11, z + var10);

                        if (var18 == DefaultMaterial.AIR.id || sourceBlocks.contains(var18) || var18 == this.blockId)
                        {
                            par1World.setBlock(x + var16, var11, z + var10, this.blockId, this.blockData);
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

    @Override
    public boolean isAnalogousTo(Resource other)
    {
        return other.getClass() == getClass() && other.blockId == this.blockId && other.blockData == this.blockData && ((IceSpikeGen) other).type == this.type;
    }

}
