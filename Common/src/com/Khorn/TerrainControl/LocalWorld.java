package com.Khorn.TerrainControl;

import java.util.ArrayList;

public interface LocalWorld
{

    public LocalBiome AddBiome(String name);
    public int getBiomesCount();
    public LocalBiome getBiomeById(int id);
    public int getBiomeIdByName(String name);

    public ArrayList<LocalBiome> getDefaultBiomes();

    public String getName();



}
