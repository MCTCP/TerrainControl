package com.khorn.terraincontrol.util.minecraftTypes;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.BiomeLoadInstruction;
import com.khorn.terraincontrol.configuration.standard.MinecraftBiomeTemplates.Beach;
import com.khorn.terraincontrol.configuration.standard.MinecraftBiomeTemplates.BirchForest;
import com.khorn.terraincontrol.configuration.standard.MinecraftBiomeTemplates.BirchForestHills;
import com.khorn.terraincontrol.configuration.standard.MinecraftBiomeTemplates.BirchForestHillsMountains;
import com.khorn.terraincontrol.configuration.standard.MinecraftBiomeTemplates.BirchForestMountains;
import com.khorn.terraincontrol.configuration.standard.MinecraftBiomeTemplates.ColdBeach;
import com.khorn.terraincontrol.configuration.standard.MinecraftBiomeTemplates.ColdTaiga;
import com.khorn.terraincontrol.configuration.standard.MinecraftBiomeTemplates.ColdTaigaHills;
import com.khorn.terraincontrol.configuration.standard.MinecraftBiomeTemplates.ColdTaigaMountains;
import com.khorn.terraincontrol.configuration.standard.MinecraftBiomeTemplates.DeepOcean;
import com.khorn.terraincontrol.configuration.standard.MinecraftBiomeTemplates.Desert;
import com.khorn.terraincontrol.configuration.standard.MinecraftBiomeTemplates.DesertHills;
import com.khorn.terraincontrol.configuration.standard.MinecraftBiomeTemplates.DesertMountains;
import com.khorn.terraincontrol.configuration.standard.MinecraftBiomeTemplates.ExtremeHills;
import com.khorn.terraincontrol.configuration.standard.MinecraftBiomeTemplates.ExtremeHillsEdge;
import com.khorn.terraincontrol.configuration.standard.MinecraftBiomeTemplates.ExtremeHillsMountains;
import com.khorn.terraincontrol.configuration.standard.MinecraftBiomeTemplates.ExtremeHillsPlus;
import com.khorn.terraincontrol.configuration.standard.MinecraftBiomeTemplates.ExtremeHillsPlusMountains;
import com.khorn.terraincontrol.configuration.standard.MinecraftBiomeTemplates.FlowerForest;
import com.khorn.terraincontrol.configuration.standard.MinecraftBiomeTemplates.Forest;
import com.khorn.terraincontrol.configuration.standard.MinecraftBiomeTemplates.ForestHills;
import com.khorn.terraincontrol.configuration.standard.MinecraftBiomeTemplates.FrozenOcean;
import com.khorn.terraincontrol.configuration.standard.MinecraftBiomeTemplates.FrozenRiver;
import com.khorn.terraincontrol.configuration.standard.MinecraftBiomeTemplates.Hell;
import com.khorn.terraincontrol.configuration.standard.MinecraftBiomeTemplates.IceMountains;
import com.khorn.terraincontrol.configuration.standard.MinecraftBiomeTemplates.IcePlains;
import com.khorn.terraincontrol.configuration.standard.MinecraftBiomeTemplates.IcePlainsSpikes;
import com.khorn.terraincontrol.configuration.standard.MinecraftBiomeTemplates.Jungle;
import com.khorn.terraincontrol.configuration.standard.MinecraftBiomeTemplates.JungleEdge;
import com.khorn.terraincontrol.configuration.standard.MinecraftBiomeTemplates.JungleEdgeMountains;
import com.khorn.terraincontrol.configuration.standard.MinecraftBiomeTemplates.JungleHills;
import com.khorn.terraincontrol.configuration.standard.MinecraftBiomeTemplates.JungleMountains;
import com.khorn.terraincontrol.configuration.standard.MinecraftBiomeTemplates.MegaSpruceTaiga;
import com.khorn.terraincontrol.configuration.standard.MinecraftBiomeTemplates.MegaTaiga;
import com.khorn.terraincontrol.configuration.standard.MinecraftBiomeTemplates.MegaTaigaHills;
import com.khorn.terraincontrol.configuration.standard.MinecraftBiomeTemplates.Mesa;
import com.khorn.terraincontrol.configuration.standard.MinecraftBiomeTemplates.MesaBryce;
import com.khorn.terraincontrol.configuration.standard.MinecraftBiomeTemplates.MesaPlateau;
import com.khorn.terraincontrol.configuration.standard.MinecraftBiomeTemplates.MesaPlateauForest;
import com.khorn.terraincontrol.configuration.standard.MinecraftBiomeTemplates.MesaPlateauForestMountains;
import com.khorn.terraincontrol.configuration.standard.MinecraftBiomeTemplates.MesaPlateauMountains;
import com.khorn.terraincontrol.configuration.standard.MinecraftBiomeTemplates.MinecraftBiomeTemplate;
import com.khorn.terraincontrol.configuration.standard.MinecraftBiomeTemplates.MushroomIsland;
import com.khorn.terraincontrol.configuration.standard.MinecraftBiomeTemplates.MushroomIslandShore;
import com.khorn.terraincontrol.configuration.standard.MinecraftBiomeTemplates.Ocean;
import com.khorn.terraincontrol.configuration.standard.MinecraftBiomeTemplates.Plains;
import com.khorn.terraincontrol.configuration.standard.MinecraftBiomeTemplates.River;
import com.khorn.terraincontrol.configuration.standard.MinecraftBiomeTemplates.RoofedForest;
import com.khorn.terraincontrol.configuration.standard.MinecraftBiomeTemplates.RoofedForestMountains;
import com.khorn.terraincontrol.configuration.standard.MinecraftBiomeTemplates.Savanna;
import com.khorn.terraincontrol.configuration.standard.MinecraftBiomeTemplates.SavannaMountains;
import com.khorn.terraincontrol.configuration.standard.MinecraftBiomeTemplates.SavannaPlateau;
import com.khorn.terraincontrol.configuration.standard.MinecraftBiomeTemplates.SavannaPlateauMountains;
import com.khorn.terraincontrol.configuration.standard.MinecraftBiomeTemplates.Sky;
import com.khorn.terraincontrol.configuration.standard.MinecraftBiomeTemplates.StoneBeach;
import com.khorn.terraincontrol.configuration.standard.MinecraftBiomeTemplates.SunflowerPlains;
import com.khorn.terraincontrol.configuration.standard.MinecraftBiomeTemplates.Swampland;
import com.khorn.terraincontrol.configuration.standard.MinecraftBiomeTemplates.SwamplandMountains;
import com.khorn.terraincontrol.configuration.standard.MinecraftBiomeTemplates.Taiga;
import com.khorn.terraincontrol.configuration.standard.MinecraftBiomeTemplates.TaigaHills;
import com.khorn.terraincontrol.configuration.standard.MinecraftBiomeTemplates.TaigaMountains;
import com.khorn.terraincontrol.configuration.standard.*;
import com.khorn.terraincontrol.logging.LogMarker;

/**
 * Enumeration containing the Proper names and IDs of the default Minecraft biomes as well as some
 * helper methods
 */
public enum DefaultBiome
{

    /**
     * Default ID, proper name and default settings class for an Ocean biome
     */
    OCEAN(0, "Ocean", Ocean.class),
    /**
     * Default ID, proper name and default settings class for a Plains biome
     */
    PLAINS(1, "Plains", Plains.class),
    /**
     * Default ID, proper name and default settings class for a Desert biome
     */
    DESERT(2, "Desert", Desert.class),
    /**
     * Default ID, proper name and default settings class for an Extreme Hills biome
     */
    EXTREME_HILLS(3, "Extreme Hills", ExtremeHills.class),
    /**
     * Default ID, proper name and default settings class for a Forest biome
     */
    FOREST(4, "Forest", Forest.class),
    /**
     * Default ID, proper name and default settings class for a Taiga biome
     */
    TAIGA(5, "Taiga", Taiga.class),
    /**
     * Default ID, proper name and default settings class for a Swampland biome
     */
    SWAMPLAND(6, "Swampland", Swampland.class),
    /**
     * Default ID, proper name and default settings class for a River biome
     */
    RIVER(7, "River", River.class),
    /**
     * Default ID, proper name and default settings class for a Hell biome
     */
    HELL(8, "Hell", Hell.class),
    /**
     * Default ID, proper name and default settings class for a Sky biome
     */
    SKY(9, "Sky", Sky.class),
    /**
     * Default ID, proper name and default settings class for a Frozen Ocean biome
     */
    FROZEN_OCEAN(10, "FrozenOcean", FrozenOcean.class),
    /**
     * Default ID, proper name and default settings class for a Frozen River biome
     */
    FROZEN_RIVER(11, "FrozenRiver", FrozenRiver.class),
    /**
     * Default ID, proper name and default settings class for an Ice Plains biome
     */
    ICE_PLAINS(12, "Ice Plains", IcePlains.class),
    /**
     * Default ID, proper name and default settings class for an Ice Mountains biome
     */
    ICE_MOUNTAINS(13, "Ice Mountains", IceMountains.class),
    /**
     * Default ID, proper name and default settings class for a Mushroom Island biome
     */
    MUSHROOM_ISLAND(14, "MushroomIsland", MushroomIsland.class),
    /**
     * Default ID, proper name and default settings class for a Mushroom Island Shore biome
     */
    MUSHROOM_SHORE(15, "MushroomIslandShore", MushroomIslandShore.class),
    /**
     * Default ID, proper name and default settings class for a Beach biome
     */
    BEACH(16, "Beach", Beach.class),
    /**
     * Default ID, proper name and default settings class for a Desert Hills biome
     */
    DESERT_HILLS(17, "DesertHills", DesertHills.class),
    /**
     * Default ID, proper name and default settings class for a Forest Hills biome
     */
    FOREST_HILLS(18, "ForestHills", ForestHills.class),
    /**
     * Default ID, proper name and default settings class for a Taiga Hills biome
     */
    TAIGA_HILLS(19, "TaigaHills", TaigaHills.class),
    /**
     * Default ID, proper name and default settings class for an Extreme Hills Edge biome
     */
    SMALL_MOUNTAINS(20, "Extreme Hills Edge", ExtremeHillsEdge.class),
    /**
     * Default ID, proper name and default settings class for a Jungle biome
     */
    JUNGLE(21, "Jungle", Jungle.class),
    /**
     * Default ID, proper name and default settings class for a Jungle Hills biome
     */
    JUNGLE_HILLS(22, "JungleHills", JungleHills.class),
    /**
     * Default ID, proper name and default settings class for... I think you understand it now.
     */
    JUNGLE_EDGE(23, "JungleEdge", JungleEdge.class),
    DEEP_OCEAN(24, "Deep Ocean", DeepOcean.class),
    STONE_BEACH(25, "Stone Beach", StoneBeach.class),
    COLD_BEACH(26, "Cold Beach", ColdBeach.class),
    BIRCH_FOREST(27, "Birch Forest", BirchForest.class),
    BIRCH_FOREST_HILLS(28, "Birch Forest Hills", BirchForestHills.class),
    ROOFED_FOREST(29, "Roofed Forest", RoofedForest.class),
    COLD_TAIGA(30, "Cold Taiga", ColdTaiga.class),
    COLD_TAIGA_HILLS(31, "Cold Taiga Hills", ColdTaigaHills.class),
    MEGA_TAIGA(32, "Mega Taiga", MegaTaiga.class),
    MEGA_TAIGA_HILLS(33, "Mega Taiga Hills", MegaTaigaHills.class),
    EXTREME_HILLS_PLUS(34, "Extreme Hills+", ExtremeHillsPlus.class),
    SAVANNA(35, "Savanna", Savanna.class),
    SAVANNA_PLATEAU(36, "Savanna Plateau", SavannaPlateau.class),
    MESA(37, "Mesa", Mesa.class),
    MESA_PLATEAU_FOREST(38, "Mesa Plateau F", MesaPlateauForest.class),
    MESA_PLATEAU(39, "Mesa Plateau", MesaPlateau.class),
    SUNFLOWER_PLAINS(129, "Sunflower Plains", SunflowerPlains.class),
    DESERT_MOUNTAINS(130, "Desert M", DesertMountains.class),
    EXTREME_HILLS_MOUNTAINS(131, "Extreme Hills M", ExtremeHillsMountains.class),
    FLOWER_FOREST(132, "Flower Forest", FlowerForest.class),
    TAIGA_MOUNTAINS(133, "Taiga M", TaigaMountains.class),
    SWAMPLAND_MOUNTAINS(134, "Swampland M", SwamplandMountains.class),
    ICE_PLAINS_SPIKES(140, "Ice Plains Spikes", IcePlainsSpikes.class),
    JUNGLE_MOUNTAINS(149, "Jungle M", JungleMountains.class),
    JUNGLE_EDGE_MOUNTAINS(151, "JungleEdge M", JungleEdgeMountains.class),
    BIRCH_FOREST_MOUNTAINS(155, "Birch Forest M", BirchForestMountains.class),
    BIRCH_FOREST_HILLS_MOUNTAINS(156, "Birch Forest Hills M", BirchForestHillsMountains.class),
    ROOFED_FOREST_MOUNTAINS(157, "Roofed Forest M", RoofedForestMountains.class),
    COLD_TAIGA_MOUNTAINS(158, "Cold Taiga M", ColdTaigaMountains.class),
    MEGA_SPRUCE_TAIGA(160, "Mega Spruce Taiga", MegaSpruceTaiga.class),
    // MEGA_SPRUCE_TAIGA_HILLS(161, "Mega Spruce Taiga Hills", MegaSpruceTaigaHills.class),
    // ^ This biome is bugged and doesn't generate properly in Minecraft
    // It registers itself with the wrong id - net.minecraft.server.BiomeBase.getBiome(161)
    // returns the Mega Spruce Taiga biome
    // TODO check if this is fixed in Minecraft 1.7.3
    EXTREME_HILLS_PLUS_MOUNTAINS(162, "Extreme Hills+ M", ExtremeHillsPlusMountains.class),
    SAVANNA_MOUNTAINS(163, "Savanna M", SavannaMountains.class),
    SAVANNA_PLATEAU_MOUNTAINS(164, "Savanna Plateau M", SavannaPlateauMountains.class),
    MESA_BRYCE(165, "Mesa (Bryce)", MesaBryce.class),
    MESA_PLATEAU_FOREST_MOUNTAINS(166, "Mesa Plateau F M", MesaPlateauForestMountains.class),
    MESA_PLATEAU_MOUNTAINS(167, "Mesa Plateau M", MesaPlateauMountains.class);
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
    private final Class<? extends MinecraftBiomeTemplate> defaultSettingsClass;
    /**
     * A DefaultBiome lookup table with the biome ID being the array index
     */
    private static DefaultBiome[] lookupID;

    private DefaultBiome(int i, String name, Class<? extends MinecraftBiomeTemplate> defaultSettings)
    {
        this.Id = i;
        this.Name = name;
        this.defaultSettingsClass = defaultSettings;
    }

    static
    {
        // Declares and Defines the DefaultBiome lookup table
        lookupID = new DefaultBiome[256];
        for (DefaultBiome biome : DefaultBiome.values())
        {
            // Register by id
            lookupID[biome.Id] = biome;
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

    public BiomeLoadInstruction getLoadInstructions(MojangSettings mojangSettings, int maxWorldHeight)
    {
        try
        {
            StandardBiomeTemplate template = (StandardBiomeTemplate) defaultSettingsClass.getConstructors()[0].newInstance(mojangSettings, maxWorldHeight);
            return new BiomeLoadInstruction(Name, Id, template);
        } catch (Exception e)
        {
            TerrainControl.log(LogMarker.FATAL, "Failed to create default biome");
            TerrainControl.printStackTrace(LogMarker.FATAL, e);

            // Use the standard settings for custom biomes
            return new BiomeLoadInstruction(Name, Id, new StandardBiomeTemplate(maxWorldHeight));
        }
    }

    /**
     * Returns true or false depending on if this DefaultBiome has the given name
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
