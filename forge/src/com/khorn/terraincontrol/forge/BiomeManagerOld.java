package com.khorn.terraincontrol.forge;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.world.ChunkPosition;
import net.minecraft.world.biome.BiomeCache;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;

import com.khorn.terraincontrol.DefaultBiome;
import com.khorn.terraincontrol.IBiomeManager;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.util.NoiseGeneratorOctaves2;

public class BiomeManagerOld extends WorldChunkManager implements IBiomeManager
{

    private WorldConfig localWrk;

    private NoiseGeneratorOctaves2 temperatureGenerator;
    private NoiseGeneratorOctaves2 wetnessGenerator;
    private NoiseGeneratorOctaves2 temperatureGenerator2;
    public double[] oldTemperature;
    public double[] oldWetness;
    private double[] oldTemperature2;
    private BiomeGenBase[] tempBiomeBases;
    private BiomeCache cache = new BiomeCache(this);

    private ArrayList<BiomeGenBase> biomesToSpawnIn = new ArrayList<BiomeGenBase>();

    private static BiomeGenBase[] biomeDiagram = new BiomeGenBase[4096];

    public BiomeManagerOld(LocalWorld world)
    {
        this.biomesToSpawnIn.add(BiomeGenBase.biomeList[DefaultBiome.FOREST.Id]);
        this.biomesToSpawnIn.add(BiomeGenBase.biomeList[DefaultBiome.PLAINS.Id]);
        this.biomesToSpawnIn.add(BiomeGenBase.biomeList[DefaultBiome.TAIGA.Id]);

        this.localWrk = world.getSettings();
        this.temperatureGenerator = new NoiseGeneratorOctaves2(new Random(world.getSeed() * 9871L), 4);
        this.wetnessGenerator = new NoiseGeneratorOctaves2(new Random(world.getSeed() * 39811L), 4);
        this.temperatureGenerator2 = new NoiseGeneratorOctaves2(new Random(world.getSeed() * 543321L), 2);

    }

    @Override
    public BiomeGenBase getBiomeGenAt(int i, int i1)
    {
        return this.cache.getBiomeGenAt(i, i1);
    }

    // Temperature
    @Override
    public float[] getTemperatures(float[] temp_out, int x, int z, int x_size, int z_size)
    {
        if ((temp_out == null) || (temp_out.length < x_size * z_size))
        {
            temp_out = new float[x_size * z_size];
        }

        this.oldTemperature = this.temperatureGenerator.a(this.oldTemperature, x, z, x_size, z_size, 0.025000000372529D / this.localWrk.oldBiomeSize, 0.025000000372529D / this.localWrk.oldBiomeSize, 0.25D);
        this.oldTemperature2 = this.temperatureGenerator2.a(this.oldTemperature2, x, z, x_size, z_size, 0.25D / this.localWrk.oldBiomeSize, 0.25D / this.localWrk.oldBiomeSize, 0.5882352941176471D);

        int i = 0;
        for (int j = 0; j < x_size; j++)
        {
            for (int k = 0; k < z_size; k++)
            {
                double d1 = this.oldTemperature2[i] * 1.1D + 0.5D;

                double d2 = 0.01D;
                double d3 = 1.0D - d2;
                double d4 = (temp_out[i] * 0.15D + 0.7D) * d3 + d1 * d2;
                d4 = 1.0D - (1.0D - d4) * (1.0D - d4);

                if (d4 < this.localWrk.minTemperature)
                    d4 = this.localWrk.minTemperature;
                if (d4 > this.localWrk.maxTemperature)
                    d4 = this.localWrk.maxTemperature;
                temp_out[i] = (float) d4;
                i++;
            }

        }
        if (this.localWrk.isDeprecated)
            this.localWrk = this.localWrk.newSettings;

        return temp_out;
    }

    // Rain
    @Override
    public float[] getRainfall(float[] temp_out, int x, int z, int x_size, int z_size)
    {
        if ((temp_out == null) || (temp_out.length < x_size * z_size))
        {
            temp_out = new float[x_size * z_size];
        }
        this.tempBiomeBases = this.getBiomeGenAt(this.tempBiomeBases, x, z, x_size, z_size, false);

        for (int i = 0; i < temp_out.length; i++)
            temp_out[i] = (float) this.oldWetness[i];

        return temp_out;

    }

    @Override
    public BiomeGenBase[] loadBlockGeneratorData(BiomeGenBase[] biomeBases, int x, int z, int x_size, int z_size)
    {
        return this.getBiomeGenAt(biomeBases, x, z, x_size, z_size, false);
    }

    @Override
    public BiomeGenBase[] getBiomeGenAt(BiomeGenBase[] paramArrayOfzp, int x, int z, int x_size, int z_size, boolean useCache)
    {
        if ((paramArrayOfzp == null) || (paramArrayOfzp.length < x_size * z_size))
        {
            paramArrayOfzp = new BiomeGenBase[x_size * z_size];
        }
        if ((useCache) && (x_size == 16) && (z_size == 16) && ((x & 0xF) == 0) && ((z & 0xF) == 0))
        {
            BiomeGenBase[] localObject = this.cache.getCachedBiomes(x, z);
            System.arraycopy(localObject, 0, paramArrayOfzp, 0, x_size * z_size);
            return paramArrayOfzp;
        }

        this.oldTemperature = this.temperatureGenerator.a(this.oldTemperature, x, z, x_size, x_size, 0.025000000372529D / this.localWrk.oldBiomeSize, 0.025000000372529D / this.localWrk.oldBiomeSize, 0.25D);
        this.oldWetness = this.wetnessGenerator.a(this.oldWetness, x, z, x_size, x_size, 0.0500000007450581D / this.localWrk.oldBiomeSize, 0.0500000007450581D / this.localWrk.oldBiomeSize, 0.3333333333333333D);
        this.oldTemperature2 = this.temperatureGenerator2.a(this.oldTemperature2, x, z, x_size, x_size, 0.25D / this.localWrk.oldBiomeSize, 0.25D / this.localWrk.oldBiomeSize, 0.5882352941176471D);

        int i = 0;
        for (int j = 0; j < x_size; j++)
        {
            for (int k = 0; k < z_size; k++)
            {
                double d1 = this.oldTemperature2[i] * 1.1D + 0.5D;

                double d2 = 0.01D;
                double d3 = 1.0D - d2;
                double d4 = (this.oldTemperature[i] * 0.15D + 0.7D) * d3 + d1 * d2;
                d2 = 0.002D;
                d3 = 1.0D - d2;
                double d5 = (this.oldWetness[i] * 0.15D + 0.5D) * d3 + d1 * d2;
                d4 = 1.0D - (1.0D - d4) * (1.0D - d4);

                if (d4 < this.localWrk.minTemperature)
                    d4 = this.localWrk.minTemperature;
                if (d5 < this.localWrk.minMoisture)
                    d5 = this.localWrk.minMoisture;
                if (d4 > this.localWrk.maxTemperature)
                    d4 = this.localWrk.maxTemperature;
                if (d5 > this.localWrk.maxMoisture)
                {
                    d5 = this.localWrk.maxMoisture;
                }
                this.oldTemperature[i] = d4;
                this.oldWetness[i] = d5;

                paramArrayOfzp[(i++)] = BiomeManagerOld.getBiomeFromDiagram(d4, d5);
            }

        }

        if (this.localWrk.isDeprecated)
            this.localWrk = this.localWrk.newSettings;

        return paramArrayOfzp;
    }

    // Check biomes list
    @Override
    @SuppressWarnings("rawtypes")
    public boolean areBiomesViable(int paramInt1, int paramInt2, int paramInt3, List paramList)
    {
        int i = paramInt1 - paramInt3 >> 2;
        int j = paramInt2 - paramInt3 >> 2;
        int k = paramInt1 + paramInt3 >> 2;
        int m = paramInt2 + paramInt3 >> 2;

        int n = k - i + 1;
        int i1 = m - j + 1;

        BiomeGenBase[] biomeArray = null;

        biomeArray = this.getBiomesForGeneration(biomeArray, i, j, n, i1);
        for (int i2 = 0; i2 < n * i1; i2++)
        {
            if (!paramList.contains(biomeArray[i2]))
                return false;
        }

        return true;
    }

    // StrongholdPosition
    @Override
    @SuppressWarnings("rawtypes")
    public ChunkPosition findBiomePosition(int paramInt1, int paramInt2, int paramInt3, List paramList, Random paramRandom)
    {
        int i = paramInt1 - paramInt3 >> 2;
        int j = paramInt2 - paramInt3 >> 2;
        int k = paramInt1 + paramInt3 >> 2;
        int m = paramInt2 + paramInt3 >> 2;

        int n = k - i + 1;
        int i1 = m - j + 1;
        BiomeGenBase[] biomeArray = null;

        biomeArray = this.getBiomesForGeneration(biomeArray, i, j, n, i1);
        ChunkPosition localChunkPosition = null;
        int i2 = 0;
        for (int i3 = 0; i3 < biomeArray.length; i3++)
        {
            int i4 = i + i3 % n << 2;
            int i5 = j + i3 / n << 2;
            if ((!paramList.contains(biomeArray[i2])) || ((localChunkPosition != null) && (paramRandom.nextInt(i2 + 1) != 0)))
                continue;
            localChunkPosition = new ChunkPosition(i4, 0, i5);
            i2++;
        }

        return localChunkPosition;
    }

    // Not use IniCache
    @Override
    public void cleanupCache()
    {
        this.cache.cleanupCache();
    }

    private static BiomeGenBase getBiomeFromDiagram(double temp, double rain)
    {
        int i = (int) (temp * 63.0D);
        int j = (int) (rain * 63.0D);
        return biomeDiagram[(i + j * 64)];
    }

    static
    {
        for (int i = 0; i < 64; i++)
        {
            for (int j = 0; j < 64; j++)
            {
                biomeDiagram[(i + j * 64)] = getBiomeDiagram(i / 63.0F, j / 63.0F);
            }
        }
    }

    private static BiomeGenBase getBiomeDiagram(double paramFloat1, double paramFloat2)
    {

        paramFloat2 *= paramFloat1;
        if (paramFloat1 < 0.1F)
            return BiomeGenBase.plains;
        if (paramFloat2 < 0.2F)
        {
            if (paramFloat1 < 0.5F)
                return BiomeGenBase.plains;
            if (paramFloat1 < 0.95F)
            {
                return BiomeGenBase.plains;
            }
            return BiomeGenBase.desert;
        }
        if ((paramFloat2 > 0.5F) && (paramFloat1 < 0.7F))
            return BiomeGenBase.swampland;
        if (paramFloat1 < 0.5F)
            return BiomeGenBase.taiga;
        if (paramFloat1 < 0.97F)
        {
            if (paramFloat2 < 0.35F)
            {
                return BiomeGenBase.taiga;
            }
            return BiomeGenBase.forest;
        }

        if (paramFloat2 < 0.45F)
            return BiomeGenBase.forest;
        if (paramFloat2 < 0.9F)
        {
            return BiomeGenBase.forest;
        }
        return BiomeGenBase.forest;
    }

    public int[] getBiomesUnZoomedTC(int[] biomeArray, int x, int z, int x_size, int z_size)
    {
        if ((biomeArray == null) || (biomeArray.length < x_size * z_size))
        {
            biomeArray = new int[x_size * z_size];
        }
        if ((x_size == 16) && (z_size == 16) && ((x & 0xF) == 0) && ((z & 0xF) == 0))
        {
            BiomeGenBase[] localObject = this.cache.getCachedBiomes(x, z);
            for (int i = 0; i < x_size * z_size; i++)
                biomeArray[i] = localObject[i].biomeID;
            return biomeArray;
        }

        this.oldTemperature = this.temperatureGenerator.a(this.oldTemperature, x, z, x_size, x_size, 0.025000000372529D / this.localWrk.oldBiomeSize, 0.025000000372529D / this.localWrk.oldBiomeSize, 0.25D);
        this.oldWetness = this.wetnessGenerator.a(this.oldWetness, x, z, x_size, x_size, 0.0500000007450581D / this.localWrk.oldBiomeSize, 0.0500000007450581D / this.localWrk.oldBiomeSize, 0.3333333333333333D);
        this.oldTemperature2 = this.temperatureGenerator2.a(this.oldTemperature2, x, z, x_size, x_size, 0.25D / this.localWrk.oldBiomeSize, 0.25D / this.localWrk.oldBiomeSize, 0.5882352941176471D);

        int i = 0;
        for (int j = 0; j < x_size; j++)
        {
            for (int k = 0; k < z_size; k++)
            {
                double d1 = this.oldTemperature2[i] * 1.1D + 0.5D;

                double d2 = 0.01D;
                double d3 = 1.0D - d2;
                double d4 = (this.oldTemperature[i] * 0.15D + 0.7D) * d3 + d1 * d2;
                d2 = 0.002D;
                d3 = 1.0D - d2;
                double d5 = (this.oldWetness[i] * 0.15D + 0.5D) * d3 + d1 * d2;
                d4 = 1.0D - (1.0D - d4) * (1.0D - d4);

                if (d4 < this.localWrk.minTemperature)
                    d4 = this.localWrk.minTemperature;
                if (d5 < this.localWrk.minMoisture)
                    d5 = this.localWrk.minMoisture;
                if (d4 > this.localWrk.maxTemperature)
                    d4 = this.localWrk.maxTemperature;
                if (d5 > this.localWrk.maxMoisture)
                {
                    d5 = this.localWrk.maxMoisture;
                }
                this.oldTemperature[i] = d4;
                this.oldWetness[i] = d5;

                biomeArray[(i++)] = BiomeManagerOld.getBiomeFromDiagram(d4, d5).biomeID;
            }

        }

        if (this.localWrk.isDeprecated)
            this.localWrk = this.localWrk.newSettings;

        return biomeArray;
    }

    private float[] Tbuffer = new float[256];

    public float[] getTemperaturesTC(int x, int z, int x_size, int z_size)
    {
        return this.getTemperatures(Tbuffer, x, z, x_size, z_size);
    }

    public int[] getBiomesTC(int[] biomeArray, int x, int z, int x_size, int z_size)
    {
        return this.getBiomesUnZoomedTC(biomeArray, x, z, x_size, z_size);
    }

    public int getBiomeTC(int x, int z)
    {
        return this.getBiomeGenAt(x, z).biomeID;
    }
}
