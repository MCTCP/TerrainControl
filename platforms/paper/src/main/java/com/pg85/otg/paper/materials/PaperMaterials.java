package com.pg85.otg.paper.materials;

import com.pg85.otg.util.materials.LocalMaterials;

import net.minecraft.server.v1_17_R1.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class PaperMaterials extends LocalMaterials
{
	// Default blocks in given tags
	// Tags aren't loaded until datapacks are loaded, on world creation. We mirror the vanilla copy of the tag to solve this.
	private static final Block[] CORAL_BLOCKS_TAG = { Blocks.TUBE_CORAL_BLOCK, Blocks.BRAIN_CORAL_BLOCK, Blocks.BUBBLE_CORAL_BLOCK, Blocks.FIRE_CORAL_BLOCK, Blocks.HORN_CORAL_BLOCK };
	private static final Block[] WALL_CORALS_TAG = { Blocks.TUBE_CORAL_WALL_FAN, Blocks.BRAIN_CORAL_WALL_FAN, Blocks.BUBBLE_CORAL_WALL_FAN, Blocks.FIRE_CORAL_WALL_FAN, Blocks.HORN_CORAL_WALL_FAN };
	private static final Block[] CORALS_TAG = { Blocks.TUBE_CORAL, Blocks.BRAIN_CORAL, Blocks.BUBBLE_CORAL, Blocks.FIRE_CORAL, Blocks.HORN_CORAL, Blocks.TUBE_CORAL_FAN, Blocks.BRAIN_CORAL_FAN, Blocks.BUBBLE_CORAL_FAN, Blocks.FIRE_CORAL_FAN, Blocks.HORN_CORAL_FAN };

	public static final Map<String, Block[]> OTG_BLOCK_TAGS = new HashMap<>(); 

	public static void init()
	{
		// Tags used for OTG configs
		// Since Spigot doesn't appear to allow registering custom tags, we have to implement our own tags logic :/.			
		// TODO: We should be including these via datapack and make sure we don't use tags before datapacks are loaded.

		OTG_BLOCK_TAGS.put("stone", new Block[]
		{
			Blocks.STONE,
			Blocks.GRANITE,
			Blocks.DIORITE,
			Blocks.ANDESITE
		});
		
		OTG_BLOCK_TAGS.put("dirt", new Block[]
		{
			Blocks.DIRT,
			Blocks.COARSE_DIRT,
			Blocks.PODZOL,
		});		
		
		OTG_BLOCK_TAGS.put("stained_clay", new Block[]
		{
			Blocks.WHITE_TERRACOTTA,
			Blocks.ORANGE_TERRACOTTA,
			Blocks.MAGENTA_TERRACOTTA,
			Blocks.LIGHT_BLUE_TERRACOTTA,
			Blocks.YELLOW_TERRACOTTA,
			Blocks.LIME_TERRACOTTA,
			Blocks.PINK_TERRACOTTA,
			Blocks.GRAY_TERRACOTTA,
			Blocks.LIGHT_GRAY_TERRACOTTA,
			Blocks.CYAN_TERRACOTTA,
			Blocks.PURPLE_TERRACOTTA,
			Blocks.BLUE_TERRACOTTA,
			Blocks.BROWN_TERRACOTTA,
			Blocks.GREEN_TERRACOTTA,
			Blocks.RED_TERRACOTTA,
			Blocks.BLACK_TERRACOTTA,	
		});
		
		OTG_BLOCK_TAGS.put("log", new Block[]
		{
			Blocks.DARK_OAK_LOG,
			Blocks.DARK_OAK_WOOD,
			Blocks.STRIPPED_DARK_OAK_LOG,
			Blocks.STRIPPED_DARK_OAK_WOOD,
			Blocks.OAK_LOG,
			Blocks.OAK_WOOD,
			Blocks.STRIPPED_OAK_LOG,
			Blocks.STRIPPED_OAK_WOOD,
			Blocks.ACACIA_LOG,
			Blocks.ACACIA_WOOD,
			Blocks.STRIPPED_ACACIA_LOG,
			Blocks.STRIPPED_ACACIA_WOOD,
			Blocks.BIRCH_LOG,
			Blocks.BIRCH_WOOD,
			Blocks.STRIPPED_BIRCH_LOG,
			Blocks.STRIPPED_BIRCH_WOOD,
			Blocks.JUNGLE_LOG,
			Blocks.STRIPPED_JUNGLE_LOG,
			Blocks.STRIPPED_JUNGLE_WOOD,
			Blocks.SPRUCE_LOG,
			Blocks.SPRUCE_WOOD,
			Blocks.STRIPPED_SPRUCE_LOG,
			Blocks.STRIPPED_SPRUCE_WOOD,
			Blocks.CRIMSON_STEM,
			Blocks.STRIPPED_CRIMSON_STEM,
			Blocks.CRIMSON_HYPHAE,
			Blocks.STRIPPED_CRIMSON_HYPHAE,
			Blocks.WARPED_STEM,
			Blocks.STRIPPED_WARPED_STEM,
			Blocks.WARPED_HYPHAE,
			Blocks.STRIPPED_WARPED_HYPHAE,
		});
		
		OTG_BLOCK_TAGS.put("air", new Block[]
		{
			Blocks.AIR,
			Blocks.CAVE_AIR,
		});
		
		OTG_BLOCK_TAGS.put("sandstone", new Block[]
		{
			Blocks.SANDSTONE,
			Blocks.CHISELED_SANDSTONE,
			Blocks.SMOOTH_SANDSTONE,				
		});
		
		OTG_BLOCK_TAGS.put("red_sandstone", new Block[]
		{
			Blocks.RED_SANDSTONE,
			Blocks.CHISELED_RED_SANDSTONE,
			Blocks.SMOOTH_RED_SANDSTONE,				
		});
		
		OTG_BLOCK_TAGS.put("long_grass", new Block[]
		{
			Blocks.DEAD_BUSH,
			Blocks.TALL_GRASS,
			Blocks.FERN,				
		});
		
		OTG_BLOCK_TAGS.put("red_flower", new Block[]
		{
			Blocks.POPPY,
			Blocks.BLUE_ORCHID,
			Blocks.ALLIUM,
			Blocks.AZURE_BLUET,
			Blocks.RED_TULIP,
			Blocks.ORANGE_TULIP,
			Blocks.WHITE_TULIP,
			Blocks.PINK_TULIP,
			Blocks.OXEYE_DAISY,
		});
		
		OTG_BLOCK_TAGS.put("quartz_block", new Block[]
		{
			Blocks.QUARTZ_BLOCK,
			Blocks.CHISELED_QUARTZ_BLOCK,
			Blocks.QUARTZ_PILLAR,
		});
		
		OTG_BLOCK_TAGS.put("prismarine", new Block[]
		{
			Blocks.PRISMARINE,
			Blocks.PRISMARINE_BRICKS,
			Blocks.DARK_PRISMARINE,
		});
		
		OTG_BLOCK_TAGS.put("concrete", new Block[]
		{
			Blocks.WHITE_CONCRETE,
			Blocks.ORANGE_CONCRETE,
			Blocks.MAGENTA_CONCRETE,
			Blocks.LIGHT_BLUE_CONCRETE,
			Blocks.YELLOW_CONCRETE,
			Blocks.LIME_CONCRETE,
			Blocks.PINK_CONCRETE,
			Blocks.GRAY_CONCRETE,
			Blocks.LIGHT_GRAY_CONCRETE,
			Blocks.CYAN_CONCRETE,
			Blocks.PURPLE_CONCRETE,
			Blocks.BLUE_CONCRETE,
			Blocks.BROWN_CONCRETE,
			Blocks.GREEN_CONCRETE,
			Blocks.RED_CONCRETE,
			Blocks.BLACK_CONCRETE,
		});

		// Coral
		CORAL_BLOCKS = Arrays.stream(CORAL_BLOCKS_TAG).map(block -> PaperMaterialData.ofBlockData(block.getBlockData())).collect(Collectors.toList());
		WALL_CORALS = Arrays.stream(WALL_CORALS_TAG).map(block -> PaperMaterialData.ofBlockData(block.getBlockData())).collect(Collectors.toList());
		CORALS = Arrays.stream(CORALS_TAG).map(block -> PaperMaterialData.ofBlockData(block.getBlockData())).collect(Collectors.toList());

		// Blocks used in OTG code

		AIR = PaperMaterialData.ofBlockData(Blocks.AIR.getBlockData());
		CAVE_AIR = PaperMaterialData.ofBlockData(Blocks.CAVE_AIR.getBlockData());
		STRUCTURE_VOID = PaperMaterialData.ofBlockData(Blocks.STRUCTURE_VOID.getBlockData());
		COMMAND_BLOCK = PaperMaterialData.ofBlockData(Blocks.COMMAND_BLOCK.getBlockData());
		STRUCTURE_BLOCK = PaperMaterialData.ofBlockData(Blocks.STRUCTURE_BLOCK.getBlockData());
		GRASS = PaperMaterialData.ofBlockData(Blocks.GRASS_BLOCK.getBlockData());
		DIRT = PaperMaterialData.ofBlockData(Blocks.DIRT.getBlockData());
		CLAY = PaperMaterialData.ofBlockData(Blocks.CLAY.getBlockData());
		TERRACOTTA = PaperMaterialData.ofBlockData(Blocks.TERRACOTTA.getBlockData());
		WHITE_TERRACOTTA = PaperMaterialData.ofBlockData(Blocks.WHITE_TERRACOTTA.getBlockData());
		ORANGE_TERRACOTTA = PaperMaterialData.ofBlockData(Blocks.ORANGE_TERRACOTTA.getBlockData());
		YELLOW_TERRACOTTA = PaperMaterialData.ofBlockData(Blocks.YELLOW_TERRACOTTA.getBlockData());
		BROWN_TERRACOTTA = PaperMaterialData.ofBlockData(Blocks.BROWN_TERRACOTTA.getBlockData());
		RED_TERRACOTTA = PaperMaterialData.ofBlockData(Blocks.RED_TERRACOTTA.getBlockData());
		SILVER_TERRACOTTA = PaperMaterialData.ofBlockData(Blocks.LIGHT_GRAY_TERRACOTTA.getBlockData());
		STONE = PaperMaterialData.ofBlockData(Blocks.STONE.getBlockData());
		SAND = PaperMaterialData.ofBlockData(Blocks.SAND.getBlockData());
		RED_SAND = PaperMaterialData.ofBlockData(Blocks.RED_SAND.getBlockData());
		SANDSTONE = PaperMaterialData.ofBlockData(Blocks.SANDSTONE.getBlockData());
		RED_SANDSTONE = PaperMaterialData.ofBlockData(Blocks.RED_SANDSTONE.getBlockData());
		GRAVEL = PaperMaterialData.ofBlockData(Blocks.GRAVEL.getBlockData());
		MOSSY_COBBLESTONE = PaperMaterialData.ofBlockData(Blocks.MOSSY_COBBLESTONE.getBlockData());
		SNOW = PaperMaterialData.ofBlockData(Blocks.SNOW.getBlockData());
		SNOW_BLOCK = PaperMaterialData.ofBlockData(Blocks.SNOW_BLOCK.getBlockData());
		TORCH = PaperMaterialData.ofBlockData(Blocks.TORCH.getBlockData());
		BEDROCK = PaperMaterialData.ofBlockData(Blocks.BEDROCK.getBlockData());
		MAGMA = PaperMaterialData.ofBlockData(Blocks.MAGMA_BLOCK.getBlockData());
		ICE = PaperMaterialData.ofBlockData(Blocks.ICE.getBlockData());
		PACKED_ICE = PaperMaterialData.ofBlockData(Blocks.PACKED_ICE.getBlockData());
		BLUE_ICE = PaperMaterialData.ofBlockData(Blocks.BLUE_ICE.getBlockData());
		FROSTED_ICE = PaperMaterialData.ofBlockData(Blocks.FROSTED_ICE.getBlockData());
		GLOWSTONE = PaperMaterialData.ofBlockData(Blocks.GLOWSTONE.getBlockData());
		MYCELIUM = PaperMaterialData.ofBlockData(Blocks.MYCELIUM.getBlockData());
		STONE_SLAB = PaperMaterialData.ofBlockData(Blocks.STONE_SLAB.getBlockData());

		// Liquids
		WATER = PaperMaterialData.ofBlockData(Blocks.WATER.getBlockData());
		LAVA = PaperMaterialData.ofBlockData(Blocks.LAVA.getBlockData());

		// Trees
		ACACIA_LOG = PaperMaterialData.ofBlockData(Blocks.ACACIA_LOG.getBlockData());
		BIRCH_LOG = PaperMaterialData.ofBlockData(Blocks.BIRCH_LOG.getBlockData());
		DARK_OAK_LOG = PaperMaterialData.ofBlockData(Blocks.DARK_OAK_LOG.getBlockData());
		OAK_LOG = PaperMaterialData.ofBlockData(Blocks.OAK_LOG.getBlockData());
		SPRUCE_LOG = PaperMaterialData.ofBlockData(Blocks.SPRUCE_LOG.getBlockData());
		ACACIA_WOOD = PaperMaterialData.ofBlockData(Blocks.ACACIA_WOOD.getBlockData());
		BIRCH_WOOD = PaperMaterialData.ofBlockData(Blocks.BIRCH_WOOD.getBlockData());
		DARK_OAK_WOOD = PaperMaterialData.ofBlockData(Blocks.DARK_OAK_WOOD.getBlockData());
		OAK_WOOD = PaperMaterialData.ofBlockData(Blocks.OAK_WOOD.getBlockData());
		SPRUCE_WOOD = PaperMaterialData.ofBlockData(Blocks.SPRUCE_WOOD.getBlockData());
		STRIPPED_ACACIA_LOG = PaperMaterialData.ofBlockData(Blocks.STRIPPED_ACACIA_LOG.getBlockData());
		STRIPPED_BIRCH_LOG = PaperMaterialData.ofBlockData(Blocks.STRIPPED_BIRCH_LOG.getBlockData());
		STRIPPED_DARK_OAK_LOG = PaperMaterialData.ofBlockData(Blocks.STRIPPED_DARK_OAK_LOG.getBlockData());
		STRIPPED_JUNGLE_LOG = PaperMaterialData.ofBlockData(Blocks.STRIPPED_JUNGLE_LOG.getBlockData());
		STRIPPED_OAK_LOG = PaperMaterialData.ofBlockData(Blocks.STRIPPED_OAK_LOG.getBlockData());
		STRIPPED_SPRUCE_LOG = PaperMaterialData.ofBlockData(Blocks.STRIPPED_SPRUCE_LOG.getBlockData());

		ACACIA_LEAVES = PaperMaterialData.ofBlockData(Blocks.ACACIA_LEAVES.getBlockData());
		BIRCH_LEAVES = PaperMaterialData.ofBlockData(Blocks.BIRCH_LEAVES.getBlockData());
		DARK_OAK_LEAVES = PaperMaterialData.ofBlockData(Blocks.DARK_OAK_LEAVES.getBlockData());
		JUNGLE_LEAVES = PaperMaterialData.ofBlockData(Blocks.JUNGLE_LEAVES.getBlockData());
		OAK_LEAVES = PaperMaterialData.ofBlockData(Blocks.OAK_LEAVES.getBlockData());
		SPRUCE_LEAVES = PaperMaterialData.ofBlockData(Blocks.SPRUCE_LEAVES.getBlockData());

		// Plants
		POPPY = PaperMaterialData.ofBlockData(Blocks.POPPY.getBlockData());
		BLUE_ORCHID = PaperMaterialData.ofBlockData(Blocks.BLUE_ORCHID.getBlockData());
		ALLIUM = PaperMaterialData.ofBlockData(Blocks.ALLIUM.getBlockData());
		AZURE_BLUET = PaperMaterialData.ofBlockData(Blocks.AZURE_BLUET.getBlockData());
		RED_TULIP = PaperMaterialData.ofBlockData(Blocks.RED_TULIP.getBlockData());
		ORANGE_TULIP = PaperMaterialData.ofBlockData(Blocks.ORANGE_TULIP.getBlockData());
		WHITE_TULIP = PaperMaterialData.ofBlockData(Blocks.WHITE_TULIP.getBlockData());
		PINK_TULIP = PaperMaterialData.ofBlockData(Blocks.PINK_TULIP.getBlockData());
		OXEYE_DAISY = PaperMaterialData.ofBlockData(Blocks.OXEYE_DAISY.getBlockData());
		YELLOW_FLOWER = PaperMaterialData.ofBlockData(Blocks.DANDELION.getBlockData());
		DEAD_BUSH = PaperMaterialData.ofBlockData(Blocks.DEAD_BUSH.getBlockData());
		FERN = PaperMaterialData.ofBlockData(Blocks.FERN.getBlockData());
		LONG_GRASS = PaperMaterialData.ofBlockData(Blocks.GRASS.getBlockData());
		
		RED_MUSHROOM_BLOCK = PaperMaterialData.ofBlockData(Blocks.RED_MUSHROOM_BLOCK.getBlockData());
		BROWN_MUSHROOM_BLOCK = PaperMaterialData.ofBlockData(Blocks.BROWN_MUSHROOM_BLOCK.getBlockData());
		RED_MUSHROOM = PaperMaterialData.ofBlockData(Blocks.RED_MUSHROOM.getBlockData());
		BROWN_MUSHROOM = PaperMaterialData.ofBlockData(Blocks.BROWN_MUSHROOM.getBlockData());

		DOUBLE_TALL_GRASS_LOWER = PaperMaterialData.ofBlockData(Blocks.TALL_GRASS.getBlockData().set(BlockTallPlant.HALF, BlockPropertyDoubleBlockHalf.LOWER));
		DOUBLE_TALL_GRASS_UPPER = PaperMaterialData.ofBlockData(Blocks.TALL_GRASS.getBlockData().set(BlockTallPlant.HALF, BlockPropertyDoubleBlockHalf.UPPER));
		LARGE_FERN_LOWER = PaperMaterialData.ofBlockData(Blocks.LARGE_FERN.getBlockData().set(BlockTallPlant.HALF, BlockPropertyDoubleBlockHalf.LOWER));
		LARGE_FERN_UPPER = PaperMaterialData.ofBlockData(Blocks.LARGE_FERN.getBlockData().set(BlockTallPlant.HALF, BlockPropertyDoubleBlockHalf.UPPER));
		LILAC_LOWER = PaperMaterialData.ofBlockData(Blocks.LILAC.getBlockData().set(BlockTallPlant.HALF, BlockPropertyDoubleBlockHalf.LOWER));
		LILAC_UPPER = PaperMaterialData.ofBlockData(Blocks.LILAC.getBlockData().set(BlockTallPlant.HALF, BlockPropertyDoubleBlockHalf.UPPER));
		PEONY_LOWER = PaperMaterialData.ofBlockData(Blocks.PEONY.getBlockData().set(BlockTallPlant.HALF, BlockPropertyDoubleBlockHalf.LOWER));
		PEONY_UPPER = PaperMaterialData.ofBlockData(Blocks.PEONY.getBlockData().set(BlockTallPlant.HALF, BlockPropertyDoubleBlockHalf.UPPER));
		ROSE_BUSH_LOWER = PaperMaterialData.ofBlockData(Blocks.ROSE_BUSH.getBlockData().set(BlockTallPlant.HALF, BlockPropertyDoubleBlockHalf.LOWER));
		ROSE_BUSH_UPPER = PaperMaterialData.ofBlockData(Blocks.ROSE_BUSH.getBlockData().set(BlockTallPlant.HALF, BlockPropertyDoubleBlockHalf.UPPER));
		SUNFLOWER_LOWER = PaperMaterialData.ofBlockData(Blocks.SUNFLOWER.getBlockData().set(BlockTallPlant.HALF, BlockPropertyDoubleBlockHalf.LOWER));
		SUNFLOWER_UPPER = PaperMaterialData.ofBlockData(Blocks.SUNFLOWER.getBlockData().set(BlockTallPlant.HALF, BlockPropertyDoubleBlockHalf.UPPER));

		ACACIA_SAPLING = PaperMaterialData.ofBlockData(Blocks.ACACIA_SAPLING.getBlockData().set(BlockSapling.STAGE, 1));
		BAMBOO_SAPLING = PaperMaterialData.ofBlockData(Blocks.BAMBOO_SAPLING.getBlockData());
		BIRCH_SAPLING = PaperMaterialData.ofBlockData(Blocks.BIRCH_SAPLING.getBlockData().set(BlockSapling.STAGE, 1));
		DARK_OAK_SAPLING = PaperMaterialData.ofBlockData(Blocks.DARK_OAK_SAPLING.getBlockData().set(BlockSapling.STAGE, 1));
		JUNGLE_SAPLING = PaperMaterialData.ofBlockData(Blocks.JUNGLE_SAPLING.getBlockData().set(BlockSapling.STAGE, 1));
		OAK_SAPLING = PaperMaterialData.ofBlockData(Blocks.OAK_SAPLING.getBlockData().set(BlockSapling.STAGE, 1));
		SPRUCE_SAPLING = PaperMaterialData.ofBlockData(Blocks.SPRUCE_SAPLING.getBlockData().set(BlockSapling.STAGE, 1));

		PUMPKIN = PaperMaterialData.ofBlockData(Blocks.PUMPKIN.getBlockData());
		CACTUS = PaperMaterialData.ofBlockData(Blocks.CACTUS.getBlockData());
		MELON_BLOCK = PaperMaterialData.ofBlockData(Blocks.MELON.getBlockData());
		VINE = PaperMaterialData.ofBlockData(Blocks.VINE.getBlockData());
		WATER_LILY = PaperMaterialData.ofBlockData(Blocks.LILY_PAD.getBlockData());
		SUGAR_CANE_BLOCK = PaperMaterialData.ofBlockData(Blocks.SUGAR_CANE.getBlockData());
		IBlockData bambooState = Blocks.BAMBOO.getBlockData().set(BlockBamboo.d, 1).set(BlockBamboo.e, BlockPropertyBambooSize.NONE).set(BlockBamboo.f, 0);
		BAMBOO = PaperMaterialData.ofBlockData(bambooState);
		BAMBOO_SMALL = PaperMaterialData.ofBlockData(bambooState.set(BlockBamboo.e, BlockPropertyBambooSize.SMALL));
		BAMBOO_LARGE = PaperMaterialData.ofBlockData(bambooState.set(BlockBamboo.e, BlockPropertyBambooSize.LARGE));
		BAMBOO_LARGE_GROWING = PaperMaterialData.ofBlockData(bambooState.set(BlockBamboo.e, BlockPropertyBambooSize.LARGE).set(BlockBamboo.f, 1));
		PODZOL = PaperMaterialData.ofBlockData(Blocks.PODZOL.getBlockData());
		SEAGRASS = PaperMaterialData.ofBlockData(Blocks.SEAGRASS.getBlockData());
		TALL_SEAGRASS_LOWER = PaperMaterialData.ofBlockData(Blocks.TALL_SEAGRASS.getBlockData().set(BlockTallSeaGrass.b, BlockPropertyDoubleBlockHalf.LOWER));
		TALL_SEAGRASS_UPPER = PaperMaterialData.ofBlockData(Blocks.TALL_SEAGRASS.getBlockData().set(BlockTallSeaGrass.b, BlockPropertyDoubleBlockHalf.UPPER));
		KELP = PaperMaterialData.ofBlockData(Blocks.KELP.getBlockData());
		KELP_PLANT = PaperMaterialData.ofBlockData(Blocks.KELP_PLANT.getBlockData());
		VINE_SOUTH = PaperMaterialData.ofBlockData(Blocks.VINE.getBlockData().set(BlockVine.SOUTH, true));
		VINE_NORTH = PaperMaterialData.ofBlockData(Blocks.VINE.getBlockData().set(BlockVine.NORTH, true));
		VINE_WEST = PaperMaterialData.ofBlockData(Blocks.VINE.getBlockData().set(BlockVine.WEST, true));
		VINE_EAST = PaperMaterialData.ofBlockData(Blocks.VINE.getBlockData().set(BlockVine.EAST, true));
		SEA_PICKLE = PaperMaterialData.ofBlockData(Blocks.SEA_PICKLE.getBlockData());

		// Ores
		COAL_ORE = PaperMaterialData.ofBlockData(Blocks.PODZOL.getBlockData());
		DIAMOND_ORE = PaperMaterialData.ofBlockData(Blocks.DIAMOND_ORE.getBlockData());
		EMERALD_ORE = PaperMaterialData.ofBlockData(Blocks.EMERALD_ORE.getBlockData());
		GOLD_ORE = PaperMaterialData.ofBlockData(Blocks.GOLD_ORE.getBlockData());
		IRON_ORE = PaperMaterialData.ofBlockData(Blocks.IRON_ORE.getBlockData());
		LAPIS_ORE = PaperMaterialData.ofBlockData(Blocks.LAPIS_ORE.getBlockData());
		QUARTZ_ORE = PaperMaterialData.ofBlockData(Blocks.NETHER_QUARTZ_ORE.getBlockData());
		REDSTONE_ORE = PaperMaterialData.ofBlockData(Blocks.REDSTONE_ORE.getBlockData());

		// Ore blocks
		GOLD_BLOCK = PaperMaterialData.ofBlockData(Blocks.GOLD_BLOCK.getBlockData());
		IRON_BLOCK = PaperMaterialData.ofBlockData(Blocks.IRON_BLOCK.getBlockData());
		REDSTONE_BLOCK = PaperMaterialData.ofBlockData(Blocks.REDSTONE_BLOCK.getBlockData());
		DIAMOND_BLOCK = PaperMaterialData.ofBlockData(Blocks.DIAMOND_BLOCK.getBlockData());
		LAPIS_BLOCK = PaperMaterialData.ofBlockData(Blocks.LAPIS_BLOCK.getBlockData());
		COAL_BLOCK = PaperMaterialData.ofBlockData(Blocks.COAL_BLOCK.getBlockData());
		QUARTZ_BLOCK = PaperMaterialData.ofBlockData(Blocks.QUARTZ_BLOCK.getBlockData());
		EMERALD_BLOCK = PaperMaterialData.ofBlockData(Blocks.EMERALD_BLOCK.getBlockData());
	}
}
