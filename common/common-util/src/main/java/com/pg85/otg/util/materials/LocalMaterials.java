package com.pg85.otg.util.materials;

import java.util.List;

public class LocalMaterials
{
	private static final String resourceDomain = "minecraft";
	private static String getRegistryKey(String resourcePath)
	{
		return resourceDomain + ":" + resourcePath;
	}
	
	// Block names used in OTG code as default values
	
	public static final String AIR_NAME = getRegistryKey("air");
	public static final String GRASS_NAME = getRegistryKey("grass_block");
	public static final String DIRT_NAME = getRegistryKey("dirt");
	public static final String STONE_NAME = getRegistryKey("stone");
	public static final String BEDROCK_NAME = getRegistryKey("bedrock");
	public static final String WATER_NAME = getRegistryKey("water");
	public static final String LAVA_NAME = getRegistryKey("lava");
	public static final String ICE_NAME = getRegistryKey("ice");
	
	// Blocks used in OTG code, these must be initialised by 
	// the forge/spigot classes extending this class.

	public static LocalMaterialData AIR;
	public static LocalMaterialData CAVE_AIR;

	// Block of grass
	public static LocalMaterialData GRASS;
	public static LocalMaterialData DIRT;
	public static LocalMaterialData PODZOL;
	public static LocalMaterialData CLAY;
	public static LocalMaterialData TERRACOTTA;
	public static LocalMaterialData WHITE_TERRACOTTA;
	public static LocalMaterialData ORANGE_TERRACOTTA;
	public static LocalMaterialData YELLOW_TERRACOTTA;
	public static LocalMaterialData BROWN_TERRACOTTA;
	public static LocalMaterialData RED_TERRACOTTA;
	public static LocalMaterialData SILVER_TERRACOTTA;
	public static LocalMaterialData STONE;
	public static LocalMaterialData SAND;
	public static LocalMaterialData RED_SAND;
	public static LocalMaterialData SANDSTONE;
	public static LocalMaterialData RED_SANDSTONE;
	public static LocalMaterialData GRAVEL;
	public static LocalMaterialData MOSSY_COBBLESTONE;
	public static LocalMaterialData SNOW;
	public static LocalMaterialData SNOW_BLOCK;
	public static LocalMaterialData TORCH;
	public static LocalMaterialData BEDROCK;
	public static LocalMaterialData MAGMA;
	public static LocalMaterialData ICE;
	public static LocalMaterialData PACKED_ICE;
	public static LocalMaterialData FROSTED_ICE;
	public static LocalMaterialData GLOWSTONE;
	public static LocalMaterialData MYCELIUM;
	public static LocalMaterialData STONE_SLAB;

	// Liquids
	public static LocalMaterialData WATER;
	public static LocalMaterialData LAVA;

	// Trees
	public static LocalMaterialData ACACIA_LOG;
	public static LocalMaterialData BIRCH_LOG;
	public static LocalMaterialData DARK_OAK_LOG;
	public static LocalMaterialData OAK_LOG;
	public static LocalMaterialData SPRUCE_LOG;
	public static LocalMaterialData STRIPPED_ACACIA_LOG;
	public static LocalMaterialData STRIPPED_BIRCH_LOG;
	public static LocalMaterialData STRIPPED_DARK_OAK_LOG;
	public static LocalMaterialData STRIPPED_JUNGLE_LOG;
	public static LocalMaterialData STRIPPED_OAK_LOG;
	public static LocalMaterialData STRIPPED_SPRUCE_LOG;
	public static LocalMaterialData ACACIA_WOOD;
	public static LocalMaterialData BIRCH_WOOD;
	public static LocalMaterialData DARK_OAK_WOOD;
	public static LocalMaterialData OAK_WOOD;
	public static LocalMaterialData SPRUCE_WOOD;	
	
	public static LocalMaterialData ACACIA_LEAVES;
	public static LocalMaterialData BIRCH_LEAVES;
	public static LocalMaterialData DARK_OAK_LEAVES;
	public static LocalMaterialData JUNGLE_LEAVES;
	public static LocalMaterialData OAK_LEAVES;
	public static LocalMaterialData SPRUCE_LEAVES;

	// Plants
	public static LocalMaterialData POPPY;
	public static LocalMaterialData BLUE_ORCHID;
	public static LocalMaterialData ALLIUM;
	public static LocalMaterialData AZURE_BLUET;
	public static LocalMaterialData RED_TULIP;
	public static LocalMaterialData ORANGE_TULIP;
	public static LocalMaterialData WHITE_TULIP;
	public static LocalMaterialData PINK_TULIP;
	public static LocalMaterialData OXEYE_DAISY;


	public static LocalMaterialData BROWN_MUSHROOM;
	public static LocalMaterialData YELLOW_FLOWER;
	public static LocalMaterialData DEAD_BUSH;
	public static LocalMaterialData LONG_GRASS;
	public static LocalMaterialData RED_MUSHROOM;
	
	public static LocalMaterialData PUMPKIN;
	public static LocalMaterialData CACTUS;	
	public static LocalMaterialData MELON_BLOCK;
	public static LocalMaterialData VINE;
	public static LocalMaterialData SAPLING;
	public static LocalMaterialData WATER_LILY;
	public static LocalMaterialData SUGAR_CANE_BLOCK;
	public static LocalMaterialData BAMBOO;
	public static LocalMaterialData BAMBOO_SMALL;
	public static LocalMaterialData BAMBOO_LARGE;
	public static LocalMaterialData BAMBOO_LARGE_GROWING;
	public static LocalMaterialData SEAGRASS;
	public static LocalMaterialData TALL_SEAGRASS_LOWER;
	public static LocalMaterialData TALL_SEAGRASS_UPPER;
	public static LocalMaterialData KELP;
	public static LocalMaterialData KELP_PLANT;
	public static LocalMaterialData VINE_NORTH;
	public static LocalMaterialData VINE_SOUTH;
	public static LocalMaterialData VINE_EAST;
	public static LocalMaterialData VINE_WEST;
	public static LocalMaterialData SEA_PICKLE;

	// Coral
	public static List<LocalMaterialData> CORAL_BLOCKS;
	public static List<LocalMaterialData> WALL_CORALS;
	public static List<LocalMaterialData> CORALS;

	public static LocalMaterialData DOUBLE_TALL_GRASS_LOWER;
	public static LocalMaterialData DOUBLE_TALL_GRASS_UPPER;
	public static LocalMaterialData LARGE_FERN_LOWER;
	public static LocalMaterialData LARGE_FERN_UPPER;
	public static LocalMaterialData LILAC_LOWER;
	public static LocalMaterialData LILAC_UPPER;
	public static LocalMaterialData PEONY_LOWER;
	public static LocalMaterialData PEONY_UPPER;
	public static LocalMaterialData ROSE_BUSH_LOWER;
	public static LocalMaterialData ROSE_BUSH_UPPER;
	public static LocalMaterialData SUNFLOWER_LOWER;
	public static LocalMaterialData SUNFLOWER_UPPER;

	// Ores
	public static LocalMaterialData COAL_ORE;
	public static LocalMaterialData DIAMOND_ORE;
	public static LocalMaterialData EMERALD_ORE;
	public static LocalMaterialData GOLD_ORE;
	public static LocalMaterialData IRON_ORE;
	public static LocalMaterialData LAPIS_ORE;
	public static LocalMaterialData QUARTZ_ORE;
	public static LocalMaterialData REDSTONE_ORE;

	// Ore blocks
	public static LocalMaterialData GOLD_BLOCK;
	public static LocalMaterialData IRON_BLOCK;
	public static LocalMaterialData REDSTONE_BLOCK;
	public static LocalMaterialData DIAMOND_BLOCK;
	public static LocalMaterialData LAPIS_BLOCK;
	public static LocalMaterialData COAL_BLOCK;
	public static LocalMaterialData QUARTZ_BLOCK;
	public static LocalMaterialData EMERALD_BLOCK;	
}