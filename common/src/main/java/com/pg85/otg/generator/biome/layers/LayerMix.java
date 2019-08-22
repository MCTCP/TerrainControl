package com.pg85.otg.generator.biome.layers;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalBiome;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.world.WorldConfig;
import com.pg85.otg.generator.biome.ArraysCache;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.network.ConfigProvider;

public class LayerMix extends Layer
{

    private ConfigProvider configs;
    private int[] riverBiomes;
    private int defaultOceanId;
    private int defaultFrozenOceanId;

    LayerMix(long seed, Layer childLayer, ConfigProvider configs, LocalWorld world, int defaultOceanId, int defaultFrozenOceanId)
    {
        super(seed);
        this.defaultOceanId = defaultOceanId;
        this.defaultFrozenOceanId = defaultFrozenOceanId;
        this.child = childLayer;
        this.configs = configs;
        this.riverBiomes = new int[world.getMaxBiomesCount()];

        for (int id = 0; id < this.riverBiomes.length; id++)
        {
            LocalBiome biome = configs.getBiomeByOTGIdOrNull(id);

            if (biome == null || biome.getBiomeConfig().riverBiome.isEmpty())
            {
                this.riverBiomes[id] = -1;
            } else {

            	LocalBiome riverBiome = world.getBiomeByNameOrNull(biome.getBiomeConfig().riverBiome);
    			if(riverBiome == null)
    			{
    				OTG.log(LogMarker.WARN, "RiverBiome: " + biome.getBiomeConfig().riverBiome + " could not be found for biome \"" + biome.getName() + "\", substituting self.");
    				riverBiome = biome;
    			}
            	this.riverBiomes[id] = riverBiome.getIds().getOTGBiomeId();
            }
        }
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
        int[] thisInts = cache.getArray(xSize * zSize);

        int currentPiece;
        int cachedId;
        
        WorldConfig worldConfig = this.configs.getWorldConfig();
        LocalBiome biome;        
        
        for (int zi = 0; zi < zSize; zi++)
        {
            for (int xi = 0; xi < xSize; xi++)
            {
                currentPiece = childInts[(xi + zi * xSize)];

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
                
                if (worldConfig.riversEnabled && (currentPiece & RiverBits) != 0 && !biome.getBiomeConfig().riverBiome.isEmpty())
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

                currentPiece = cachedId;

                thisInts[(xi + zi * xSize)] = currentPiece;
            }
        }
        return thisInts;
    }

    private int[] getOnlyRivers(LocalWorld world, ArraysCache cache, int x, int z, int xSize, int zSize)
    {
        int[] childInts = this.child.getInts(world, cache, x, z, xSize, zSize);
        int[] thisInts = cache.getArray(xSize * zSize);
        
        WorldConfig worldConfig = this.configs.getWorldConfig();
        
        int currentPiece;
        int cachedId;
        LocalBiome biome;
        for (int zi = 0; zi < zSize; zi++)
        {
            for (int xi = 0; xi < xSize; xi++)
            {
                currentPiece = childInts[(xi + zi * xSize)];

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

                if (worldConfig.riversEnabled && (currentPiece & RiverBits) != 0 && !biome.getBiomeConfig().riverBiome.isEmpty())
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
