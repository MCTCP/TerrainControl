package com.khorn.terraincontrol.biomelayers.layers;


import com.khorn.terraincontrol.DefaultBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.biomelayers.ArrayCache;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.WorldConfig;

public class LayerMixWithRiver extends Layer
{
    public LayerMixWithRiver(long paramLong, Layer paramGenLayer, Layer riverLayer, WorldConfig config, LocalWorld world)
    {
        super(paramLong);
        this.child = paramGenLayer;
        this.worldConfig = config;
        this.RiverLayer = riverLayer;
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
    private Layer RiverLayer;

    public void SetWorldSeed(long seed)
    {
        super.SetWorldSeed(seed);
        RiverLayer.SetWorldSeed(seed);
    }

    @Override
    public int[] GetBiomes(ArrayCache arrayCache, int x, int z, int x_size, int z_size)
    {

        int[] arrayOfInt1 = this.child.GetBiomes(arrayCache, x, z, x_size, z_size);
        int[] arrayOfInt2 = this.RiverLayer.GetBiomes(arrayCache, x, z, x_size, z_size);
        int[] arrayOfInt3 = arrayCache.GetArray( x_size * z_size);

        int currentPiece;
        int currentRiver;
        int cachedId;
        for (int i = 0; i < z_size; i++)
        {
            for (int j = 0; j < x_size; j++)
            {
                currentPiece = arrayOfInt1[(j + i * x_size)];
                currentRiver = arrayOfInt2[(j + i * x_size)];

                if ((currentPiece & LandBit) != 0)
                    cachedId = currentPiece & BiomeBits;
                else if (this.worldConfig.FrozenOcean && (currentPiece & IceBit) != 0)
                    cachedId = DefaultBiome.FROZEN_OCEAN.Id;
                else
                    cachedId = DefaultBiome.OCEAN.Id;

                if (this.worldConfig.RiversEnabled && (currentRiver & RiverBits) != 0 && !this.worldConfig.biomeConfigs[cachedId].RiverBiome.isEmpty())
                    currentPiece = this.RiverBiomes[cachedId];
                else
                    currentPiece = cachedId;

                arrayOfInt3[(j + i * x_size)] = currentPiece;
            }
        }

        return arrayOfInt3;
    }
}
