import com.Khorn.TerrainControl.Configuration.WorldConfig;
import com.Khorn.TerrainControl.IBiomeManager;
import com.Khorn.TerrainControl.LocalWorld;
import com.Khorn.TerrainControl.Util.NoiseGeneratorOctaves2;

import java.util.List;
import java.util.Random;

public class BiomeManagerOld extends ro implements IBiomeManager
{

    private WorldConfig localWrk;

    private NoiseGeneratorOctaves2 TempGen;
    private NoiseGeneratorOctaves2 RainGen;
    private NoiseGeneratorOctaves2 TempGen2;
    public double[] old_temperature;
    public double[] old_rain;
    private double[] old_temperature2;
    private abi[] temp_biomeBases;
    private pj Cache = new pj(this);



    private static abi[] BiomeDiagram = new abi[4096];

    public BiomeManagerOld(LocalWorld world)
    {
        super();
        this.localWrk = world.getSettings();
        this.TempGen = new NoiseGeneratorOctaves2(new Random(world.getSeed() * 9871L), 4);
        this.RainGen = new NoiseGeneratorOctaves2(new Random(world.getSeed() * 39811L), 4);
        this.TempGen2 = new NoiseGeneratorOctaves2(new Random(world.getSeed() * 543321L), 2);


    }

    @Override
    public abi a(int i, int i1)
    {
        return this.Cache.b(i, i1);
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
    public abi[] a(abi[] biomeBases, int x, int z, int x_size, int z_size)
    {
        return this.a(biomeBases, x, z, x_size, z_size, false);
    }


    @Override
    public abi[] a(abi[] paramArrayOfzp, int x, int z, int x_size, int z_size, boolean useCache)
    {
        if ((paramArrayOfzp == null) || (paramArrayOfzp.length < x_size * z_size))
        {
            paramArrayOfzp = new abi[x_size * z_size];
        }
        if ((useCache) && (x_size == 16) && (z_size == 16) && ((x & 0xF) == 0) && ((z & 0xF) == 0))
        {
            abi[] localObject = this.Cache.c(x, z);
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

        abi[] biomeArray = null;

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
    public qk a(int paramInt1, int paramInt2, int paramInt3, List paramList, Random paramRandom)
    {
        int i = paramInt1 - paramInt3 >> 2;
        int j = paramInt2 - paramInt3 >> 2;
        int k = paramInt1 + paramInt3 >> 2;
        int m = paramInt2 + paramInt3 >> 2;

        int n = k - i + 1;
        int i1 = m - j + 1;
        abi[] biomeArray = null;

        biomeArray = this.a(biomeArray, i, j, n, i1);
        qk localChunkPosition = null;
        int i2 = 0;
        for (int i3 = 0; i3 < biomeArray.length; i3++)
        {
            int i4 = i + i3 % n << 2;
            int i5 = j + i3 / n << 2;
            if ((!paramList.contains(biomeArray[i2])) || ((localChunkPosition != null) && (paramRandom.nextInt(i2 + 1) != 0)))
                continue;
            localChunkPosition = new qk(i4, 0, i5);
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


    private static abi getBiomeFromDiagram(double temp, double rain)
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

    private static abi getBiomeDiagram(double paramFloat1, double paramFloat2)
    {

        paramFloat2 *= paramFloat1;
        if (paramFloat1 < 0.1F)
            return abi.c;
        if (paramFloat2 < 0.2F)
        {
            if (paramFloat1 < 0.5F)
                return abi.c;
            if (paramFloat1 < 0.95F)
            {
                return abi.c;
            }
            return abi.d;
        }
        if ((paramFloat2 > 0.5F) && (paramFloat1 < 0.7F))
            return abi.h;
        if (paramFloat1 < 0.5F)
            return abi.g;
        if (paramFloat1 < 0.97F)
        {
            if (paramFloat2 < 0.35F)
            {
                return abi.g;
            }
            return abi.f;
        }

        if (paramFloat2 < 0.45F)
            return abi.c;
        if (paramFloat2 < 0.9F)
        {
            return abi.f;
        }
        return abi.f;
    }

    public int[] getBiomesUnZoomedTC(int[] biomeArray, int x, int z, int x_size, int z_size)
    {
        if ((biomeArray == null) || (biomeArray.length < x_size * z_size))
        {
            biomeArray = new int[x_size * z_size];
        }
        if ( (x_size == 16) && (z_size == 16) && ((x & 0xF) == 0) && ((z & 0xF) == 0))
        {
            abi[] localObject = this.Cache.c(x, z);
            for(int i= 0; i< x_size*z_size;i++)
                biomeArray[i] = localObject[i].M;
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

                biomeArray[(i++)] = BiomeManagerOld.getBiomeFromDiagram(d4, d5).M;
            }

        }

        if (this.localWrk.isDeprecated)
            this.localWrk = this.localWrk.newSettings;

        return biomeArray;
    }

    private float[] Tbuffer = new float[256];
    public float[] getTemperaturesTC(int x, int z, int x_size, int z_size)
    {
        return this.a(Tbuffer,x,z,x_size,z_size);
    }

    public int[] getBiomesTC(int[] biomeArray, int x, int z, int x_size, int z_size)
    {
        return this.getBiomesUnZoomedTC(biomeArray,x,z,x_size,z_size);
    }

    public int getBiomeTC(int x, int z)
    {
        return this.a(x,z).M;
    }
}
