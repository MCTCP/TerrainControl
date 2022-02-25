package com.pg85.otg.forge.materials;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.util.materials.LocalMaterials;

import net.minecraft.world.level.block.state.properties.BambooLeaves;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.tags.BlockTags;
import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;
import java.util.HashSet;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import net.minecraft.world.level.block.BambooBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.TallSeagrassBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;

public class ForgeMaterials extends LocalMaterials
{
	// Default blocks in given tags
	// Tags aren't loaded until datapacks are loaded, on world creation. We mirror the vanilla copy of the tag to solve this.
	private static final Block[] CORAL_BLOCKS_TAG = { Blocks.TUBE_CORAL_BLOCK, Blocks.BRAIN_CORAL_BLOCK, Blocks.BUBBLE_CORAL_BLOCK, Blocks.FIRE_CORAL_BLOCK, Blocks.HORN_CORAL_BLOCK };
	private static final Block[] WALL_CORALS_TAG = { Blocks.TUBE_CORAL_WALL_FAN, Blocks.BRAIN_CORAL_WALL_FAN, Blocks.BUBBLE_CORAL_WALL_FAN, Blocks.FIRE_CORAL_WALL_FAN, Blocks.HORN_CORAL_WALL_FAN };
	private static final Block[] CORALS_TAG = { Blocks.TUBE_CORAL, Blocks.BRAIN_CORAL, Blocks.BUBBLE_CORAL, Blocks.FIRE_CORAL, Blocks.HORN_CORAL, Blocks.TUBE_CORAL_FAN, Blocks.BRAIN_CORAL_FAN, Blocks.BUBBLE_CORAL_FAN, Blocks.FIRE_CORAL_FAN, Blocks.HORN_CORAL_FAN };

	@SuppressWarnings("serial")
	public static void init()
	{
		// Tags used for OTG configs
		// TODO: We should be including these via datapack and make sure we don't use tags before datapacks are loaded.
		
		BlockTags.createOptional(new ResourceLocation(Constants.MOD_ID_SHORT, "stone"), new HashSet<Supplier<Block>>()
		{{
			add(() -> { return Blocks.STONE; });
			add(() -> { return Blocks.GRANITE; });
			add(() -> { return Blocks.DIORITE; });
			add(() -> { return Blocks.ANDESITE; });
		}});

		BlockTags.createOptional(new ResourceLocation(Constants.MOD_ID_SHORT, "dirt"), new HashSet<Supplier<Block>>()
		{{
			add(() -> { return Blocks.DIRT; });
			add(() -> { return Blocks.COARSE_DIRT; });
			add(() -> { return Blocks.PODZOL; });			
		}});

		BlockTags.createOptional(new ResourceLocation(Constants.MOD_ID_SHORT, "stained_clay"), new HashSet<Supplier<Block>>()
		{{		
			add(() -> { return Blocks.WHITE_TERRACOTTA; });
			add(() -> { return Blocks.ORANGE_TERRACOTTA; });
			add(() -> { return Blocks.MAGENTA_TERRACOTTA; });
			add(() -> { return Blocks.LIGHT_BLUE_TERRACOTTA; });
			add(() -> { return Blocks.YELLOW_TERRACOTTA; });
			add(() -> { return Blocks.LIME_TERRACOTTA; });
			add(() -> { return Blocks.PINK_TERRACOTTA; });
			add(() -> { return Blocks.GRAY_TERRACOTTA; });
			add(() -> { return Blocks.LIGHT_GRAY_TERRACOTTA; });
			add(() -> { return Blocks.CYAN_TERRACOTTA; });
			add(() -> { return Blocks.PURPLE_TERRACOTTA; });
			add(() -> { return Blocks.BLUE_TERRACOTTA; });
			add(() -> { return Blocks.BROWN_TERRACOTTA; });
			add(() -> { return Blocks.GREEN_TERRACOTTA; });
			add(() -> { return Blocks.RED_TERRACOTTA; });
			add(() -> { return Blocks.BLACK_TERRACOTTA; });			
		}});

		BlockTags.createOptional(new ResourceLocation(Constants.MOD_ID_SHORT, "log"), new HashSet<Supplier<Block>>()
		{{
			add(() -> { return Blocks.DARK_OAK_LOG; });
			add(() -> { return Blocks.DARK_OAK_WOOD; });
			add(() -> { return Blocks.STRIPPED_DARK_OAK_LOG; });
			add(() -> { return Blocks.STRIPPED_DARK_OAK_WOOD; });
			add(() -> { return Blocks.OAK_LOG; });
			add(() -> { return Blocks.OAK_WOOD; });
			add(() -> { return Blocks.STRIPPED_OAK_LOG; });
			add(() -> { return Blocks.STRIPPED_OAK_WOOD; });
			add(() -> { return Blocks.ACACIA_LOG; });
			add(() -> { return Blocks.ACACIA_WOOD; });
			add(() -> { return Blocks.STRIPPED_ACACIA_LOG; });
			add(() -> { return Blocks.STRIPPED_ACACIA_WOOD; });
			add(() -> { return Blocks.BIRCH_LOG; });
			add(() -> { return Blocks.BIRCH_WOOD; });
			add(() -> { return Blocks.STRIPPED_BIRCH_LOG; });
			add(() -> { return Blocks.STRIPPED_BIRCH_WOOD; });
			add(() -> { return Blocks.JUNGLE_LOG; });
			add(() -> { return Blocks.STRIPPED_JUNGLE_LOG; });
			add(() -> { return Blocks.STRIPPED_JUNGLE_WOOD; });
			add(() -> { return Blocks.SPRUCE_LOG; });
			add(() -> { return Blocks.SPRUCE_WOOD; });
			add(() -> { return Blocks.STRIPPED_SPRUCE_LOG; });
			add(() -> { return Blocks.STRIPPED_SPRUCE_WOOD; });
			add(() -> { return Blocks.CRIMSON_STEM; });
			add(() -> { return Blocks.STRIPPED_CRIMSON_STEM; });
			add(() -> { return Blocks.CRIMSON_HYPHAE; });
			add(() -> { return Blocks.STRIPPED_CRIMSON_HYPHAE; });
			add(() -> { return Blocks.WARPED_STEM; });
			add(() -> { return Blocks.STRIPPED_WARPED_STEM; });
			add(() -> { return Blocks.WARPED_HYPHAE; });
			add(() -> { return Blocks.STRIPPED_WARPED_HYPHAE; });
		}});
		
		BlockTags.createOptional(new ResourceLocation(Constants.MOD_ID_SHORT, "air"), new HashSet<Supplier<Block>>()
		{{
			add(() -> { return Blocks.AIR; });
			add(() -> { return Blocks.CAVE_AIR; });
		}});
		
		BlockTags.createOptional(new ResourceLocation(Constants.MOD_ID_SHORT, "sandstone"), new HashSet<Supplier<Block>>()
		{{
			add(() -> { return Blocks.SANDSTONE; });
			add(() -> { return Blocks.CHISELED_SANDSTONE; });
			add(() -> { return Blocks.SMOOTH_SANDSTONE; });
		}});
		
		BlockTags.createOptional(new ResourceLocation(Constants.MOD_ID_SHORT, "red_sandstone"), new HashSet<Supplier<Block>>()
		{{
			add(() -> { return Blocks.RED_SANDSTONE; });
			add(() -> { return Blocks.CHISELED_RED_SANDSTONE; });
			add(() -> { return Blocks.SMOOTH_RED_SANDSTONE; });
		}});
		
		BlockTags.createOptional(new ResourceLocation(Constants.MOD_ID_SHORT, "long_grass"), new HashSet<Supplier<Block>>()
		{{		
			add(() -> { return Blocks.DEAD_BUSH; });
			add(() -> { return Blocks.TALL_GRASS; });
			add(() -> { return Blocks.FERN; });
		}});

		BlockTags.createOptional(new ResourceLocation(Constants.MOD_ID_SHORT, "red_flower"), new HashSet<Supplier<Block>>()
		{{		
			add(() -> { return Blocks.POPPY; });
			add(() -> { return Blocks.BLUE_ORCHID; });
			add(() -> { return Blocks.ALLIUM; });
			add(() -> { return Blocks.AZURE_BLUET; });
			add(() -> { return Blocks.RED_TULIP; });
			add(() -> { return Blocks.ORANGE_TULIP; });
			add(() -> { return Blocks.WHITE_TULIP; });
			add(() -> { return Blocks.PINK_TULIP; });
			add(() -> { return Blocks.OXEYE_DAISY; });
		}});

		BlockTags.createOptional(new ResourceLocation(Constants.MOD_ID_SHORT, "quartz_block"), new HashSet<Supplier<Block>>()
		{{
			add(() -> { return Blocks.QUARTZ_BLOCK; });
			add(() -> { return Blocks.CHISELED_QUARTZ_BLOCK; });
			add(() -> { return Blocks.QUARTZ_PILLAR; });
		}});

		BlockTags.createOptional(new ResourceLocation(Constants.MOD_ID_SHORT, "prismarine"), new HashSet<Supplier<Block>>()
		{{
			add(() -> { return Blocks.PRISMARINE; });
			add(() -> { return Blocks.PRISMARINE_BRICKS; });
			add(() -> { return Blocks.DARK_PRISMARINE; });
		}});

		BlockTags.createOptional(new ResourceLocation(Constants.MOD_ID_SHORT, "concrete"), new HashSet<Supplier<Block>>()
		{{
			add(() -> { return Blocks.WHITE_CONCRETE; });
			add(() -> { return Blocks.ORANGE_CONCRETE; });
			add(() -> { return Blocks.MAGENTA_CONCRETE; });
			add(() -> { return Blocks.LIGHT_BLUE_CONCRETE; });
			add(() -> { return Blocks.YELLOW_CONCRETE; });
			add(() -> { return Blocks.LIME_CONCRETE; });
			add(() -> { return Blocks.PINK_CONCRETE; });
			add(() -> { return Blocks.GRAY_CONCRETE; });
			add(() -> { return Blocks.LIGHT_GRAY_CONCRETE; });
			add(() -> { return Blocks.CYAN_CONCRETE; });
			add(() -> { return Blocks.PURPLE_CONCRETE; });
			add(() -> { return Blocks.BLUE_CONCRETE; });
			add(() -> { return Blocks.BROWN_CONCRETE; });
			add(() -> { return Blocks.GREEN_CONCRETE; });
			add(() -> { return Blocks.RED_CONCRETE; });
			add(() -> { return Blocks.BLACK_CONCRETE; });
		}});

		// Coral
		CORAL_BLOCKS = Arrays.stream(CORAL_BLOCKS_TAG).map(block -> ForgeMaterialData.ofBlockState(block.defaultBlockState())).collect(Collectors.toList());
		WALL_CORALS = Arrays.stream(WALL_CORALS_TAG).map(block -> ForgeMaterialData.ofBlockState(block.defaultBlockState())).collect(Collectors.toList());
		CORALS = Arrays.stream(CORALS_TAG).map(block -> ForgeMaterialData.ofBlockState(block.defaultBlockState())).collect(Collectors.toList());
				
		// Blocks used in OTG code
		
		AIR = ForgeMaterialData.ofBlockState(Blocks.AIR.defaultBlockState());
		CAVE_AIR = ForgeMaterialData.ofBlockState(Blocks.CAVE_AIR.defaultBlockState());
		STRUCTURE_VOID = ForgeMaterialData.ofBlockState(Blocks.STRUCTURE_VOID.defaultBlockState());
		COMMAND_BLOCK = ForgeMaterialData.ofBlockState(Blocks.COMMAND_BLOCK.defaultBlockState());
		STRUCTURE_BLOCK = ForgeMaterialData.ofBlockState(Blocks.STRUCTURE_BLOCK.defaultBlockState());
		GRASS = ForgeMaterialData.ofBlockState(Blocks.GRASS.defaultBlockState());
		DIRT = ForgeMaterialData.ofBlockState(Blocks.DIRT.defaultBlockState());
		CLAY = ForgeMaterialData.ofBlockState(Blocks.CLAY.defaultBlockState());
		TERRACOTTA = ForgeMaterialData.ofBlockState(Blocks.TERRACOTTA.defaultBlockState());
		WHITE_TERRACOTTA = ForgeMaterialData.ofBlockState(Blocks.WHITE_TERRACOTTA.defaultBlockState());
		ORANGE_TERRACOTTA = ForgeMaterialData.ofBlockState(Blocks.ORANGE_TERRACOTTA.defaultBlockState());
		YELLOW_TERRACOTTA = ForgeMaterialData.ofBlockState(Blocks.YELLOW_TERRACOTTA.defaultBlockState());
		BROWN_TERRACOTTA = ForgeMaterialData.ofBlockState(Blocks.BROWN_TERRACOTTA.defaultBlockState());
		RED_TERRACOTTA = ForgeMaterialData.ofBlockState(Blocks.RED_TERRACOTTA.defaultBlockState());
		SILVER_TERRACOTTA = ForgeMaterialData.ofBlockState(Blocks.LIGHT_GRAY_TERRACOTTA.defaultBlockState());
		STONE = ForgeMaterialData.ofBlockState(Blocks.STONE.defaultBlockState());
		DEEPSLATE = ForgeMaterialData.ofBlockState(Blocks.DEEPSLATE.defaultBlockState());
		SAND = ForgeMaterialData.ofBlockState(Blocks.SAND.defaultBlockState());
		RED_SAND = ForgeMaterialData.ofBlockState(Blocks.RED_SAND.defaultBlockState());
		SANDSTONE = ForgeMaterialData.ofBlockState(Blocks.SANDSTONE.defaultBlockState());
		RED_SANDSTONE = ForgeMaterialData.ofBlockState(Blocks.RED_SANDSTONE.defaultBlockState());
		GRAVEL = ForgeMaterialData.ofBlockState(Blocks.GRAVEL.defaultBlockState());
		MOSSY_COBBLESTONE = ForgeMaterialData.ofBlockState(Blocks.MOSSY_COBBLESTONE.defaultBlockState());
		SNOW = ForgeMaterialData.ofBlockState(Blocks.SNOW.defaultBlockState());
		SNOW_BLOCK = ForgeMaterialData.ofBlockState(Blocks.SNOW_BLOCK.defaultBlockState());
		TORCH = ForgeMaterialData.ofBlockState(Blocks.TORCH.defaultBlockState());
		BEDROCK = ForgeMaterialData.ofBlockState(Blocks.BEDROCK.defaultBlockState());
		MAGMA = ForgeMaterialData.ofBlockState(Blocks.MAGMA_BLOCK.defaultBlockState());
		ICE = ForgeMaterialData.ofBlockState(Blocks.ICE.defaultBlockState());
		PACKED_ICE = ForgeMaterialData.ofBlockState(Blocks.PACKED_ICE.defaultBlockState());
		BLUE_ICE = ForgeMaterialData.ofBlockState(Blocks.BLUE_ICE.defaultBlockState());
		FROSTED_ICE = ForgeMaterialData.ofBlockState(Blocks.FROSTED_ICE.defaultBlockState());
		GLOWSTONE = ForgeMaterialData.ofBlockState(Blocks.GLOWSTONE.defaultBlockState());
		MYCELIUM = ForgeMaterialData.ofBlockState(Blocks.MYCELIUM.defaultBlockState());
		STONE_SLAB = ForgeMaterialData.ofBlockState(Blocks.STONE_SLAB.defaultBlockState());

		// Liquids
		WATER = ForgeMaterialData.ofBlockState(Blocks.WATER.defaultBlockState());
		LAVA = ForgeMaterialData.ofBlockState(Blocks.LAVA.defaultBlockState());

		// Trees
		ACACIA_LOG = ForgeMaterialData.ofBlockState(Blocks.ACACIA_LOG.defaultBlockState());
		BIRCH_LOG = ForgeMaterialData.ofBlockState(Blocks.BIRCH_LOG.defaultBlockState());
		DARK_OAK_LOG = ForgeMaterialData.ofBlockState(Blocks.DARK_OAK_LOG.defaultBlockState());
		OAK_LOG = ForgeMaterialData.ofBlockState(Blocks.OAK_LOG.defaultBlockState());
		SPRUCE_LOG = ForgeMaterialData.ofBlockState(Blocks.SPRUCE_LOG.defaultBlockState());
		ACACIA_WOOD = ForgeMaterialData.ofBlockState(Blocks.ACACIA_WOOD.defaultBlockState());
		BIRCH_WOOD = ForgeMaterialData.ofBlockState(Blocks.BIRCH_WOOD.defaultBlockState());
		DARK_OAK_WOOD = ForgeMaterialData.ofBlockState(Blocks.DARK_OAK_WOOD.defaultBlockState());
		OAK_WOOD = ForgeMaterialData.ofBlockState(Blocks.OAK_WOOD.defaultBlockState());
		SPRUCE_WOOD = ForgeMaterialData.ofBlockState(Blocks.SPRUCE_WOOD.defaultBlockState());			
		STRIPPED_ACACIA_LOG = ForgeMaterialData.ofBlockState(Blocks.STRIPPED_ACACIA_LOG.defaultBlockState());
		STRIPPED_BIRCH_LOG = ForgeMaterialData.ofBlockState(Blocks.STRIPPED_BIRCH_LOG.defaultBlockState());
		STRIPPED_DARK_OAK_LOG = ForgeMaterialData.ofBlockState(Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
		STRIPPED_JUNGLE_LOG = ForgeMaterialData.ofBlockState(Blocks.STRIPPED_JUNGLE_LOG.defaultBlockState());
		STRIPPED_OAK_LOG = ForgeMaterialData.ofBlockState(Blocks.STRIPPED_OAK_LOG.defaultBlockState());
		STRIPPED_SPRUCE_LOG = ForgeMaterialData.ofBlockState(Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState());
		
		ACACIA_LEAVES = ForgeMaterialData.ofBlockState(Blocks.ACACIA_LEAVES.defaultBlockState());
		BIRCH_LEAVES = ForgeMaterialData.ofBlockState(Blocks.BIRCH_LEAVES.defaultBlockState());
		DARK_OAK_LEAVES = ForgeMaterialData.ofBlockState(Blocks.DARK_OAK_LEAVES.defaultBlockState());
		JUNGLE_LEAVES = ForgeMaterialData.ofBlockState(Blocks.JUNGLE_LEAVES.defaultBlockState());
		OAK_LEAVES = ForgeMaterialData.ofBlockState(Blocks.OAK_LEAVES.defaultBlockState());
		SPRUCE_LEAVES = ForgeMaterialData.ofBlockState(Blocks.SPRUCE_LEAVES.defaultBlockState());

		// Plants
		POPPY = ForgeMaterialData.ofBlockState(Blocks.POPPY.defaultBlockState());
		BLUE_ORCHID = ForgeMaterialData.ofBlockState(Blocks.BLUE_ORCHID.defaultBlockState());
		ALLIUM = ForgeMaterialData.ofBlockState(Blocks.ALLIUM.defaultBlockState());
		AZURE_BLUET = ForgeMaterialData.ofBlockState(Blocks.AZURE_BLUET.defaultBlockState());
		RED_TULIP = ForgeMaterialData.ofBlockState(Blocks.RED_TULIP.defaultBlockState());
		ORANGE_TULIP = ForgeMaterialData.ofBlockState(Blocks.ORANGE_TULIP.defaultBlockState());
		WHITE_TULIP = ForgeMaterialData.ofBlockState(Blocks.WHITE_TULIP.defaultBlockState());
		PINK_TULIP = ForgeMaterialData.ofBlockState(Blocks.PINK_TULIP.defaultBlockState());
		OXEYE_DAISY = ForgeMaterialData.ofBlockState(Blocks.OXEYE_DAISY.defaultBlockState());		
		YELLOW_FLOWER = ForgeMaterialData.ofBlockState(Blocks.DANDELION.defaultBlockState());
		DEAD_BUSH = ForgeMaterialData.ofBlockState(Blocks.DEAD_BUSH.defaultBlockState());
		FERN = ForgeMaterialData.ofBlockState(Blocks.FERN.defaultBlockState());
		LONG_GRASS = ForgeMaterialData.ofBlockState(Blocks.GRASS.defaultBlockState());
		
		RED_MUSHROOM_BLOCK = ForgeMaterialData.ofBlockState(Blocks.RED_MUSHROOM_BLOCK.defaultBlockState());
		BROWN_MUSHROOM_BLOCK = ForgeMaterialData.ofBlockState(Blocks.BROWN_MUSHROOM_BLOCK.defaultBlockState());		
		RED_MUSHROOM = ForgeMaterialData.ofBlockState(Blocks.RED_MUSHROOM.defaultBlockState());
		BROWN_MUSHROOM = ForgeMaterialData.ofBlockState(Blocks.BROWN_MUSHROOM.defaultBlockState());

		DOUBLE_TALL_GRASS_LOWER = ForgeMaterialData.ofBlockState(Blocks.TALL_GRASS.defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER));
		DOUBLE_TALL_GRASS_UPPER = ForgeMaterialData.ofBlockState(Blocks.TALL_GRASS.defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER));
		LARGE_FERN_LOWER = ForgeMaterialData.ofBlockState(Blocks.LARGE_FERN.defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER));
		LARGE_FERN_UPPER = ForgeMaterialData.ofBlockState(Blocks.LARGE_FERN.defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER));		
		LILAC_LOWER = ForgeMaterialData.ofBlockState(Blocks.LILAC.defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER));
		LILAC_UPPER = ForgeMaterialData.ofBlockState(Blocks.LILAC.defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER));		
		PEONY_LOWER = ForgeMaterialData.ofBlockState(Blocks.PEONY.defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER));
		PEONY_UPPER = ForgeMaterialData.ofBlockState(Blocks.PEONY.defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER));		
		ROSE_BUSH_LOWER = ForgeMaterialData.ofBlockState(Blocks.ROSE_BUSH.defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER));
		ROSE_BUSH_UPPER = ForgeMaterialData.ofBlockState(Blocks.ROSE_BUSH.defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER));	
		SUNFLOWER_LOWER = ForgeMaterialData.ofBlockState(Blocks.SUNFLOWER.defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER));
		SUNFLOWER_UPPER = ForgeMaterialData.ofBlockState(Blocks.SUNFLOWER.defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER));

		ACACIA_SAPLING = ForgeMaterialData.ofBlockState(Blocks.ACACIA_SAPLING.defaultBlockState());
		BAMBOO_SAPLING = ForgeMaterialData.ofBlockState(Blocks.BAMBOO_SAPLING.defaultBlockState());
		BIRCH_SAPLING = ForgeMaterialData.ofBlockState(Blocks.BIRCH_SAPLING.defaultBlockState());
		DARK_OAK_SAPLING = ForgeMaterialData.ofBlockState(Blocks.DARK_OAK_SAPLING.defaultBlockState());
		JUNGLE_SAPLING = ForgeMaterialData.ofBlockState(Blocks.JUNGLE_SAPLING.defaultBlockState());
		OAK_SAPLING = ForgeMaterialData.ofBlockState(Blocks.OAK_SAPLING.defaultBlockState());
		SPRUCE_SAPLING = ForgeMaterialData.ofBlockState(Blocks.SPRUCE_SAPLING.defaultBlockState());
		
		PUMPKIN = ForgeMaterialData.ofBlockState(Blocks.PUMPKIN.defaultBlockState());
		CACTUS = ForgeMaterialData.ofBlockState(Blocks.CACTUS.defaultBlockState());
		MELON_BLOCK = ForgeMaterialData.ofBlockState(Blocks.MELON.defaultBlockState());
		VINE = ForgeMaterialData.ofBlockState(Blocks.VINE.defaultBlockState());
		WATER_LILY = ForgeMaterialData.ofBlockState(Blocks.LILY_PAD.defaultBlockState());
		SUGAR_CANE_BLOCK = ForgeMaterialData.ofBlockState(Blocks.SUGAR_CANE.defaultBlockState());
		BlockState bambooState = Blocks.BAMBOO.defaultBlockState().setValue(BambooBlock.AGE, 1).setValue(BambooBlock.LEAVES, BambooLeaves.NONE).setValue(BambooBlock.STAGE, 0);
		BAMBOO = ForgeMaterialData.ofBlockState(bambooState);
		BAMBOO_SMALL = ForgeMaterialData.ofBlockState(bambooState.setValue(BambooBlock.LEAVES, BambooLeaves.SMALL));
		BAMBOO_LARGE = ForgeMaterialData.ofBlockState(bambooState.setValue(BambooBlock.LEAVES, BambooLeaves.LARGE));
		BAMBOO_LARGE_GROWING = ForgeMaterialData.ofBlockState(bambooState.setValue(BambooBlock.LEAVES, BambooLeaves.LARGE).setValue(BambooBlock.STAGE, 1));
		PODZOL = ForgeMaterialData.ofBlockState(Blocks.PODZOL.defaultBlockState());
		SEAGRASS = ForgeMaterialData.ofBlockState(Blocks.SEAGRASS.defaultBlockState());
		TALL_SEAGRASS_LOWER = ForgeMaterialData.ofBlockState(Blocks.TALL_SEAGRASS.defaultBlockState().setValue(TallSeagrassBlock.HALF, DoubleBlockHalf.LOWER));
		TALL_SEAGRASS_UPPER = ForgeMaterialData.ofBlockState(Blocks.TALL_SEAGRASS.defaultBlockState().setValue(TallSeagrassBlock.HALF, DoubleBlockHalf.UPPER));
		KELP = ForgeMaterialData.ofBlockState(Blocks.KELP.defaultBlockState());
		KELP_PLANT = ForgeMaterialData.ofBlockState(Blocks.KELP_PLANT.defaultBlockState());
		VINE_SOUTH = ForgeMaterialData.ofBlockState(Blocks.VINE.defaultBlockState().setValue(VineBlock.SOUTH, true));
		VINE_NORTH = ForgeMaterialData.ofBlockState(Blocks.VINE.defaultBlockState().setValue(VineBlock.NORTH, true));
		VINE_WEST = ForgeMaterialData.ofBlockState(Blocks.VINE.defaultBlockState().setValue(VineBlock.WEST, true));
		VINE_EAST = ForgeMaterialData.ofBlockState(Blocks.VINE.defaultBlockState().setValue(VineBlock.EAST, true));
		SEA_PICKLE = ForgeMaterialData.ofBlockState(Blocks.SEA_PICKLE.defaultBlockState());

		// Ores
		COAL_ORE = ForgeMaterialData.ofBlockState(Blocks.COAL_ORE.defaultBlockState());
		DIAMOND_ORE = ForgeMaterialData.ofBlockState(Blocks.DIAMOND_ORE.defaultBlockState());
		EMERALD_ORE = ForgeMaterialData.ofBlockState(Blocks.EMERALD_ORE.defaultBlockState());
		GOLD_ORE = ForgeMaterialData.ofBlockState(Blocks.GOLD_ORE.defaultBlockState());
		IRON_ORE = ForgeMaterialData.ofBlockState(Blocks.IRON_ORE.defaultBlockState());
		LAPIS_ORE = ForgeMaterialData.ofBlockState(Blocks.LAPIS_ORE.defaultBlockState());
		QUARTZ_ORE = ForgeMaterialData.ofBlockState(Blocks.NETHER_QUARTZ_ORE.defaultBlockState());
		REDSTONE_ORE = ForgeMaterialData.ofBlockState(Blocks.REDSTONE_ORE.defaultBlockState());

		// Ore blocks
		GOLD_BLOCK = ForgeMaterialData.ofBlockState(Blocks.GOLD_BLOCK.defaultBlockState());
		IRON_BLOCK = ForgeMaterialData.ofBlockState(Blocks.IRON_BLOCK.defaultBlockState());
		REDSTONE_BLOCK = ForgeMaterialData.ofBlockState(Blocks.REDSTONE_BLOCK.defaultBlockState());
		DIAMOND_BLOCK = ForgeMaterialData.ofBlockState(Blocks.DIAMOND_BLOCK.defaultBlockState());
		LAPIS_BLOCK = ForgeMaterialData.ofBlockState(Blocks.LAPIS_BLOCK.defaultBlockState());
		COAL_BLOCK = ForgeMaterialData.ofBlockState(Blocks.COAL_BLOCK.defaultBlockState());
		QUARTZ_BLOCK = ForgeMaterialData.ofBlockState(Blocks.QUARTZ_BLOCK.defaultBlockState());
		EMERALD_BLOCK = ForgeMaterialData.ofBlockState(Blocks.EMERALD_BLOCK.defaultBlockState());
	}
}
