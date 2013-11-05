package com.khorn.terraincontrol.util.minecraftTypes;

/**
 * Terrain Control replaces some of the standard Minecraft structure generator.
 * As of Minecraft 1.6.4, structures are saved to disk. Each structure needs
 * an unique id. To make sure that vanilla worlds aren't affected, TC uses
 * unique structure names, which can be found in this class.
 * <p>
 * All names of replaced structures are simply "TC" + vanilla name.
 * 
 */
public class StructureNames
{
    public static final String MINESHAFT = "Mineshaft";
    public static final String NETHER_FORTRESS = "Fortress";
    public static final String VILLAGE = "TCVillage";
    public static final String STRONGHOLD = "Stronghold";
    public static final String RARE_BUILDING = "TCTemple";

    private StructureNames()
    {
    }
}
