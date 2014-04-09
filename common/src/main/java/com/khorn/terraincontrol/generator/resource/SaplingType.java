package com.khorn.terraincontrol.generator.resource;

import com.khorn.terraincontrol.util.minecraftTypes.TreeType;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents all sapling types, including mushrooms. Note that not every
 * {@link TreeType} in Minecraft has its own sapling.
 *
 */
public enum SaplingType
{
    /**
     * Used as a wildcard for defining something that grows from all saplings.
     */
    All,
    Oak,
    Redwood,
    Birch,
    SmallJungle,
    BigJungle,
    RedMushroom,
    BrownMushroom,
    Acacia,
    DarkOak,
    HugeRedwood;

    // Case insensitive index
    private static Map<String, SaplingType> BY_NAME = new HashMap<String, SaplingType>();
    static
    {
        for (SaplingType type : values())
        {
            BY_NAME.put(type.name().toLowerCase(), type);
        }
    }

    /**
     * Gets whether this sapling grows a tree. For example,
     * {@link SaplingType#RedMushroom} doesn't grow a tree.
     *
     * @return Whether this sapling grows a tree.
     */
    public boolean growsTree()
    {
        return this != RedMushroom && this != BrownMushroom;
    }

    /**
     * Gets whether this sapling requires four saplings (three neighbour
     * saplings) to grow a tree. If yes, the saplings must be placed in a 2x2
     * square.
     *
     * @return True if four sapling
     */
    public boolean requiresFourSaplings()
    {
        return this == BigJungle || this == DarkOak || this == HugeRedwood;
    }

    /**
     * Gets the sapling type by its name. Name is case insensitive.
     * 
     * @param name
     *            The name to look up.
     * @return The sapling type.
     */
    public static SaplingType get(String name)
    {
        return BY_NAME.get(name.toLowerCase());
    }

}
