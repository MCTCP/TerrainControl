package com.pg85.otg.generator.biome.layers;

import com.pg85.otg.common.LocalBiome;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.world.WorldConfig;
import com.pg85.otg.generator.biome.ArraysCache;
import com.pg85.otg.network.ConfigProvider;

public class LayerMixWithRiver extends Layer
{
    private int defaultOceanId;
    private int defaultFrozenOceanId;
	
    public LayerMixWithRiver(long seed, Layer childLayer, Layer riverLayer, ConfigProvider configs, LocalWorld world, int defaultOceanId, int defaultFrozenOceanId)
    {
        super(seed);
        this.defaultOceanId = defaultOceanId;
        this.defaultFrozenOceanId = defaultFrozenOceanId;
        this.child = childLayer;
        this.configs = configs;
        this.riverLayer = riverLayer;
        this.riverBiomes = new int[world.getMaxBiomesCount()];

        for (int id = 0; id < this.riverBiomes.length; id++)
        {
            LocalBiome biome = configs.getBiomeByOTGIdOrNull(id);
            
            if (biome == null || biome.getBiomeConfig().riverBiome.isEmpty())
            {
                this.riverBiomes[id] = -1;
            } else {
            	this.riverBiomes[id] = world.getBiomeByNameOrNull(biome.getBiomeConfig().riverBiome).getIds().getOTGBiomeId();
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
        for (int zi = 0; zi < zSize; zi++)
        {
            for (int xi = 0; xi < xSize; xi++)
            {
                currentPiece = childInts[(xi + zi * xSize)];
                currentRiver = riverInts[(xi + zi * xSize)];

                if ((currentPiece & LandBit) != 0)
                {
                    cachedId = currentPiece & BiomeBits;
                }
                else if (worldConfig.frozenOcean && (currentPiece & IceBit) != 0)
                {
                    cachedId = this.defaultFrozenOceanId;
                } else {
                    cachedId = this.defaultOceanId;
                }

                LocalBiome biome = this.configs.getBiomeByOTGIdOrNull(cachedId);
                
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
                    cachedId = currentPiece & BiomeBits;
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
        for (int zi = 0; zi < zSize; zi++)
        {
            for (int xi = 0; xi < xSize; xi++)
            {
                currentPiece = childInts[(xi + zi * xSize)];
                currentRiver = riverInts[(xi + zi * xSize)];

                if ((currentPiece & LandBit) != 0)
                {
                    cachedId = currentPiece & BiomeBits;
                }
                else if (worldConfig.frozenOcean && (currentPiece & IceBit) != 0)
                {
                    cachedId = this.defaultFrozenOceanId;
                } else {
                    cachedId = this.defaultOceanId;
                }

                LocalBiome biome = this.configs.getBiomeByOTGIdOrNull(cachedId);
                
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
