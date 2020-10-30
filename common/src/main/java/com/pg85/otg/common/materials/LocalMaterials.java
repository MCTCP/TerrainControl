package com.pg85.otg.common.materials;

public class LocalMaterials
{
    // Block names used in OTG code as default values

    private static String resourceDomain = "minecraft";
    private static String getRegistryKey(String resourcePath)
    {
    	return resourceDomain + ":" + resourcePath;
    }

    public static String AIR_NAME = getRegistryKey("air");
    public static String GRASS_NAME = getRegistryKey("grass_block");
    public static String DIRT_NAME = getRegistryKey("dirt");

	public static String COARSE_DIRT_NAME = getRegistryKey("coarse_dirt");
	public static String CLAY_NAME = getRegistryKey("clay");
	public static String TERRACOTTA_NAME = getRegistryKey("terracotta");
	public static String STAINED_CLAY_NAME = getRegistryKey("gray_glazed_terracotta");
	public static String WHITE_STAINED_CLAY_NAME = getRegistryKey("white_glazed_terracotta");
	public static String ORANGE_STAINED_CLAY_NAME = getRegistryKey("orange_glazed_terracotta");
	public static String YELLOW_STAINED_CLAY_NAME = getRegistryKey("yellow_glazed_terracotta");
	public static String BROWN_STAINED_CLAY_NAME = getRegistryKey("brown_glazed_terracotta");
	public static String RED_STAINED_CLAY_NAME = getRegistryKey("red_glazed_terracotta");
	public static String SILVER_STAINED_CLAY_NAME = getRegistryKey("light_gray_glazed_terracotta");
	public static String STONE_NAME = getRegistryKey("stone");
	public static String SAND_NAME = getRegistryKey("sand");
	public static String RED_SAND_NAME = getRegistryKey("red_sand");
	public static String SANDSTONE_NAME = getRegistryKey("sandstone");

	public static String RED_SANDSTONE_NAME = getRegistryKey("red_sandstone");
	public static String GRAVEL_NAME = getRegistryKey("gravel");
	public static String MOSSY_COBBLESTONE_NAME = getRegistryKey("mossy_cobblestone");
	public static String SNOW_NAME = getRegistryKey("snow_layer");
	public static String SNOW_BLOCK_NAME = getRegistryKey("snow");
	public static String TORCH_NAME = getRegistryKey("torch");
	public static String BEDROCK_NAME = getRegistryKey("bedrock");
	public static String MAGMA_NAME = getRegistryKey("magma_block");
	public static String ICE_NAME = getRegistryKey("ice");
	public static String PACKED_ICE_NAME = getRegistryKey("packed_ice");
	public static String FROSTED_ICE_NAME = getRegistryKey("blue_ice");
	public static String GLOWSTONE_NAME = getRegistryKey("glowstone");
	public static String MYCELIUM_NAME = getRegistryKey("mycelium");
	public static String STONE_SLAB_NAME = getRegistryKey("stone_slab");

    // Liquids
	public static String WATER_NAME = getRegistryKey("water");
	public static String LAVA_NAME = getRegistryKey("lava");

    // Trees    
	public static String LOG_NAME = getRegistryKey("oak_log");
	public static String LOG_2_NAME = getRegistryKey("birch_log");
	public static String LEAVES_NAME = getRegistryKey("oak_leaves");
	public static String LEAVES_2_NAME = getRegistryKey("birch_leaves");

    // Plants
	public static String RED_ROSE_NAME = getRegistryKey("rose_bush");
	public static String BROWN_MUSHROOM_NAME = getRegistryKey("brown_mushroom");
	public static String YELLOW_FLOWER_NAME = getRegistryKey("dandelion");
	public static String DEAD_BUSH_NAME = getRegistryKey("dead_bush");
	public static String LONG_GRASS_NAME = getRegistryKey("tall_grass");
	public static String DOUBLE_PLANT_NAME = getRegistryKey("sunflower");
	public static String RED_MUSHROOM_NAME = getRegistryKey("red_mushroom");

	public static String PUMPKIN_NAME = getRegistryKey("pumpkin");
	public static String CACTUS_NAME = getRegistryKey("cactus");
	public static String MELON_BLOCK_NAME = getRegistryKey("melon");
	public static String VINE_NAME = getRegistryKey("vine");
	public static String SAPLING_NAME = getRegistryKey("oak_sapling");
	public static String WATER_LILY_NAME = getRegistryKey("lily_pad");
	public static String SUGAR_CANE_BLOCK_NAME = getRegistryKey("sugar_cane");

    // Ores
	public static String COAL_ORE_NAME = getRegistryKey("coal_ore");
	public static String DIAMOND_ORE_NAME = getRegistryKey("diamond_ore");
	public static String EMERALD_ORE_NAME = getRegistryKey("emerald_ore");
	public static String GLOWING_REDSTONE_ORE_NAME = getRegistryKey("redstone_ore");
	public static String GOLD_ORE_NAME = getRegistryKey("gold_ore");
	public static String IRON_ORE_NAME = getRegistryKey("iron_ore");
	public static String LAPIS_ORE_NAME = getRegistryKey("lapis_ore");
	public static String QUARTZ_ORE_NAME = getRegistryKey("nether_quartz_ore");
	public static String REDSTONE_ORE_NAME = getRegistryKey("redstone_ore");

	// Ore blocks
	public static String GOLD_BLOCK_NAME = getRegistryKey("gold_block");
	public static String IRON_BLOCK_NAME = getRegistryKey("iron_block");
	public static String REDSTONE_BLOCK_NAME = getRegistryKey("redstone_block");
	public static String DIAMOND_BLOCK_NAME = getRegistryKey("diamond_block");
	public static String LAPIS_BLOCK_NAME = getRegistryKey("lapis_block");
	public static String COAL_BLOCK_NAME = getRegistryKey("coal_block");
	public static String QUARTZ_BLOCK_NAME = getRegistryKey("nether_quartz_ore");
	public static String EMERALD_BLOCK_NAME = getRegistryKey("emerald_ore");

    // Blocks used in OTG code, these must be initialised by 
	// the forge/spigot classes extending this class.
	// TODO: Make this prettier

    public static LocalMaterialData AIR;

    public static LocalMaterialData GRASS;
    public static LocalMaterialData DIRT;
    public static LocalMaterialData COARSE_DIRT;
    public static LocalMaterialData CLAY;
    public static LocalMaterialData TERRACOTTA;
    public static LocalMaterialData STAINED_CLAY;
    public static LocalMaterialData WHITE_STAINED_CLAY;
    public static LocalMaterialData ORANGE_STAINED_CLAY;
    public static LocalMaterialData YELLOW_STAINED_CLAY;
    public static LocalMaterialData BROWN_STAINED_CLAY;
    public static LocalMaterialData RED_STAINED_CLAY;
    public static LocalMaterialData SILVER_STAINED_CLAY;
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
    public static LocalMaterialData STATIONARY_WATER;    
    public static LocalMaterialData LAVA;
    public static LocalMaterialData STATIONARY_LAVA;

    // Trees    
    public static LocalMaterialData LOG; 
    public static LocalMaterialData LOG_2; 
    public static LocalMaterialData LEAVES; 
    public static LocalMaterialData LEAVES_2;

    // Plants
    public static LocalMaterialData RED_ROSE;
	public static LocalMaterialData BROWN_MUSHROOM;
	public static LocalMaterialData YELLOW_FLOWER;
	public static LocalMaterialData DEAD_BUSH;
	public static LocalMaterialData LONG_GRASS;
	public static LocalMaterialData DOUBLE_PLANT;
	public static LocalMaterialData RED_MUSHROOM;

	public static LocalMaterialData PUMPKIN;
    public static LocalMaterialData CACTUS;    
    public static LocalMaterialData MELON_BLOCK;
    public static LocalMaterialData VINE;
    public static LocalMaterialData SAPLING;
    public static LocalMaterialData WATER_LILY;
    public static LocalMaterialData SUGAR_CANE_BLOCK;    

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