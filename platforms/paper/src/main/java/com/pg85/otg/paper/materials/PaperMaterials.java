package com.pg85.otg.paper.materials;

import com.pg85.otg.util.materials.LocalMaterials;

import net.minecraft.world.level.block.BambooBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.TallSeagrassBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BambooLeaves;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

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
		CORAL_BLOCKS = Arrays.stream(CORAL_BLOCKS_TAG).map(block -> PaperMaterialData.ofBlockData(block.defaultBlockState())).collect(Collectors.toList());
		WALL_CORALS = Arrays.stream(WALL_CORALS_TAG).map(block -> PaperMaterialData.ofBlockData(block.defaultBlockState())).collect(Collectors.toList());
		CORALS = Arrays.stream(CORALS_TAG).map(block -> PaperMaterialData.ofBlockData(block.defaultBlockState())).collect(Collectors.toList());

		// Blocks used in OTG code

		AIR = PaperMaterialData.ofBlockData(Blocks.AIR.defaultBlockState());
		CAVE_AIR = PaperMaterialData.ofBlockData(Blocks.CAVE_AIR.defaultBlockState());
		STRUCTURE_VOID = PaperMaterialData.ofBlockData(Blocks.STRUCTURE_VOID.defaultBlockState());
		COMMAND_BLOCK = PaperMaterialData.ofBlockData(Blocks.COMMAND_BLOCK.defaultBlockState());
		STRUCTURE_BLOCK = PaperMaterialData.ofBlockData(Blocks.STRUCTURE_BLOCK.defaultBlockState());
		GRASS = PaperMaterialData.ofBlockData(Blocks.GRASS_BLOCK.defaultBlockState());
		DIRT = PaperMaterialData.ofBlockData(Blocks.DIRT.defaultBlockState());
		CLAY = PaperMaterialData.ofBlockData(Blocks.CLAY.defaultBlockState());
		TERRACOTTA = PaperMaterialData.ofBlockData(Blocks.TERRACOTTA.defaultBlockState());
		WHITE_TERRACOTTA = PaperMaterialData.ofBlockData(Blocks.WHITE_TERRACOTTA.defaultBlockState());
		ORANGE_TERRACOTTA = PaperMaterialData.ofBlockData(Blocks.ORANGE_TERRACOTTA.defaultBlockState());
		YELLOW_TERRACOTTA = PaperMaterialData.ofBlockData(Blocks.YELLOW_TERRACOTTA.defaultBlockState());
		BROWN_TERRACOTTA = PaperMaterialData.ofBlockData(Blocks.BROWN_TERRACOTTA.defaultBlockState());
		RED_TERRACOTTA = PaperMaterialData.ofBlockData(Blocks.RED_TERRACOTTA.defaultBlockState());
		SILVER_TERRACOTTA = PaperMaterialData.ofBlockData(Blocks.LIGHT_GRAY_TERRACOTTA.defaultBlockState());
		STONE = PaperMaterialData.ofBlockData(Blocks.STONE.defaultBlockState());
		SAND = PaperMaterialData.ofBlockData(Blocks.SAND.defaultBlockState());
		RED_SAND = PaperMaterialData.ofBlockData(Blocks.RED_SAND.defaultBlockState());
		SANDSTONE = PaperMaterialData.ofBlockData(Blocks.SANDSTONE.defaultBlockState());
		RED_SANDSTONE = PaperMaterialData.ofBlockData(Blocks.RED_SANDSTONE.defaultBlockState());
		GRAVEL = PaperMaterialData.ofBlockData(Blocks.GRAVEL.defaultBlockState());
		MOSSY_COBBLESTONE = PaperMaterialData.ofBlockData(Blocks.MOSSY_COBBLESTONE.defaultBlockState());
		SNOW = PaperMaterialData.ofBlockData(Blocks.SNOW.defaultBlockState());
		SNOW_BLOCK = PaperMaterialData.ofBlockData(Blocks.SNOW_BLOCK.defaultBlockState());
		TORCH = PaperMaterialData.ofBlockData(Blocks.TORCH.defaultBlockState());
		BEDROCK = PaperMaterialData.ofBlockData(Blocks.BEDROCK.defaultBlockState());
		MAGMA = PaperMaterialData.ofBlockData(Blocks.MAGMA_BLOCK.defaultBlockState());
		ICE = PaperMaterialData.ofBlockData(Blocks.ICE.defaultBlockState());
		PACKED_ICE = PaperMaterialData.ofBlockData(Blocks.PACKED_ICE.defaultBlockState());
		BLUE_ICE = PaperMaterialData.ofBlockData(Blocks.BLUE_ICE.defaultBlockState());
		FROSTED_ICE = PaperMaterialData.ofBlockData(Blocks.FROSTED_ICE.defaultBlockState());
		GLOWSTONE = PaperMaterialData.ofBlockData(Blocks.GLOWSTONE.defaultBlockState());
		MYCELIUM = PaperMaterialData.ofBlockData(Blocks.MYCELIUM.defaultBlockState());
		STONE_SLAB = PaperMaterialData.ofBlockData(Blocks.STONE_SLAB.defaultBlockState());
		AMETHYST_BLOCK = PaperMaterialData.ofBlockData(Blocks.AMETHYST_BLOCK.defaultBlockState());
		BUDDING_AMETHYST = PaperMaterialData.ofBlockData(Blocks.BUDDING_AMETHYST.defaultBlockState());
		CALCITE = PaperMaterialData.ofBlockData(Blocks.CALCITE.defaultBlockState());
		SMOOTH_BASALT = PaperMaterialData.ofBlockData(Blocks.SMOOTH_BASALT.defaultBlockState());
		SMALL_AMETHYST_BUD = PaperMaterialData.ofBlockData(Blocks.SMALL_AMETHYST_BUD.defaultBlockState());
		MEDIUM_AMETHYST_BUD = PaperMaterialData.ofBlockData(Blocks.MEDIUM_AMETHYST_BUD.defaultBlockState());
		LARGE_AMETHYST_BUD = PaperMaterialData.ofBlockData(Blocks.LARGE_AMETHYST_BUD.defaultBlockState());
		AMETHYST_CLUSTER = PaperMaterialData.ofBlockData(Blocks.AMETHYST_CLUSTER.defaultBlockState());
		GRANITE = PaperMaterialData.ofBlockData(Blocks.GRANITE.defaultBlockState());
		TUFF = PaperMaterialData.ofBlockData(Blocks.TUFF.defaultBlockState());

		// Liquids
		WATER = PaperMaterialData.ofBlockData(Blocks.WATER.defaultBlockState());
		LAVA = PaperMaterialData.ofBlockData(Blocks.LAVA.defaultBlockState());

		// Trees
		ACACIA_LOG = PaperMaterialData.ofBlockData(Blocks.ACACIA_LOG.defaultBlockState());
		BIRCH_LOG = PaperMaterialData.ofBlockData(Blocks.BIRCH_LOG.defaultBlockState());
		DARK_OAK_LOG = PaperMaterialData.ofBlockData(Blocks.DARK_OAK_LOG.defaultBlockState());
		OAK_LOG = PaperMaterialData.ofBlockData(Blocks.OAK_LOG.defaultBlockState());
		SPRUCE_LOG = PaperMaterialData.ofBlockData(Blocks.SPRUCE_LOG.defaultBlockState());
		ACACIA_WOOD = PaperMaterialData.ofBlockData(Blocks.ACACIA_WOOD.defaultBlockState());
		BIRCH_WOOD = PaperMaterialData.ofBlockData(Blocks.BIRCH_WOOD.defaultBlockState());
		DARK_OAK_WOOD = PaperMaterialData.ofBlockData(Blocks.DARK_OAK_WOOD.defaultBlockState());
		OAK_WOOD = PaperMaterialData.ofBlockData(Blocks.OAK_WOOD.defaultBlockState());
		SPRUCE_WOOD = PaperMaterialData.ofBlockData(Blocks.SPRUCE_WOOD.defaultBlockState());
		STRIPPED_ACACIA_LOG = PaperMaterialData.ofBlockData(Blocks.STRIPPED_ACACIA_LOG.defaultBlockState());
		STRIPPED_BIRCH_LOG = PaperMaterialData.ofBlockData(Blocks.STRIPPED_BIRCH_LOG.defaultBlockState());
		STRIPPED_DARK_OAK_LOG = PaperMaterialData.ofBlockData(Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
		STRIPPED_JUNGLE_LOG = PaperMaterialData.ofBlockData(Blocks.STRIPPED_JUNGLE_LOG.defaultBlockState());
		STRIPPED_OAK_LOG = PaperMaterialData.ofBlockData(Blocks.STRIPPED_OAK_LOG.defaultBlockState());
		STRIPPED_SPRUCE_LOG = PaperMaterialData.ofBlockData(Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState());

		ACACIA_LEAVES = PaperMaterialData.ofBlockData(Blocks.ACACIA_LEAVES.defaultBlockState());
		BIRCH_LEAVES = PaperMaterialData.ofBlockData(Blocks.BIRCH_LEAVES.defaultBlockState());
		DARK_OAK_LEAVES = PaperMaterialData.ofBlockData(Blocks.DARK_OAK_LEAVES.defaultBlockState());
		JUNGLE_LEAVES = PaperMaterialData.ofBlockData(Blocks.JUNGLE_LEAVES.defaultBlockState());
		OAK_LEAVES = PaperMaterialData.ofBlockData(Blocks.OAK_LEAVES.defaultBlockState());
		SPRUCE_LEAVES = PaperMaterialData.ofBlockData(Blocks.SPRUCE_LEAVES.defaultBlockState());

		// Plants
		POPPY = PaperMaterialData.ofBlockData(Blocks.POPPY.defaultBlockState());
		BLUE_ORCHID = PaperMaterialData.ofBlockData(Blocks.BLUE_ORCHID.defaultBlockState());
		ALLIUM = PaperMaterialData.ofBlockData(Blocks.ALLIUM.defaultBlockState());
		AZURE_BLUET = PaperMaterialData.ofBlockData(Blocks.AZURE_BLUET.defaultBlockState());
		RED_TULIP = PaperMaterialData.ofBlockData(Blocks.RED_TULIP.defaultBlockState());
		ORANGE_TULIP = PaperMaterialData.ofBlockData(Blocks.ORANGE_TULIP.defaultBlockState());
		WHITE_TULIP = PaperMaterialData.ofBlockData(Blocks.WHITE_TULIP.defaultBlockState());
		PINK_TULIP = PaperMaterialData.ofBlockData(Blocks.PINK_TULIP.defaultBlockState());
		OXEYE_DAISY = PaperMaterialData.ofBlockData(Blocks.OXEYE_DAISY.defaultBlockState());
		YELLOW_FLOWER = PaperMaterialData.ofBlockData(Blocks.DANDELION.defaultBlockState());
		DEAD_BUSH = PaperMaterialData.ofBlockData(Blocks.DEAD_BUSH.defaultBlockState());
		FERN = PaperMaterialData.ofBlockData(Blocks.FERN.defaultBlockState());
		LONG_GRASS = PaperMaterialData.ofBlockData(Blocks.GRASS.defaultBlockState());
		
		RED_MUSHROOM_BLOCK = PaperMaterialData.ofBlockData(Blocks.RED_MUSHROOM_BLOCK.defaultBlockState());
		BROWN_MUSHROOM_BLOCK = PaperMaterialData.ofBlockData(Blocks.BROWN_MUSHROOM_BLOCK.defaultBlockState());
		RED_MUSHROOM = PaperMaterialData.ofBlockData(Blocks.RED_MUSHROOM.defaultBlockState());
		BROWN_MUSHROOM = PaperMaterialData.ofBlockData(Blocks.BROWN_MUSHROOM.defaultBlockState());

		DOUBLE_TALL_GRASS_LOWER = PaperMaterialData.ofBlockData(Blocks.TALL_GRASS.defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER));
		DOUBLE_TALL_GRASS_UPPER = PaperMaterialData.ofBlockData(Blocks.TALL_GRASS.defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER));
		LARGE_FERN_LOWER = PaperMaterialData.ofBlockData(Blocks.LARGE_FERN.defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER));
		LARGE_FERN_UPPER = PaperMaterialData.ofBlockData(Blocks.LARGE_FERN.defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER));
		LILAC_LOWER = PaperMaterialData.ofBlockData(Blocks.LILAC.defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER));
		LILAC_UPPER = PaperMaterialData.ofBlockData(Blocks.LILAC.defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER));
		PEONY_LOWER = PaperMaterialData.ofBlockData(Blocks.PEONY.defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER));
		PEONY_UPPER = PaperMaterialData.ofBlockData(Blocks.PEONY.defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER));
		ROSE_BUSH_LOWER = PaperMaterialData.ofBlockData(Blocks.ROSE_BUSH.defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER));
		ROSE_BUSH_UPPER = PaperMaterialData.ofBlockData(Blocks.ROSE_BUSH.defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER));
		SUNFLOWER_LOWER = PaperMaterialData.ofBlockData(Blocks.SUNFLOWER.defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER));
		SUNFLOWER_UPPER = PaperMaterialData.ofBlockData(Blocks.SUNFLOWER.defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER));

		ACACIA_SAPLING = PaperMaterialData.ofBlockData(Blocks.ACACIA_SAPLING.defaultBlockState().setValue(SaplingBlock.STAGE, 1));
		BAMBOO_SAPLING = PaperMaterialData.ofBlockData(Blocks.BAMBOO_SAPLING.defaultBlockState());
		BIRCH_SAPLING = PaperMaterialData.ofBlockData(Blocks.BIRCH_SAPLING.defaultBlockState().setValue(SaplingBlock.STAGE, 1));
		DARK_OAK_SAPLING = PaperMaterialData.ofBlockData(Blocks.DARK_OAK_SAPLING.defaultBlockState().setValue(SaplingBlock.STAGE, 1));
		JUNGLE_SAPLING = PaperMaterialData.ofBlockData(Blocks.JUNGLE_SAPLING.defaultBlockState().setValue(SaplingBlock.STAGE, 1));
		OAK_SAPLING = PaperMaterialData.ofBlockData(Blocks.OAK_SAPLING.defaultBlockState().setValue(SaplingBlock.STAGE, 1));
		SPRUCE_SAPLING = PaperMaterialData.ofBlockData(Blocks.SPRUCE_SAPLING.defaultBlockState().setValue(SaplingBlock.STAGE, 1));

		PUMPKIN = PaperMaterialData.ofBlockData(Blocks.PUMPKIN.defaultBlockState());
		CACTUS = PaperMaterialData.ofBlockData(Blocks.CACTUS.defaultBlockState());
		MELON_BLOCK = PaperMaterialData.ofBlockData(Blocks.MELON.defaultBlockState());
		VINE = PaperMaterialData.ofBlockData(Blocks.VINE.defaultBlockState());
		WATER_LILY = PaperMaterialData.ofBlockData(Blocks.LILY_PAD.defaultBlockState());
		SUGAR_CANE_BLOCK = PaperMaterialData.ofBlockData(Blocks.SUGAR_CANE.defaultBlockState());
		BlockState bambooState = Blocks.BAMBOO.defaultBlockState().setValue(BambooBlock.AGE, 1).setValue(BambooBlock.LEAVES, BambooLeaves.NONE).setValue(BambooBlock.STAGE, 0);
		BAMBOO = PaperMaterialData.ofBlockData(bambooState);
		BAMBOO_SMALL = PaperMaterialData.ofBlockData(bambooState.setValue(BambooBlock.LEAVES, BambooLeaves.SMALL));
		BAMBOO_LARGE = PaperMaterialData.ofBlockData(bambooState.setValue(BambooBlock.LEAVES, BambooLeaves.LARGE));
		BAMBOO_LARGE_GROWING = PaperMaterialData.ofBlockData(bambooState.setValue(BambooBlock.LEAVES, BambooLeaves.LARGE).setValue(BambooBlock.STAGE, 1));
		PODZOL = PaperMaterialData.ofBlockData(Blocks.PODZOL.defaultBlockState());
		SEAGRASS = PaperMaterialData.ofBlockData(Blocks.SEAGRASS.defaultBlockState());
		TALL_SEAGRASS_LOWER = PaperMaterialData.ofBlockData(Blocks.TALL_SEAGRASS.defaultBlockState().setValue(TallSeagrassBlock.HALF, DoubleBlockHalf.LOWER));
		TALL_SEAGRASS_UPPER = PaperMaterialData.ofBlockData(Blocks.TALL_SEAGRASS.defaultBlockState().setValue(TallSeagrassBlock.HALF, DoubleBlockHalf.UPPER));
		KELP = PaperMaterialData.ofBlockData(Blocks.KELP.defaultBlockState());
		KELP_PLANT = PaperMaterialData.ofBlockData(Blocks.KELP_PLANT.defaultBlockState());
		VINE_SOUTH = PaperMaterialData.ofBlockData(Blocks.VINE.defaultBlockState().setValue(VineBlock.SOUTH, true));
		VINE_NORTH = PaperMaterialData.ofBlockData(Blocks.VINE.defaultBlockState().setValue(VineBlock.NORTH, true));
		VINE_WEST = PaperMaterialData.ofBlockData(Blocks.VINE.defaultBlockState().setValue(VineBlock.WEST, true));
		VINE_EAST = PaperMaterialData.ofBlockData(Blocks.VINE.defaultBlockState().setValue(VineBlock.EAST, true));
		SEA_PICKLE = PaperMaterialData.ofBlockData(Blocks.SEA_PICKLE.defaultBlockState());

		// Ores
		COAL_ORE = PaperMaterialData.ofBlockData(Blocks.PODZOL.defaultBlockState());
		DIAMOND_ORE = PaperMaterialData.ofBlockData(Blocks.DIAMOND_ORE.defaultBlockState());
		EMERALD_ORE = PaperMaterialData.ofBlockData(Blocks.EMERALD_ORE.defaultBlockState());
		GOLD_ORE = PaperMaterialData.ofBlockData(Blocks.GOLD_ORE.defaultBlockState());
		IRON_ORE = PaperMaterialData.ofBlockData(Blocks.IRON_ORE.defaultBlockState());
		COPPER_ORE = PaperMaterialData.ofBlockData(Blocks.COPPER_ORE.defaultBlockState());
		LAPIS_ORE = PaperMaterialData.ofBlockData(Blocks.LAPIS_ORE.defaultBlockState());
		QUARTZ_ORE = PaperMaterialData.ofBlockData(Blocks.NETHER_QUARTZ_ORE.defaultBlockState());
		REDSTONE_ORE = PaperMaterialData.ofBlockData(Blocks.REDSTONE_ORE.defaultBlockState());

		// Ore blocks
		GOLD_BLOCK = PaperMaterialData.ofBlockData(Blocks.GOLD_BLOCK.defaultBlockState());
		IRON_BLOCK = PaperMaterialData.ofBlockData(Blocks.IRON_BLOCK.defaultBlockState());
		REDSTONE_BLOCK = PaperMaterialData.ofBlockData(Blocks.REDSTONE_BLOCK.defaultBlockState());
		DIAMOND_BLOCK = PaperMaterialData.ofBlockData(Blocks.DIAMOND_BLOCK.defaultBlockState());
		LAPIS_BLOCK = PaperMaterialData.ofBlockData(Blocks.LAPIS_BLOCK.defaultBlockState());
		COAL_BLOCK = PaperMaterialData.ofBlockData(Blocks.COAL_BLOCK.defaultBlockState());
		QUARTZ_BLOCK = PaperMaterialData.ofBlockData(Blocks.QUARTZ_BLOCK.defaultBlockState());
		EMERALD_BLOCK = PaperMaterialData.ofBlockData(Blocks.EMERALD_BLOCK.defaultBlockState());
		RAW_IRON_BLOCK = PaperMaterialData.ofBlockData(Blocks.RAW_IRON_BLOCK.defaultBlockState());
		RAW_COPPER_BLOCK = PaperMaterialData.ofBlockData(Blocks.RAW_COPPER_BLOCK.defaultBlockState());
	}
}
