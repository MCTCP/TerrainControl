package com.pg85.otg.generator.biome.layers;

import com.pg85.otg.LocalBiome;
import com.pg85.otg.LocalWorld;
import com.pg85.otg.OTG;
import com.pg85.otg.configuration.ConfigProvider;
import com.pg85.otg.configuration.WorldConfig;
import com.pg85.otg.generator.biome.ArraysCache;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.minecraftTypes.DefaultBiome;

public class LayerMix extends Layer
{

    private ConfigProvider configs;
    private int[] riverBiomes;

    public LayerMix(long seed, Layer childLayer, ConfigProvider configs, LocalWorld world)
    {
        super(seed);
        this.child = childLayer;
        this.configs = configs;
        this.riverBiomes = new int[world.getMaxBiomesCount()];

        for (int id = 0; id < this.riverBiomes.length; id++)
        {
            LocalBiome biome = configs.getBiomeByIdOrNull(id);

            if (biome == null || biome.getBiomeConfig().riverBiome.isEmpty())
            {
                this.riverBiomes[id] = -1;
            } else {

            	LocalBiome riverBiome = world.getBiomeByNameOrNull(biome.getBiomeConfig().riverBiome);
    			if(riverBiome == null)
    			{
    				OTG.log(LogMarker.ERROR, "RiverBiome: " + biome.getBiomeConfig().riverBiome + " could not be found for biome \"" + biome.getName() + "\"");
    			}
            	this.riverBiomes[id] = riverBiome.getIds().getOTGBiomeId();

                //this.riverBiomes[id] = world.getBiomeByName(biome.getBiomeConfig().riverBiome).getIds().getGenerationId();
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
        WorldConfig worldConfig = this.configs.getWorldConfig();

        int currentPiece;
        int cachedId;
        
        int defaultOceanId = world.getBiomeByNameOrNull(worldConfig.defaultOceanBiome).getIds().getOTGBiomeId();        
        int defaultFrozenOceanId = world.getBiomeByNameOrNull(worldConfig.defaultFrozenOceanBiome).getIds().getOTGBiomeId();
        
        for (int zi = 0; zi < zSize; zi++)
        {
            for (int xi = 0; xi < xSize; xi++)
            {
                currentPiece = childInts[(xi + zi * xSize)];

                if ((currentPiece & LandBit) != 0)
                {
                    cachedId = currentPiece & BiomeBits;
                    
                    if(cachedId == 0) // TODO: When does this happen, is it okay for this to happen, shouldn't there be a land biome available?
                    {
                    	cachedId = defaultOceanId;
                    }                    
                }
                else if (worldConfig.FrozenOcean && (currentPiece & IceBit) != 0)
                {
                    cachedId = defaultFrozenOceanId;
                } else {
                    cachedId = defaultOceanId;
                }
                
                LocalBiome biome = this.configs.getBiomeByIdOrNull(cachedId);
                
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

        int defaultOceanId = world.getBiomeByNameOrNull(worldConfig.defaultOceanBiome).getIds().getOTGBiomeId();        
        int defaultFrozenOceanId = world.getBiomeByNameOrNull(worldConfig.defaultFrozenOceanBiome).getIds().getOTGBiomeId();
        
        int currentPiece;
        int cachedId;
        for (int zi = 0; zi < zSize; zi++)
        {
            for (int xi = 0; xi < xSize; xi++)
            {
                currentPiece = childInts[(xi + zi * xSize)];

                if ((currentPiece & LandBit) != 0)
                {
                    cachedId = currentPiece & BiomeBits;
                    
                    if(cachedId == 0) // TODO: When does this happen, is it okay for this to happen, shouldn't there be a land biome available?
                    {
                    	cachedId = defaultOceanId;
                    }                      
                }
                else if (worldConfig.FrozenOcean && (currentPiece & IceBit) != 0)
                {
                    cachedId = defaultFrozenOceanId;
                } else {
                    cachedId = defaultOceanId;
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

        int defaultOceanId = world.getBiomeByNameOrNull(worldConfig.defaultOceanBiome).getIds().getOTGBiomeId();        
        int defaultFrozenOceanId = world.getBiomeByNameOrNull(worldConfig.defaultFrozenOceanBiome).getIds().getOTGBiomeId();
        
        int currentPiece;
        int cachedId;
        for (int zi = 0; zi < zSize; zi++)
        {
            for (int xi = 0; xi < xSize; xi++)
            {
                currentPiece = childInts[(xi + zi * xSize)];

                if ((currentPiece & LandBit) != 0)
                {
                    cachedId = currentPiece & BiomeBits;
                    
                    if(cachedId == 0) // TODO: When does this happen, is it okay for this to happen, shouldn't there be a land biome available?
                    {
                    	cachedId = defaultOceanId;
                    }                      
                }
                else if (worldConfig.FrozenOcean && (currentPiece & IceBit) != 0)
                {
                    cachedId = defaultFrozenOceanId;
                } else {
                    cachedId = defaultOceanId;
                }

                LocalBiome biome = this.configs.getBiomeByIdOrNull(cachedId);

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
