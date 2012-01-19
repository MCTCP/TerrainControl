package com.Khorn.TerrainControl;

import java.util.ArrayList;

public interface LocalWorld
{
    //Biome init
    public LocalBiome AddBiome(String name);
    public int getBiomesCount();
    public LocalBiome getBiomeById(int id);
    public int getBiomeIdByName(String name);

    // Biome manager
    public int[] getBiomesUnZoomed(int[] biomeArray, int x,int z, int x_size, int z_size);
    public float[] getTemperatures( int x,int z, int x_size, int z_size);
    public int[] getBiomes(int[] biomeArray, int x,int z, int x_size, int z_size);

    // temperature*rain
    public double getBiomeFactorForOldBM(int index);

    // TerrainGens

    public void PlaceTerrainObjects(int x, int z, byte[] chunkArray);

    public long getSeed();

    public ArrayList<LocalBiome> getDefaultBiomes();

    public String getName();


    public int getHeight();

    public int getHeightBits();


}
