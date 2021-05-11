package com.pg85.otg.spigot.materials;

import com.pg85.otg.OTG;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.FifoMap;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.LocalMaterialTag;
import com.pg85.otg.util.materials.LocalMaterials;
import net.minecraft.server.v1_16_R3.*;

import java.util.Arrays;
import java.util.stream.Collectors;

public class SpigotMaterials extends LocalMaterials
{
	// Default blocks in given tags
	// Tags aren't loaded until datapacks are loaded, on world creation. We mirror the vanilla copy of the tag to solve this.
	private static final Block[] CORAL_BLOCKS_TAG = {Blocks.TUBE_CORAL_BLOCK, Blocks.BRAIN_CORAL_BLOCK, Blocks.BUBBLE_CORAL_BLOCK, Blocks.FIRE_CORAL_BLOCK, Blocks.HORN_CORAL_BLOCK};
	private static final Block[] WALL_CORALS_TAG = {Blocks.TUBE_CORAL_WALL_FAN, Blocks.BRAIN_CORAL_WALL_FAN, Blocks.BUBBLE_CORAL_WALL_FAN, Blocks.FIRE_CORAL_WALL_FAN, Blocks.HORN_CORAL_WALL_FAN};
	private static final Block[] CORALS_TAG = {Blocks.TUBE_CORAL, Blocks.BRAIN_CORAL, Blocks.BUBBLE_CORAL, Blocks.FIRE_CORAL, Blocks.HORN_CORAL, Blocks.TUBE_CORAL_FAN, Blocks.BRAIN_CORAL_FAN, Blocks.BUBBLE_CORAL_FAN, Blocks.FIRE_CORAL_FAN, Blocks.HORN_CORAL_FAN};


	// TODO: Smaller cache should be ok, only most frequently used should be cached?
	private static final FifoMap<String, LocalMaterialData> CachedMaterials = new FifoMap<>(4096);
	private static final FifoMap<String, LocalMaterialTag> CachedTags = new FifoMap<>(4096);

	static
	{
		// Blocks used in OTG code
		try
		{
			AIR = readMaterial(LocalMaterials.AIR_NAME);
			GRASS = readMaterial(LocalMaterials.GRASS_NAME);
			DIRT = readMaterial(LocalMaterials.DIRT_NAME);
			CLAY = readMaterial(LocalMaterials.CLAY_NAME);
			TERRACOTTA = readMaterial(LocalMaterials.TERRACOTTA_NAME);
			WHITE_TERRACOTTA = readMaterial(LocalMaterials.WHITE_TERRACOTTA_NAME);
			ORANGE_TERRACOTTA = readMaterial(LocalMaterials.ORANGE_TERRACOTTA_NAME);
			YELLOW_TERRACOTTA = readMaterial(LocalMaterials.YELLOW_TERRACOTTA_NAME);
			BROWN_TERRACOTTA = readMaterial(LocalMaterials.BROWN_TERRACOTTA_NAME);
			RED_TERRACOTTA = readMaterial(LocalMaterials.RED_TERRACOTTA_NAME);
			SILVER_TERRACOTTA = readMaterial(LocalMaterials.SILVER_TERRACOTTA_NAME);
			STONE = readMaterial(LocalMaterials.STONE_NAME);
			SAND = readMaterial(LocalMaterials.SAND_NAME);
			RED_SAND = readMaterial(LocalMaterials.RED_SAND_NAME);
			SANDSTONE = readMaterial(LocalMaterials.SANDSTONE_NAME);
			RED_SANDSTONE = readMaterial(LocalMaterials.RED_SANDSTONE_NAME);
			GRAVEL = readMaterial(LocalMaterials.GRAVEL_NAME);
			MOSSY_COBBLESTONE = readMaterial(LocalMaterials.MOSSY_COBBLESTONE_NAME);
			SNOW = readMaterial(LocalMaterials.SNOW_NAME);
			SNOW_BLOCK = readMaterial(LocalMaterials.SNOW_BLOCK_NAME);
			TORCH = readMaterial(LocalMaterials.TORCH_NAME);
			BEDROCK = readMaterial(LocalMaterials.BEDROCK_NAME);
			MAGMA = readMaterial(LocalMaterials.MAGMA_NAME);
			ICE = readMaterial(LocalMaterials.ICE_NAME);
			PACKED_ICE = readMaterial(LocalMaterials.PACKED_ICE_NAME);
			FROSTED_ICE = readMaterial(LocalMaterials.FROSTED_ICE_NAME);
			GLOWSTONE = readMaterial(LocalMaterials.GLOWSTONE_NAME);
			MYCELIUM = readMaterial(LocalMaterials.MYCELIUM_NAME);
			STONE_SLAB = readMaterial(LocalMaterials.STONE_SLAB_NAME);

			// Liquids
			WATER = readMaterial(LocalMaterials.WATER_NAME);
			LAVA = readMaterial(LocalMaterials.LAVA_NAME);

			// Trees
			ACACIA_LOG = readMaterial(LocalMaterials.ACACIA_LOG_NAME);
			BIRCH_LOG = readMaterial(LocalMaterials.BIRCH_LOG_NAME);
			DARK_OAK_LOG = readMaterial(LocalMaterials.DARK_OAK_LOG_NAME);
			OAK_LOG = readMaterial(LocalMaterials.OAK_LOG_NAME);
			SPRUCE_LOG = readMaterial(LocalMaterials.SPRUCE_LOG_NAME);
			STRIPPED_ACACIA_LOG = readMaterial(LocalMaterials.STRIPPED_ACACIA_LOG_NAME);
			STRIPPED_BIRCH_LOG = readMaterial(LocalMaterials.STRIPPED_BIRCH_LOG_NAME);
			STRIPPED_DARK_OAK_LOG = readMaterial(LocalMaterials.STRIPPED_DARK_OAK_LOG_NAME);
			STRIPPED_JUNGLE_LOG = readMaterial(LocalMaterials.STRIPPED_JUNGLE_LOG_NAME);
			STRIPPED_OAK_LOG = readMaterial(LocalMaterials.STRIPPED_OAK_LOG_NAME);
			STRIPPED_SPRUCE_LOG = readMaterial(LocalMaterials.STRIPPED_SPRUCE_LOG_NAME);

			ACACIA_LEAVES = readMaterial(LocalMaterials.ACACIA_LEAVES_NAME);
			BIRCH_LEAVES = readMaterial(LocalMaterials.BIRCH_LEAVES_NAME);
			DARK_OAK_LEAVES = readMaterial(LocalMaterials.DARK_OAK_LEAVES_NAME);
			JUNGLE_LEAVES = readMaterial(LocalMaterials.JUNGLE_LEAVES_NAME);
			OAK_LEAVES = readMaterial(LocalMaterials.OAK_LEAVES_NAME);
			SPRUCE_LEAVES = readMaterial(LocalMaterials.SPRUCE_LEAVES_NAME);

			// Plants
			POPPY = readMaterial(LocalMaterials.POPPY_NAME);
			BLUE_ORCHID = readMaterial(LocalMaterials.BLUE_ORCHID_NAME);
			ALLIUM = readMaterial(LocalMaterials.ALLIUM_NAME);
			AZURE_BLUET = readMaterial(LocalMaterials.AZURE_BLUET_NAME);
			RED_TULIP = readMaterial(LocalMaterials.RED_TULIP_NAME);
			ORANGE_TULIP = readMaterial(LocalMaterials.ORANGE_TULIP_NAME);
			WHITE_TULIP = readMaterial(LocalMaterials.WHITE_TULIP_NAME);
			PINK_TULIP = readMaterial(LocalMaterials.PINK_TULIP_NAME);
			OXEYE_DAISY = readMaterial(LocalMaterials.OXEYE_DAISY_NAME);
			BROWN_MUSHROOM = readMaterial(LocalMaterials.BROWN_MUSHROOM_NAME);
			YELLOW_FLOWER = readMaterial(LocalMaterials.YELLOW_FLOWER_NAME);
			DEAD_BUSH = readMaterial(LocalMaterials.DEAD_BUSH_NAME);
			LONG_GRASS = readMaterial(LocalMaterials.LONG_GRASS_NAME);
			RED_MUSHROOM = readMaterial(LocalMaterials.RED_MUSHROOM_NAME);

			DOUBLE_TALL_GRASS_LOWER = SpigotMaterialData.ofBlockData(((SpigotMaterialData) readMaterial(LocalMaterials.DOUBLE_TALL_GRASS_NAME)).internalBlock().set(BlockTallPlant.HALF, BlockPropertyDoubleBlockHalf.LOWER));
			DOUBLE_TALL_GRASS_UPPER = SpigotMaterialData.ofBlockData(((SpigotMaterialData) readMaterial(LocalMaterials.DOUBLE_TALL_GRASS_NAME)).internalBlock().set(BlockTallPlant.HALF, BlockPropertyDoubleBlockHalf.UPPER));
			LARGE_FERN_LOWER = SpigotMaterialData.ofBlockData(((SpigotMaterialData) readMaterial(LocalMaterials.LARGE_FERN_NAME)).internalBlock().set(BlockTallPlant.HALF, BlockPropertyDoubleBlockHalf.LOWER));
			LARGE_FERN_UPPER = SpigotMaterialData.ofBlockData(((SpigotMaterialData) readMaterial(LocalMaterials.LARGE_FERN_NAME)).internalBlock().set(BlockTallPlant.HALF, BlockPropertyDoubleBlockHalf.UPPER));
			LILAC_LOWER = SpigotMaterialData.ofBlockData(((SpigotMaterialData) readMaterial(LocalMaterials.LILAC_NAME)).internalBlock().set(BlockTallPlant.HALF, BlockPropertyDoubleBlockHalf.LOWER));
			LILAC_UPPER = SpigotMaterialData.ofBlockData(((SpigotMaterialData) readMaterial(LocalMaterials.LILAC_NAME)).internalBlock().set(BlockTallPlant.HALF, BlockPropertyDoubleBlockHalf.UPPER));
			PEONY_LOWER = SpigotMaterialData.ofBlockData(((SpigotMaterialData) readMaterial(LocalMaterials.PEONY_NAME)).internalBlock().set(BlockTallPlant.HALF, BlockPropertyDoubleBlockHalf.LOWER));
			PEONY_UPPER = SpigotMaterialData.ofBlockData(((SpigotMaterialData) readMaterial(LocalMaterials.PEONY_NAME)).internalBlock().set(BlockTallPlant.HALF, BlockPropertyDoubleBlockHalf.UPPER));
			ROSE_BUSH_LOWER = SpigotMaterialData.ofBlockData(((SpigotMaterialData) readMaterial(LocalMaterials.ROSE_BUSH_NAME)).internalBlock().set(BlockTallPlant.HALF, BlockPropertyDoubleBlockHalf.LOWER));
			ROSE_BUSH_UPPER = SpigotMaterialData.ofBlockData(((SpigotMaterialData) readMaterial(LocalMaterials.ROSE_BUSH_NAME)).internalBlock().set(BlockTallPlant.HALF, BlockPropertyDoubleBlockHalf.UPPER));
			SUNFLOWER_LOWER = SpigotMaterialData.ofBlockData(((SpigotMaterialData) readMaterial(LocalMaterials.SUNFLOWER_NAME)).internalBlock().set(BlockTallPlant.HALF, BlockPropertyDoubleBlockHalf.LOWER));
			SUNFLOWER_UPPER = SpigotMaterialData.ofBlockData(((SpigotMaterialData) readMaterial(LocalMaterials.SUNFLOWER_NAME)).internalBlock().set(BlockTallPlant.HALF, BlockPropertyDoubleBlockHalf.UPPER));

			PUMPKIN = readMaterial(LocalMaterials.PUMPKIN_NAME);
			CACTUS = readMaterial(LocalMaterials.CACTUS_NAME);
			MELON_BLOCK = readMaterial(LocalMaterials.MELON_BLOCK_NAME);
			VINE = readMaterial(LocalMaterials.VINE_NAME);
			SAPLING = readMaterial(LocalMaterials.SAPLING_NAME);
			WATER_LILY = readMaterial(LocalMaterials.WATER_LILY_NAME);
			SUGAR_CANE_BLOCK = readMaterial(LocalMaterials.SUGAR_CANE_BLOCK_NAME);
			IBlockData bambooState = Blocks.BAMBOO.getBlockData().set(BlockBamboo.d, 1).set(BlockBamboo.e, BlockPropertyBambooSize.NONE).set(BlockBamboo.f, 0);
			BAMBOO = SpigotMaterialData.ofBlockData(bambooState);
			BAMBOO_SMALL = SpigotMaterialData.ofBlockData(bambooState.set(BlockBamboo.e, BlockPropertyBambooSize.SMALL));
			BAMBOO_LARGE = SpigotMaterialData.ofBlockData(bambooState.set(BlockBamboo.e, BlockPropertyBambooSize.LARGE));
			BAMBOO_LARGE_GROWING = SpigotMaterialData.ofBlockData(bambooState.set(BlockBamboo.e, BlockPropertyBambooSize.LARGE).set(BlockBamboo.f, 1));
			PODZOL = readMaterial(LocalMaterials.PODZOL_NAME);
			SEAGRASS = SpigotMaterialData.ofBlockData(Blocks.SEAGRASS.getBlockData());
			TALL_SEAGRASS_LOWER = SpigotMaterialData.ofBlockData(Blocks.TALL_SEAGRASS.getBlockData().set(BlockTallSeaGrass.b, BlockPropertyDoubleBlockHalf.LOWER));
			TALL_SEAGRASS_UPPER = SpigotMaterialData.ofBlockData(Blocks.TALL_SEAGRASS.getBlockData().set(BlockTallSeaGrass.b, BlockPropertyDoubleBlockHalf.UPPER));
			KELP = SpigotMaterialData.ofBlockData(Blocks.KELP.getBlockData());
			KELP_PLANT = SpigotMaterialData.ofBlockData(Blocks.KELP_PLANT.getBlockData());
			VINE_SOUTH = SpigotMaterialData.ofBlockData(Blocks.VINE.getBlockData().set(BlockVine.SOUTH, true));
			VINE_NORTH = SpigotMaterialData.ofBlockData(Blocks.VINE.getBlockData().set(BlockVine.NORTH, true));
			VINE_WEST = SpigotMaterialData.ofBlockData(Blocks.VINE.getBlockData().set(BlockVine.WEST, true));
			VINE_EAST = SpigotMaterialData.ofBlockData(Blocks.VINE.getBlockData().set(BlockVine.EAST, true));
			SEA_PICKLE = SpigotMaterialData.ofBlockData(Blocks.SEA_PICKLE.getBlockData());

			// Coral
			CORAL_BLOCKS = Arrays.stream(CORAL_BLOCKS_TAG).map(block -> SpigotMaterialData.ofBlockData(block.getBlockData())).collect(Collectors.toList());
			WALL_CORALS = Arrays.stream(WALL_CORALS_TAG).map(block -> SpigotMaterialData.ofBlockData(block.getBlockData())).collect(Collectors.toList());
			CORALS = Arrays.stream(CORALS_TAG).map(block -> SpigotMaterialData.ofBlockData(block.getBlockData())).collect(Collectors.toList());

			// Ores
			COAL_ORE = readMaterial(LocalMaterials.COAL_ORE_NAME);
			DIAMOND_ORE = readMaterial(LocalMaterials.DIAMOND_ORE_NAME);
			EMERALD_ORE = readMaterial(LocalMaterials.EMERALD_ORE_NAME);
			GLOWING_REDSTONE_ORE = readMaterial(LocalMaterials.GLOWING_REDSTONE_ORE_NAME);
			GOLD_ORE = readMaterial(LocalMaterials.GOLD_ORE_NAME);
			IRON_ORE = readMaterial(LocalMaterials.IRON_ORE_NAME);
			LAPIS_ORE = readMaterial(LocalMaterials.LAPIS_ORE_NAME);
			QUARTZ_ORE = readMaterial(LocalMaterials.QUARTZ_ORE_NAME);
			REDSTONE_ORE = readMaterial(LocalMaterials.REDSTONE_ORE_NAME);

			// Ore blocks
			GOLD_BLOCK = readMaterial(LocalMaterials.GOLD_BLOCK_NAME);
			IRON_BLOCK = readMaterial(LocalMaterials.IRON_BLOCK_NAME);
			REDSTONE_BLOCK = readMaterial(LocalMaterials.REDSTONE_BLOCK_NAME);
			DIAMOND_BLOCK = readMaterial(LocalMaterials.DIAMOND_BLOCK_NAME);
			LAPIS_BLOCK = readMaterial(LocalMaterials.LAPIS_BLOCK_NAME);
			COAL_BLOCK = readMaterial(LocalMaterials.COAL_BLOCK_NAME);
			QUARTZ_BLOCK = readMaterial(LocalMaterials.QUARTZ_BLOCK_NAME);
			EMERALD_BLOCK = readMaterial(LocalMaterials.EMERALD_BLOCK_NAME);
		}
		catch (InvalidConfigException ex)
		{
			OTG.log(LogMarker.ERROR, "Could not load default blocks for this version of minecraft, exiting.");
			throw new RuntimeException("Could not load default blocks for this version of minecraft, exiting.");
		}
	}

	static LocalMaterialData readMaterial (String name) throws InvalidConfigException
	{
		if (name == null)
		{
			return null;
		}

		LocalMaterialData material = CachedMaterials.get(name);
		if (material != null)
		{
			return material;
		}
		else if (CachedMaterials.containsKey(name))
		{
			throw new InvalidConfigException("Cannot read block: " + name);
		}

		try
		{
			material = SpigotMaterialData.ofString(name);
		}
		catch (InvalidConfigException ex)
		{
			// Happens when a non existing block name is used.
			String breakpoint = "";
			OTG.log(LogMarker.WARN, "Invalid config: " + ex.getMessage());
			OTG.log(LogMarker.WARN, "Replacing with blank");
		}

		CachedMaterials.put(name, material);

		return material;
	}
	
	static LocalMaterialTag readTag(String name) throws InvalidConfigException
	{
		if(name == null)
		{
			return null;
		}
		
		LocalMaterialTag tag = CachedTags.get(name);
		if(tag != null)
		{
			return tag;
		}

		tag = SpigotMaterialTag.ofString(name);
		CachedTags.put(name, tag);	
		return tag;
	}
}
