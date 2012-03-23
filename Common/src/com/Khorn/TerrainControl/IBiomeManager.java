package com.khorn.terraincontrol;

public interface IBiomeManager
{
    public int[] getBiomesUnZoomedTC(int[] biomeArray, int x, int z, int x_size, int z_size);

    public float[] getTemperaturesTC(int x, int z, int x_size, int z_size);

    public int[] getBiomesTC(int[] biomeArray, int x, int z, int x_size, int z_size);

    public int getBiomeTC(int x, int z);
}