package com.Khorn.TerrainControl;

import com.Khorn.TerrainControl.Generator.ResourceGens.TreeType;

import java.util.ArrayList;
import java.util.Random;

public interface LocalWorld
{
    //Biome init
    public LocalBiome AddBiome(String name);
    public int getBiomesCount();
    public LocalBiome getBiomeById(int id);
    public int getBiomeIdByName(String name);
    public ArrayList<LocalBiome> getDefaultBiomes();

    // Biome manager
    public int[] getBiomesUnZoomed(int[] biomeArray, int x,int z, int x_size, int z_size);
    public float[] getTemperatures( int x,int z, int x_size, int z_size);
    public int[] getBiomes(int[] biomeArray, int x,int z, int x_size, int z_size);
    public int getBiome(int x,int z);

    // temperature*rain
    public double getBiomeFactorForOldBM(int index);

    // Default generators

    public void PrepareTerrainObjects(int x, int z, byte[] chunkArray);
    public void PlaceDungeons(Random rand, int x, int y, int z);
    public void PlaceTree(TreeType type, Random rand, int x, int y, int z);
    public void PlacePonds(int BlockId, Random rand,int x, int y, int z);
    public void PlaceIce(int x, int z);
    public boolean PlaceTerrainObjects(Random rand, int chunk_x, int chunk_z);

    public void DoReplace();


    // Blocks
    public int getLiquidHeight(int x, int z);
    public boolean isEmpty(int x, int y, int z);
    public int GetRawBlockId(int x, int y, int z);
    public void SetRawBlockIdAndData(int x, int y, int z, int BlockId, int Data);
    public void SetRawBlockId(int x, int y, int z, int BlockId);
    public void SetBlockId(int x,int y, int z, int BlockId);
    public int getHighestBlockYAt(int x, int z);
    public DefaultMaterial getMaterial(int x,int y, int z);
    public void setChunksCreations( boolean createNew);





    public LocalWorld getSettings();
    public String getName();

    // Terrain init
    public long getSeed();
    public int getHeight();
    public int getHeightBits();


}
