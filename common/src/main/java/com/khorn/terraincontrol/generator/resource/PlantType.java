package com.khorn.terraincontrol.generator.resource;

import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.exception.InvalidConfigException;
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
            // Fall back on block name + data
            plantType = new PlantType(TerrainControl.readMaterial(name));
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
    private final LocalMaterialData topBlock;
    private final LocalMaterialData bottomBlock;

    /**
     * Creates a single-block plant with the given name.
     * 
     * @param name Custom name for this plant.
     * @param material The material of the block.
     * @param data The data value of the block.
     */
    protected PlantType(String name, DefaultMaterial material, int data)
    {
        this.name = name;
        this.topBlock = null;
        this.bottomBlock = TerrainControl.toLocalMaterialData(material, data);
    }

    /**
     * Creates a single-block plant.
     * 
     * @param material Material of the plant.
     */
    protected PlantType(LocalMaterialData material)
    {
        this.name = material.toString();
        this.topBlock = null;
        this.bottomBlock = material;
    }

    /**
     * Creates a two-block-high plant with the given name.
     * 
     * @param name Name of the plant.
     * @param material The material of the plant.
     * @param bottomData Data value for the bottom.
     * @param topData Data value for the top.
     */
    protected PlantType(String name, DefaultMaterial material, int bottomData, int topData)
    {
        this.name = name;
        this.topBlock = TerrainControl.toLocalMaterialData(material, topData);
        this.bottomBlock = TerrainControl.toLocalMaterialData(material, bottomData);
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
        world.setBlock(x, y, z, bottomBlock);
        if (topBlock != null)
        {
            world.setBlock(x, y + 1, z, topBlock);
        }
    }

    @Override
    public String toString()
    {
        return name;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((bottomBlock == null) ? 0 : bottomBlock.hashCode());
        result = prime * result + ((topBlock == null) ? 0 : topBlock.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (!(obj instanceof PlantType))
        {
            return false;
        }
        PlantType other = (PlantType) obj;
        if (bottomBlock == null)
        {
            if (other.bottomBlock != null)
            {
                return false;
            }
        } else if (!bottomBlock.equals(other.bottomBlock))
        {
            return false;
        }
        if (topBlock == null)
        {
            if (other.topBlock != null)
            {
                return false;
            }
        } else if (!topBlock.equals(other.topBlock))
        {
            return false;
        }
        return true;
    }

    /**
     * Gets the bottom block of this plant.
     * 
     * @return The bottom block.
     */
    public LocalMaterialData getBottomMaterial()
    {
        return bottomBlock;
    }

    /**
     * Gets the top block of this plant. May be null.
     * 
     * @return The top block, or null if this plant only has one block.
     */
    public LocalMaterialData getTopMaterial()
    {
        return topBlock;
    }

}
