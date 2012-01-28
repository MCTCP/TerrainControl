import com.Khorn.TerrainControl.Configuration.WorldConfig;
import com.Khorn.TerrainControl.IBiomeManager;
import com.Khorn.TerrainControl.LocalWorld;
import com.Khorn.TerrainControl.Util.NoiseGeneratorOctaves2;

import java.util.List;
import java.util.Random;

public class BiomeManagerOld extends qu implements IBiomeManager
{

    private WorldConfig localWrk;

    private NoiseGeneratorOctaves2 TempGen;
    private NoiseGeneratorOctaves2 RainGen;
    private NoiseGeneratorOctaves2 TempGen2;
    public double[] old_temperature;
    public double[] old_rain;
    private double[] old_temperature2;
    private zp[] temp_biomeBases;
    private op Cache = new op(this);



    private static zp[] BiomeDiagram = new zp[4096];

    public BiomeManagerOld(LocalWorld world)
    {
        super();
        this.localWrk = world.getSettings();
        this.TempGen = new NoiseGeneratorOctaves2(new Random(world.getSeed() * 9871L), 4);
        this.RainGen = new NoiseGeneratorOctaves2(new Random(world.getSeed() * 39811L), 4);
        this.TempGen2 = new NoiseGeneratorOctaves2(new Random(world.getSeed() * 543321L), 2);


    }

    @Override
    public zp a(int i, int i1)
    {
        return this.Cache.b(i, i1);
    }

    public float b(int paramInt1, int paramInt2)
    {
        return this.Cache.d(paramInt1, paramInt2);
    }

    @Override
    public float a(int i, int i1, int i2)
    {
        return a(this.Cache.c(i, i1), i2);
    }

    @Override
    public float[] b(int i, int i1, int i2, int i3)
    {
        this.b = a(this.b, i, i1, i2, i3);
        return this.b;
    }

    // Temperature
    @Override
    public float[] a(float[] temp_out, int x, int z, int x_size, int z_size)
    {
        if ((temp_out == null) || (temp_out.length < x_size * z_size))
        {
            temp_out = new float[x_size * z_size];
        }

        this.old_temperature = this.TempGen.a(this.old_temperature, x, z, x_size, z_size, 0.025000000372529D / this.localWrk.oldBiomeSize, 0.025000000372529D / this.localWrk.oldBiomeSize, 0.25D);
        this.old_temperature2 = this.TempGen2.a(this.old_temperature2, x, z, x_size, z_size, 0.25D / this.localWrk.oldBiomeSize, 0.25D / this.localWrk.oldBiomeSize, 0.5882352941176471D);

        int i = 0;
        for (int j = 0; j < x_size; j++)
        {
            for (int k = 0; k < z_size; k++)
            {
                double d1 = this.old_temperature2[i] * 1.1D + 0.5D;

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
    public float[] b(float[] temp_out, int x, int z, int x_size, int z_size)
    {
        if ((temp_out == null) || (temp_out.length < x_size * z_size))
        {
            temp_out = new float[x_size * z_size];
        }
        this.temp_biomeBases = this.a(this.temp_biomeBases, x, z, x_size, z_size, false);

        for (int i = 0; i < temp_out.length; i++)
            temp_out[i] = (float) this.old_rain[i];

        return temp_out;

    }

    @Override
    public zp[] a(zp[] biomeBases, int x, int z, int x_size, int z_size)
    {
        return this.a(biomeBases, x, z, x_size, z_size, false);
    }

    public zp[] a(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    {
        if ((paramInt3 == 16) && (paramInt4 == 16) && ((paramInt1 & 0xF) == 0) && ((paramInt2 & 0xF) == 0))
        {
            return this.Cache.e(paramInt1, paramInt2);
        }

        this.a = b(this.a, paramInt1, paramInt2, paramInt3, paramInt4);
        return this.a;
    }

    @Override
    public zp[] a(zp[] paramArrayOfzp, int x, int z, int x_size, int z_size, boolean useCache)
    {
        if ((paramArrayOfzp == null) || (paramArrayOfzp.length < x_size * z_size))
        {
            paramArrayOfzp = new zp[x_size * z_size];
        }
        if ((useCache) && (x_size == 16) && (z_size == 16) && ((x & 0xF) == 0) && ((z & 0xF) == 0))
        {
            zp[] localObject = this.Cache.e(x, z);
            System.arraycopy(localObject, 0, paramArrayOfzp, 0, x_size * z_size);
            return paramArrayOfzp;
        }


        this.old_temperature = this.TempGen.a(this.old_temperature, x, z, x_size, x_size, 0.025000000372529D / this.localWrk.oldBiomeSize, 0.025000000372529D / this.localWrk.oldBiomeSize, 0.25D);
        this.old_rain = this.RainGen.a(this.old_rain, x, z, x_size, x_size, 0.0500000007450581D / this.localWrk.oldBiomeSize, 0.0500000007450581D / this.localWrk.oldBiomeSize, 0.3333333333333333D);
        this.old_temperature2 = this.TempGen2.a(this.old_temperature2, x, z, x_size, x_size, 0.25D / this.localWrk.oldBiomeSize, 0.25D / this.localWrk.oldBiomeSize, 0.5882352941176471D);

        int i = 0;
        for (int j = 0; j < x_size; j++)
        {
            for (int k = 0; k < z_size; k++)
            {
                double d1 = this.old_temperature2[i] * 1.1D + 0.5D;

                double d2 = 0.01D;
                double d3 = 1.0D - d2;
                double d4 = (this.old_temperature[i] * 0.15D + 0.7D) * d3 + d1 * d2;
                d2 = 0.002D;
                d3 = 1.0D - d2;
                double d5 = (this.old_rain[i] * 0.15D + 0.5D) * d3 + d1 * d2;
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
                this.old_temperature[i] = d4;
                this.old_rain[i] = d5;

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
    public boolean a(int paramInt1, int paramInt2, int paramInt3, List paramList)
    {
        int i = paramInt1 - paramInt3 >> 2;
        int j = paramInt2 - paramInt3 >> 2;
        int k = paramInt1 + paramInt3 >> 2;
        int m = paramInt2 + paramInt3 >> 2;

        int n = k - i + 1;
        int i1 = m - j + 1;

        zp[] biomeArray = null;

        biomeArray = this.a(biomeArray, i, j, n, i1);
        for (int i2 = 0; i2 < n * i1; i2++)
        {
            if (!paramList.contains(biomeArray[i2]))
                return false;
        }

        return true;
    }

    //StrongholdPosition
    @Override
    @SuppressWarnings("rawtypes")
    public pr a(int paramInt1, int paramInt2, int paramInt3, List paramList, Random paramRandom)
    {
        int i = paramInt1 - paramInt3 >> 2;
        int j = paramInt2 - paramInt3 >> 2;
        int k = paramInt1 + paramInt3 >> 2;
        int m = paramInt2 + paramInt3 >> 2;

        int n = k - i + 1;
        int i1 = m - j + 1;
        zp[] biomeArray = null;

        biomeArray = this.a(biomeArray, i, j, n, i1);
        pr localChunkPosition = null;
        int i2 = 0;
        for (int i3 = 0; i3 < biomeArray.length; i3++)
        {
            int i4 = i + i3 % n << 2;
            int i5 = j + i3 / n << 2;
            if ((!paramList.contains(biomeArray[i2])) || ((localChunkPosition != null) && (paramRandom.nextInt(i2 + 1) != 0)))
                continue;
            localChunkPosition = new pr(i4, 0, i5);
            i2++;
        }

        return localChunkPosition;
    }

    //Not use IniCache
    @Override
    public void b()
    {
        this.Cache.a();
    }


    private static zp getBiomeFromDiagram(double temp, double rain)
    {
        int i = (int) (temp * 63.0D);
        int j = (int) (rain * 63.0D);
        return BiomeDiagram[(i + j * 64)];
    }

    static
    {
        for (int i = 0; i < 64; i++)
        {
            for (int j = 0; j < 64; j++)
            {
                BiomeDiagram[(i + j * 64)] = getBiomeDiagram(i / 63.0F, j / 63.0F);
            }
        }
    }

    private static zp getBiomeDiagram(double paramFloat1, double paramFloat2)
    {

        paramFloat2 *= paramFloat1;
        if (paramFloat1 < 0.1F)
            return zp.c;
        if (paramFloat2 < 0.2F)
        {
            if (paramFloat1 < 0.5F)
                return zp.c;
            if (paramFloat1 < 0.95F)
            {
                return zp.c;
            }
            return zp.d;
        }
        if ((paramFloat2 > 0.5F) && (paramFloat1 < 0.7F))
            return zp.h;
        if (paramFloat1 < 0.5F)
            return zp.g;
        if (paramFloat1 < 0.97F)
        {
            if (paramFloat2 < 0.35F)
            {
                return zp.g;
            }
            return zp.f;
        }

        if (paramFloat2 < 0.45F)
            return zp.c;
        if (paramFloat2 < 0.9F)
        {
            return zp.f;
        }
        return zp.f;
    }

    public int[] getBiomesUnZoomedTC(int[] biomeArray, int x, int z, int x_size, int z_size)
    {
        if ((biomeArray == null) || (biomeArray.length < x_size * z_size))
        {
            biomeArray = new int[x_size * z_size];
        }
        if ( (x_size == 16) && (z_size == 16) && ((x & 0xF) == 0) && ((z & 0xF) == 0))
        {
            zp[] localObject = this.Cache.e(x, z);
            for(int i= 0; i< x_size*z_size;i++)
                biomeArray[i] = localObject[i].K;
            return biomeArray;
        }


        this.old_temperature = this.TempGen.a(this.old_temperature, x, z, x_size, x_size, 0.025000000372529D / this.localWrk.oldBiomeSize, 0.025000000372529D / this.localWrk.oldBiomeSize, 0.25D);
        this.old_rain = this.RainGen.a(this.old_rain, x, z, x_size, x_size, 0.0500000007450581D / this.localWrk.oldBiomeSize, 0.0500000007450581D / this.localWrk.oldBiomeSize, 0.3333333333333333D);
        this.old_temperature2 = this.TempGen2.a(this.old_temperature2, x, z, x_size, x_size, 0.25D / this.localWrk.oldBiomeSize, 0.25D / this.localWrk.oldBiomeSize, 0.5882352941176471D);

        int i = 0;
        for (int j = 0; j < x_size; j++)
        {
            for (int k = 0; k < z_size; k++)
            {
                double d1 = this.old_temperature2[i] * 1.1D + 0.5D;

                double d2 = 0.01D;
                double d3 = 1.0D - d2;
                double d4 = (this.old_temperature[i] * 0.15D + 0.7D) * d3 + d1 * d2;
                d2 = 0.002D;
                d3 = 1.0D - d2;
                double d5 = (this.old_rain[i] * 0.15D + 0.5D) * d3 + d1 * d2;
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
                this.old_temperature[i] = d4;
                this.old_rain[i] = d5;

                biomeArray[(i++)] = BiomeManagerOld.getBiomeFromDiagram(d4, d5).K;
            }

        }

        if (this.localWrk.isDeprecated)
            this.localWrk = this.localWrk.newSettings;

        return biomeArray;
    }

    public float[] getTemperaturesTC(int x, int z, int x_size, int z_size)
    {
        return this.a(this.b,x,z,x_size,z_size);
    }

    public int[] getBiomesTC(int[] biomeArray, int x, int z, int x_size, int z_size)
    {
        return this.getBiomesUnZoomedTC(biomeArray,x,z,x_size,z_size);
    }

    public int getBiomeTC(int x, int z)
    {
        return this.a(x,z).K;
    }
}
