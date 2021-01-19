package com.pg85.otg.util.materials;

public class LocalMaterials
{
    // Block names used in OTG code as default values
    private static final String resourceDomain = "minecraft";
    private static String getRegistryKey(String resourcePath)
    {
    	return resourceDomain + ":" + resourcePath;
    }

    public static final String AIR_NAME = getRegistryKey("air");
    public static final String GRASS_NAME = getRegistryKey("grass_block");
    public static final String DIRT_NAME = getRegistryKey("dirt");

	public static final String CLAY_NAME = getRegistryKey("clay");
	public static final String TERRACOTTA_NAME = getRegistryKey("terracotta");
	public static final String WHITE_TERRACOTTA_NAME = getRegistryKey("white_terracotta");
	public static final String ORANGE_TERRACOTTA_NAME = getRegistryKey("orange_terracotta");
	public static final String YELLOW_TERRACOTTA_NAME = getRegistryKey("yellow_terracotta");
	public static final String BROWN_TERRACOTTA_NAME = getRegistryKey("brown_terracotta");
	public static final String RED_TERRACOTTA_NAME = getRegistryKey("red_terracotta");
	public static final String SILVER_TERRACOTTA_NAME = getRegistryKey("light_gray_terracotta");
	public static final String STONE_NAME = getRegistryKey("stone");
	public static final String SAND_NAME = getRegistryKey("sand");
	public static final String RED_SAND_NAME = getRegistryKey("red_sand");
	public static final String SANDSTONE_NAME = getRegistryKey("sandstone");

	public static final String RED_SANDSTONE_NAME = getRegistryKey("red_sandstone");
	public static final String GRAVEL_NAME = getRegistryKey("gravel");
	public static final String MOSSY_COBBLESTONE_NAME = getRegistryKey("mossy_cobblestone");
	public static final String SNOW_NAME = getRegistryKey("snow");
	public static final String SNOW_BLOCK_NAME = getRegistryKey("snow_block");
	public static final String TORCH_NAME = getRegistryKey("torch");
	public static final String BEDROCK_NAME = getRegistryKey("bedrock");
	public static final String MAGMA_NAME = getRegistryKey("magma_block");
	public static final String ICE_NAME = getRegistryKey("ice");
	public static final String PACKED_ICE_NAME = getRegistryKey("packed_ice");
	public static final String FROSTED_ICE_NAME = getRegistryKey("blue_ice");
	public static final String GLOWSTONE_NAME = getRegistryKey("glowstone");
	public static final String MYCELIUM_NAME = getRegistryKey("mycelium");
	public static final String STONE_SLAB_NAME = getRegistryKey("stone_slab");

    // Liquids
	public static final String WATER_NAME = getRegistryKey("water");
	public static final String LAVA_NAME = getRegistryKey("lava");

    // Trees    
	public static final String ACACIA_LOG_NAME = getRegistryKey("acacia_log");
	public static final String BIRCH_LOG_NAME = getRegistryKey("birch_log");
	public static final String DARK_OAK_LOG_NAME = getRegistryKey("dark_oak_log");
	public static final String OAK_LOG_NAME = getRegistryKey("oak_log");
	public static final String SPRUCE_LOG_NAME = getRegistryKey("spruce_log");
	public static final String STRIPPED_ACACIA_LOG_NAME = getRegistryKey("stripped_acacia_log");
	public static final String STRIPPED_BIRCH_LOG_NAME = getRegistryKey("stripped_birch_log");
	public static final String STRIPPED_DARK_OAK_LOG_NAME = getRegistryKey("stripped_dark_oak_log");
	public static final String STRIPPED_JUNGLE_LOG_NAME = getRegistryKey("stripped_jungle_log");
	public static final String STRIPPED_OAK_LOG_NAME = getRegistryKey("stripped_oak_log");
	public static final String STRIPPED_SPRUCE_LOG_NAME = getRegistryKey("stripped_spruce_log");
	
	public static final String ACACIA_LEAVES_NAME = getRegistryKey("acacia_leaves");
	public static final String BIRCH_LEAVES_NAME = getRegistryKey("birch_leaves");
	public static final String DARK_OAK_LEAVES_NAME = getRegistryKey("dark_oak_leaves");
	public static final String JUNGLE_LEAVES_NAME = getRegistryKey("jungle_leaves");
	public static final String OAK_LEAVES_NAME = getRegistryKey("oak_leaves");
	public static final String SPRUCE_LEAVES_NAME = getRegistryKey("spruce_leaves");

    // Plants
	public static final String RED_ROSE_NAME = getRegistryKey("rose_bush");
	public static final String BROWN_MUSHROOM_NAME = getRegistryKey("brown_mushroom");
	public static final String YELLOW_FLOWER_NAME = getRegistryKey("dandelion");
	public static final String DEAD_BUSH_NAME = getRegistryKey("dead_bush");
	public static final String LONG_GRASS_NAME = getRegistryKey("tall_grass");
	public static final String RED_MUSHROOM_NAME = getRegistryKey("red_mushroom");
	
	public static final String DOUBLE_TALL_GRASS_NAME = getRegistryKey("tall_grass");
	public static final String LARGE_FERN_NAME = getRegistryKey("large_fern");
	public static final String LILAC_NAME = getRegistryKey("lilac");

	public static final String PEONY_NAME = getRegistryKey("peony");
	public static final String ROSE_BUSH_NAME = getRegistryKey("rose_bush");
	public static final String SUNFLOWER_NAME = getRegistryKey("sunflower");

	public static final String PUMPKIN_NAME = getRegistryKey("pumpkin");
	public static final String CACTUS_NAME = getRegistryKey("cactus");
	public static final String MELON_BLOCK_NAME = getRegistryKey("melon");
	public static final String VINE_NAME = getRegistryKey("vine");
	public static final String SAPLING_NAME = getRegistryKey("oak_sapling");
	public static final String WATER_LILY_NAME = getRegistryKey("lily_pad");
	public static final String SUGAR_CANE_BLOCK_NAME = getRegistryKey("sugar_cane");
	public static final String PODZOL_NAME = getRegistryKey("podzol");

    // Ores
	public static final String COAL_ORE_NAME = getRegistryKey("coal_ore");
	public static final String DIAMOND_ORE_NAME = getRegistryKey("diamond_ore");
	public static final String EMERALD_ORE_NAME = getRegistryKey("emerald_ore");
	public static final String GLOWING_REDSTONE_ORE_NAME = getRegistryKey("redstone_ore");
	public static final String GOLD_ORE_NAME = getRegistryKey("gold_ore");
	public static final String IRON_ORE_NAME = getRegistryKey("iron_ore");
	public static final String LAPIS_ORE_NAME = getRegistryKey("lapis_ore");
	public static final String QUARTZ_ORE_NAME = getRegistryKey("nether_quartz_ore");
	public static final String REDSTONE_ORE_NAME = getRegistryKey("redstone_ore");

	// Ore blocks
	public static final String GOLD_BLOCK_NAME = getRegistryKey("gold_block");
	public static final String IRON_BLOCK_NAME = getRegistryKey("iron_block");
	public static final String REDSTONE_BLOCK_NAME = getRegistryKey("redstone_block");
	public static final String DIAMOND_BLOCK_NAME = getRegistryKey("diamond_block");
	public static final String LAPIS_BLOCK_NAME = getRegistryKey("lapis_block");
	public static final String COAL_BLOCK_NAME = getRegistryKey("coal_block");
	public static final String QUARTZ_BLOCK_NAME = getRegistryKey("nether_quartz_ore");
	public static final String EMERALD_BLOCK_NAME = getRegistryKey("emerald_ore");

    // Blocks used in OTG code, these must be initialised by 
	// the forge/spigot classes extending this class.
	// TODO: Make this prettier

    public static LocalMaterialData AIR;

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
	
    public static LocalMaterialData ACACIA_LEAVES;
    public static LocalMaterialData BIRCH_LEAVES;
    public static LocalMaterialData DARK_OAK_LEAVES;
    public static LocalMaterialData JUNGLE_LEAVES;
    public static LocalMaterialData OAK_LEAVES;
    public static LocalMaterialData SPRUCE_LEAVES;

    // Plants
    public static LocalMaterialData RED_ROSE;
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
	public static LocalMaterialData GLOWING_REDSTONE_ORE;
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