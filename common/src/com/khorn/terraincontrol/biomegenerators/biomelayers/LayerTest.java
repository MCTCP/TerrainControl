package com.khorn.terraincontrol.biomegenerators.biomelayers;

import com.khorn.terraincontrol.biomegenerators.ArraysCache;
import com.khorn.terraincontrol.biomegenerators.OutputType;

public class LayerTest extends Layer
{
    public LayerTest(long paramLong, Layer paramGenLayer, boolean _converted)
    {
        super(paramLong);
        this.child = paramGenLayer;
        this.converted = _converted;
    }
    private boolean converted = false;

    @Override
    public int[] GetBiomes(ArraysCache arraysCache, int x, int z, int x_size, int z_size)
    {
        int[] arrayOfInt1 = this.child.GetBiomes(arraysCache, x, z, x_size, z_size);

        if (arraysCache.outputType == OutputType.BIOME_TEST)
        {
            for (int t = 0; t < arraysCache.BiomeTestOutput.length; t++)
            {
                if (arraysCache.BiomeTestOutput[t] == null)
                {
                    arraysCache.BiomeTestOutput[t] = new int[arrayOfInt1.length];
                    arraysCache.BiomeTestSize[t] = x_size;
                    arraysCache.BiomeTestConverted[t] = converted;
                    arraysCache.BiomeTestX[t] = x;
                    arraysCache.BiomeTestZ[t] = z;
                    System.arraycopy(arrayOfInt1, 0, arraysCache.BiomeTestOutput[t], 0, arrayOfInt1.length);

                    break;
                }

            }
        }
        return arrayOfInt1;
    }
}
