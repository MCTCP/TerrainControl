import com.khorn.terraincontrol.biomelayers.layers.Layer;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.DefaultBiome;
import com.khorn.terraincontrol.IBiomeManager;
import com.khorn.terraincontrol.LocalWorld;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BiomeManager extends rs implements IBiomeManager
{
    private Layer UnZoomedLayer;
    private Layer BiomeLayer;

    private pn Cache = new pn(this);

    private ArrayList<abn> biomesToSpawnIn = new ArrayList<abn>();


    private WorldConfig worldConfig;


    public BiomeManager(LocalWorld world)
    {
        this.biomesToSpawnIn.add(abn.a[DefaultBiome.FOREST.Id]);
        this.biomesToSpawnIn.add(abn.a[DefaultBiome.PLAINS.Id]);
        this.biomesToSpawnIn.add(abn.a[DefaultBiome.TAIGA.Id]);
        this.biomesToSpawnIn.add(abn.a[DefaultBiome.DESERT_HILLS.Id]);
        this.biomesToSpawnIn.add(abn.a[DefaultBiome.FOREST_HILLS.Id]);
        this.biomesToSpawnIn.add(abn.a[DefaultBiome.JUNGLE.Id]);
        this.biomesToSpawnIn.add(abn.a[DefaultBiome.JUNGLE_HILLS.Id]);

        this.Init(world);

    }

    public void Init(LocalWorld world)
    {
        this.worldConfig = world.getSettings();

        this.Cache = new pn(this);


        Layer[] layers = Layer.Init(world.getSeed(), world);

        this.UnZoomedLayer = layers[0];
        this.BiomeLayer = layers[1];

    }

    @SuppressWarnings("rawtypes")
    public List a()
    {
        return this.biomesToSpawnIn;
    }

    //get biome
    public abn a(int paramInt1, int paramInt2)
    {

        return this.Cache.b(paramInt1, paramInt2);
    }


    //rain
    public float[] b(float[] paramArrayOfFloat, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    {
        if ((paramArrayOfFloat == null) || (paramArrayOfFloat.length < paramInt3 * paramInt4))
        {
            paramArrayOfFloat = new float[paramInt3 * paramInt4];
        }

        int[] arrayOfInt = this.BiomeLayer.Calculate(paramInt1, paramInt2, paramInt3, paramInt4);
        for (int i = 0; i < paramInt3 * paramInt4; i++)
        {
            float f1 =  worldConfig.biomeConfigs[arrayOfInt[i]].getWetness() / 65536.0F;
            if (f1 < this.worldConfig.minMoisture)
                f1 = this.worldConfig.minMoisture;
            if (f1 > this.worldConfig.maxMoisture)
                f1 = this.worldConfig.maxMoisture;
            paramArrayOfFloat[i] = f1;
        }

        return paramArrayOfFloat;
    }

    //Temperature
    public float[] a(float[] paramArrayOfFloat, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    {
        if ((paramArrayOfFloat == null) || (paramArrayOfFloat.length < paramInt3 * paramInt4))
        {
            paramArrayOfFloat = new float[paramInt3 * paramInt4];
        }

        int[] arrayOfInt = this.BiomeLayer.Calculate(paramInt1, paramInt2, paramInt3, paramInt4);
        for (int i = 0; i < paramInt3 * paramInt4; i++)
        {
            float f1 = worldConfig.biomeConfigs[arrayOfInt[i]].getTemperature() / 65536.0F;
            if (f1 < this.worldConfig.minTemperature)
                f1 = this.worldConfig.minTemperature;
            if (f1 > this.worldConfig.maxTemperature)
                f1 = this.worldConfig.maxTemperature;
            paramArrayOfFloat[i] = f1;
        }

        return paramArrayOfFloat;
    }

    public abn[] a(abn[] paramArrayOfBiomeBase, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    {
        if ((paramArrayOfBiomeBase == null) || (paramArrayOfBiomeBase.length < paramInt3 * paramInt4))
        {
            paramArrayOfBiomeBase = new abn[paramInt3 * paramInt4];
        }

        int[] arrayOfInt = this.UnZoomedLayer.Calculate(paramInt1, paramInt2, paramInt3, paramInt4);
        for (int i = 0; i < paramInt3 * paramInt4; i++)
        {
            paramArrayOfBiomeBase[i] = abn.a[arrayOfInt[i]];
        }

        return paramArrayOfBiomeBase;
    }

    public abn[] a(abn[] paramArrayOfBiomeBase, int paramInt1, int paramInt2, int paramInt3, int paramInt4, boolean paramBoolean)
    {
        if ((paramArrayOfBiomeBase == null) || (paramArrayOfBiomeBase.length < paramInt3 * paramInt4))
        {
            paramArrayOfBiomeBase = new abn[paramInt3 * paramInt4];
        }

        if ((paramBoolean) && (paramInt3 == 16) && (paramInt4 == 16) && ((paramInt1 & 0xF) == 0) && ((paramInt2 & 0xF) == 0))
        {

            abn[] localObject = this.Cache.c(paramInt1, paramInt2);
            System.arraycopy(localObject, 0, paramArrayOfBiomeBase, 0, paramInt3 * paramInt4);
            return paramArrayOfBiomeBase;
        }
        int[] localObject = this.BiomeLayer.Calculate(paramInt1, paramInt2, paramInt3, paramInt4);
        for (int i = 0; i < paramInt3 * paramInt4; i++)
        {
            paramArrayOfBiomeBase[i] = abn.a[localObject[i]];
        }

        return paramArrayOfBiomeBase;
    }

    @SuppressWarnings("rawtypes")
    public boolean a(int paramInt1, int paramInt2, int paramInt3, List paramList)
    {
        int i = paramInt1 - paramInt3 >> 2;
        int j = paramInt2 - paramInt3 >> 2;
        int k = paramInt1 + paramInt3 >> 2;
        int m = paramInt2 + paramInt3 >> 2;

        int n = k - i + 1;
        int i1 = m - j + 1;
        int[] arrayOfInt = this.UnZoomedLayer.Calculate(i, j, n, i1);
        for (int i2 = 0; i2 < n * i1; i2++)
        {
            if (arrayOfInt[i2] >= DefaultBiome.values().length)
                return false;
            abn localBiomeBase = abn.a[arrayOfInt[i2]];
            if (!paramList.contains(localBiomeBase))
                return false;
        }

        return true;
    }

    @SuppressWarnings("rawtypes")
    public qo a(int paramInt1, int paramInt2, int paramInt3, List paramList, Random paramRandom)
    {
        int i = paramInt1 - paramInt3 >> 2;
        int j = paramInt2 - paramInt3 >> 2;
        int k = paramInt1 + paramInt3 >> 2;
        int m = paramInt2 + paramInt3 >> 2;

        int n = k - i + 1;
        int i1 = m - j + 1;
        int[] arrayOfInt = this.UnZoomedLayer.Calculate(i, j, n, i1);
        qo localChunkPosition = null;
        int i2 = 0;
        for (int i3 = 0; i3 < arrayOfInt.length; i3++)
        {
            if (arrayOfInt[i3] >= DefaultBiome.values().length)
                continue;
            int i4 = i + i3 % n << 2;
            int i5 = j + i3 / n << 2;
            abn localBiomeBase = abn.a[arrayOfInt[i3]];
            if ((!paramList.contains(localBiomeBase)) || ((localChunkPosition != null) && (paramRandom.nextInt(i2 + 1) != 0)))
                continue;
            localChunkPosition = new qo(i4, 0, i5);
            i2++;
        }

        return localChunkPosition;
    }

    public void b()
    {

        this.Cache.a();
    }

    public int[] getBiomesUnZoomedTC(int[] biomeArray, int x, int z, int x_size, int z_size)
    {
        if ((biomeArray == null) || (biomeArray.length < x_size * z_size))
        {
            biomeArray = new int[x_size * z_size];
        }

        int[] arrayOfInt = this.UnZoomedLayer.Calculate(x, z, x_size, z_size);

        System.arraycopy(arrayOfInt, 0, biomeArray, 0, x_size * z_size);

        return biomeArray;
    }
    private float[] Tbuffer = new float[256];
    
    public float[] getTemperaturesTC(int x, int z, int x_size, int z_size)
    {
        return this.a(Tbuffer, x, z, x_size, z_size);
    }

    public int[] getBiomesTC(int[] biomeArray, int x, int z, int x_size, int z_size)
    {
        if ((biomeArray == null) || (biomeArray.length < x_size * z_size))
        {
            biomeArray = new int[x_size * z_size];
        }

        if ((x_size == 16) && (z_size == 16) && ((x & 0xF) == 0) && ((z & 0xF) == 0))
        {

            abn[] localObject = this.Cache.c(x, z);
            for (int i = 0; i < x_size * z_size; i++)
                biomeArray[i] = localObject[i].M;

            return biomeArray;
        }

        int[] arrayOfInt = this.BiomeLayer.Calculate(x, z, x_size, z_size);

        System.arraycopy(arrayOfInt, 0, biomeArray, 0, x_size * z_size);

        return biomeArray;

    }

    public int getBiomeTC(int x, int z)
    {
        return this.a(x, z).M;
    }
}