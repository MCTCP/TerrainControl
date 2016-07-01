package com.khorn.terraincontrol.util.minecraftTypes;

import com.khorn.terraincontrol.configuration.standard.PluginStandardValues;
import com.khorn.terraincontrol.exception.InvalidConfigException;

import java.util.HashMap;
import java.util.Map;

/**
 * Structure parts: parts of a vanilla structure that Mojang has saved to a NBT
 * file. The NBT files can be found in the Minecraft JAR file (for example
 * .minecraft/versions/1.10/1.10.jar) in the assets/minecraft/structures folder.
 *
 * <p>Only vanilla NBT structure parts a listed here, no custom/modded ones.
 * Older Minecraft structures use Java code instead of NBT files to spawn
 * themselves. Those are not included here.
 */
public enum DefaultStructurePart
{
    ENDCITY_BASE_FLOOR("endcity/base_floor"),
    ENDCITY_BASE_ROOF("endcity/base_roof"),
    ENDCITY_BRIDGE_END("endcity/bridge_end"),
    ENDCITY_BRIDGE_GENTLE_STAIRS("endcity/bridge_gentle_stairs"),
    ENDCITY_BRIDGE_PIECE("endcity/bridge_piece"),
    ENDCITY_BRIDGE_STEEP_STAIRS("endcity/bridge_steep_stairs"),
    ENDCITY_FAT_TOWER_BASE("endcity/fat_tower_base"),
    ENDCITY_FAT_TOWER_MIDDLE("endcity/fat_tower_middle"),
    ENDCITY_FAT_TOWER_TOP("endcity/fat_tower_top"),
    ENDCITY_SECOND_FLOOR("endcity/second_floor"),
    ENDCITY_SECOND_FLOOR_2("endcity/second_floor_2"),
    ENDCITY_SECOND_ROOF("endcity/second_roof"),
    ENDCITY_SHIP("endcity/ship"),
    ENDCITY_THIRD_FLOOR("endcity/third_floor"),
    ENDCITY_THIRD_FLOOR_B("endcity/third_floor_b"),
    ENDCITY_THIRD_FLOOR_C("endcity/third_floor_c"),
    ENDCITY_THIRD_ROOF("endcity/third_roof"),
    ENDCITY_TOWER_BASE("endcity/tower_base"),
    ENDCITY_TOWER_FLOOR("endcity/tower_floor"),
    ENDCITY_TOWER_PIECE("endcity/tower_piece"),
    ENDCITY_TOWER_TOP("endcity/tower_top"),
    FOSSIL_SKULL_01("minecraft:fossils/fossil_skull_01"),
    FOSSIL_SKULL_01_COAL("minecraft:fossils/fossil_skull_01_coal"),
    FOSSIL_SKULL_02("minecraft:fossils/fossil_skull_02"),
    FOSSIL_SKULL_02_COAL("minecraft:fossils/fossil_skull_02_coal"),
    FOSSIL_SKULL_03("minecraft:fossils/fossil_skull_03"),
    FOSSIL_SKULL_03_COAL("minecraft:fossils/fossil_skull_03_coal"),
    FOSSIL_SKULL_04("minecraft:fossils/fossil_skull_04"),
    FOSSIL_SKULL_04_COAL("minecraft:fossils/fossil_skull_04_coal"),
    FOSSIL_SPINE_01("minecraft:fossils/fossil_spine_01"),
    FOSSIL_SPINE_01_COAL("minecraft:fossils/fossil_spine_01_coal"),
    FOSSIL_SPINE_02("minecraft:fossils/fossil_spine_02"),
    FOSSIL_SPINE_02_COAL("minecraft:fossils/fossil_spine_02_coal"),
    FOSSIL_SPINE_03("minecraft:fossils/fossil_spine_03"),
    FOSSIL_SPINE_03_COAL("minecraft:fossils/fossil_spine_03_coal"),
    FOSSIL_SPINE_04("minecraft:fossils/fossil_spine_04"),
    FOSSIL_SPINE_04_COAL("minecraft:fossils/fossil_spine_04_coal"),
    IGLOO_TOP("minecraft:igloo/igloo_top"),
    IGLOO_MIDDLE("minecraft:igloo/igloo_middle"),
    IGLOO_BOTTOM("minecraft:igloo/igloo_bottom");

    private static final Map<String, DefaultStructurePart> byPath = new HashMap<String, DefaultStructurePart>();

    static
    {
        for (DefaultStructurePart defaultStructurePart : values())
        {
            byPath.put(defaultStructurePart.path, defaultStructurePart);
        }
    }

    private final String path;

    /**
     * Looks a structure part up by the name given by Mojang.
     * @param name The structure part name.
     * @return The structure part.
     * @throws InvalidConfigException If no such structure part exists.
     */
    public static DefaultStructurePart getDefaultStructurePart(String name) throws InvalidConfigException
    {
        String lookupName = name;
        if (!lookupName.startsWith("minecraft:"))
        {
            lookupName = "minecraft:" + lookupName;
        }
        lookupName = lookupName.toLowerCase();

        DefaultStructurePart result = byPath.get(lookupName);
        if (result == null)
        {
            throw new InvalidConfigException(PluginStandardValues.PLUGIN_NAME + " doesn't know about a structure part called " + name);
        }

        return result;
    }

    DefaultStructurePart(String path)
    {
        this.path = path;
    }

    /**
     * Gets the path of the stucture, for example "minecraft:igloo/igloo_top".
     * The prefix "minecraft:" means that the file is stored in the Minecraft
     * JAR file.
     * @return The path.
     */
    public String getPath()
    {
        return path;
    }

    /**
     * {@inheritDoc}
     * @return Same result as {@link #getPath()}. Using the other method might
     * make the intent of the code more clear.
     */
    @Override
    public String toString()
    {
        return getPath();
    }
}
