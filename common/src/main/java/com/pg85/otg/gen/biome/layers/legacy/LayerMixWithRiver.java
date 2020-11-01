package com.pg85.otg.gen.biome.layers.legacy;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalBiome;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.config.world.WorldConfig;
import com.pg85.otg.gen.biome.ArraysCache;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.network.ConfigProvider;

public class LayerMixWithRiver extends Layer
{
    private int defaultFrozenOceanId;
	
    LayerMixWithRiver(long seed, Layer childLayer, Layer riverLayer, ConfigProvider configs, LocalWorld world, int defaultOceanId, int defaultFrozenOceanId)
    {
        super(seed, defaultOceanId);
        this.defaultFrozenOceanId = defaultFrozenOceanId;
        this.child = childLayer;
        this.configs = configs;
        this.riverLayer = riverLayer;
        this.riverBiomes = new int[world.getMaxBiomesCount()];
        LocalBiome biome;
        LocalBiome riverBiome;
        
        for (int id = 0; id < this.riverBiomes.length; id++)
        {
            biome = configs.getBiomeByOTGIdOrNull(id);

            this.riverBiomes[id] = -1;
            if (biome != null && !biome.getBiomeConfig().riverBiome.isEmpty())
            {
            	riverBiome = world.getBiomeByNameOrNull(biome.getBiomeConfig().riverBiome);
            	if(riverBiome != null)
            	{
            		this.riverBiomes[id] = riverBiome.getIds().getOTGBiomeId();
            	} else {
            		OTG.log(LogMarker.WARN, "River biome \"" + biome.getBiomeConfig().riverBiome + "\" for biome " + biome.getBiomeConfig().getName() + " could not be found.");
            	}
            }
        }
    }

    private ConfigProvider configs;
    private int[] riverBiomes;
    private Layer riverLayer;

    @Override
    public void initWorldGenSeed(long worldSeed)
    {
        super.initWorldGenSeed(worldSeed);
        riverLayer.initWorldGenSeed(worldSeed + 31337);
    }

    @Override
    public int[] getInts(LocalWorld world, ArraysCache cache, int x, int z, int xSize, int zSize)
    {
        switch (cache.outputType)
        {
            case FULL:
                return this.getFull(world, cache, x, z, xSize, zSize);
            case WITHOUT_RIVERS:
                return this.getWithoutRivers(world, cache, x, z, xSize, zSize);
            case ONLY_RIVERS:
                return this.getOnlyRivers(world, cache, x, z, xSize, zSize);
            default:
                throw new UnsupportedOperationException("Unknown/invalid output type: " + cache.outputType);
        }
    }

    private int[] getFull(LocalWorld world, ArraysCache cache, int x, int z, int xSize, int zSize)
    {
        int[] childInts = this.child.getInts(world, cache, x, z, xSize, zSize);
        int[] riverInts = this.riverLayer.getInts(world, cache, x, z, xSize, zSize);
        int[] thisInts = cache.getArray(xSize * zSize);

		WorldConfig worldConfig = this.configs.getWorldConfig();
        
        int currentPiece;
        int currentRiver;
        int cachedId;
        LocalBiome biome;
        for (int zi = 0; zi < zSize; zi++)
        {
            for (int xi = 0; xi < xSize; xi++)
            {
                currentPiece = childInts[(xi + zi * xSize)];
                currentRiver = riverInts[(xi + zi * xSize)];

                if ((currentPiece & LandBit) != 0)
                {
                	if((currentPiece & BiomeBitsAreSetBit) != 0)
                	{
                		cachedId = currentPiece & BiomeBits;	
                	} else {
                		// TODO: When does this happen, is it okay for this to happen, shouldn't there be a land biome available?
                		cachedId = this.defaultOceanId;
                	}
                }
                else if (worldConfig.frozenOcean && (currentPiece & IceBit) != 0)
                {
                    cachedId = this.defaultFrozenOceanId;
                } else {
                    cachedId = this.defaultOceanId;
                }

                biome = this.configs.getBiomeByOTGIdOrNull(cachedId);
                
                if (worldConfig.riversEnabled && (currentRiver & RiverBits) != 0 && !biome.getBiomeConfig().riverBiome.isEmpty())
                {
                	currentPiece = this.riverBiomes[cachedId];
                } else {
                    currentPiece = cachedId;
                }
                thisInts[(xi + zi * xSize)] = currentPiece;
            }
        }
        return thisInts;
    }

    private int[] getWithoutRivers(LocalWorld world, ArraysCache cache, int x, int z, int xSize, int zSize)
    {
        int[] childInts = this.child.getInts(world, cache, x, z, xSize, zSize);
        int[] thisInts = cache.getArray(xSize * zSize);

		WorldConfig worldConfig = this.configs.getWorldConfig();
        
        int currentPiece;
        int cachedId;
        for (int zi = 0; zi < zSize; zi++)
        {
            for (int xi = 0; xi < xSize; xi++)
            {
                currentPiece = childInts[(xi + zi * xSize)];
                // TODO: When/why was this commented out? Might be useful?
                // currentRiver = riverInts[(j + i * x_size)];

                if ((currentPiece & LandBit) != 0)
                {
                	if((currentPiece & BiomeBitsAreSetBit) != 0)
                	{
                		cachedId = currentPiece & BiomeBits;	
                	} else {
                		// TODO: When does this happen, is it okay for this to happen, shouldn't there be a land biome available?
                		cachedId = this.defaultOceanId;
                	}
                }
                else if (worldConfig.frozenOcean && (currentPiece & IceBit) != 0)
                {
                    cachedId = this.defaultFrozenOceanId;
                } else {
                    cachedId = this.defaultOceanId;
                }

                // TODO: When/why was this commented out? Might be useful?
                /*if (this.worldConfig.riversEnabled && (currentRiver & RiverBits) != 0 && !this.worldConfig.biomeConfigs[cachedId].riverBiome.isEmpty())
                    currentPiece = this.riverBiomes[cachedId];
                else*/
                currentPiece = cachedId;

                thisInts[(xi + zi * xSize)] = currentPiece;
            }
        }
        return thisInts;
    }

    private int[] getOnlyRivers(LocalWorld world, ArraysCache cache, int x, int z, int xSize, int zSize)
    {
        int[] childInts = this.child.getInts(world, cache, x, z, xSize, zSize);
        int[] riverInts = this.riverLayer.getInts(world, cache, x, z, xSize, zSize);
        int[] thisInts = cache.getArray(xSize * zSize);
        WorldConfig worldConfig = this.configs.getWorldConfig();
       
        int currentPiece;
        int currentRiver;
        int cachedId;
        LocalBiome biome;
        for (int zi = 0; zi < zSize; zi++)
        {
            for (int xi = 0; xi < xSize; xi++)
            {
                currentPiece = childInts[(xi + zi * xSize)];
                currentRiver = riverInts[(xi + zi * xSize)];

                if ((currentPiece & LandBit) != 0)
                {
                	if((currentPiece & BiomeBitsAreSetBit) != 0)
                	{
                		cachedId = currentPiece & BiomeBits;	
                	} else {
                		// TODO: When does this happen, is it okay for this to happen, shouldn't there be a land biome available?
                		cachedId = this.defaultOceanId;
                	}
                }
                else if (worldConfig.frozenOcean && (currentPiece & IceBit) != 0)
                {
                    cachedId = this.defaultFrozenOceanId;
                } else {
                    cachedId = this.defaultOceanId;
                }

                biome = this.configs.getBiomeByOTGIdOrNull(cachedId);
                
                if (worldConfig.riversEnabled && (currentRiver & RiverBits) != 0 && !biome.getBiomeConfig().riverBiome.isEmpty())
                {
                	currentPiece = 1;
                } else {
                    currentPiece = 0;
                }
                thisInts[(xi + zi * xSize)] = currentPiece;
            }
        }
        return thisInts;
    }
}
