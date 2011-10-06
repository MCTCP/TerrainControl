package com.Khorn.PTMBukkit.Generator;

import com.Khorn.PTMBukkit.BiomeConfig;
import com.Khorn.PTMBukkit.WorldConfig;
import net.minecraft.server.BiomeBase;
import net.minecraft.server.GenLayer;
import net.minecraft.server.IntCache;

public class GenLayerBiomePTM extends GenLayer
{
    private BiomeBase[] BiomeTable;

    public GenLayerBiomePTM(long paramLong, GenLayer paramGenLayer, WorldConfig config)
    {
        super(paramLong);
        this.a = paramGenLayer;
        this.BuildBiomeTable(config);
    }

    private void BuildBiomeTable(WorldConfig config)
    {
        int length = 0;
        for (BiomeConfig cfg : config.biomeConfigs)
            length += cfg.BiomeChance;

        if(length == 0)
        {
            System.out.println("PhoenixTerrainMod: all biomes turned off, all will be ocean");
            this.BiomeTable = new BiomeBase[1];
            this.BiomeTable[0] = BiomeBase.OCEAN;
            return;
        }

        this.BiomeTable = new BiomeBase[length];

        int i = 0;
        for (BiomeConfig cfg : config.biomeConfigs)
        {
            for (int t = 0; t < cfg.BiomeChance; t++)
            {
                BiomeTable[i] = cfg.Biome;
                i++;
            }
        }

    }


    public int[] a(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    {
        int[] arrayOfInt1 = this.a.a(paramInt1, paramInt2, paramInt3, paramInt4);

        int[] arrayOfInt2 = IntCache.a(paramInt3 * paramInt4);
        for (int i = 0; i < paramInt4; i++)
        {
            for (int j = 0; j < paramInt3; j++)
            {
                a(j + paramInt1, i + paramInt2);
                arrayOfInt2[(j + i * paramInt3)] = (arrayOfInt1[(j + i * paramInt3)] > 0 ? this.BiomeTable[a(this.BiomeTable.length)].y : 0);
            }
        }

        return arrayOfInt2;
    }
}