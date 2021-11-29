package com.pg85.otg.util.minecraft;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.interfaces.IWorldGenRegion;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.LocalMaterials;

/**
 * Holds all small plants (1 or 2 blocks) of Minecraft so that users don't
 * have to use the confusing ids and data values Mojang and Bukkit gave them.
 */
public class PlantType
{
	// Builds lookup map
	private static final Map<String, PlantType> LOOKUP_MAP = new TreeMap<String, PlantType>(String.CASE_INSENSITIVE_ORDER);
	private static final Map<String, PlantType> ALIAS_LOOKUP_MAP = new TreeMap<String, PlantType>(String.CASE_INSENSITIVE_ORDER);

	public static final PlantType SeaGrass = register(new PlantType("SeaGrass", LocalMaterials.SEAGRASS));
	public static final PlantType Allium = register(new PlantType("Allium", LocalMaterials.ALLIUM));
	public static final PlantType AzureBluet = register(new PlantType("AzureBluet", LocalMaterials.AZURE_BLUET));
	public static final PlantType BlueOrchid = register(new PlantType("BlueOrchid", LocalMaterials.BLUE_ORCHID));
	public static final PlantType BrownMushroom = register(new PlantType("BrownMushroom", LocalMaterials.BROWN_MUSHROOM));
	public static final PlantType Dandelion = register(new PlantType("Dandelion", LocalMaterials.YELLOW_FLOWER));
	public static final PlantType DeadBush = register(new PlantType("DeadBush", LocalMaterials.DEAD_BUSH));
	public static final PlantType DoubleTallSeaGrass = register(new PlantType("DoubleTallSeaGrass", LocalMaterials.TALL_SEAGRASS_LOWER, LocalMaterials.TALL_SEAGRASS_UPPER));	
	public static final PlantType DoubleTallGrass = register(new PlantType("DoubleTallgrass", new String[] { "minecraft:double_plant:2", "double_plant:2" }, LocalMaterials.DOUBLE_TALL_GRASS_LOWER, LocalMaterials.DOUBLE_TALL_GRASS_UPPER));
	public static final PlantType Fern = register(new PlantType("Fern", LocalMaterials.FERN));
	public static final PlantType LargeFern = register(new PlantType("LargeFern", new String[] { "minecraft:double_plant:3", "double_plant:3" }, LocalMaterials.LARGE_FERN_LOWER, LocalMaterials.LARGE_FERN_UPPER));
	public static final PlantType Lilac = register(new PlantType("Lilac", new String[] { "minecraft:double_plant:1", "double_plant:1" }, LocalMaterials.LILAC_LOWER, LocalMaterials.LILAC_UPPER));
	public static final PlantType OrangeTulip = register(new PlantType("OrangeTulip", LocalMaterials.ORANGE_TULIP));
	public static final PlantType OxeyeDaisy = register(new PlantType("OxeyeDaisy", LocalMaterials.OXEYE_DAISY));
	public static final PlantType Peony = register(new PlantType("Peony", new String[] { "minecraft:double_plant:5", "double_plant:5" }, LocalMaterials.PEONY_LOWER, LocalMaterials.PEONY_UPPER));
	public static final PlantType PinkTulip = register(new PlantType("PinkTulip", LocalMaterials.PINK_TULIP));
	public static final PlantType Poppy = register(new PlantType("Poppy", LocalMaterials.POPPY));
	public static final PlantType RedMushroom = register(new PlantType("RedMushroom", LocalMaterials.RED_MUSHROOM));
	public static final PlantType RedTulip = register(new PlantType("RedTulip", LocalMaterials.RED_TULIP));
	public static final PlantType RoseBush = register(new PlantType("RoseBush", new String[] { "minecraft:double_plant:4", "double_plant:4" }, LocalMaterials.ROSE_BUSH_LOWER, LocalMaterials.ROSE_BUSH_UPPER));
	public static final PlantType Sunflower = register(new PlantType("Sunflower", new String[] { "minecraft:double_plant", "double_plant", "minecraft:double_plant:0", "double_plant:0" }, LocalMaterials.SUNFLOWER_LOWER, LocalMaterials.SUNFLOWER_UPPER));
	public static final PlantType Tallgrass = register(new PlantType("Tallgrass", LocalMaterials.LONG_GRASS));
	public static final PlantType WhiteTulip = register(new PlantType("WhiteTulip", LocalMaterials.WHITE_TULIP));
	public static final PlantType BerryBush = register(new PlantType("BerryBush", LocalMaterials.BERRY_BUSH));
	
	/**
	 * Gets the plant with the given name. The name can be one of the premade
	 * plant types or a blockName:data combination.
	 * 
	 * @param name Name of the plant type, case insensitive.
	 * @return The plant type.
	 * @throws InvalidConfigException If the name is invalid.
	 */
	public static PlantType getPlant(String name, IMaterialReader materialReader) throws InvalidConfigException
	{
		PlantType plantType = LOOKUP_MAP.get(name.toLowerCase());
		if (plantType == null)
		{
			plantType = ALIAS_LOOKUP_MAP.get(name.toLowerCase());
			if (plantType == null)
			{
				LocalMaterialData material = materialReader.readMaterial(name);
				// Fall back on block name + data
				plantType = new PlantType(material);
			}
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
	 * {@link #getPlant(String, IMaterialReader)}.
	 * 
	 * @param plantType The plant type.
	 * @return The plant type provided.
	 */
	private static PlantType register(PlantType plantType)
	{
		LOOKUP_MAP.put(plantType.toString().toLowerCase(), plantType);
		if(plantType.aliases != null)
		{
			for(String alias : plantType.aliases)
			{
				ALIAS_LOOKUP_MAP.put(alias.toLowerCase(), plantType);
			}
		}
		return plantType;
	}

	private final String name;
	private final String[] aliases;
	private LocalMaterialData topBlock;
	private LocalMaterialData bottomBlock;

	/**
	 * Creates a single-block plant with the given name.
	 * 
	 * @param name Custom name for this plant.
	 * @param material The material of the block.
	 */
	private PlantType(String name, String[] aliases, LocalMaterialData material)
	{
		this.name = name;
		this.aliases = aliases;
		this.topBlock = null;
		this.bottomBlock = material;
	}
	private PlantType(String name, LocalMaterialData material)
	{
		this(name, (String[])null, material);
	}

	/**
	 * Creates a single-block plant.
	 * 
	 * @param material Material of the plant.
	 */
	private PlantType(LocalMaterialData material)
	{
		this.name = material.toString();
		this.aliases = null;
		this.topBlock = null;
		this.bottomBlock = material;
	}

	/**
	 * Creates a two-block-high plant with the given name.
	 * 
	 * @param name Name of the plant.
	 * @param bottomMaterial The bottom material of the plant.
	 * @param topMaterial The top material of the plant, if it has one
	 */
	private PlantType(String name, String[] aliases, LocalMaterialData bottomMaterial, LocalMaterialData topMaterial)
	{
		this.name = name;
		this.aliases = aliases;
		this.bottomBlock = bottomMaterial;
		this.topBlock = topMaterial;
	}
	private PlantType(String name, LocalMaterialData bottomMaterial, LocalMaterialData topMaterial)
	{
		this(name, null, bottomMaterial, topMaterial);
	}

	/**
	 * Gets the name of this plant type. You can get an equivalent plant back
	 * type using {@link #getPlant(String, IMaterialReader)}.
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
	 * @param worldGenregion The world to spawn in.
	 * @param x X position of the plant.
	 * @param y Y position of the lowest block of the plant.
	 * @param z Z position of the plant.
	 */
	public void spawn(IWorldGenRegion worldGenregion, int x, int y, int z)
	{		
		worldGenregion.setBlock(x, y, z, bottomBlock);
		if (topBlock != null)
		{
			worldGenregion.setBlock(x, y + 1, z, topBlock);
		}
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
	
	@Override
	public String toString()
	{
		return name;
	}	
}
