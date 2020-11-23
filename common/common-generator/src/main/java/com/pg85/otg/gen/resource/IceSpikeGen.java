package com.pg85.otg.gen.resource;

import com.pg85.otg.config.ConfigFunction;
import com.pg85.otg.config.biome.Resource;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.logging.ILogger;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.helpers.MathHelper;
import com.pg85.otg.util.helpers.RandomHelper;
import com.pg85.otg.util.interfaces.IBiomeConfig;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.interfaces.IWorldGenRegion;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.MaterialSet;

import java.util.List;
import java.util.Random;
public class IceSpikeGen extends Resource
{
    public enum SpikeType
    {
        Basement,
        HugeSpike,
        SmallSpike;
    }

    private final int maxAltitude;
    private final int minAltitude;
    private final MaterialSet sourceBlocks;
    private SpikeType type;

    public IceSpikeGen(IBiomeConfig biomeConfig, List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
    {
        super(biomeConfig, args, logger, materialReader);
        assureSize(2, args);

        material = materialReader.readMaterial(args.get(0));

        // Read type
        String typeString = args.get(1);
        this.type = null;
        for(SpikeType possibleType : SpikeType.values())
        {
            if (possibleType.toString().equalsIgnoreCase(typeString))
            {
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
        minAltitude = readInt(args.get(4), Constants.WORLD_DEPTH, Constants.WORLD_HEIGHT - 1);
        maxAltitude = readInt(args.get(5), minAltitude, Constants.WORLD_HEIGHT - 1);

        sourceBlocks = readMaterials(args, 6, materialReader);
    }

    @Override
    public int getPriority()
    {
        return -21;
    }

    @Override
    public boolean isAnalogousTo(ConfigFunction<IBiomeConfig> other, ILogger logger)
    {
        // Enforces shape as well
        return super.isAnalogousTo(other, logger) && ((IceSpikeGen) other).type == this.type;
    }

    @Override
    public String toString()
    {
        return "IceSpike(" + material + "," + type + "," + frequency + "," + rarity + "," + minAltitude + "," + maxAltitude + makeMaterials(sourceBlocks) + ")";
    }

    @Override
    public void spawn(IWorldGenRegion worldGenregion, Random random, boolean villageInChunk, int x, int z, ChunkCoordinate chunkBeingPopulated)
    {
        switch(type) {
            case Basement:
                spawnBasement(worldGenregion, random, x, z, chunkBeingPopulated);
                break;
            case HugeSpike:
                spawnSpike(worldGenregion, random, x, z, true, chunkBeingPopulated);
                break;
            case SmallSpike:
                spawnSpike(worldGenregion, random, x, z, false, chunkBeingPopulated);
                break;
        }
    }

    private void spawnBasement(IWorldGenRegion worldGenregion, Random random,int x, int z, ChunkCoordinate chunkBeingPopulated)
    {
    	// Make sure we stay within population bounds, anything outside won't be spawned (unless it's in an existing chunk).
        int y = RandomHelper.numberInRange(random, this.minAltitude, this.maxAltitude);

        LocalMaterialData worldMaterial;
        while (y > 2 && (worldMaterial = worldGenregion.getMaterial(x, y, z, chunkBeingPopulated)) != null && worldMaterial.isAir())
        {
            y--;
        }
        
        //parseMaterials(worldGenregion.getWorldConfig(), material, sourceBlocks);
        
        if ((worldMaterial = worldGenregion.getMaterial(x, y, z, chunkBeingPopulated)) == null || !this.sourceBlocks.contains(worldMaterial))
        {
            return;
        }
        
        int radius = random.nextInt(2) + 2;
        int one = 1;
        int deltaX;
        int deltaZ;
        for (int actualX = x - radius; actualX <= x + radius; actualX++)
        {
            for (int actualZ = z - radius; actualZ <= z + radius; actualZ++)
            {
                deltaX = actualX - x;
                deltaZ = actualZ - z;
                if (deltaX * deltaX + deltaZ * deltaZ <= radius * radius)
                {
                    for (int deltaY = y - one; deltaY <= y + one; deltaY++)
                    {
                    	worldMaterial = worldGenregion.getMaterial(actualX, deltaY, actualZ, chunkBeingPopulated);
                        if (worldMaterial != null && this.sourceBlocks.contains(worldMaterial))
                        {
                        	worldGenregion.setBlock(actualX, deltaY, actualZ, this.material, null, chunkBeingPopulated, true);
                        }
                    }
                }
            }
        }
    }

    private void spawnSpike(IWorldGenRegion worldGenregion, Random random, int x, int z, boolean hugeSpike, ChunkCoordinate chunkBeingPopulated)
    {
    	// Make sure we stay within population bounds, anything outside won't be spawned (unless it's in an existing chunk).
        int y = RandomHelper.numberInRange(random, minAltitude, maxAltitude);
        LocalMaterialData worldMaterial;
        while (y > 2 && (worldMaterial = worldGenregion.getMaterial(x, y, z, chunkBeingPopulated)) != null && worldMaterial.isAir())
        {
            --y;
        }
        
        //parseMaterials(worldGenregion.getWorldConfig(), material, sourceBlocks);

        if ((worldMaterial = worldGenregion.getMaterial(x, y, z, chunkBeingPopulated)) == null || !sourceBlocks.contains(worldMaterial))
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
        float var9;
        int var10;
        int var11;
        float var12;
        float var14;
        
        for (var8 = 0; var8 < var6; ++var8)
        {
            var9 = (1.0F - (float) var8 / (float) var6) * var7;
            var10 = MathHelper.ceil(var9);

            for (var11 = -var10; var11 <= var10; ++var11)
            {
                var12 = MathHelper.abs(var11) - 0.25F;

                for (int var13 = -var10; var13 <= var10; ++var13)
                {
                    var14 = MathHelper.abs(var13) - 0.25F;

                    if ((var11 == 0 && var13 == 0 || var12 * var12 + var14 * var14 <= var9 * var9) && (var11 != -var10 && var11 != var10 && var13 != -var10 && var13 != var10 || random.nextFloat() <= 0.75F))
                    {
                        if (
                    		(worldMaterial = worldGenregion.getMaterial(x + var11, y + var8, z + var13, chunkBeingPopulated)) != null && 
                    		(worldMaterial.isAir() || sourceBlocks.contains(worldMaterial)))
                        {
                        	worldGenregion.setBlock(x + var11, y + var8, z + var13, this.material, null, chunkBeingPopulated, true);
                        }

                        if (var8 != 0 && var10 > 1)
                        {
                            if (
                        		(worldMaterial = worldGenregion.getMaterial(x + var11, y - var8, z + var13, chunkBeingPopulated)) != null && 
                        		(worldMaterial.isAir() || sourceBlocks.contains(worldMaterial)))
                            {
                            	worldGenregion.setBlock(x + var11, y - var8, z + var13, this.material, null, chunkBeingPopulated, true);
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

        int var17;
        for (int var16 = -var8; var16 <= var8; ++var16)
        {
            var10 = -var8;

            while (var10 <= var8)
            {
                var11 = y - 1;
                var17 = 50;

                if (Math.abs(var16) == 1 && Math.abs(var10) == 1)
                {
                    var17 = random.nextInt(5);
                }

                while (true)
                {
                    if (var11 > 50)
                    {
                    	if(
                			(worldMaterial = worldGenregion.getMaterial(x + var16, var11, z + var10, chunkBeingPopulated)) != null &&
        					(
    							worldMaterial.isAir() || 
    							sourceBlocks.contains(worldMaterial) || 
    							worldMaterial.equals(this.material)
							)
            			)
                    	{
                    		worldGenregion.setBlock(x + var16, var11, z + var10, this.material, null, chunkBeingPopulated, true);
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
