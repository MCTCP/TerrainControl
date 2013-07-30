package com.khorn.terraincontrol;

/**
 * Enumeration containing the Proper names and IDs of the default Minecraft
 * biomes as well as some helper methods
 */
public enum DefaultBiome
{

    /**
     * Default ID and proper name for an Ocean biome
     */
    OCEAN(0, "Ocean"),
    /**
     * Default ID and proper name for a Plains biome
     */
    PLAINS(1, "Plains"),
    /**
     * Default ID and proper name for a Desert biome
     */
    DESERT(2, "Desert"),
    /**
     * Default ID and proper name for an Extreme Hills biome
     */
    EXTREME_HILLS(3, "Extreme Hills"),
    /**
     * Default ID and proper name for a Forest biome
     */
    FOREST(4, "Forest"),
    /**
     * Default ID and proper name for a Taiga biome
     */
    TAIGA(5, "Taiga"),
    /**
     * Default ID and proper name for a Swampland biome
     */
    SWAMPLAND(6, "Swampland"),
    /**
     * Default ID and proper name for a River biome
     */
    RIVER(7, "River"),
    /**
     * Default ID and proper name for a Hell biome
     */
    HELL(8, "Hell"),
    /**
     * Default ID and proper name for a Sky biome
     */
    SKY(9, "Sky"),
    /**
     * Default ID and proper name for a Frozen Ocean biome
     */
    FROZEN_OCEAN(10, "FrozenOcean"),
    /**
     * Default ID and proper name for a Frozen River biome
     */
    FROZEN_RIVER(11, "FrozenRiver"),
    /**
     * Default ID and proper name for an Ice Plains biome
     */
    ICE_PLAINS(12, "Ice Plains"),
    /**
     * Default ID and proper name for an Ice Mountains biome
     */
    ICE_MOUNTAINS(13, "Ice Mountains"),
    /**
     * Default ID and proper name for a Mushroom Island biome
     */
    MUSHROOM_ISLAND(14, "MushroomIsland"),
    /**
     * Default ID and proper name for a Mushroom Island Shore biome
     */
    MUSHROOM_SHORE(15, "MushroomIslandShore"),
    /**
     * Default ID and proper name for a Beach biome
     */
    BEACH(16, "Beach"),
    /**
     * Default ID and proper name for a Desert Hills biome
     */
    DESERT_HILLS(17, "DesertHills"),
    /**
     * Default ID and proper name for a Forest Hills biome
     */
    FOREST_HILLS(18, "ForestHills"),
    /**
     * Default ID and proper name for a Taiga Hills biome
     */
    TAIGA_HILLS(19, "TaigaHills"),
    /**
     * Default ID and proper name for an Extreme Hills Edge biome
     */
    SMALL_MOUNTAINS(20, "Extreme Hills Edge"),
    /**
     * Default ID and proper name for a Jungle biome
     */
    JUNGLE(21, "Jungle"),
    /**
     * Default ID and proper name for a Jungle Hills biome
     */
    JUNGLE_HILLS(22, "JungleHills");
    /**
     * The ID of the specific default biome represented
     */
    public final int Id;
    /**
     * The proper name of the specific default biome represented
     */
    public final String Name;
    /**
     * A DefaultBiome lookup table with the biome ID being the array index
     */
    private static DefaultBiome[] lookupID;

    private DefaultBiome(int i, String name)
    {
        this.Id = i;
        this.Name = name;
    }

    static
    {
        // Declares and Defines the DefaultBiome lookup table
        lookupID = new DefaultBiome[DefaultBiome.values().length + 1];
        for (DefaultBiome biome : DefaultBiome.values())
        {
            lookupID[biome.Id] = biome;
        }
    }

    /**
     * Returns a DefaultBiome object with the given biome ID
     * <p/>
     * @param id the ID of the DeafultBiome that is to be returned
     * <p/>
     * @return A DefaultBiome with the given ID
     */
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

    /**
     * Returns true or false depending on if this DefaultBiome has the
     * given name
     * <p/>
     * @param name The string to test this.Name against
     * <p/>
     * @return Boolean whether or not this DefaultBiome has the given name
     */
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
