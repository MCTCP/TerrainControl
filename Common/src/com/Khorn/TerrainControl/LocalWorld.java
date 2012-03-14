package com.Khorn.TerrainControl;

import com.Khorn.TerrainControl.Configuration.WorldConfig;
import com.Khorn.TerrainControl.Generator.ResourceGens.TreeType;

import java.util.ArrayList;
import java.util.Random;

public interface LocalWorld
{
    //Biome init
    public LocalBiome AddBiome(String name);

    public LocalBiome getNullBiome(String name);

    public int getBiomesCount();

    public LocalBiome getBiomeById(int id);

    public LocalBiome getBiomeByName(String name);

    public int getBiomeIdByName(String name);

    public ArrayList<LocalBiome> getDefaultBiomes();

    // Biome manager
    public int[] getBiomesUnZoomed(int[] biomeArray, int x, int z, int x_size, int z_size);

    public float[] getTemperatures(int x, int z, int x_size, int z_size);

    public int[] getBiomes(int[] biomeArray, int x, int z, int x_size, int z_size);

    public int getBiome(int x, int z);

    public LocalBiome getLocalBiome(int x, int z);

    // temperature*rain
    public double getBiomeFactorForOldBM(int index);

    // Default generators

    public void PrepareTerrainObjects(int x, int z, byte[] chunkArray, boolean dry);

    public void PlaceDungeons(Random rand, int x, int y, int z);

    public void PlaceTree(TreeType type, Random rand, int x, int y, int z);

    public void PlacePonds(int BlockId, Random rand, int x, int y, int z);

    public void PlaceIce(int x, int z);

    public boolean PlaceTerrainObjects(Random rand, int chunk_x, int chunk_z);

    public void DoReplace();


    // Blocks

    public int getTypeId(int x, int y, int z);

    public boolean isEmpty(int x, int y, int z);

    public void setBlock(final int x, final int y, final int z, final int typeId, final int data, final boolean updateLight, final boolean applyPhysics, final boolean notifyPlayers);

    public void setBlock(final int x, final int y, final int z, final int typeId, final int data);

    //public void setRawBlockIdAndData(int x, int y, int z, int BlockId, int Data);

    //public void setRawBlockId(int x, int y, int z, int BlockId);

    //public void setBlockId(int x, int y, int z, int BlockId);

    //public void setBlockIdAndData(int x, int y, int z, int BlockId, int Data);


    public int getLiquidHeight(int x, int z);

    public int getHighestBlockYAt(int x, int z);

    public DefaultMaterial getMaterial(int x, int y, int z);

    public void setChunksCreations(boolean createNew);

    public int getLightLevel(int x, int y, int z);

    public boolean isLoaded(int x, int y, int z);


    public WorldConfig getSettings();

    public String getName();

    // Terrain init
    public long getSeed();

    public int getHeight();


    public int getHeightBits();

    public void setHeightBits(int heightBits);

}