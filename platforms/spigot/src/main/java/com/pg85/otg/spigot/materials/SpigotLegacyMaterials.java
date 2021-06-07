package com.pg85.otg.spigot.materials;

import com.pg85.otg.OTG;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.materials.LegacyMaterials;

import net.minecraft.server.v1_16_R3.*;

public class SpigotLegacyMaterials
{
	static IBlockData fromLegacyBlockName (String oldBlockName)
	{
		switch (oldBlockName)
		{
			// TODO: These minecraft:xxx blocks no longer exist, so cannot be parsed by mc.
			// We should parse them here, but atm we're not falling back to legacy parsing
			// for those blocks. Should make that work, and also handle minecraft:xxx:data.
			/*
			case "minecraft:silver_shulker_box":
				return Blocks.LIGHT_GRAY_SHULKER_BOX.defaultBlockState();
			case "minecraft:silver_glazed_terracotta":
				return Blocks.LIGHT_GRAY_GLAZED_TERRACOTTA.defaultBlockState();
			*/	
			case "stationary_water":
				return Blocks.WATER.getBlockData();
			case "stationary_lava":
				return Blocks.LAVA.getBlockData();
			case "stained_clay":
			case "hard_clay":
				return Blocks.TERRACOTTA.getBlockData();
			case "step":
				return Blocks.SMOOTH_STONE_SLAB.getBlockData();
			case "sugar_cane_block":
				return Blocks.SUGAR_CANE.getBlockData();
			case "melon_block":
				return Blocks.MELON.getBlockData();
			case "water_lily":
				return Blocks.LILY_PAD.getBlockData();
			case "soil":
				return Blocks.FARMLAND.getBlockData();
			case "grass":
				return Blocks.GRASS_BLOCK.getBlockData();
			case "long_grass":
				return Blocks.TALL_GRASS.getBlockData();
			case "mycel":
				return Blocks.MYCELIUM.getBlockData();
			case "snow_layer":
				return Blocks.SNOW.getBlockData();
			case "leaves":
				return Blocks.OAK_LEAVES.getBlockData().set(BlockLeaves.DISTANCE, 1);
			case "leaves_2":
				return Blocks.ACACIA_LEAVES.getBlockData().set(BlockLeaves.DISTANCE, 1);
			case "red_rose":
				return Blocks.POPPY.getBlockData();
			// TODO: This only spawns the bottom half?
			case "double_plant":
				return Blocks.SUNFLOWER.getBlockData();

			case "wood_stairs":
			case "oak_stairs":
				return Blocks.OAK_STAIRS.getBlockData().set(BlockStairs.FACING, EnumDirection.NORTH);
			case "birch_wood_stairs":
			case "birch_stairs":
				return Blocks.BIRCH_STAIRS.getBlockData().set(BlockStairs.FACING, EnumDirection.NORTH);
			case "spruce_wood_stairs":
			case "spruce_stairs":
				return Blocks.SPRUCE_STAIRS.getBlockData().set(BlockStairs.FACING, EnumDirection.NORTH);
			case "jungle_wood_stairs":
			case "jungle_stairs":
				return Blocks.JUNGLE_STAIRS.getBlockData().set(BlockStairs.FACING, EnumDirection.NORTH);
			case "cobblestone_stairs":
			case "stone_stairs":
				return Blocks.COBBLESTONE_STAIRS.getBlockData().set(BlockStairs.FACING, EnumDirection.NORTH);
			case "stone_brick_stairs":
			case "smooth_stairs":
				return Blocks.STONE_BRICK_STAIRS.getBlockData().set(BlockStairs.FACING, EnumDirection.NORTH);
			case "brick_stairs":
				return Blocks.BRICK_STAIRS.getBlockData().set(BlockStairs.FACING, EnumDirection.NORTH);
			case "nether_brick_stairs":
				return Blocks.NETHER_BRICK_STAIRS.getBlockData().set(BlockStairs.FACING, EnumDirection.NORTH);
			case "sandstone_stairs":
				return Blocks.SANDSTONE_STAIRS.getBlockData().set(BlockStairs.FACING, EnumDirection.NORTH);
			case "quartz_stairs":
				return Blocks.QUARTZ_STAIRS.getBlockData().set(BlockStairs.FACING, EnumDirection.NORTH);
			case "acacia_stairs":
				return Blocks.ACACIA_STAIRS.getBlockData().set(BlockStairs.FACING, EnumDirection.NORTH);
			case "dark_oak_stairs":
				return Blocks.DARK_OAK_STAIRS.getBlockData().set(BlockStairs.FACING, EnumDirection.NORTH);
			case "red_sandstone_stairs":
				return Blocks.RED_SANDSTONE_STAIRS.getBlockData().set(BlockStairs.FACING, EnumDirection.NORTH);
			case "purpur_stairs":
				return Blocks.PURPUR_STAIRS.getBlockData().set(BlockStairs.FACING, EnumDirection.NORTH);

			case "quartz_ore":
				return Blocks.NETHER_QUARTZ_ORE.getBlockData();				
			case "yellow_flower":
				return Blocks.DANDELION.getBlockData();
			case "web":
				return Blocks.COBWEB.getBlockData();
			case "standing_banner":
				return Blocks.WHITE_BANNER.getBlockData();
			case "wall_banner":
				return Blocks.WHITE_WALL_BANNER.getBlockData();
			case "redstone_lamp_on":
				return Blocks.REDSTONE_LAMP.getBlockData().set(BlockRedstoneLamp.a, true);
			case "redstone_lamp_off":
				return Blocks.REDSTONE_LAMP.getBlockData().set(BlockRedstoneLamp.a, false);
			case "wool":
				return Blocks.WHITE_WOOL.getBlockData();
			case "log":
			case "wood":
				return Blocks.OAK_LOG.getBlockData();
			case "log_2":
				return Blocks.ACACIA_LOG.getBlockData();
			case "magma":
				return Blocks.MAGMA_BLOCK.getBlockData();
			case "tallgrass":
				return Blocks.GRASS.getBlockData();
			case "cobble_wall":
				return Blocks.COBBLESTONE_WALL.getBlockData();
			case "iron_fence":
				return Blocks.IRON_BARS.getBlockData();
			case "workbench":
				return Blocks.CRAFTING_TABLE.getBlockData();
			case "enchantment_table":
				return Blocks.ENCHANTING_TABLE.getBlockData();
			case "mob_spawner":
				return Blocks.INFESTED_STONE.getBlockData();
			case "double_step":
				return IRegistry.BLOCK.get(new MinecraftKey("minecraft:smooth_stone_slab"))
						.getBlockData().set(BlockStepAbstract.a,
								BlockPropertySlabType.DOUBLE);
			case "smooth_brick":
				return Blocks.STONE_BRICKS.getBlockData();
			case "rails":
				return Blocks.RAIL.getBlockData();
			case "fence":
				return Blocks.OAK_FENCE.getBlockData();
			case "nether_fence":
				return Blocks.NETHER_BRICK_FENCE.getBlockData();				
			case "wood_step":
				return Blocks.OAK_SLAB.getBlockData();
			case "thin_glass":
				return Blocks.GLASS_PANE.getBlockData();
			case "stone_plate":
				return Blocks.STONE_PRESSURE_PLATE.getBlockData();
			case "wood_plate":
				return Blocks.OAK_PRESSURE_PLATE.getBlockData();
			case "wood_double_step":
				return Blocks.OAK_SLAB.getBlockData().set(BlockStepAbstract.a, BlockPropertySlabType.DOUBLE);
			case "brick":
				return Blocks.BRICKS.getBlockData();
			case "iron_door_block":
				return Blocks.IRON_DOOR.getBlockData();
			case "carpet":
				return Blocks.WHITE_CARPET.getBlockData();
			case "carrot":
				return Blocks.CARROTS.getBlockData();
			case "skull":
				return Blocks.SKELETON_SKULL.getBlockData();
			case "nether_wart":
				return Blocks.NETHER_WART.getBlockData();				
			case "nether_wart_block":
				return Blocks.NETHER_WART_BLOCK.getBlockData();				
			case "nether_brick":
				return Blocks.NETHER_BRICKS.getBlockData();
			case "red_nether_brick":
				return Blocks.RED_NETHER_BRICKS.getBlockData();				
			case "end_bricks":
			case "ender_bricks":
				return Blocks.END_STONE_BRICKS.getBlockData();
			case "end_stone":
			case "ender_stone":
				return Blocks.END_STONE.getBlockData();				
			case "mcpitman":
				return Blocks.CREEPER_HEAD.getBlockData();
			case "pg85":
				return Blocks.ZOMBIE_HEAD.getBlockData();
			case "supercoder":
				return Blocks.CAKE.getBlockData();
			case "authvin":
				return Blocks.WET_SPONGE.getBlockData();
			case "josh":
				return Blocks.BARREL.getBlockData();
			case "wahrheit":
				return Blocks.LECTERN.getBlockData();
			case "lordsmellypants":
				return Blocks.FLOWER_POT.getBlockData();
			default:
				return null;
		}
	}

	static IBlockData fromLegacyBlockNameOrIdWithData (String blockName, int data)
	{
		if (blockName == null || blockName.trim().isEmpty())
		{
			return null;
		}

		try
		{
			int blockId = Integer.parseInt(blockName);
			blockName = LegacyMaterials.blockNameFromLegacyBlockId(blockId);
			if (blockName == null)
			{
				return null;
			}
		}
		catch (NumberFormatException ignored)
		{

		}

		try
		{
			switch (blockName)
			{
				// Support "GRASS:0" here, or it will be misinterpreted as the new grass (plant)
				case "grass":
					if (data == 0)
					{
						return Blocks.GRASS_BLOCK.getBlockData();
					}

					// Legacy blocks with block data that are now their own block
				case "banner":
				case "white_banner":
					return IRegistry.BLOCK.get(getFlatKey("minecraft:banner", data)).getBlockData();

				// TODO: How does facing for bed blocks in bo's work for 1.12.2, can only specify color via data?
				case "bed":
				case "white_bed":
					return IRegistry.BLOCK.get(getFlatKey("minecraft:bed", data)).getBlockData();

				case "carpet":
				case "white_carpet":
					return IRegistry.BLOCK.get(getFlatKey("minecraft:carpet", data)).getBlockData();

				case "cobblestone_wall":
				case "cobble_wall":
					switch (data)
					{
						case 0:
						default:
							return Blocks.COBBLESTONE_WALL.getBlockData();
						case 1:
							return Blocks.MOSSY_COBBLESTONE_WALL.getBlockData();
					}
				case "concrete":
				case "white_concrete":
					return IRegistry.BLOCK.get(getFlatKey("minecraft:concrete", data)).getBlockData();

				case "concrete_powder":
				case "white_concrete_powder":
					return IRegistry.BLOCK.get(getFlatKey("minecraft:concrete_powder", data)).getBlockData();

				case "dirt":
					return IRegistry.BLOCK.get(getFlatKey("minecraft:dirt", data)).getBlockData();

				// TODO: This only spawns the bottom half?
				case "double_plant":
				case "sunflower":
				case "rose_bush":
				case "tall_grass":
					return IRegistry.BLOCK.get(getFlatKey("minecraft:double_plant", data)).getBlockData();

				case "double_stone_slab":
				case "smooth_stone":
					return IRegistry.BLOCK.get(getFlatKey("minecraft:stone_slab", data))
							.getBlockData().set(BlockStepAbstract.a, BlockPropertySlabType.DOUBLE);

				// TODO: Did this even exist for 1.12.2?
				case "double_wooden_slab":
				case "wood_double_step":
					return IRegistry.BLOCK.get(getFlatKey("minecraft:wooden_slab", data))
							.getBlockData().set(BlockStepAbstract.a, BlockPropertySlabType.DOUBLE);

				case "leaves":
				case "oak_leaves":
					return IRegistry.BLOCK.get(getFlatKey("minecraft:leaves", data % 4))
							.getBlockData().set(BlockLeaves.DISTANCE, 1);

				case "leaves2":
				case "leaves_2":
				case "acacia_leaves":
					return IRegistry.BLOCK.get(getFlatKey("minecraft:leaves2", data % 4)).getBlockData();

				case "monster_egg":
				case "monster_eggs":
				case "infested_stone":
					return IRegistry.BLOCK.get(getFlatKey("minecraft:monster_egg", data)).getBlockData();

				case "planks":
				case "wood":
				case "oak_planks":
					return IRegistry.BLOCK.get(getFlatKey("minecraft:planks", data)).getBlockData();

				case "prismarine":
					return IRegistry.BLOCK.get(getFlatKey("minecraft:prismarine", data)).getBlockData();

				case "purpur_slab":
					return Blocks.PURPUR_SLAB.getBlockData()
						.set(BlockStepAbstract.a,
							data == 0 ? BlockPropertySlabType.BOTTOM :
							data == 8 ? BlockPropertySlabType.TOP : BlockPropertySlabType.BOTTOM);
				case "purpur_double_slab":
					return Blocks.PURPUR_SLAB.getBlockData()
						.set(BlockStepAbstract.a, BlockPropertySlabType.DOUBLE);		

				case "red_flower":
				case "red_rose":
					return IRegistry.BLOCK.get(getFlatKey("minecraft:red_flower", data)).getBlockData();

				case "red_sandstone":
					return IRegistry.BLOCK.get(getFlatKey("minecraft:red_sandstone", data)).getBlockData();

				case "red_sandstone_slab":
				case "stone_slab2":
					switch (data)
					{
						case 0:
						default:
							return Blocks.RED_SANDSTONE_SLAB.getBlockData().set(BlockStepAbstract.a, BlockPropertySlabType.BOTTOM);
						case 8:
							return Blocks.RED_SANDSTONE_SLAB.getBlockData().set(BlockStepAbstract.a, BlockPropertySlabType.TOP);
					}
				case "double_red_sandstone_slab":
				case "double_stone_slab2":
					return Blocks.RED_SANDSTONE_SLAB.getBlockData().set(BlockStepAbstract.a, BlockPropertySlabType.DOUBLE);

				case "sand":
					switch (data)
					{
						case 0:
						default:
							return Blocks.SAND.getBlockData();
						case 1:
							return Blocks.RED_SAND.getBlockData();
					}

				case "sandstone":
					return IRegistry.BLOCK.get(getFlatKey("minecraft:sandstone", data)).getBlockData();

				case "sapling":
				case "oak_sapling":
					return IRegistry.BLOCK.get(getFlatKey("minecraft:sapling", data)).getBlockData();

				case "skull":
				case "skeleton_skull":
					return IRegistry.BLOCK.get(getFlatKey("minecraft:skull", data)).getBlockData();

				case "sponge":
					switch (data)
					{
						case 0:
						default:
							return Blocks.SPONGE.getBlockData();
						case 1:
							return Blocks.WET_SPONGE.getBlockData();
					}
				case "stained_glass":
				case "white_stained_glass":
					return IRegistry.BLOCK.get(getFlatKey("minecraft:stained_glass", data)).getBlockData();

				case "stained_glass_pane":
				case "white_stained_glass_pane":
				case "thin_glass":
					return IRegistry.BLOCK.get(getFlatKey("minecraft:stained_glass_pane", data)).getBlockData();

				case "stained_hardened_clay":
				case "stained_clay":
				case "white_terracotta":
					return IRegistry.BLOCK.get(getFlatKey("minecraft:stained_hardened_clay", data)).getBlockData();

				case "stone":
					return IRegistry.BLOCK.get(getFlatKey("minecraft:stone", data)).getBlockData();

				case "stone_slab":
				case "step":
					return IRegistry.BLOCK.get(getFlatKey("minecraft:stone_slab", data % 8))
						.getBlockData().set(BlockStepAbstract.a,
							data >= 8 ? BlockPropertySlabType.TOP : BlockPropertySlabType.BOTTOM);

				case "double_step":
					return IRegistry.BLOCK.get(getFlatKey("minecraft:stone_slab", data % 8))
						.getBlockData().set(BlockStepAbstract.a, BlockPropertySlabType.DOUBLE);					
					
				case "stonebrick":
				case "stone_bricks":
				case "smooth_brick":
					return IRegistry.BLOCK.get(getFlatKey("minecraft:stonebrick", data)).getBlockData();

				case "tallgrass":
				case "long_grass":
					switch (data)
					{
						case 1:
						default:
							return Blocks.GRASS.getBlockData();
						case 2:
							return Blocks.FERN.getBlockData();
					}
				case "wooden_slab":
				case "wood_step":
				case "oak_slab":
					return IRegistry.BLOCK.get(getFlatKey("minecraft:wooden_slab", data % 8))
							.getBlockData().set(BlockStepAbstract.a,
									data >= 8 ? BlockPropertySlabType.TOP : BlockPropertySlabType.BOTTOM);

				case "wool":
				case "white_wool":
					return IRegistry.BLOCK.get(getFlatKey("minecraft:wool", data)).getBlockData();


				// Blocks with data
				case "fire":
					return Blocks.FIRE.getBlockData().set(BlockFire.AGE, data);
				case "cake":
				case "cake_block":
					return Blocks.CAKE.getBlockData().set(BlockCake.BITES, data);
				case "stone_pressure_plate":
				case "stone_plate":
					return Blocks.STONE_PRESSURE_PLATE.getBlockData().set(BlockPressurePlateBinary.POWERED, getBit(data, 0) == 1);
				case "wooden_pressure_plate":
				case "wood_plate":
				case "oak_pressure_plate":
					return Blocks.OAK_PRESSURE_PLATE.getBlockData().set(BlockPressurePlateBinary.POWERED, getBit(data, 0) == 1);
				case "light_weighted_pressure_plate":
				case "gold_plate":					
					return Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE.getBlockData().set(BlockPressurePlateWeighted.POWER, data);
				case "heavy_weighted_pressure_plate":
				case "iron_plate":					
					return Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE.getBlockData().set(BlockPressurePlateWeighted.POWER, data);
				case "snow_layer":
					return Blocks.SNOW.getBlockData().set(BlockSnow.LAYERS, data);
				case "cactus":
					return Blocks.CACTUS.getBlockData().set(BlockCactus.AGE, data);
				case "reeds":
					return Blocks.SUGAR_CANE.getBlockData().set(BlockReed.AGE, data);
				case "jukebox":
					return Blocks.JUKEBOX.getBlockData().set(BlockJukeBox.HAS_RECORD, data == 1);
				case "wheat":
				case "crops":
					return Blocks.WHEAT.getBlockData().set(BlockCrops.AGE, data);
				case "carrot":
				case "carrots":
					return Blocks.CARROTS.getBlockData().set(BlockCarrots.AGE, data);
				case "potato":
				case "potatoes":
					return Blocks.POTATOES.getBlockData().set(BlockPotatoes.AGE, data);
				case "beetroot":
				case "beetroots":
					return Blocks.BEETROOTS.getBlockData().set(BlockBeetroot.AGE, data);
				case "farmland":
				case "soil":
					return Blocks.FARMLAND.getBlockData().set(BlockSoil.MOISTURE, data);
				case "anvil":
					return getAnvilWithData(0, data);
				case "log":
				case "oak_log":
					return getLogWithData(data);
				case "log_2":
				case "acacia_log":
					return getLog2WithData(data);
				case "quartz_block":
					return getQuartzBlockWithData(data);
				case "torch":
					return getTorchWithData(0, data);
				case "redstone_torch_off":
				case "redstone_torch":
					return getTorchWithData(1, data);
				case "redstone_torch_on":
					return getTorchWithData(2, data);
				case "rails":
					return Blocks.RAIL.getBlockData().set(BlockMinecartTrack.SHAPE, getRailShape(data));
				case "powered_rail":
				case "golden_rail":
					return getRailsWithData(0, data);
				case "detector_rail":
					return getRailsWithData(1, data);
				case "activator_rail":
					return getRailsWithData(2, data);
				case "hay_block":
					return Blocks.HAY_BLOCK.getBlockData().set(BlockHay.AXIS, getPillarAxisXYZ(data));
				case "bone_block":
					return Blocks.BONE_BLOCK.getBlockData().set(BlockRotatable.AXIS, getAxisXYZ(data));
				case "wood_stairs":
				case "oak_stairs":
					return getStairsWithData(0, data);
				case "birch_wood_stairs":
				case "birch_stairs":
					return getStairsWithData(1, data);
				case "spruce_wood_stairs":
				case "spruce_stairs":
					return getStairsWithData(2, data);
				case "jungle_wood_stairs":
				case "jungle_stairs":
					return getStairsWithData(3, data);
				case "cobblestone_stairs":
				case "stone_stairs":
					return getStairsWithData(4, data);
				case "brick_stairs":
					return getStairsWithData(5, data);
				case "nether_brick_stairs":
					return getStairsWithData(7, data);
				case "sandstone_stairs":
					return getStairsWithData(8, data);
				case "quartz_stairs":
					return getStairsWithData(9, data);
				case "acacia_stairs":
					return getStairsWithData(10, data);
				case "dark_oak_stairs":
					return getStairsWithData(11, data);
				case "red_sandstone_stairs":
					return getStairsWithData(12, data);
				case "purpur_stairs":
					return getStairsWithData(13, data);
				case "stone_brick_stairs":
				case "smooth_stairs":					
					return getStairsWithData(14, data);
				case "lever":
					return getLeverOrButtonWithData(0, data);
				case "stone_button":
					return getLeverOrButtonWithData(1, data);
				case "wood_button":
				case "wooden_button":
					return getLeverOrButtonWithData(2, data);
				case "wooden_door":
				case "oak_door":
					return getDoorWithData(0, data);
				case "iron_door_block":
				case "iron_door":
					return getDoorWithData(1, data);
				case "spruce_door":
					return getDoorWithData(2, data);
				case "birch_door":
					return getDoorWithData(3, data);
				case "jungle_door":
					return getDoorWithData(4, data);
				case "acacia_door":
					return getDoorWithData(5, data);
				case "dark_oak_door":
					return getDoorWithData(6, data);
				case "oak_sign":
				case "sign_post":
				case "sign": // TODO: This will also pick up wall signs
					return getSignPostWithData(data);
				case "standing_banner":
					return Blocks.RED_BANNER.getBlockData().set(BlockBanner.ROTATION, data);
				case "wall_banner":
					return Blocks.WHITE_WALL_BANNER.getBlockData().set(BlockBannerWall.a, getFacingNorthSouthWestEast(data));
				case "end_rod":
					return Blocks.END_ROD.getBlockData().set(BlockEndRod.FACING, getFacingDownUpNorthSouthWestEast(data));
				case "daylight_detector":
					return Blocks.DAYLIGHT_DETECTOR.getBlockData().set(BlockDaylightDetector.POWER, data);
				case "command":
				case "command_block":
					return getCommandBlockWithData(0, data);
				case "command_repeating":
				case "repeating_command_block":
					return getCommandBlockWithData(1, data);
				case "command_chain":
				case "chain_command_block":
					return getCommandBlockWithData(2, data);
				case "white_shulker_box":
					return getShulkerBoxWithData(0, data);
				case "orange_shulker_box":
					return getShulkerBoxWithData(1, data);
				case "magenta_shulker_box":
					return getShulkerBoxWithData(2, data);
				case "light_blue_shulker_box":
					return getShulkerBoxWithData(3, data);
				case "yellow_shulker_box":
					return getShulkerBoxWithData(4, data);
				case "lime_shulker_box":
					return getShulkerBoxWithData(5, data);
				case "pink_shulker_box":
					return getShulkerBoxWithData(6, data);
				case "gray_shulker_box":
					return getShulkerBoxWithData(7, data);
				case "light_gray_shulker_box":
				case "silver_shulker_box":
					return getShulkerBoxWithData(8, data);
				case "cyan_shulker_box":
					return getShulkerBoxWithData(9, data);
				case "purple_shulker_box":
					return getShulkerBoxWithData(10, data);
				case "blue_shulker_box":
					return getShulkerBoxWithData(11, data);
				case "brown_shulker_box":
					return getShulkerBoxWithData(12, data);
				case "green_shulker_box":
					return getShulkerBoxWithData(13, data);
				case "red_shulker_box":
					return getShulkerBoxWithData(14, data);
				case "black_shulker_box":
					return getShulkerBoxWithData(15, data);
				case "shulker_box":
					return getShulkerBoxWithData(16, data);
				case "ladder":
					return getLadderChestOrFurnaceWithData(0, data);
				case "chest":
					return getLadderChestOrFurnaceWithData(1, data);
				case "ender_chest":
					return getLadderChestOrFurnaceWithData(2, data);
				case "trapped_chest":
					return getLadderChestOrFurnaceWithData(3, data);
				case "furnace":
					return getLadderChestOrFurnaceWithData(4, data);
				case "burning_furnace":
					return getLadderChestOrFurnaceWithData(5, data);
				case "wall_sign":
				case "oak_wall_sign":
					return getWallSignWithData(data);
				case "observer":
					return getObserverWithData(data);
				case "dispenser":
					return getDispenserHopperDropperWithData(0, data);
				case "dropper":
					return getDispenserHopperDropperWithData(1, data);
				case "hopper":
					return getDispenserHopperDropperWithData(2, data);
				case "pumpkin_stem":
					// TODO: Hopefully this auto-updates to ATTACHED_PUMPKIN_STEM when placed next to a pumpkin block..
					return Blocks.PUMPKIN_STEM.getBlockData().set(BlockStem.AGE, data);
				case "melon_stem":
					// TODO: Hopefully this auto-updates to ATTACHED_MELON_STEM when placed next to a melon block..
					return Blocks.MELON_STEM.getBlockData().set(BlockStem.AGE, data);
				case "carved_pumpkin":
				case "pumpkin":
					return getJackOLanternOrPumpkinWithData(0, data);
				case "jack_o_lantern":
				case "lit_pumpkin":
					return getJackOLanternOrPumpkinWithData(1, data);
				case "diode_block_off":
				case "repeater":
				case "unpowered_repeater":
					return getRepeaterWithData(0, data);
				case "diode_block_on":
				case "powered_repeater":
					return getRepeaterWithData(1, data);
				case "redstone":
				case "redstone_wire":
					return Blocks.REDSTONE_WIRE.getBlockData().set(BlockRedstoneWire.POWER, data);
				case "redstone_comparator_off":
				case "comparator":
					return getComparatorWithData(0, data);
				case "redstone_comparator_on":
					return getComparatorWithData(1, data);
				// TODO: How does facing for bed blocks in bo's work for 1.12.2, can only specify color via data?
				case "bed_block":
					return getBedBlockWithData(data);
				case "trap_door":
				case "trapdoor":
				case "oak_trapdoor":
					return getTrapDoorBlockWithData(0, data);
				case "iron_trapdoor":
					return getTrapDoorBlockWithData(1, data);
				case "piston_base":
				case "piston":
					return getPistonWithData(0, data);
				case "piston_sticky_base":
				case "sticky_piston":
					return getPistonWithData(1, data);
				case "piston_extension":
				case "piston_head":
					return getPistonHeadWithData(data);
				case "huge_mushroom_1":
				case "brown_mushroom_block":
					return getHugeMushroomWithData(0, data);
				case "huge_mushroom_2":
				case "red_mushroom_block":
					return getHugeMushroomWithData(1, data);
				case "vine":
					return getVineWithData(data);
				case "fence_gate":
				case "oak_fence_gate":
					return getFenceGateWithData(0, data);
				case "spruce_fence_gate":
					return getFenceGateWithData(1, data);
				case "birch_fence_gate":
					return getFenceGateWithData(2, data);
				case "jungle_fence_gate":
					return getFenceGateWithData(3, data);
				case "dark_oak_fence_gate":
					return getFenceGateWithData(4, data);
				case "acacia_fence_gate":
					return getFenceGateWithData(5, data);
				case "cocoa":
					return getCocoaWithData(data);
				// What about trip-wire (the wire itself)?
				case "tripwire_hook":
					return getTripWireHookWithData(data);
				case "tripwire":
					return getTripWireWithData(data);
				case "purpur_pillar":
					return Blocks.PURPUR_PILLAR.getBlockData().set(BlockRotatable.AXIS, getPillarAxisXYZ(data));
				case "nether_wart":
					return Blocks.NETHER_WART.getBlockData().set(BlockNetherWart.AGE, data);
				case "brewing_stand":
					return Blocks.BREWING_STAND.getBlockData()
							.set(BlockBrewingStand.HAS_BOTTLE[0], getBit(data, 0) == 1)
							.set(BlockBrewingStand.HAS_BOTTLE[1], getBit(data, 1) == 1)
							.set(BlockBrewingStand.HAS_BOTTLE[2], getBit(data, 2) == 1)
							;
				case "cauldron":
					return Blocks.CAULDRON.getBlockData().set(BlockCauldron.LEVEL, data);
				case "portal":
					return Blocks.NETHER_PORTAL.getBlockData().set(BlockPortal.AXIS, getAxisXZ(data));
				case "end_portal_frame":
				case "ender_portal_frame":
					return getEndPortalFrameWithData(data);
				case "structure_block":
					return getStructureBlockWithData(data);
				case "black_glazed_terracotta":
					return getGlazedTerracottaWithData(0, data);
				case "blue_glazed_terracotta":
					return getGlazedTerracottaWithData(1, data);
				case "brown_glazed_terracotta":
					return getGlazedTerracottaWithData(2, data);
				case "cyan_glazed_terracotta":
					return getGlazedTerracottaWithData(3, data);
				case "gray_glazed_terracotta":
					return getGlazedTerracottaWithData(4, data);
				case "green_glazed_terracotta":
					return getGlazedTerracottaWithData(5, data);
				case "light_blue_glazed_terracotta":
					return getGlazedTerracottaWithData(6, data);
				case "lime_glazed_terracotta":
					return getGlazedTerracottaWithData(7, data);
				case "magenta_glazed_terracotta":
					return getGlazedTerracottaWithData(8, data);
				case "orange_glazed_terracotta":
					return getGlazedTerracottaWithData(9, data);
				case "pink_glazed_terracotta":
					return getGlazedTerracottaWithData(10, data);
				case "purple_glazed_terracotta":
					return getGlazedTerracottaWithData(11, data);
				case "red_glazed_terracotta":
					return getGlazedTerracottaWithData(12, data);
				case "silver_glazed_terracotta":
					return getGlazedTerracottaWithData(13, data);
				case "white_glazed_terracotta":
					return getGlazedTerracottaWithData(14, data);
				case "yellow_glazed_terracotta":
					return getGlazedTerracottaWithData(15, data);
				default:
					return null;
			}
		}
		catch (IllegalArgumentException ex)
		{
			OTG.log(LogMarker.INFO, "Could not parse block with data, illegal data: " + blockName + ":" + data + ". Exception: " + ex.getMessage());
		}
		catch (NullPointerException ex)
		{
			OTG.log(LogMarker.WARN, "Encountered a null pointer trying to parse " + blockName + ":" + data + ". Exception: " + ex.getMessage());
		}
		return null;
	}

	private static MinecraftKey getFlatKey (String name, int data) throws NullPointerException
	{
		String result = DataConverterFlatten.a(name, data);
		if (result == null)
		{
			throw new NullPointerException();
		}
		return new MinecraftKey(result);
	}


	private static IBlockData getAnvilWithData (int material, int data)
	{
		EnumDirection orientation = getBit(data, 0) == 0 ? EnumDirection.NORTH : EnumDirection.WEST;
		switch (material)
		{
			case 0:
				// 0x4 0x8 state: regular (0x4 & 0x8 = 0), slightly damaged (0x4 = 1), very damaged (0x8 = 1)				
				if ((getBit(data, 2) & getBit(data, 3)) == 0)
				{
					return Blocks.ANVIL.getBlockData().set(BlockAnvil.FACING, orientation);
				}
				else if (getBit(data, 2) == 1)
				{
					return Blocks.CHIPPED_ANVIL.getBlockData().set(BlockAnvil.FACING, orientation);
				}
				else if (getBit(data, 3) == 1)
				{
					return Blocks.DAMAGED_ANVIL.getBlockData().set(BlockAnvil.FACING, orientation);
				}
			case 1:
				return Blocks.CHIPPED_ANVIL.getBlockData().set(BlockAnvil.FACING, orientation);
			case 2:
				return Blocks.DAMAGED_ANVIL.getBlockData().set(BlockAnvil.FACING, orientation);
			default:
				return null;
		}
	}

	private static IBlockData getLogWithData (int data)
	{
		int material = getBits(data, 0, 2);
		int orientation = getBits(data, 2, 2);
		EnumDirection.EnumAxis axisDirection = orientation == 0 ? EnumDirection.EnumAxis.Y : orientation == 1 ? EnumDirection.EnumAxis.X : orientation == 2 ? EnumDirection.EnumAxis.Z : EnumDirection.EnumAxis.Y;
		boolean bark = orientation == 3;
		switch (material)
		{
			case 0:
				if (bark) return Blocks.OAK_WOOD.getBlockData().set(BlockRotatable.AXIS, axisDirection);
				return Blocks.OAK_LOG.getBlockData().set(BlockRotatable.AXIS, axisDirection);
			case 1:
				if (bark) return Blocks.SPRUCE_WOOD.getBlockData().set(BlockRotatable.AXIS, axisDirection);
				return Blocks.SPRUCE_LOG.getBlockData().set(BlockRotatable.AXIS, axisDirection);
			case 2:
				if (bark) return Blocks.BIRCH_WOOD.getBlockData().set(BlockRotatable.AXIS, axisDirection);
				return Blocks.BIRCH_LOG.getBlockData().set(BlockRotatable.AXIS, axisDirection);
			case 3:
				if (bark) return Blocks.JUNGLE_WOOD.getBlockData().set(BlockRotatable.AXIS, axisDirection);
				return Blocks.JUNGLE_LOG.getBlockData().set(BlockRotatable.AXIS, axisDirection);
			default:
				return Blocks.OAK_LOG.getBlockData();
		}
	}

	private static IBlockData getLog2WithData (int data)
	{
		int material = getBits(data, 0, 2);
		int orientation = getBits(data, 2, 2);
		EnumDirection.EnumAxis axisDirection = orientation == 0 ? EnumDirection.EnumAxis.Y : orientation == 1 ? EnumDirection.EnumAxis.X : orientation == 2 ? EnumDirection.EnumAxis.Z : EnumDirection.EnumAxis.Y;
		boolean bark = orientation == 3;
		switch (material)
		{
			case 0:
				if (bark) return Blocks.ACACIA_WOOD.getBlockData().set(BlockRotatable.AXIS, axisDirection);
				return Blocks.ACACIA_LOG.getBlockData().set(BlockRotatable.AXIS, axisDirection);
			case 1:
				if (bark) return Blocks.DARK_OAK_WOOD.getBlockData().set(BlockRotatable.AXIS, axisDirection);
				return Blocks.DARK_OAK_LOG.getBlockData().set(BlockRotatable.AXIS, axisDirection);
			default:
				return Blocks.ACACIA_LOG.getBlockData();
		}
	}

	private static IBlockData getQuartzBlockWithData (int data)
	{
		switch (data)
		{
			case 0:
			default:
				return Blocks.QUARTZ_BLOCK.getBlockData();
			case 1:
				return Blocks.CHISELED_QUARTZ_BLOCK.getBlockData();
			case 2:
				return Blocks.QUARTZ_PILLAR.getBlockData().set(BlockRotatable.AXIS, EnumDirection.EnumAxis.Y);
			case 3:
				return Blocks.QUARTZ_PILLAR.getBlockData().set(BlockRotatable.AXIS, EnumDirection.EnumAxis.X);
			case 4:
				return Blocks.QUARTZ_PILLAR.getBlockData().set(BlockRotatable.AXIS, EnumDirection.EnumAxis.Z);
		}
	}

	private static IBlockData getTorchWithData (int material, int data)
	{
		switch (material)
		{
			case 0:
				switch (data)
				{
					case 0:
					case 5:
					default:
						return Blocks.TORCH.getBlockData();
					case 1:
						return Blocks.WALL_TORCH.getBlockData().set(BlockTorchWall.a, EnumDirection.EAST);
					case 2:
						return Blocks.WALL_TORCH.getBlockData().set(BlockTorchWall.a, EnumDirection.WEST);
					case 3:
						return Blocks.WALL_TORCH.getBlockData().set(BlockTorchWall.a, EnumDirection.SOUTH);
					case 4:
						return Blocks.WALL_TORCH.getBlockData().set(BlockTorchWall.a, EnumDirection.NORTH);
				}
			case 1:
				switch (data)
				{
					case 0:
					case 5:
					default:
						return Blocks.REDSTONE_TORCH.getBlockData();
					case 1:
						return Blocks.REDSTONE_WALL_TORCH.getBlockData().set(BlockRedstoneTorchWall.LIT, false).set(BlockRedstoneTorchWall.b, EnumDirection.EAST);
					case 2:
						return Blocks.REDSTONE_WALL_TORCH.getBlockData().set(BlockRedstoneTorchWall.LIT, false).set(BlockRedstoneTorchWall.b, EnumDirection.WEST);
					case 3:
						return Blocks.REDSTONE_WALL_TORCH.getBlockData().set(BlockRedstoneTorchWall.LIT, false).set(BlockRedstoneTorchWall.b, EnumDirection.SOUTH);
					case 4:
						return Blocks.REDSTONE_WALL_TORCH.getBlockData().set(BlockRedstoneTorchWall.LIT, false).set(BlockRedstoneTorchWall.b, EnumDirection.NORTH);
				}
			case 2:
				switch (data)
				{
					case 0:
					case 5:
					default:
						return Blocks.REDSTONE_TORCH.getBlockData().set(BlockRedstoneTorchWall.LIT, true);
					case 1:
						return Blocks.REDSTONE_WALL_TORCH.getBlockData().set(BlockRedstoneTorchWall.LIT, true).set(BlockRedstoneTorchWall.b, EnumDirection.EAST);
					case 2:
						return Blocks.REDSTONE_WALL_TORCH.getBlockData().set(BlockRedstoneTorchWall.LIT, true).set(BlockRedstoneTorchWall.b, EnumDirection.WEST);
					case 3:
						return Blocks.REDSTONE_WALL_TORCH.getBlockData().set(BlockRedstoneTorchWall.LIT, true).set(BlockRedstoneTorchWall.b, EnumDirection.SOUTH);
					case 4:
						return Blocks.REDSTONE_WALL_TORCH.getBlockData().set(BlockRedstoneTorchWall.LIT, true).set(BlockRedstoneTorchWall.b, EnumDirection.NORTH);
				}
			default:
				return null;
		}
	}

	private static IBlockData getRailsWithData (int material, int data)
	{
		int shape = getBits(data, 0, 3);
		int active = getBit(data, 3);
		switch (material)
		{
			case 0:
				return Blocks.POWERED_RAIL.getBlockData().set(BlockPoweredRail.SHAPE, getRailShape(shape)).set(BlockPoweredRail.POWERED, active == 1);
			case 1:
				return Blocks.DETECTOR_RAIL.getBlockData().set(BlockMinecartDetector.SHAPE, getRailShape(shape)).set(BlockMinecartDetector.POWERED, active == 1);
			case 2:
				return Blocks.ACTIVATOR_RAIL.getBlockData().set(BlockPoweredRail.SHAPE, getRailShape(shape)).set(BlockPoweredRail.POWERED, active == 1);
			default:
				return null;
		}
	}

	private static IBlockData getStairsWithData (int material, int data)
	{
		int facing = getBits(data, 0, 2);
		int half = getBit(data, 2);
		IBlockData output;
		switch (material)
		{
			case 0:
				output = Blocks.OAK_STAIRS.getBlockData();
				break;
			case 1:
				output = Blocks.BIRCH_STAIRS.getBlockData();
				break;
			case 2:
				output = Blocks.SPRUCE_STAIRS.getBlockData();
				break;
			case 3:
				output = Blocks.JUNGLE_STAIRS.getBlockData();
				break;
			case 4:
				output = Blocks.COBBLESTONE_STAIRS.getBlockData();
				break;
			case 5:
				output = Blocks.BRICK_STAIRS.getBlockData();
				break;
			case 6:
				output = Blocks.STONE_STAIRS.getBlockData();
				break;
			case 7:
				output = Blocks.NETHER_BRICK_STAIRS.getBlockData();
				break;
			case 8:
				output = Blocks.SANDSTONE_STAIRS.getBlockData();
				break;
			case 9:
				output = Blocks.QUARTZ_STAIRS.getBlockData();
				break;
			case 10:
				output = Blocks.ACACIA_STAIRS.getBlockData();
				break;
			case 11:
				output = Blocks.DARK_OAK_STAIRS.getBlockData();
				break;
			case 12:
				output = Blocks.RED_SANDSTONE_STAIRS.getBlockData();
				break;
			case 13:
				output = Blocks.PURPUR_STAIRS.getBlockData();
				break;
			case 14:
				output = Blocks.STONE_BRICK_STAIRS.getBlockData();
				break;
			default:
				return null;
		}
		return output
				.set(BlockStairs.FACING, getFacingEastWestSouthNorth(facing))
				.set(BlockStairs.HALF, half == 0 ? BlockPropertyHalf.BOTTOM : BlockPropertyHalf.TOP);
	}

	private static IBlockData getLeverOrButtonWithData (int material, int data)
	{
		int facing = getBits(data, 0, 3);
		int powered = getBit(data, 3);
		BlockPropertyAttachPosition face = facing == 0 || facing == 7 ? BlockPropertyAttachPosition.CEILING : facing == 1 || facing == 2 || facing == 3 || facing == 4 ? BlockPropertyAttachPosition.WALL : facing == 5 || facing == 6 ? BlockPropertyAttachPosition.FLOOR : BlockPropertyAttachPosition.FLOOR;

		switch (material)
		{
			case 0:
				return Blocks.LEVER.getBlockData().set(BlockLever.FACE, face).set(BlockLever.FACING, getFacingLever(facing)).set(BlockLever.POWERED, powered == 1);
			case 1:
				return Blocks.STONE_BUTTON.getBlockData().set(BlockStoneButton.FACE, face).set(BlockStoneButton.FACING, getFacingButton(facing)).set(BlockStoneButton.POWERED, powered == 1);
			case 2:
				return Blocks.OAK_BUTTON.getBlockData().set(BlockWoodButton.FACE, face).set(BlockWoodButton.FACING, getFacingButton(facing)).set(BlockWoodButton.POWERED, powered == 1);
			default:
				return null;
		}
	}

	private static IBlockData getDoorWithData (int material, int data)
	{
		// 0x8 Half
		// Top half of door:
		// 0x1 Hinge side
		// 0x2 powered
		// Bottom half of door:
		// 0x1 0x2 facing
		// 0x4 closed/open
		int half = getBit(data, 3);
		int hinge = getBit(data, 0);
		int powered = getBit(data, 1);
		int facing = getBits(data, 0, 2);
		int open = getBit(data, 2);
		IBlockData blockState;
		switch (material)
		{
			case 0:
				blockState = Blocks.OAK_DOOR.getBlockData();
				break;
			case 1:
				blockState = Blocks.IRON_DOOR.getBlockData();
				break;
			case 2:
				blockState = Blocks.SPRUCE_DOOR.getBlockData();
				break;
			case 3:
				blockState = Blocks.BIRCH_DOOR.getBlockData();
				break;
			case 4:
				blockState = Blocks.JUNGLE_DOOR.getBlockData();
				break;
			case 5:
				blockState = Blocks.ACACIA_DOOR.getBlockData();
				break;
			case 6:
				blockState = Blocks.DARK_OAK_DOOR.getBlockData();
				break;
			default:
				return null;
		}
		return half == 0 ?
			   blockState
					   .set(BlockDoor.HALF, BlockPropertyDoubleBlockHalf.LOWER)
					   .set(BlockDoor.FACING, getFacingEastSouthWestNorth(facing))
					   .set(BlockDoor.OPEN, open == 1)
						 : blockState
					   .set(BlockDoor.HALF, BlockPropertyDoubleBlockHalf.UPPER)
					   .set(BlockDoor.HINGE, hinge == 0 ? BlockPropertyDoorHinge.LEFT : BlockPropertyDoorHinge.RIGHT)
					   .set(BlockDoor.POWERED, powered == 1)
				;
	}

	private static IBlockData getSignPostWithData (int data)
	{
		int rotation = getBits(data, 0, 4);
		// TODO: Hopefully rotation is still mapped to the same int values as 1.12..
		return Blocks.OAK_SIGN.getBlockData().set(BlockFloorSign.ROTATION, rotation);
	}

	private static IBlockData getWallSignWithData (int data)
	{
		int facing = getBits(data, 0, 3);
		return Blocks.OAK_WALL_SIGN.getBlockData().set(BlockWallSign.FACING, getFacingNorthSouthWestEast(facing));
	}

	// TODO: Can't find information on 1.12 command block block data, what about facing?
	private static IBlockData getCommandBlockWithData (int material, int data)
	{
		IBlockData blockState;
		switch (material)
		{
			case 0:
				blockState = Blocks.COMMAND_BLOCK.getBlockData();
				break;
			case 1:
				blockState = Blocks.REPEATING_COMMAND_BLOCK.getBlockData();
				break;
			case 2:
				blockState = Blocks.CHAIN_COMMAND_BLOCK.getBlockData();
				break;
			default:
				return null;
		}
		return blockState;
	}

	private static IBlockData getShulkerBoxWithData (int material, int data)
	{
		IBlockData blockState;
		switch (material)
		{
			case 0:
				blockState = Blocks.WHITE_SHULKER_BOX.getBlockData();
				break;
			case 1:
				blockState = Blocks.ORANGE_SHULKER_BOX.getBlockData();
				break;
			case 2:
				blockState = Blocks.MAGENTA_SHULKER_BOX.getBlockData();
				break;
			case 3:
				blockState = Blocks.LIGHT_BLUE_SHULKER_BOX.getBlockData();
				break;
			case 4:
				blockState = Blocks.YELLOW_SHULKER_BOX.getBlockData();
				break;
			case 5:
				blockState = Blocks.LIME_SHULKER_BOX.getBlockData();
				break;
			case 6:
				blockState = Blocks.PINK_SHULKER_BOX.getBlockData();
				break;
			case 7:
				blockState = Blocks.GRAY_SHULKER_BOX.getBlockData();
				break;
			case 8:
				blockState = Blocks.LIGHT_GRAY_SHULKER_BOX.getBlockData();
				break;
			case 9:
				blockState = Blocks.CYAN_SHULKER_BOX.getBlockData();
				break;
			case 10:
				blockState = Blocks.PURPLE_SHULKER_BOX.getBlockData();
				break;
			case 11:
				blockState = Blocks.BLUE_SHULKER_BOX.getBlockData();
				break;
			case 12:
				blockState = Blocks.BROWN_SHULKER_BOX.getBlockData();
				break;
			case 13:
				blockState = Blocks.GREEN_SHULKER_BOX.getBlockData();
				break;
			case 14:
				blockState = Blocks.RED_SHULKER_BOX.getBlockData();
				break;
			case 15:
				blockState = Blocks.BLACK_SHULKER_BOX.getBlockData();
				break;
			case 16:
				blockState = Blocks.SHULKER_BOX.getBlockData();
				break;
			default:
				return null;
		}
		return blockState.set(BlockShulkerBox.a, getFacingDownEastNorthSouthUpWest(data));
	}

	private static IBlockData getLadderChestOrFurnaceWithData (int material, int data)
	{
		int facing = getBits(data, 0, 3);
		switch (material)
		{
			case 0:
				return Blocks.LADDER.getBlockData().set(BlockLadder.FACING, getFacingNorthSouthWestEast(facing));
			case 1:
				return Blocks.CHEST.getBlockData().set(BlockChest.FACING, getFacingNorthSouthWestEast(facing));
			case 2:
				return Blocks.ENDER_CHEST.getBlockData().set(BlockEnderChest.FACING, getFacingNorthSouthWestEast(facing));
			case 3:
				return Blocks.TRAPPED_CHEST.getBlockData().set(BlockChestTrapped.FACING, getFacingNorthSouthWestEast(facing));
			case 4:
				return Blocks.FURNACE.getBlockData().set(BlockFurnace.FACING, getFacingNorthSouthWestEast(facing)).set(BlockFurnace.LIT, false);
			case 5:
				return Blocks.FURNACE.getBlockData().set(BlockFurnace.FACING, getFacingNorthSouthWestEast(facing)).set(BlockFurnace.LIT, true);
			default:
				return null;
		}
	}

	private static IBlockData getDispenserHopperDropperWithData (int material, int data)
	{
		int facing = getBits(data, 0, 3);
		int active = getBit(data, 3);
		switch (material)
		{
			case 0:
				return Blocks.DISPENSER.getBlockData().set(BlockDispenser.FACING, getFacingDownUpNorthSouthWestEast(facing)).set(BlockDispenser.TRIGGERED, active == 1);
			case 1:
				return Blocks.DROPPER.getBlockData().set(BlockDropper.FACING, getFacingDownUpNorthSouthWestEast(facing)).set(BlockDropper.TRIGGERED, active == 1);
			case 2:
				return Blocks.HOPPER.getBlockData().set(BlockHopper.FACING, getFacingDownUpNorthSouthWestEast(facing)).set(BlockHopper.ENABLED, active == 1);
			default:
				return null;
		}
	}

	private static IBlockData getJackOLanternOrPumpkinWithData (int material, int data)
	{
		int facing = getBits(data, 0, 2);
		switch (material)
		{
			case 0:
				return Blocks.CARVED_PUMPKIN.getBlockData().set(BlockPumpkinCarved.FACING, getFacingSouthWestNorthEast(facing));
			case 1:
				return Blocks.JACK_O_LANTERN.getBlockData().set(BlockPumpkinCarved.FACING, getFacingSouthWestNorthEast(facing));
			default:
				return null;
		}
	}

	private static IBlockData getObserverWithData (int data)
	{
		int facing = getBits(data, 0, 3);
		int powered = getBit(data, 3);
		return Blocks.OBSERVER.getBlockData().set(BlockObserver.FACING, getFacingDownUpNorthSouthWestEast(facing)).set(BlockObserver.b, powered == 1);
	}

	private static IBlockData getRepeaterWithData (int material, int data)
	{
		int facing = getBits(data, 0, 2);
		int delay = getBits(data, 2, 2) + 1;
		IBlockData blockState;
		switch (material)
		{
			case 0:
				blockState = Blocks.REPEATER.getBlockData().set(BlockRepeater.c, false);
				break;
			case 1:
				blockState = Blocks.REPEATER.getBlockData().set(BlockRepeater.c, true);
				break;
			default:
				return null;
		}
		return blockState
				.set(BlockRepeater.DELAY, delay)
				.set(BlockRepeater.FACING, getFacingSouthWestNorthEast(facing))
				;
	}

	private static IBlockData getComparatorWithData (int material, int data)
	{
		int facing = getBits(data, 0, 2);
		int mode = getBit(data, 2);
		int powered = material == 1 ? 1 : getBit(data, 3);
		return Blocks.COMPARATOR.getBlockData()
				.set(BlockRedstoneComparator.FACING, getFacingSouthWestNorthEast(facing))
				.set(BlockRedstoneComparator.MODE, mode == 0 ? BlockPropertyComparatorMode.COMPARE : BlockPropertyComparatorMode.SUBTRACT)
				.set(BlockRedstoneComparator.c, powered == 1)
				;
	}

	private static IBlockData getBedBlockWithData (int data)
	{
		int facing = getBits(data, 0, 2);
		int occupied = getBit(data, 2);
		int part = getBit(data, 3);
		return Blocks.RED_BED.getBlockData()
				.set(BlockBed.FACING, getFacingSouthWestNorthEast(facing))
				.set(BlockBed.OCCUPIED, occupied == 1)
				.set(BlockBed.PART, part == 0 ? BlockPropertyBedPart.FOOT : BlockPropertyBedPart.HEAD)
				;
	}

	private static IBlockData getTrapDoorBlockWithData (int material, int data)
	{
		int facing = getBits(data, 0, 2);
		int open = getBit(data, 2);
		int half = getBit(data, 3);
		IBlockData blockState;
		switch (material)
		{
			case 0:
				blockState = Blocks.OAK_TRAPDOOR.getBlockData();
				break;
			case 1:
				blockState = Blocks.IRON_TRAPDOOR.getBlockData();
				break;
			default:
				return null;
		}
		return blockState
				.set(BlockTrapdoor.FACING, getFacingSouthNorthEastWest(facing))
				.set(BlockTrapdoor.HALF, half == 0 ? BlockPropertyHalf.BOTTOM : BlockPropertyHalf.TOP)
				.set(BlockTrapdoor.OPEN, open == 1)
				;
	}

	private static IBlockData getPistonWithData (int material, int data)
	{
		int facing = getBits(data, 0, 3);
		int extended = getBit(data, 3);
		switch (material)
		{
			case 0:
				return Blocks.PISTON.getBlockData().set(BlockPiston.EXTENDED, extended == 1).set(BlockPiston.FACING, getFacingDownUpNorthSouthWestEast(facing));
			case 1:
				return Blocks.STICKY_PISTON.getBlockData().set(BlockPiston.EXTENDED, extended == 1).set(BlockPiston.FACING, getFacingDownUpNorthSouthWestEast(facing));
			default:
				return null;
		}
	}

	private static IBlockData getPistonHeadWithData (int data)
	{
		int facing = getBits(data, 0, 3);
		int type = getBit(data, 3);
		return Blocks.PISTON_HEAD.getBlockData()
				.set(BlockPistonExtension.FACING, getFacingDownUpNorthSouthWestEast(facing))
				.set(BlockPistonExtension.TYPE, type == 0 ? BlockPropertyPistonType.DEFAULT : BlockPropertyPistonType.STICKY)
				;
	}

	private static IBlockData getHugeMushroomWithData (int material, int data)
	{
		boolean down = data == 14 || data == 15;
		boolean up = data == 1 || data == 2 || data == 3 || data == 4 || data == 5 || data == 6 || data == 7 || data == 8 || data == 9 || data == 14 || data == 15;
		boolean north = data == 1 || data == 2 || data == 3 || data == 10 || data == 14 || data == 15;
		boolean east = data == 3 || data == 6 || data == 9 || data == 10 || data == 14 || data == 15;
		boolean south = data == 7 || data == 8 || data == 9 || data == 10 || data == 14 || data == 15;
		boolean west = data == 1 || data == 4 || data == 7 || data == 10 || data == 14 || data == 15;
		IBlockData blockState;
		if (data == 10 || data == 15)
		{
			blockState = Blocks.MUSHROOM_STEM.getBlockData();
		}
		else
		{
			switch (material)
			{
				case 0:
					blockState = Blocks.BROWN_MUSHROOM_BLOCK.getBlockData();
					break;
				case 1:
					blockState = Blocks.RED_MUSHROOM_BLOCK.getBlockData();
					break;
				default:
					return null;
			}
		}
		return blockState
				.set(BlockHugeMushroom.f, down)
				.set(BlockHugeMushroom.e, up)
				.set(BlockHugeMushroom.a, north)
				.set(BlockHugeMushroom.b, east)
				.set(BlockHugeMushroom.c, south)
				.set(BlockHugeMushroom.d, west)
				;
	}

	private static IBlockData getVineWithData (int data)
	{
		int south = getBit(data, 0);
		int west = getBit(data, 1);
		int north = getBit(data, 2);
		int east = getBit(data, 3);
		int up = data == 0 ? 1 : 0; // TODO: Should also be true if there's a block above, test if this is done dynamically.
		return Blocks.VINE.getBlockData()
				.set(BlockVine.EAST, east == 1)
				.set(BlockVine.NORTH, north == 1)
				.set(BlockVine.SOUTH, south == 1)
				.set(BlockVine.WEST, west == 1)
				.set(BlockVine.UP, up == 1)
				;
	}

	private static IBlockData getFenceGateWithData (int material, int data)
	{
		int facing = getBits(data, 0, 2);
		int open = getBit(data, 2);
		IBlockData blockState;
		switch (material)
		{
			case 0:
				blockState = Blocks.BIRCH_FENCE_GATE.getBlockData();
				break;
			case 1:
				blockState = Blocks.OAK_FENCE_GATE.getBlockData();
				break;
			case 2:
				blockState = Blocks.SPRUCE_FENCE_GATE.getBlockData();
				break;
			case 3:
				blockState = Blocks.JUNGLE_FENCE_GATE.getBlockData();
				break;
			case 4:
				blockState = Blocks.DARK_OAK_FENCE_GATE.getBlockData();
				break;
			case 5:
				blockState = Blocks.ACACIA_FENCE_GATE.getBlockData();
				break;
			default:
				return null;
		}
		return blockState
				.set(BlockFenceGate.FACING, getFacingSouthWestNorthEast(facing))
				.set(BlockFenceGate.OPEN, open == 1)
				;
	}

	private static IBlockData getCocoaWithData (int data)
	{
		int facing = getBits(data, 0, 2);
		int age = getBits(data, 2, 2);
		return Blocks.COCOA.getBlockData().set(BlockCocoa.FACING, getFacingNorthEastSouthWest(facing)).set(BlockCocoa.AGE, age);
	}

	private static IBlockData getTripWireHookWithData (int data)
	{
		int facing = getBits(data, 0, 2);
		int attached = getBit(data, 2);
		int powered = getBit(data, 3);
		return Blocks.TRIPWIRE_HOOK.getBlockData()
				.set(BlockTripwireHook.ATTACHED, attached == 1)
				.set(BlockTripwireHook.FACING, getFacingSouthWestNorthEast(facing))
				.set(BlockTripwireHook.POWERED, powered == 1)
				;
	}

	private static IBlockData getEndPortalFrameWithData (int data)
	{
		int facing = getBits(data, 0, 2);
		int eye = getBit(data, 2);
		return Blocks.END_PORTAL_FRAME.getBlockData().set(BlockEnderPortalFrame.EYE, eye == 1).set(BlockEnderPortalFrame.FACING, getFacingSouthWestNorthEast(facing));
	}

	private static IBlockData getStructureBlockWithData (int data)
	{
		BlockPropertyStructureMode structureBlockMode = data == 0 ? BlockPropertyStructureMode.DATA : data == 1 ? BlockPropertyStructureMode.SAVE : data == 2 ? BlockPropertyStructureMode.LOAD : data == 3 ? BlockPropertyStructureMode.LOAD : BlockPropertyStructureMode.DATA;
		return Blocks.STRUCTURE_BLOCK.getBlockData().set(BlockStructure.a, structureBlockMode);
	}

	private static IBlockData getGlazedTerracottaWithData (int material, int data)
	{
		int facing = getBits(data, 0, 2);
		IBlockData blockState;
		switch (material)
		{
			case 0:
				blockState = Blocks.BLACK_GLAZED_TERRACOTTA.getBlockData();
				break;
			case 1:
				blockState = Blocks.BLUE_GLAZED_TERRACOTTA.getBlockData();
				break;
			case 2:
				blockState = Blocks.BROWN_GLAZED_TERRACOTTA.getBlockData();
				break;
			case 3:
				blockState = Blocks.CYAN_GLAZED_TERRACOTTA.getBlockData();
				break;
			case 4:
				blockState = Blocks.GRAY_GLAZED_TERRACOTTA.getBlockData();
				break;
			case 5:
				blockState = Blocks.GREEN_GLAZED_TERRACOTTA.getBlockData();
				break;
			case 6:
				blockState = Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA.getBlockData();
				break;
			case 7:
				blockState = Blocks.LIME_GLAZED_TERRACOTTA.getBlockData();
				break;
			case 8:
				blockState = Blocks.MAGENTA_GLAZED_TERRACOTTA.getBlockData();
				break;
			case 9:
				blockState = Blocks.ORANGE_GLAZED_TERRACOTTA.getBlockData();
				break;
			case 10:
				blockState = Blocks.PINK_GLAZED_TERRACOTTA.getBlockData();
				break;
			case 11:
				blockState = Blocks.PURPLE_GLAZED_TERRACOTTA.getBlockData();
				break;
			case 12:
				blockState = Blocks.RED_GLAZED_TERRACOTTA.getBlockData();
				break;
			case 13:
				blockState = Blocks.LIGHT_GRAY_GLAZED_TERRACOTTA.getBlockData();
				break;
			case 14:
				blockState = Blocks.WHITE_GLAZED_TERRACOTTA.getBlockData();
				break;
			case 15:
				blockState = Blocks.YELLOW_GLAZED_TERRACOTTA.getBlockData();
				break;
			default:
				return Blocks.BLACK_GLAZED_TERRACOTTA.getBlockData();
		}
		return blockState.set(BlockGlazedTerracotta.FACING, getFacingSouthWestNorthEast(facing));
	}

	private static IBlockData getTripWireWithData (int data)
	{
		int active = getBit(data, 0);
		int attached = getBit(data, 2);
		int disarmed = getBit(data, 3);
		return Blocks.TRIPWIRE.getBlockData()
				.set(BlockTripwire.POWERED, active == 1)
				.set(BlockTripwire.ATTACHED, attached == 1)
				.set(BlockTripwire.DISARMED, disarmed == 1)
				;
	}

	//	

	private static EnumDirection.EnumAxis getAxisXYZ (int data)
	{
		switch (data)
		{
			case 0:
				return EnumDirection.EnumAxis.X;
			case 2:
				return EnumDirection.EnumAxis.Z;
			case 1:
			default:
				return EnumDirection.EnumAxis.Y;
		}
	}

	private static EnumDirection.EnumAxis getAxisXZ (int data)
	{
		switch (data)
		{
			case 2:
				return EnumDirection.EnumAxis.Z;
			case 1:
			default:
				return EnumDirection.EnumAxis.X;
		}
	}

	private static EnumDirection getFacingSouthWestNorthEast (int data)
	{
		switch (data)
		{
			case 1:
				return EnumDirection.WEST;
			case 2:
				return EnumDirection.NORTH;
			case 3:
				return EnumDirection.EAST;
			case 0:
			default:
				return EnumDirection.SOUTH;
		}
	}

	private static EnumDirection getFacingNorthSouthWestEast (int data)
	{
		switch (data)
		{
			case 3:
				return EnumDirection.SOUTH;
			case 4:
				return EnumDirection.WEST;
			case 5:
				return EnumDirection.EAST;
			case 2:
			default:
				return EnumDirection.NORTH;
		}
	}

	private static EnumDirection getFacingNorthEastSouthWest (int data)
	{
		switch (data)
		{
			case 1:
				return EnumDirection.EAST;
			case 2:
				return EnumDirection.SOUTH;
			case 3:
				return EnumDirection.WEST;
			case 0:
			default:
				return EnumDirection.NORTH;
		}
	}

	private static EnumDirection getFacingDownUpNorthSouthWestEast (int data)
	{
		switch (data)
		{
			case 1:
				return EnumDirection.UP;
			case 2:
				return EnumDirection.NORTH;
			case 3:
				return EnumDirection.SOUTH;
			case 4:
				return EnumDirection.WEST;
			case 5:
				return EnumDirection.EAST;
			case 0:
			default:
				return EnumDirection.DOWN;
		}
	}

	private static EnumDirection getFacingSouthNorthEastWest (int data)
	{
		switch (data)
		{
			case 1:
				return EnumDirection.NORTH;
			case 2:
				return EnumDirection.EAST;
			case 3:
				return EnumDirection.WEST;
			case 0:
			default:
				return EnumDirection.SOUTH;
		}
	}

	private static EnumDirection getFacingEastSouthWestNorth (int data)
	{
		switch (data)
		{
			case 1:
				return EnumDirection.SOUTH;
			case 2:
				return EnumDirection.WEST;
			case 3:
				return EnumDirection.NORTH;
			case 0:
			default:
				return EnumDirection.EAST;
		}
	}

	private static EnumDirection getFacingEastWestSouthNorth (int data)
	{
		switch (data)
		{
			case 1:
				return EnumDirection.WEST;
			case 2:
				return EnumDirection.SOUTH;
			case 3:
				return EnumDirection.NORTH;
			case 0:
			default:
				return EnumDirection.EAST;
		}
	}

	// TODO: Couldn't find docs for 1.12.2 shulker box	
	// data values, these rotations may be incorrect.
	private static EnumDirection getFacingDownEastNorthSouthUpWest (int data)
	{
		switch (data)
		{
			case 0:
				return EnumDirection.DOWN;
			case 1:
				return EnumDirection.EAST;
			case 2:
				return EnumDirection.NORTH;
			case 3:
				return EnumDirection.SOUTH;
			case 5:
				return EnumDirection.WEST;
			case 4:
			default:
				return EnumDirection.UP;
		}
	}
	
	private static EnumDirection.EnumAxis getPillarAxisXYZ(int data)
	{
		switch(data)
		{
			case 0:
				return EnumDirection.EnumAxis.Y;
			case 4:
				return EnumDirection.EnumAxis.X;
			case 8:
				return EnumDirection.EnumAxis.Z;
			default:
				return EnumDirection.EnumAxis.Y;
		}
	}

	// TODO: Test this
	private static EnumDirection getFacingLever (int data)
	{
		switch (data)
		{
			case 2:
				return EnumDirection.WEST;
			case 3:
			case 5:
			case 7:
				return EnumDirection.SOUTH;
			case 4:
				return EnumDirection.NORTH;
			case 0:
			case 1:
			case 6:
			default:
				return EnumDirection.EAST;
		}
	}

	private static EnumDirection getFacingButton (int data)
	{
		switch (data)
		{
			case 2:
				return EnumDirection.WEST;
			case 3:
				return EnumDirection.SOUTH;
			case 4:
				return EnumDirection.NORTH;
			case 1:
			default:
				return EnumDirection.EAST;
		}
	}

	private static BlockPropertyTrackPosition getRailShape (int shape)
	{
		switch (shape)
		{
			case 1:
				return BlockPropertyTrackPosition.EAST_WEST;
			case 2:
				return BlockPropertyTrackPosition.ASCENDING_EAST;
			case 3:
				return BlockPropertyTrackPosition.ASCENDING_WEST;
			case 4:
				return BlockPropertyTrackPosition.ASCENDING_NORTH;
			case 5:
				return BlockPropertyTrackPosition.ASCENDING_SOUTH;
			case 6:
				return BlockPropertyTrackPosition.SOUTH_EAST;
			case 7:
				return BlockPropertyTrackPosition.SOUTH_WEST;
			case 8:
				return BlockPropertyTrackPosition.NORTH_WEST;
			case 9:
				return BlockPropertyTrackPosition.NORTH_EAST;
			case 0:
			default:
				return BlockPropertyTrackPosition.NORTH_SOUTH;
		}
	}

	private static int getBits (int source, int index, int length)
	{
		int bits = 0;
		for (int i = 0; i < length; i++)
		{
			bits = bits | (getBit(source, index + i) << i);
		}
		return bits;
	}

	private static int getBit (int source, int index)
	{
		return ((source & (1 << index)) >> index);
	}
}
