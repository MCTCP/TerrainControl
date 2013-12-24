package com.khorn.terraincontrol.generator.biome;


import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.generator.noise.NoiseGeneratorOldOctaves;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultBiome;

import java.util.Random;

public class OldBiomeGenerator extends BiomeGenerator
{

    private NoiseGeneratorOldOctaves temperatureGenerator1;
    private NoiseGeneratorOldOctaves wetnessGenerator;
    private NoiseGeneratorOldOctaves temperatureGenerator2;
    
    public double[] oldTemperature1;
    public double[] oldWetness;
    private double[] oldTemperature2;

    private static int[] biomeDiagram = new int[4096];
    private static boolean hasGeneratedBiomeDiagram;

    public OldBiomeGenerator(LocalWorld world, BiomeCache cache)
    {
        super(world, cache);
        this.temperatureGenerator1 = new NoiseGeneratorOldOctaves(new Random(world.getSeed() * 9871L), 4);
        this.wetnessGenerator = new NoiseGeneratorOldOctaves(new Random(world.getSeed() * 39811L), 4);
        this.temperatureGenerator2 = new NoiseGeneratorOldOctaves(new Random(world.getSeed() * 543321L), 2);

        if (!hasGeneratedBiomeDiagram)
        {
            hasGeneratedBiomeDiagram = true;
            OldBiomeGenerator.generateBiomeDiagram();
        }
    }

    @Override
    public float[] getRainfall(float[] temp_out, int x, int z, int x_size, int z_size)
    {
        if ((temp_out == null) || (temp_out.length < x_size * z_size))
        {
            temp_out = new float[x_size * z_size];
        }
        //???>>	Is this needed? I cant find a usage...
        int[] temp_biomeBases = new int[x_size * z_size];
        this.getBiomes(temp_biomeBases, x, z, x_size, z_size, false);

        for (int i = 0; i < temp_out.length; i++)
        {
            temp_out[i] = (float) this.oldWetness[i];
        }

        return temp_out;
    }

    public int[] getBiomes(int[] paramArrayOfBiomeBase, int x, int z, int x_size, int z_size, boolean useCache)
    {
        if ((paramArrayOfBiomeBase == null) || (paramArrayOfBiomeBase.length < x_size * z_size))
        {
            paramArrayOfBiomeBase = new int[x_size * z_size];
        }
        if ((useCache) && (x_size == 16) && (z_size == 16) && ((x & 0xF) == 0) && ((z & 0xF) == 0))
        {
            int[] localObject = this.cache.getCachedBiomes(x, z);
            System.arraycopy(localObject, 0, paramArrayOfBiomeBase, 0, x_size * z_size);
            return paramArrayOfBiomeBase;
        }

        WorldConfig worldConfig = world.getSettings().worldConfig;
        this.oldTemperature1 = this.temperatureGenerator1.a(this.oldTemperature1, x, z, x_size, x_size, 0.025000000372529D / worldConfig.oldBiomeSize, 0.025000000372529D / worldConfig.oldBiomeSize, 0.25D);
        this.oldWetness = this.wetnessGenerator.a(this.oldWetness, x, z, x_size, x_size, 0.0500000007450581D / worldConfig.oldBiomeSize, 0.0500000007450581D / worldConfig.oldBiomeSize, 0.3333333333333333D);
        this.oldTemperature2 = this.temperatureGenerator2.a(this.oldTemperature2, x, z, x_size, x_size, 0.25D / worldConfig.oldBiomeSize, 0.25D / worldConfig.oldBiomeSize, 0.5882352941176471D);

        int i = 0;
        for (int j = 0; j < x_size; j++)
        {
            for (int k = 0; k < z_size; k++)
            {
                double d1 = this.oldTemperature2[i] * 1.1D + 0.5D;

                double d2 = 0.01D;
                double d3 = 1.0D - d2;
                double d4 = (this.oldTemperature1[i] * 0.15D + 0.7D) * d3 + d1 * d2;
                d2 = 0.002D;
                d3 = 1.0D - d2;
                double d5 = (this.oldWetness[i] * 0.15D + 0.5D) * d3 + d1 * d2;
                d4 = 1.0D - (1.0D - d4) * (1.0D - d4);

                if (d4 < worldConfig.minTemperature)
                {
                    d4 = worldConfig.minTemperature;
                }
                if (d5 < worldConfig.minMoisture)
                {
                    d5 = worldConfig.minMoisture;
                }
                if (d4 > worldConfig.maxTemperature)
                {
                    d4 = worldConfig.maxTemperature;
                }
                if (d5 > worldConfig.maxMoisture)
                {
                    d5 = worldConfig.maxMoisture;
                }
                this.oldTemperature1[i] = d4;
                this.oldWetness[i] = d5;

                paramArrayOfBiomeBase[(i++)] = OldBiomeGenerator.getBiomeFromDiagram(d4, d5);
            }

        }

        return paramArrayOfBiomeBase;
    }

    @Override
    public int[] getBiomes(int[] biomeArray, int x, int z, int x_size, int z_size, OutputType outputType)
    {
        return getBiomes(biomeArray, x, z, x_size, z_size, false);
    }

    @Override
    public int getBiome(int x, int z)
    {
        return this.cache.getBiome(x, z);
    }

    @Override
    public void cleanupCache()
    {
        this.cache.cleanupCache();
    }

    private static int getBiomeFromDiagram(double temp, double rain)
    {
        int i = (int) (temp * 63.0D);
        int j = (int) (rain * 63.0D);
        return biomeDiagram[(i + j * 64)];
    }

    private static void generateBiomeDiagram()
    {
        for (int i = 0; i < 64; i++)
        {
            for (int j = 0; j < 64; j++)
            {
                biomeDiagram[(i + j * 64)] = generatePositionOnBiomeDiagram(i / 63.0F, j / 63.0F);
            }
        }
    }

    private static int generatePositionOnBiomeDiagram(double paramFloat1, double paramFloat2)
    {
        paramFloat2 *= paramFloat1;
        if (paramFloat1 < 0.1F)
        {
            return DefaultBiome.PLAINS.Id;
        }
        if (paramFloat2 < 0.2F)
        {
            if (paramFloat1 < 0.5F)
            {
                return DefaultBiome.PLAINS.Id;
            }
            if (paramFloat1 < 0.95F)
            {
                return DefaultBiome.PLAINS.Id;
            }
            return DefaultBiome.DESERT.Id;
        }
        if ((paramFloat2 > 0.5F) && (paramFloat1 < 0.7F))
        {
            return DefaultBiome.SWAMPLAND.Id;
        }
        if (paramFloat1 < 0.5F)
        {
            return DefaultBiome.TAIGA.Id;
        }
        if (paramFloat1 < 0.97F)
        {
            if (paramFloat2 < 0.35F)
            {
                return DefaultBiome.TAIGA.Id;
            }
            return DefaultBiome.FOREST.Id;
        }

        if (paramFloat2 < 0.45F)
        {
            return DefaultBiome.PLAINS.Id;
        }

        if (paramFloat2 < 0.9F)
        {
            return DefaultBiome.FOREST.Id;
        }

        return DefaultBiome.FOREST.Id;
    }

    @Override
    public boolean canGenerateUnZoomed()
    {
        return false;
    }

}
