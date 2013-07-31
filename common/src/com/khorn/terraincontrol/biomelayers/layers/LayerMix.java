package com.khorn.terraincontrol.biomelayers.layers;


import com.khorn.terraincontrol.DefaultBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.biomelayers.ArrayCache;
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

            if (biomeConfig == null || biomeConfig.riverBiome.isEmpty())
                this.RiverBiomes[id] = -1;
            else
                this.RiverBiomes[id] = world.getBiomeIdByName(biomeConfig.riverBiome);

        }
    }

    private WorldConfig worldConfig;
    private int[] RiverBiomes;

    @Override
    public int[] GetBiomes(ArrayCache arrayCache, int x, int z, int x_size, int z_size)
    {

        int[] arrayOfInt1 = this.child.GetBiomes(arrayCache, x, z, x_size, z_size);

        int[] arrayOfInt2 = arrayCache.GetArray( x_size * z_size);

        int currentPiece;
        int cachedId;
        for (int i = 0; i < z_size; i++)
        {
            for (int j = 0; j < x_size; j++)
            {
                currentPiece = arrayOfInt1[(j + i * x_size)];

                if ((currentPiece & LandBit) != 0)
                    cachedId = currentPiece & BiomeBits;
                else if (this.worldConfig.FrozenOcean && (currentPiece & IceBit) != 0)
                    cachedId = DefaultBiome.FROZEN_OCEAN.Id;
                else
                    cachedId = DefaultBiome.OCEAN.Id;

                if (this.worldConfig.riversEnabled && (currentPiece & RiverBits) != 0 && !this.worldConfig.biomeConfigs[cachedId].riverBiome.isEmpty())
                    currentPiece = this.RiverBiomes[cachedId];
                else
                    currentPiece = cachedId;

                arrayOfInt2[(j + i * x_size)] = currentPiece;
            }
        }

        return arrayOfInt2;
    }
}
