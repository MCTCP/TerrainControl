package com.khorn.terraincontrol.generator.biome.layers;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.configuration.ConfigProvider;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.generator.biome.ArraysCache;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultBiome;

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
                this.riverBiomes[id] = -1;
            else
                this.riverBiomes[id] = world.getBiomeByName(biome.getBiomeConfig().riverBiome).getIds().getGenerationId();

        }
    }

    @Override
    public int[] getInts(ArraysCache cache, int x, int z, int xSize, int zSize)
    {
        switch (cache.outputType)
        {
            case FULL:
                return this.getFull(cache, x, z, xSize, zSize);
            case WITHOUT_RIVERS:
                return this.getWithoutRivers(cache, x, z, xSize, zSize);
            case ONLY_RIVERS:
                return this.getOnlyRivers(cache, x, z, xSize, zSize);
            default:
                throw new UnsupportedOperationException("Unknown/invalid output type: " + cache.outputType);
        }

    }

    private int[] getFull(ArraysCache cache, int x, int z, int xSize, int zSize)
    {
        int[] childInts = this.child.getInts(cache, x, z, xSize, zSize);
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
                    cachedId = currentPiece & BiomeBits;
                else if (worldConfig.FrozenOcean && (currentPiece & IceBit) != 0)
                    cachedId = DefaultBiome.FROZEN_OCEAN.Id;
                else
                    cachedId = DefaultBiome.OCEAN.Id;

                if (worldConfig.riversEnabled && (currentPiece & RiverBits) != 0
                        && !this.configs.getBiomeByIdOrNull(cachedId).getBiomeConfig().riverBiome.isEmpty())
                    currentPiece = this.riverBiomes[cachedId];
                else
                    currentPiece = cachedId;

                thisInts[(xi + zi * xSize)] = currentPiece;
            }
        }
        return thisInts;
    }

    private int[] getWithoutRivers(ArraysCache cache, int x, int z, int xSize, int zSize)
    {
        int[] childInts = this.child.getInts(cache, x, z, xSize, zSize);
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
                    cachedId = currentPiece & BiomeBits;
                else if (worldConfig.FrozenOcean && (currentPiece & IceBit) != 0)
                    cachedId = DefaultBiome.FROZEN_OCEAN.Id;
                else
                    cachedId = DefaultBiome.OCEAN.Id;

                currentPiece = cachedId;

                thisInts[(xi + zi * xSize)] = currentPiece;
            }
        }
        return thisInts;
    }

    private int[] getOnlyRivers(ArraysCache cache, int x, int z, int xSize, int zSize)
    {
        int[] childInts = this.child.getInts(cache, x, z, xSize, zSize);
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
                    cachedId = currentPiece & BiomeBits;
                else if (worldConfig.FrozenOcean && (currentPiece & IceBit) != 0)
                    cachedId = DefaultBiome.FROZEN_OCEAN.Id;
                else
                    cachedId = DefaultBiome.OCEAN.Id;

                if (worldConfig.riversEnabled && (currentPiece & RiverBits) != 0
                        && !this.configs.getBiomeByIdOrNull(cachedId).getBiomeConfig().riverBiome.isEmpty())
                    currentPiece = 1;
                else
                    currentPiece = 0;

                thisInts[(xi + zi * xSize)] = currentPiece;
            }
        }
        return thisInts;
    }

}
