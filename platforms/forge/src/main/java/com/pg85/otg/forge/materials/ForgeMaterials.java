package com.pg85.otg.forge.materials;

import com.pg85.otg.OTG;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.FifoMap;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.LocalMaterials;

import net.minecraft.block.BambooBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoublePlantBlock;
import net.minecraft.state.properties.BambooLeaves;
import net.minecraft.state.properties.DoubleBlockHalf;

class ForgeMaterials extends LocalMaterials
{
	private static final FifoMap<String, LocalMaterialData> CachedMaterials = new FifoMap<String, LocalMaterialData>(4096); // TODO: Smaller cache should be ok, only most frequently used should be cached?
	
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
			RED_ROSE = readMaterial(LocalMaterials.RED_ROSE_NAME);
			BROWN_MUSHROOM = readMaterial(LocalMaterials.BROWN_MUSHROOM_NAME);
			YELLOW_FLOWER = readMaterial(LocalMaterials.YELLOW_FLOWER_NAME);
			DEAD_BUSH = readMaterial(LocalMaterials.DEAD_BUSH_NAME);
			LONG_GRASS = readMaterial(LocalMaterials.LONG_GRASS_NAME);
			RED_MUSHROOM = readMaterial(LocalMaterials.RED_MUSHROOM_NAME);

			DOUBLE_TALL_GRASS_LOWER = ForgeMaterialData.ofMinecraftBlockState(((ForgeMaterialData)readMaterial(LocalMaterials.DOUBLE_TALL_GRASS_NAME)).internalBlock().with(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER));
			DOUBLE_TALL_GRASS_UPPER = ForgeMaterialData.ofMinecraftBlockState(((ForgeMaterialData)readMaterial(LocalMaterials.DOUBLE_TALL_GRASS_NAME)).internalBlock().with(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER));
			LARGE_FERN_LOWER = ForgeMaterialData.ofMinecraftBlockState(((ForgeMaterialData)readMaterial(LocalMaterials.LARGE_FERN_NAME)).internalBlock().with(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER));
			LARGE_FERN_UPPER = ForgeMaterialData.ofMinecraftBlockState(((ForgeMaterialData)readMaterial(LocalMaterials.LARGE_FERN_NAME)).internalBlock().with(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER));
			LILAC_LOWER = ForgeMaterialData.ofMinecraftBlockState(((ForgeMaterialData)readMaterial(LocalMaterials.LILAC_NAME)).internalBlock().with(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER));
			LILAC_UPPER = ForgeMaterialData.ofMinecraftBlockState(((ForgeMaterialData)readMaterial(LocalMaterials.LILAC_NAME)).internalBlock().with(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER));
			PEONY_LOWER = ForgeMaterialData.ofMinecraftBlockState(((ForgeMaterialData)readMaterial(LocalMaterials.PEONY_NAME)).internalBlock().with(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER));
			PEONY_UPPER = ForgeMaterialData.ofMinecraftBlockState(((ForgeMaterialData)readMaterial(LocalMaterials.PEONY_NAME)).internalBlock().with(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER));
			ROSE_BUSH_LOWER = ForgeMaterialData.ofMinecraftBlockState(((ForgeMaterialData)readMaterial(LocalMaterials.ROSE_BUSH_NAME)).internalBlock().with(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER));
			ROSE_BUSH_UPPER = ForgeMaterialData.ofMinecraftBlockState(((ForgeMaterialData)readMaterial(LocalMaterials.ROSE_BUSH_NAME)).internalBlock().with(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER));
			SUNFLOWER_LOWER = ForgeMaterialData.ofMinecraftBlockState(((ForgeMaterialData)readMaterial(LocalMaterials.SUNFLOWER_NAME)).internalBlock().with(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER));
			SUNFLOWER_UPPER = ForgeMaterialData.ofMinecraftBlockState(((ForgeMaterialData)readMaterial(LocalMaterials.SUNFLOWER_NAME)).internalBlock().with(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER));
			
			PUMPKIN = readMaterial(LocalMaterials.PUMPKIN_NAME);
			CACTUS = readMaterial(LocalMaterials.CACTUS_NAME);
			MELON_BLOCK = readMaterial(LocalMaterials.MELON_BLOCK_NAME);
			VINE = readMaterial(LocalMaterials.VINE_NAME);
			SAPLING = readMaterial(LocalMaterials.SAPLING_NAME);
			WATER_LILY = readMaterial(LocalMaterials.WATER_LILY_NAME);
			SUGAR_CANE_BLOCK = readMaterial(LocalMaterials.SUGAR_CANE_BLOCK_NAME);
			BlockState bambooState = Blocks.BAMBOO.getDefaultState().with(BambooBlock.PROPERTY_AGE, 1).with(BambooBlock.PROPERTY_BAMBOO_LEAVES, BambooLeaves.NONE).with(BambooBlock.PROPERTY_STAGE, 0);
			BAMBOO = ForgeMaterialData.ofMinecraftBlockState(bambooState);
			BAMBOO_SMALL = ForgeMaterialData.ofMinecraftBlockState(bambooState.with(BambooBlock.PROPERTY_BAMBOO_LEAVES, BambooLeaves.SMALL));
			BAMBOO_LARGE = ForgeMaterialData.ofMinecraftBlockState(bambooState.with(BambooBlock.PROPERTY_BAMBOO_LEAVES, BambooLeaves.LARGE));
			BAMBOO_LARGE_GROWING = ForgeMaterialData.ofMinecraftBlockState(bambooState.with(BambooBlock.PROPERTY_BAMBOO_LEAVES, BambooLeaves.LARGE).with(BambooBlock.PROPERTY_STAGE, 1));
			PODZOL = readMaterial(LocalMaterials.PODZOL_NAME);

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
		} catch(InvalidConfigException ex) {
			OTG.log(LogMarker.ERROR, "Could not load default blocks for this version of minecraft, exiting.");
			throw new RuntimeException("Could not load default blocks for this version of minecraft, exiting.");
		}
	};

	static LocalMaterialData readMaterial(String name) throws InvalidConfigException
	{
		if(name == null)
		{
			return null;
		}
		
		LocalMaterialData material = CachedMaterials.get(name);
		if(material != null)
		{
			return material;
		}
		else if(CachedMaterials.containsKey(name))
		{
			throw new InvalidConfigException("Cannot read block: " + name);
		}
		
		try
		{
			material = ForgeMaterialData.ofString(name);
		}
		catch(InvalidConfigException ex)
		{
			// Happens when a non existing block name is used.
			String breakpoint = "";
		}

		CachedMaterials.put(name, material);
		
		return material;
	}
}
