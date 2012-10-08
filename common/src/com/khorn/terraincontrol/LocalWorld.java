package com.khorn.terraincontrol;

import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.generator.resourcegens.TreeType;

import java.util.ArrayList;
import java.util.Random;

public interface LocalWorld
{
    //Biome init
    public LocalBiome AddBiome(String name, int id);

    public LocalBiome getNullBiome(String name);

    public int getMaxBiomesCount(); // With static id allocation this is not a required feature.

    public int getFreeBiomeId();
    
    public LocalBiome getBiomeById(int id);


    public int getBiomeIdByName(String name);

    public ArrayList<LocalBiome> getDefaultBiomes();

    // Biome manager
    public int[] getBiomesUnZoomed(int[] biomeArray, int x, int z, int x_size, int z_size);

    public float[] getTemperatures(int x, int z, int x_size, int z_size);

    public int[] getBiomes(int[] biomeArray, int x, int z, int x_size, int z_size);

    public int getBiome(int x, int z);


    // temperature*rain
    public double getBiomeFactorForOldBM(int index);

    // Default generators

    public void PrepareTerrainObjects(int x, int z, byte[] chunkArray, boolean dry);

    public void PlaceDungeons(Random rand, int x, int y, int z);

    public boolean PlaceTree(TreeType type, Random rand, int x, int y, int z);

    public void PlaceIce(int x, int z);

    public boolean PlaceTerrainObjects(Random rand, int chunk_x, int chunk_z);

    public void DoBlockReplace();

    public void DoBiomeReplace();


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
    
    public int getSolidHeight(int x, int z);

    public int getHighestBlockYAt(int x, int z);

    public DefaultMaterial getMaterial(int x, int y, int z);

    public void setChunksCreations(boolean createNew);

    public int getLightLevel(int x, int y, int z);

    public boolean isLoaded(int x, int y, int z);


    public WorldConfig getSettings();

    public void setSettings(WorldConfig settings);

    public String getName();

    // Terrain init
    public long getSeed();

    public int getHeight();


    public int getHeightBits();

    public void setHeightBits(int heightBits);

}