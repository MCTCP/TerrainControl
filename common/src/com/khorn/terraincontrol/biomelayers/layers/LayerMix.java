package com.khorn.terraincontrol.biomelayers.layers;


import com.khorn.terraincontrol.DefaultBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.biomelayers.ArraysCache;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.WorldConfig;

public class LayerMix extends Layer
{
    public LayerMix(long paramLong, Layer paramGenLayer, WorldConfig config, LocalWorld world)
    {
        super(paramLong);
        this.child = paramGenLayer;
        this.worldConfig = config;
        this.RiverBiomes = new int[world.getMaxBiomesCount()];

        for (int id = 0; id < this.RiverBiomes.length; id++)
        {
            BiomeConfig biomeConfig = config.biomeConfigs[id];

            if (biomeConfig == null || biomeConfig.RiverBiome.isEmpty())
                this.RiverBiomes[id] = -1;
            else
                this.RiverBiomes[id] = world.getBiomeIdByName(biomeConfig.RiverBiome);

        }
    }

    private WorldConfig worldConfig;
    private int[] RiverBiomes;

    @Override
    public int[] GetBiomes(int cacheId, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    {

        int[] arrayOfInt1 = this.child.GetBiomes(cacheId, paramInt1, paramInt2, paramInt3, paramInt4);

        int[] arrayOfInt2 = ArraysCache.GetArray(cacheId, paramInt3 * paramInt4);

        int currentPiece;
        int cachedId;
        for (int i = 0; i < paramInt4; i++)
        {
            for (int j = 0; j < paramInt3; j++)
            {
                currentPiece = arrayOfInt1[(j + i * paramInt3)];

                if ((currentPiece & LandBit) != 0)
                    cachedId = currentPiece & BiomeBits;
                else if (this.worldConfig.FrozenOcean && (currentPiece & IceBit) != 0)
                    cachedId = DefaultBiome.FROZEN_OCEAN.Id;
                else
                    cachedId = DefaultBiome.OCEAN.Id;

                if (this.worldConfig.RiversEnabled && (currentPiece & RiverBits) != 0 && !this.worldConfig.biomeConfigs[cachedId].RiverBiome.isEmpty())
                    currentPiece = this.RiverBiomes[cachedId];
                else
                    currentPiece = cachedId;

                arrayOfInt2[(j + i * paramInt3)] = currentPiece;
            }
        }

        return arrayOfInt2;
    }
}
