package com.khorn.terraincontrol.util.minecraftTypes;

import com.khorn.terraincontrol.configuration.standard.StandardBiomeFactory;
import com.khorn.terraincontrol.configuration.standard.StandardMinecraftBiomes.*;

/**
 * Enumeration containing the Proper names and IDs of the default Minecraft
 * biomes as well as some helper methods
 */
public enum DefaultBiome
{

    /**
     * Default ID and proper name for an Ocean biome
     */
    OCEAN(0, "Ocean", Ocean.class),
    /**
     * Default ID and proper name for a Plains biome
     */
    PLAINS(1, "Plains", Plains.class),
    /**
     * Default ID and proper name for a Desert biome
     */
    DESERT(2, "Desert", Desert.class),
    /**
     * Default ID and proper name for an Extreme Hills biome
     */
    EXTREME_HILLS(3, "Extreme Hills", ExtremeHills.class),
    /**
     * Default ID and proper name for a Forest biome
     */
    FOREST(4, "Forest", Forest.class),
    /**
     * Default ID and proper name for a Taiga biome
     */
    TAIGA(5, "Taiga", Taiga.class),
    /**
     * Default ID and proper name for a Swampland biome
     */
    SWAMPLAND(6, "Swampland", Swampland.class),
    /**
     * Default ID and proper name for a River biome
     */
    RIVER(7, "River", River.class),
    /**
     * Default ID and proper name for a Hell biome
     */
    HELL(8, "Hell", Hell.class),
    /**
     * Default ID and proper name for a Sky biome
     */
    SKY(9, "Sky", Sky.class),
    /**
     * Default ID and proper name for a Frozen Ocean biome
     */
    FROZEN_OCEAN(10, "FrozenOcean", FrozenOcean.class),
    /**
     * Default ID and proper name for a Frozen River biome
     */
    FROZEN_RIVER(11, "FrozenRiver", FrozenRiver.class),
    /**
     * Default ID and proper name for an Ice Plains biome
     */
    ICE_PLAINS(12, "Ice Plains", IcePlains.class),
    /**
     * Default ID and proper name for an Ice Mountains biome
     */
    ICE_MOUNTAINS(13, "Ice Mountains", IceMountains.class),
    /**
     * Default ID and proper name for a Mushroom Island biome
     */
    MUSHROOM_ISLAND(14, "MushroomIsland", MushroomIsland.class),
    /**
     * Default ID and proper name for a Mushroom Island Shore biome
     */
    MUSHROOM_SHORE(15, "MushroomIslandShore", MushroomIslandShore.class),
    /**
     * Default ID and proper name for a Beach biome
     */
    BEACH(16, "Beach", Beach.class),
    /**
     * Default ID and proper name for a Desert Hills biome
     */
    DESERT_HILLS(17, "DesertHills", DesertHills.class),
    /**
     * Default ID and proper name for a Forest Hills biome
     */
    FOREST_HILLS(18, "ForestHills", ForestHills.class),
    /**
     * Default ID and proper name for a Taiga Hills biome
     */
    TAIGA_HILLS(19, "TaigaHills", TaigaHills.class),
    /**
     * Default ID and proper name for an Extreme Hills Edge biome
     */
    SMALL_MOUNTAINS(20, "Extreme Hills Edge", ExtremeHillsEdge.class),
    /**
     * Default ID and proper name for a Jungle biome
     */
    JUNGLE(21, "Jungle", Jungle.class),
    /**
     * Default ID and proper name for a Jungle Hills biome
     */
    JUNGLE_HILLS(22, "JungleHills", JungleHills.class);
    /**
     * The ID of the specific default biome represented
     */
    public final int Id;
    /**
     * The proper name of the specific default biome represented
     */
    public final String Name;
    /**
     * Default settings of this biome. Access this using
     * {@link DefaultBiomeSettings#getDefaultSettings(com.khorn.terraincontrol.LocalBiome, int)}
     */
    private final Class<? extends StandardBiomeFactory> defaultSettingsClass;
    /**
     * A DefaultBiome lookup table with the biome ID being the array index
     */
    private static DefaultBiome[] lookupID;

    private DefaultBiome(int i, String name, Class<? extends StandardBiomeFactory> defaultSettings)
    {
        this.Id = i;
        this.Name = name;
        this.defaultSettingsClass = defaultSettings;
    }

    static
    {
        // Declares and Defines the DefaultBiome lookup table
        lookupID = new DefaultBiome[DefaultBiome.values().length + 1];
        for (DefaultBiome biome : DefaultBiome.values())
        {
            // Register by id
            lookupID[biome.Id] = biome;

            // Register the default settings
            StandardBiomeFactory.registerDefaultSettings(biome.Id, biome.defaultSettingsClass);
        }
    }

    /**
     * Returns a DefaultBiome object with the given biome ID
     * 
     * @param id the ID of the DeafultBiome that is to be returned
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
     * Returns true or false depending on if this DefaultBiome has the given
     * name
     * 
     * @param name The string to test this.Name against
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
    
    /**
     * Returns the biome id depending on if this DefaultBiome has the
     * given name
     * <p/>
     * @param name The string to test this.Name against
     * <p/>
     * @return int the Id of the biome with String name
     */
    public static Integer getId(String name)
    {
        for (DefaultBiome biome : DefaultBiome.values())
        {
            if (biome.Name.equals(name))
            {
                return biome.Id;
            }
        }
        return null;
    }
    
}
