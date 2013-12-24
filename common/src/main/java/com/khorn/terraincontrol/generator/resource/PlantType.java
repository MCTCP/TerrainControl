package com.khorn.terraincontrol.generator.resource;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.util.helpers.StringHelper;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultMaterial;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * Holds all small plants (1 or 2 blocks) of Minecraft so that users don't
 * have to use the confusing ids and data values Mojang and Bukkit gave them.
 */
public class PlantType
{
    // Builds lookup map
    private static final Map<String, PlantType> LOOKUP_MAP = new TreeMap<String, PlantType>(String.CASE_INSENSITIVE_ORDER);

    public static final PlantType Allium = register(new PlantType("Allium", DefaultMaterial.RED_ROSE, 2));
    public static final PlantType AzureBluet = register(new PlantType("AzureBluet", DefaultMaterial.RED_ROSE, 3));
    public static final PlantType BlueOrchid = register(new PlantType("BlueOrchid", DefaultMaterial.RED_ROSE, 1));
    public static final PlantType BrownMushroom = register(new PlantType("BrownMushroom", DefaultMaterial.BROWN_MUSHROOM, 0));
    public static final PlantType Dandelion = register(new PlantType("Dandelion", DefaultMaterial.YELLOW_FLOWER, 0));
    public static final PlantType DeadBush = register(new PlantType("DeadBush", DefaultMaterial.DEAD_BUSH, 0));
    public static final PlantType DoubleTallgrass = register(new PlantType("DoubleTallgrass", DefaultMaterial.DOUBLE_PLANT, 2, 8));
    public static final PlantType Fern = register(new PlantType("Fern", DefaultMaterial.LONG_GRASS, 2));
    public static final PlantType LargeFern = register(new PlantType("LargeFern", DefaultMaterial.DOUBLE_PLANT, 3, 8));
    public static final PlantType Lilac = register(new PlantType("Lilac", DefaultMaterial.DOUBLE_PLANT, 1, 8));
    public static final PlantType OrangeTulip = register(new PlantType("OrangeTulip", DefaultMaterial.RED_ROSE, 5));
    public static final PlantType OxeyeDaisy = register(new PlantType("OxeyeDaisy", DefaultMaterial.RED_ROSE, 8));
    public static final PlantType Peony = register(new PlantType("Peony", DefaultMaterial.DOUBLE_PLANT, 5, 8));
    public static final PlantType PinkTulip = register(new PlantType("PinkTulip", DefaultMaterial.RED_ROSE, 7));
    public static final PlantType Poppy = register(new PlantType("Poppy", DefaultMaterial.RED_ROSE, 0));
    public static final PlantType RedMushroom = register(new PlantType("RedMushroom", DefaultMaterial.RED_MUSHROOM, 0));
    public static final PlantType RedTulip = register(new PlantType("RedTulip", DefaultMaterial.RED_ROSE, 4));
    public static final PlantType RoseBush = register(new PlantType("RoseBush", DefaultMaterial.DOUBLE_PLANT, 4, 8));
    public static final PlantType Sunflower = register(new PlantType("Sunflower", DefaultMaterial.DOUBLE_PLANT, 0, 8));
    public static final PlantType Tallgrass = register(new PlantType("Tallgrass", DefaultMaterial.LONG_GRASS, 1));
    public static final PlantType WhiteTulip = register(new PlantType("WhiteTulip", DefaultMaterial.RED_ROSE, 6));

    /**
     * Gets the plant with the given name. The name can be one of the premade
     * plant types or a blockName:data combination.
     * 
     * @param name Name of the plant type, case insensitive.
     * @return The plant type.
     * @throws InvalidConfigException If the name is invalid.
     */
    public static PlantType getPlant(String name) throws InvalidConfigException
    {
        PlantType plantType = LOOKUP_MAP.get(name);
        if (plantType == null)
        {
            // Fall back on block id/name + data
            int blockId = StringHelper.readBlockId(name);
            int blockData = StringHelper.readBlockData(name);
            plantType = new PlantType(StringHelper.makeMaterial(blockId, blockData), blockId, blockData);
        }
        return plantType;
    }

    /**
     * Gets all registered plant types.
     * 
     * @return All registered plant types.
     */
    public static Collection<PlantType> values()
    {
        return LOOKUP_MAP.values();
    }

    /**
     * Registers the plant type so that it can be retrieved using
     * {@link #getPlant(String)}.
     * 
     * @param plantType The plant type.
     * @return The plant type provided.
     */
    public static PlantType register(PlantType plantType)
    {
        LOOKUP_MAP.put(plantType.toString(), plantType);
        return plantType;
    }

    private final String name;
    private final int blockId;
    private final int topData;
    private final int bottomData;

    /**
     * Creates a single-block plant.
     * 
     * @param material The material of the block.
     * @param data The data value of the block.
     */
    protected PlantType(String name, DefaultMaterial material, int data)
    {
        this(name, material.id, data);
    }

    /**
     * Creates a two-block-high plant.
     * 
     * @param material The material of the plant.
     * @param bottomData Data value for the bottom.
     * @param topData Data value for the top.
     */
    protected PlantType(String name, DefaultMaterial material, int bottomData, int topData)
    {
        this.name = name;
        this.blockId = material.id;
        this.bottomData = bottomData;
        this.topData = topData;
    }

    /**
     * Creates a single-block plant.
     * 
     * @param id The id of the block.
     * @param data The data value of the block.
     */
    protected PlantType(String name, int id, int data)
    {
        this.name = name;
        this.blockId = id;
        this.bottomData = data;
        this.topData = -1;
    }

    int getBlockId()
    {
        return blockId;
    }

    int getBottomBlockData()
    {
        return bottomData;
    }

    /**
     * Gets the name of this plant type. You can get an equivalent plant back
     * type using {@link #getPlant(String)}.
     * 
     * @return The name of this plant type.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Spawns this plant in the world.
     * 
     * @param world The world to spawn in.
     * @param x X position of the plant.
     * @param y Y position of the lowest block of the plant.
     * @param z Z position of the plant.
     */
    public void spawn(LocalWorld world, int x, int y, int z)
    {
        world.setBlock(x, y, z, blockId, bottomData);
        if (topData != -1)
        {
            world.setBlock(x, y + 1, z, blockId, topData);
        }
    }

    @Override
    public String toString()
    {
        return name;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if (!(other instanceof PlantType))
        {
            return false;
        }
        return ((PlantType) other).getName().equalsIgnoreCase(this.getName());
    }

    @Override
    public int hashCode()
    {
        return name.toLowerCase().hashCode() - 108;
    }

}
