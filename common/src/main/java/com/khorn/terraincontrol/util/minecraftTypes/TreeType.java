package com.khorn.terraincontrol.util.minecraftTypes;

/**
 * Represents all tree types in Minecraft.
 *
 */
public enum TreeType
{
    Acacia,
    BigTree,
    Birch,
    CocoaTree,
    DarkOak,
    /**
     * Soft-deprecated, renamed to Birch
     */
    Forest("Birch"),
    GroundBush,
    HugeMushroom,
    JungleTree,
    SwampTree,
    Taiga1,
    Taiga2,
    HugeTaiga1,
    HugeTaiga2,
    TallBirch,
    Tree;

    private final String name;

    /**
     * Creates a new tree type.
     */
    private TreeType()
    {
        this.name = name();
    }

    /**
     * Creates a new tree type. When this type is written to the configs, the
     * provided name will be used instead. This allows for renaming tree types
     * while still being able to read old ones.
     * 
     * @param name The name used for writing.
     */
    private TreeType(String name)
    {
        this.name = name;
    }

    public String toString()
    {
        // Overridden so that the correct name is used when writing
        // to the config files
        return name;
    }
}