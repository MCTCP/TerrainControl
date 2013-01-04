package com.khorn.terraincontrol;

public enum DefaultBiome
{
    OCEAN(0, "Ocean"),
    PLAINS(1, "Plains"),
    DESERT(2, "Desert"),
    EXTREME_HILLS(3, "Extreme Hills"),
    FOREST(4, "Forest"),
    TAIGA(5, "Taiga"),
    SWAMPLAND(6, "Swampland"),
    RIVER(7, "River"),
    HELL(8, "Hell"),
    SKY(9, "Sky"),
    FROZEN_OCEAN(10, "FrozenOcean"),
    FROZEN_RIVER(11, "FrozenRiver"),
    ICE_PLAINS(12, "Ice Plains"),
    ICE_MOUNTAINS(13, "Ice Mountains"),
    MUSHROOM_ISLAND(14, "MushroomIsland"),
    MUSHROOM_SHORE(15, "MushroomIslandShore"),
    BEACH(16, "Beach"),
    DESERT_HILLS(17, "DesertHills"),
    FOREST_HILLS(18, "ForestHills"),
    TAIGA_HILLS(19, "TaigaHills"),
    SMALL_MOUNTAINS(20, "Extreme Hills Edge"),
    JUNGLE(21, "Jungle"),
    JUNGLE_HILLS(22, "JungleHills");

    public final int Id;
    public final String Name;
    private static DefaultBiome[] lookupID;

    private DefaultBiome(int i, String name)
    {
        this.Id = i;
        this.Name = name;
    }

    static
    {
        lookupID = new DefaultBiome[DefaultBiome.values().length + 1];
        for (DefaultBiome biome : DefaultBiome.values())
        {
            lookupID[biome.Id] = biome;
        }
    }

    public static DefaultBiome getBiome(int id)
    {
        if (id < lookupID.length)
        {
            return lookupID[id];
        } else
        {
            return null;
        }
    }

    public static boolean Contain(String name)
    {
        for (DefaultBiome biome : DefaultBiome.values())
        {
            if (biome.Name.equals(name))
            {
                return true;
            }
        }
        return false;
    }
}
