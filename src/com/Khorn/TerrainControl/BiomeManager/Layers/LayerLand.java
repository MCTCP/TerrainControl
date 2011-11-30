package com.Khorn.TerrainControl.BiomeManager.Layers;


import net.minecraft.server.*;

public class LayerLand extends Layer
{
    public LayerLand(long paramLong, Layer paramGenLayer, int _chance)
    {
        super(paramLong);
        this.a = paramGenLayer;
        this.chance = _chance;
    }

    public int chance = 5;

    @Override
    public int[] a(int x, int z, int x_size, int z_size)
    {

        int[] arrayOfInt1 = this.a.a(x, z, x_size, z_size);

        int[] arrayOfInt2 = IntCache.a(x_size * z_size);
        for (int i = 0; i < z_size; i++)
        {
            for (int j = 0; j < x_size; j++)
            {
                a(x + j, z + i);
                if (a(chance) == 0)
                    arrayOfInt2[(j + i * x_size)] = arrayOfInt1[(j + i * x_size)] | LandBit;
                else
                    arrayOfInt2[(j + i * x_size)] = arrayOfInt1[(j + i * x_size)];
            }
        }

        return arrayOfInt2;
    }
}
