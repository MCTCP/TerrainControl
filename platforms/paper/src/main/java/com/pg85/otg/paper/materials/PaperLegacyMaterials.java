package com.pg85.otg.paper.materials;

import com.pg85.otg.OTG;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;
import com.pg85.otg.util.minecraft.BlockNames;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;

public class PaperLegacyMaterials
{
	static BlockState fromLegacyBlockName (String oldBlockName)
	{
		if (oldBlockName.matches("minecraft:[A-Za-z]+:[0-9]+")) {
			int stateId = Integer.parseInt(oldBlockName.split(":")[2]);
			return fromLegacyBlockNameOrIdWithData(oldBlockName.split(":")[1], stateId);
		}
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
			case "flowing_water":
				return Blocks.WATER.defaultBlockState();
			case "stationary_lava":
			case "flowing_lava":
				return Blocks.LAVA.defaultBlockState();
			case "stained_clay":
				return Blocks.WHITE_TERRACOTTA.defaultBlockState();
			case "hard_clay":
				return Blocks.TERRACOTTA.defaultBlockState();
			case "step":
				return Blocks.SMOOTH_STONE_SLAB.defaultBlockState();
			case "sugar_cane_block":
				return Blocks.SUGAR_CANE.defaultBlockState();
			case "melon_block":
				return Blocks.MELON.defaultBlockState();
			case "water_lily":
				return Blocks.LILY_PAD.defaultBlockState();
			case "soil":
				return Blocks.FARMLAND.defaultBlockState();
			case "grass":
				return Blocks.GRASS_BLOCK.defaultBlockState();
			case "long_grass":
				return Blocks.TALL_GRASS.defaultBlockState();
			case "mycel":
				return Blocks.MYCELIUM.defaultBlockState();
			case "snow_layer":
				return Blocks.SNOW.defaultBlockState();
			case "leaves":
				return Blocks.OAK_LEAVES.defaultBlockState().set(LeavesBlock.DISTANCE, 1);
			case "leaves_2":
				return Blocks.ACACIA_LEAVES.defaultBlockState().set(LeavesBlock.DISTANCE, 1);
			case "red_rose":
				return Blocks.POPPY.defaultBlockState();
			// TODO: This only spawns the bottom half?
			case "double_plant":
				return Blocks.SUNFLOWER.defaultBlockState();

			case "wood_stairs":
			case "oak_stairs":
				return Blocks.OAK_STAIRS.defaultBlockState().set(StairBlock.FACING, EnumDirection.EAST);
			case "birch_wood_stairs":
			case "birch_stairs":
				return Blocks.BIRCH_STAIRS.defaultBlockState().set(StairBlock.FACING, EnumDirection.EAST);
			case "spruce_wood_stairs":
			case "spruce_stairs":
				return Blocks.SPRUCE_STAIRS.defaultBlockState().set(StairBlock.FACING, EnumDirection.EAST);
			case "jungle_wood_stairs":
			case "jungle_stairs":
				return Blocks.JUNGLE_STAIRS.defaultBlockState().set(StairBlock.FACING, EnumDirection.EAST);
			case "cobblestone_stairs":
			case "stone_stairs":
				return Blocks.COBBLESTONE_STAIRS.defaultBlockState().set(StairBlock.FACING, EnumDirection.EAST);
			case "stone_brick_stairs":
			case "smooth_stairs":
				return Blocks.STONE_BRICK_STAIRS.defaultBlockState().set(StairBlock.FACING, EnumDirection.EAST);
			case "brick_stairs":
				return Blocks.BRICK_STAIRS.defaultBlockState().set(StairBlock.FACING, EnumDirection.EAST);
			case "nether_brick_stairs":
				return Blocks.NETHER_BRICK_STAIRS.defaultBlockState().set(StairBlock.FACING, EnumDirection.EAST);
			case "sandstone_stairs":
				return Blocks.SANDSTONE_STAIRS.defaultBlockState().set(StairBlock.FACING, EnumDirection.EAST);
			case "quartz_stairs":
				return Blocks.QUARTZ_STAIRS.defaultBlockState().set(StairBlock.FACING, EnumDirection.EAST);
			case "acacia_stairs":
				return Blocks.ACACIA_STAIRS.defaultBlockState().set(StairBlock.FACING, EnumDirection.EAST);
			case "dark_oak_stairs":
				return Blocks.DARK_OAK_STAIRS.defaultBlockState().set(StairBlock.FACING, EnumDirection.EAST);
			case "red_sandstone_stairs":
				return Blocks.RED_SANDSTONE_STAIRS.defaultBlockState().set(StairBlock.FACING, EnumDirection.EAST);
			case "purpur_stairs":
				return Blocks.PURPUR_STAIRS.defaultBlockState().set(StairBlock.FACING, EnumDirection.EAST);
			case "wooden_button":
			case "wood_button":
				return Blocks.OAK_BUTTON.defaultBlockState().set(BlockWoodButton.FACING, EnumDirection.NORTH);
			case "waterlily":
				return Blocks.LILY_PAD.defaultBlockState();
			case "quartz_ore":
				return Blocks.NETHER_QUARTZ_ORE.defaultBlockState();				
			case "yellow_flower":
				return Blocks.DANDELION.defaultBlockState();
			case "web":
				return Blocks.COBWEB.defaultBlockState();
			case "standing_banner":
				return Blocks.WHITE_BANNER.defaultBlockState();
			case "wall_banner":
				return Blocks.WHITE_WALL_BANNER.defaultBlockState();
			case "redstone_lamp_on":
				return Blocks.REDSTONE_LAMP.defaultBlockState().set(BlockRedstoneLamp.a, true);
			case "redstone_lamp_off":
				return Blocks.REDSTONE_LAMP.defaultBlockState().set(BlockRedstoneLamp.a, false);
			case "wool":
				return Blocks.WHITE_WOOL.defaultBlockState();
			case "log":
			case "wood":
				return Blocks.OAK_LOG.defaultBlockState();
			case "log_2":
				return Blocks.ACACIA_LOG.defaultBlockState();
			case "magma":
				return Blocks.MAGMA_BLOCK.defaultBlockState();
			case "tallgrass":
				return Blocks.GRASS.defaultBlockState();
			case "cobble_wall":
				return Blocks.COBBLESTONE_WALL.defaultBlockState();
			case "iron_fence":
				return Blocks.IRON_BARS.defaultBlockState();
			case "workbench":
				return Blocks.CRAFTING_TABLE.defaultBlockState();
			case "enchantment_table":
				return Blocks.ENCHANTING_TABLE.defaultBlockState();
			case "mob_spawner":
				return Blocks.INFESTED_STONE.defaultBlockState();
			case "double_step":
				return Registry.BLOCK.get(new ResourceLocation("minecraft:smooth_stone_slab"))
						.defaultBlockState().set(BlockStepAbstract.a,
								BlockPropertySlabType.DOUBLE);
			case "smooth_brick":
				return Blocks.STONE_BRICKS.defaultBlockState();
			case "rails":
				return Blocks.RAIL.defaultBlockState();
			case "fence":
				return Blocks.OAK_FENCE.defaultBlockState();
			case "nether_fence":
				return Blocks.NETHER_BRICK_FENCE.defaultBlockState();				
			case "wood_step":
				return Blocks.OAK_SLAB.defaultBlockState();
			case "thin_glass":
				return Blocks.GLASS_PANE.defaultBlockState();
			case "stained_glass_pane":
				return Blocks.WHITE_STAINED_GLASS_PANE.defaultBlockState();
			case "stone_plate":
				return Blocks.STONE_PRESSURE_PLATE.defaultBlockState();
			case "wood_plate":
				return Blocks.OAK_PRESSURE_PLATE.defaultBlockState();
			case "wood_double_step":
				return Blocks.OAK_SLAB.defaultBlockState().setValue(BlockStepAbstract.a, BlockPropertySlabType.DOUBLE);
			case "brick":
				return Blocks.BRICKS.defaultBlockState();
			case "iron_door_block":
				return Blocks.IRON_DOOR.defaultBlockState();
			case "carpet":
				return Blocks.WHITE_CARPET.defaultBlockState();
			case "carrot":
				return Blocks.CARROTS.defaultBlockState();
			case "skull":
				return Blocks.SKELETON_SKULL.defaultBlockState();
			case "nether_wart":
				return Blocks.NETHER_WART.defaultBlockState();				
			case "nether_wart_block":
				return Blocks.NETHER_WART_BLOCK.defaultBlockState();				
			case "nether_brick":
				return Blocks.NETHER_BRICKS.defaultBlockState();
			case "red_nether_brick":
				return Blocks.RED_NETHER_BRICKS.defaultBlockState();				
			case "end_bricks":
			case "ender_bricks":
				return Blocks.END_STONE_BRICKS.defaultBlockState();
			case "end_stone":
			case "ender_stone":
				return Blocks.END_STONE.defaultBlockState();				
			case "mcpitman":
				return Blocks.CREEPER_HEAD.defaultBlockState();
			case "pg85":
				return Blocks.SHROOMLIGHT.defaultBlockState();
			case "supercoder":
				return Blocks.CAKE.defaultBlockState();
			case "authvin":
				return Blocks.WET_SPONGE.defaultBlockState();
			case "josh":
				return Blocks.BARREL.defaultBlockState();
			case "wahrheit":
				return Blocks.LECTERN.defaultBlockState();
			case "lordsmellypants":
				return Blocks.FLOWER_POT.defaultBlockState();
			case "frank":
				return Blocks.JUKEBOX.defaultBlockState();				
			default:
				return null;
		}
	}

	static BlockState fromLegacyBlockNameOrIdWithData (String blockName, int data)
	{
		if (blockName == null || blockName.trim().isEmpty())
		{
			return null;
		}

		try
		{
			int blockId = Integer.parseInt(blockName);
			blockName = BlockNames.blockNameFromLegacyBlockId(blockId);
			if (blockName == null)
			{
				return null;
			}
		}
		catch (NumberFormatException ignored) { }

		try
		{
			switch (blockName)
			{
				// Support "GRASS:0" here, or it will be misinterpreted as the new grass (plant)
				case "grass":
					if (data == 0)
					{
						return Blocks.GRASS_BLOCK.defaultBlockState();
					}

					// Legacy blocks with block data that are now their own block
				case "banner":
				case "white_banner":
					return Registry.BLOCK.get(getFlatKey("minecraft:banner", data)).defaultBlockState();

				// TODO: How does facing for bed blocks in bo's work for 1.12.2, can only specify color via data?
				case "bed":
				case "white_bed":
					return Registry.BLOCK.get(getFlatKey("minecraft:bed", data)).defaultBlockState();

				case "carpet":
				case "white_carpet":
					return Registry.BLOCK.get(getFlatKey("minecraft:carpet", data)).defaultBlockState();

				case "cobblestone_wall":
				case "cobble_wall":
					switch (data)
					{
						case 0:
						default:
							return Blocks.COBBLESTONE_WALL.defaultBlockState();
						case 1:
							return Blocks.MOSSY_COBBLESTONE_WALL.defaultBlockState();
					}
				case "concrete":
				case "white_concrete":
					return Registry.BLOCK.get(getFlatKey("minecraft:concrete", data)).defaultBlockState();

				case "concrete_powder":
				case "white_concrete_powder":
					return Registry.BLOCK.get(getFlatKey("minecraft:concrete_powder", data)).defaultBlockState();

				case "dirt":
					return Registry.BLOCK.get(getFlatKey("minecraft:dirt", data)).defaultBlockState();

				// TODO: This only spawns the bottom half?
				case "double_plant":
				case "sunflower":
				case "rose_bush":
				case "tall_grass":
					return Registry.BLOCK.get(getFlatKey("minecraft:double_plant", data)).defaultBlockState();

				case "double_stone_slab":
				case "smooth_stone":
					return Registry.BLOCK.get(getFlatKey("minecraft:stone_slab", data))
							.defaultBlockState().setValue(BlockStepAbstract.a, BlockPropertySlabType.DOUBLE);

				// TODO: Did this even exist for 1.12.2?
				case "double_wooden_slab":
				case "wood_double_step":
					return Registry.BLOCK.get(getFlatKey("minecraft:wooden_slab", data))
							.defaultBlockState().setValue(BlockStepAbstract.a, BlockPropertySlabType.DOUBLE);

				case "leaves":
				case "oak_leaves":
					return Registry.BLOCK.get(getFlatKey("minecraft:leaves", data % 4))
							.defaultBlockState().setValue(LeavesBlock.DISTANCE, 1);

				case "leaves2":
				case "leaves_2":
				case "acacia_leaves":
					return Registry.BLOCK.get(getFlatKey("minecraft:leaves2", data % 4)).defaultBlockState();

				case "monster_egg":
				case "monster_eggs":
				case "infested_stone":
					return Registry.BLOCK.get(getFlatKey("minecraft:monster_egg", data)).defaultBlockState();

				case "planks":
				case "wood":
				case "oak_planks":
					return Registry.BLOCK.get(getFlatKey("minecraft:planks", data)).defaultBlockState();

				case "prismarine":
					return Registry.BLOCK.get(getFlatKey("minecraft:prismarine", data)).defaultBlockState();

				case "purpur_slab":
					return Blocks.PURPUR_SLAB.defaultBlockState()
						.set(BlockStepAbstract.a,
							data == 0 ? BlockPropertySlabType.BOTTOM :
							data == 8 ? BlockPropertySlabType.TOP : BlockPropertySlabType.BOTTOM);
				case "purpur_double_slab":
					return Blocks.PURPUR_SLAB.defaultBlockState()
						.set(BlockStepAbstract.a, BlockPropertySlabType.DOUBLE);		

				case "red_flower":
				case "red_rose":
					return Registry.BLOCK.get(getFlatKey("minecraft:red_flower", data)).defaultBlockState();

				case "red_sandstone":
					return Registry.BLOCK.get(getFlatKey("minecraft:red_sandstone", data)).defaultBlockState();

				case "red_sandstone_slab":
				case "stone_slab2":
					switch (data)
					{
						case 0:
						default:
							return Blocks.RED_SANDSTONE_SLAB.defaultBlockState().set(BlockStepAbstract.a, BlockPropertySlabType.BOTTOM);
						case 8:
							return Blocks.RED_SANDSTONE_SLAB.defaultBlockState().set(BlockStepAbstract.a, BlockPropertySlabType.TOP);
					}
				case "double_red_sandstone_slab":
				case "double_stone_slab2":
					return Blocks.RED_SANDSTONE_SLAB.defaultBlockState().set(BlockStepAbstract.a, BlockPropertySlabType.DOUBLE);

				case "sand":
					switch (data)
					{
						case 0:
						default:
							return Blocks.SAND.defaultBlockState();
						case 1:
							return Blocks.RED_SAND.defaultBlockState();
					}

				case "sandstone":
					return Registry.BLOCK.get(getFlatKey("minecraft:sandstone", data)).defaultBlockState();

				case "sapling":
				case "oak_sapling":
					return Registry.BLOCK.get(getFlatKey("minecraft:sapling", data)).defaultBlockState();

				case "skull":
				case "skeleton_skull":
					return Blocks.SKELETON_SKULL.defaultBlockState().set(BlockSkull.a, data);
				case "sponge":
					switch (data)
					{
						case 0:
						default:
							return Blocks.SPONGE.defaultBlockState();
						case 1:
							return Blocks.WET_SPONGE.defaultBlockState();
					}
				case "stained_glass":
				case "white_stained_glass":
					return Registry.BLOCK.get(getFlatKey("minecraft:stained_glass", data)).defaultBlockState();

				case "stained_glass_pane":
				case "white_stained_glass_pane":
				case "thin_glass":
					return Registry.BLOCK.get(getFlatKey("minecraft:stained_glass_pane", data)).defaultBlockState();

				case "stained_hardened_clay":
				case "stained_clay":
				case "white_terracotta":
					return Registry.BLOCK.get(getFlatKey("minecraft:stained_hardened_clay", data)).defaultBlockState();

				case "stone":
					return Registry.BLOCK.get(getFlatKey("minecraft:stone", data)).defaultBlockState();

				case "stone_slab":
				case "step":
					return Registry.BLOCK.get(getFlatKey("minecraft:stone_slab", data % 8))
						.defaultBlockState().set(BlockStepAbstract.a,
							data >= 8 ? BlockPropertySlabType.TOP : BlockPropertySlabType.BOTTOM);

				case "double_step":
					return Registry.BLOCK.get(getFlatKey("minecraft:stone_slab", data % 8))
						.defaultBlockState().set(BlockStepAbstract.a, BlockPropertySlabType.DOUBLE);					
					
				case "stonebrick":
				case "stone_bricks":
				case "smooth_brick":
					return Registry.BLOCK.get(getFlatKey("minecraft:stonebrick", data)).defaultBlockState();

				case "tallgrass":
				case "long_grass":
					switch (data)
					{
						case 1:
						default:
							return Blocks.GRASS.defaultBlockState();
						case 2:
							return Blocks.FERN.defaultBlockState();
					}
				case "wooden_slab":
				case "wood_step":
				case "oak_slab":
					return Registry.BLOCK.get(getFlatKey("minecraft:wooden_slab", data % 8))
							.defaultBlockState().set(BlockStepAbstract.a,
									data >= 8 ? BlockPropertySlabType.TOP : BlockPropertySlabType.BOTTOM);

				case "wool":
				case "white_wool":
					return Registry.BLOCK.get(getFlatKey("minecraft:wool", data)).defaultBlockState();


				// Blocks with data
				case "fire":
					return Blocks.FIRE.defaultBlockState().set(BlockFire.AGE, data);
				case "cake":
				case "cake_block":
					return Blocks.CAKE.defaultBlockState().set(BlockCake.BITES, data);
				case "stone_pressure_plate":
				case "stone_plate":
					return Blocks.STONE_PRESSURE_PLATE.defaultBlockState().set(BlockPressurePlateBinary.POWERED, getBit(data, 0) == 1);
				case "wooden_pressure_plate":
				case "wood_plate":
				case "oak_pressure_plate":
					return Blocks.OAK_PRESSURE_PLATE.defaultBlockState().set(BlockPressurePlateBinary.POWERED, getBit(data, 0) == 1);
				case "light_weighted_pressure_plate":
				case "gold_plate":					
					return Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE.defaultBlockState().set(BlockPressurePlateWeighted.POWER, data);
				case "heavy_weighted_pressure_plate":
				case "iron_plate":					
					return Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE.defaultBlockState().set(BlockPressurePlateWeighted.POWER, data);
				case "snow_layer":
					return Blocks.SNOW.defaultBlockState().set(BlockSnow.LAYERS, data);
				case "cactus":
					return Blocks.CACTUS.defaultBlockState().set(BlockCactus.AGE, data);
				case "reeds":
					return Blocks.SUGAR_CANE.defaultBlockState().set(BlockReed.AGE, data);
				case "jukebox":
					return Blocks.JUKEBOX.defaultBlockState().set(BlockJukeBox.HAS_RECORD, data == 1);
				case "wheat":
				case "crops":
					return Blocks.WHEAT.defaultBlockState().set(BlockCrops.AGE, data);
				case "carrot":
				case "carrots":
					return Blocks.CARROTS.defaultBlockState().set(BlockCarrots.AGE, data);
				case "potato":
				case "potatoes":
					return Blocks.POTATOES.defaultBlockState().set(BlockPotatoes.AGE, data);
				case "beetroot":
				case "beetroots":
					return Blocks.BEETROOTS.defaultBlockState().set(BlockBeetroot.AGE, data);
				case "farmland":
				case "soil":
					return Blocks.FARMLAND.defaultBlockState().set(BlockSoil.MOISTURE, data);
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
					return Blocks.RAIL.defaultBlockState().set(BlockMinecartTrack.SHAPE, getRailShape(data));
				case "powered_rail":
				case "golden_rail":
					return getRailsWithData(0, data);
				case "detector_rail":
					return getRailsWithData(1, data);
				case "activator_rail":
					return getRailsWithData(2, data);
				case "hay_block":
					return Blocks.HAY_BLOCK.defaultBlockState().set(BlockHay.AXIS, getPillarAxisXYZ(data));
				case "bone_block":
					return Blocks.BONE_BLOCK.defaultBlockState().set(BlockRotatable.AXIS, getAxisXYZ(data));
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
					return Blocks.RED_BANNER.defaultBlockState().set(BlockBanner.ROTATION, data);
				case "wall_banner":
					return Blocks.WHITE_WALL_BANNER.defaultBlockState().set(BlockBannerWall.a, getFacingNorthSouthWestEast(data));
				case "end_rod":
					return Blocks.END_ROD.defaultBlockState().set(BlockEndRod.FACING, getFacingDownUpNorthSouthWestEast(data));
				case "daylight_detector":
					return Blocks.DAYLIGHT_DETECTOR.defaultBlockState().set(BlockDaylightDetector.POWER, data);
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
					return Blocks.PUMPKIN_STEM.defaultBlockState().set(BlockStem.AGE, data);
				case "melon_stem":
					// TODO: Hopefully this auto-updates to ATTACHED_MELON_STEM when placed next to a melon block..
					return Blocks.MELON_STEM.defaultBlockState().set(BlockStem.AGE, data);
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
					return Blocks.REDSTONE_WIRE.defaultBlockState().set(BlockRedstoneWire.POWER, data);
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
					return Blocks.PURPUR_PILLAR.defaultBlockState().set(BlockRotatable.AXIS, getPillarAxisXYZ(data));
				case "nether_wart":
					return Blocks.NETHER_WART.defaultBlockState().set(BlockNetherWart.AGE, data);
				case "brewing_stand":
					return Blocks.BREWING_STAND.defaultBlockState()
							.set(BlockBrewingStand.HAS_BOTTLE[0], getBit(data, 0) == 1)
							.set(BlockBrewingStand.HAS_BOTTLE[1], getBit(data, 1) == 1)
							.set(BlockBrewingStand.HAS_BOTTLE[2], getBit(data, 2) == 1)
							;
				case "cauldron":
					return Blocks.CAULDRON.defaultBlockState().set(BlockCauldron.LEVEL, data);
				case "portal":
					return Blocks.NETHER_PORTAL.defaultBlockState().set(BlockPortal.AXIS, getAxisXZ(data));
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
			if(OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.CONFIGS))
			{
				OTG.getEngine().getLogger().log(LogLevel.ERROR, LogCategory.CONFIGS, "Could not parse block with data, illegal data: " + blockName + ":" + data + ". Exception: " + ex.getMessage());
			}
		}
		catch (NullPointerException ex)
		{
			if(OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.CONFIGS))
			{
				OTG.getEngine().getLogger().log(LogLevel.ERROR, LogCategory.CONFIGS, "Encountered a null pointer trying to parse " + blockName + ":" + data + ". Exception: " + ex.getMessage());
			}
		}
		return null;
	}

	private static ResourceLocation getFlatKey (String name, int data) throws NullPointerException
	{
		String result = DataConverterFlatten.a(name, data);
		if (result == null)
		{
			throw new NullPointerException();
		}
		return new MinecraftKey(result);
	}


	private static BlockState getAnvilWithData (int material, int data)
	{
		EnumDirection orientation = getBit(data, 0) == 0 ? EnumDirection.NORTH : EnumDirection.WEST;
		switch (material)
		{
			case 0:
				// 0x4 0x8 state: regular (0x4 & 0x8 = 0), slightly damaged (0x4 = 1), very damaged (0x8 = 1)				
				if ((getBit(data, 2) & getBit(data, 3)) == 0)
				{
					return Blocks.ANVIL.defaultBlockState().set(BlockAnvil.FACING, orientation);
				}
				else if (getBit(data, 2) == 1)
				{
					return Blocks.CHIPPED_ANVIL.defaultBlockState().set(BlockAnvil.FACING, orientation);
				}
				else if (getBit(data, 3) == 1)
				{
					return Blocks.DAMAGED_ANVIL.defaultBlockState().set(BlockAnvil.FACING, orientation);
				}
			case 1:
				return Blocks.CHIPPED_ANVIL.defaultBlockState().set(BlockAnvil.FACING, orientation);
			case 2:
				return Blocks.DAMAGED_ANVIL.defaultBlockState().set(BlockAnvil.FACING, orientation);
			default:
				return null;
		}
	}

	private static BlockState getLogWithData (int data)
	{
		int material = getBits(data, 0, 2);
		int orientation = getBits(data, 2, 2);
		EnumDirection.EnumAxis axisDirection = orientation == 0 ? EnumDirection.EnumAxis.Y : orientation == 1 ? EnumDirection.EnumAxis.X : orientation == 2 ? EnumDirection.EnumAxis.Z : EnumDirection.EnumAxis.Y;
		boolean bark = orientation == 3;
		switch (material)
		{
			case 0:
				if (bark) return Blocks.OAK_WOOD.defaultBlockState().set(BlockRotatable.AXIS, axisDirection);
				return Blocks.OAK_LOG.defaultBlockState().set(BlockRotatable.AXIS, axisDirection);
			case 1:
				if (bark) return Blocks.SPRUCE_WOOD.defaultBlockState().set(BlockRotatable.AXIS, axisDirection);
				return Blocks.SPRUCE_LOG.defaultBlockState().set(BlockRotatable.AXIS, axisDirection);
			case 2:
				if (bark) return Blocks.BIRCH_WOOD.defaultBlockState().set(BlockRotatable.AXIS, axisDirection);
				return Blocks.BIRCH_LOG.defaultBlockState().set(BlockRotatable.AXIS, axisDirection);
			case 3:
				if (bark) return Blocks.JUNGLE_WOOD.defaultBlockState().set(BlockRotatable.AXIS, axisDirection);
				return Blocks.JUNGLE_LOG.defaultBlockState().set(BlockRotatable.AXIS, axisDirection);
			default:
				return Blocks.OAK_LOG.defaultBlockState();
		}
	}

	private static BlockState getLog2WithData (int data)
	{
		int material = getBits(data, 0, 2);
		int orientation = getBits(data, 2, 2);
		EnumDirection.EnumAxis axisDirection = orientation == 0 ? EnumDirection.EnumAxis.Y : orientation == 1 ? EnumDirection.EnumAxis.X : orientation == 2 ? EnumDirection.EnumAxis.Z : EnumDirection.EnumAxis.Y;
		boolean bark = orientation == 3;
		switch (material)
		{
			case 0:
				if (bark) return Blocks.ACACIA_WOOD.defaultBlockState().set(BlockRotatable.AXIS, axisDirection);
				return Blocks.ACACIA_LOG.defaultBlockState().set(BlockRotatable.AXIS, axisDirection);
			case 1:
				if (bark) return Blocks.DARK_OAK_WOOD.defaultBlockState().set(BlockRotatable.AXIS, axisDirection);
				return Blocks.DARK_OAK_LOG.defaultBlockState().set(BlockRotatable.AXIS, axisDirection);
			default:
				return Blocks.ACACIA_LOG.defaultBlockState();
		}
	}

	private static BlockState getQuartzBlockWithData (int data)
	{
		switch (data)
		{
			case 0:
			default:
				return Blocks.QUARTZ_BLOCK.defaultBlockState();
			case 1:
				return Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState();
			case 2:
				return Blocks.QUARTZ_PILLAR.defaultBlockState().set(BlockRotatable.AXIS, EnumDirection.EnumAxis.Y);
			case 3:
				return Blocks.QUARTZ_PILLAR.defaultBlockState().set(BlockRotatable.AXIS, EnumDirection.EnumAxis.X);
			case 4:
				return Blocks.QUARTZ_PILLAR.defaultBlockState().set(BlockRotatable.AXIS, EnumDirection.EnumAxis.Z);
		}
	}

	private static BlockState getTorchWithData (int material, int data)
	{
		switch (material)
		{
			case 0:
				switch (data)
				{
					case 0:
					case 5:
					default:
						return Blocks.TORCH.defaultBlockState();
					case 1:
						return Blocks.WALL_TORCH.defaultBlockState().set(BlockTorchWall.a, EnumDirection.EAST);
					case 2:
						return Blocks.WALL_TORCH.defaultBlockState().set(BlockTorchWall.a, EnumDirection.WEST);
					case 3:
						return Blocks.WALL_TORCH.defaultBlockState().set(BlockTorchWall.a, EnumDirection.SOUTH);
					case 4:
						return Blocks.WALL_TORCH.defaultBlockState().set(BlockTorchWall.a, EnumDirection.NORTH);
				}
			case 1:
				switch (data)
				{
					case 0:
					case 5:
					default:
						return Blocks.REDSTONE_TORCH.defaultBlockState();
					case 1:
						return Blocks.REDSTONE_WALL_TORCH.defaultBlockState().set(BlockRedstoneTorchWall.LIT, false).set(BlockRedstoneTorchWall.b, EnumDirection.EAST);
					case 2:
						return Blocks.REDSTONE_WALL_TORCH.defaultBlockState().set(BlockRedstoneTorchWall.LIT, false).set(BlockRedstoneTorchWall.b, EnumDirection.WEST);
					case 3:
						return Blocks.REDSTONE_WALL_TORCH.defaultBlockState().set(BlockRedstoneTorchWall.LIT, false).set(BlockRedstoneTorchWall.b, EnumDirection.SOUTH);
					case 4:
						return Blocks.REDSTONE_WALL_TORCH.defaultBlockState().set(BlockRedstoneTorchWall.LIT, false).set(BlockRedstoneTorchWall.b, EnumDirection.NORTH);
				}
			case 2:
				switch (data)
				{
					case 0:
					case 5:
					default:
						return Blocks.REDSTONE_TORCH.defaultBlockState().set(BlockRedstoneTorchWall.LIT, true);
					case 1:
						return Blocks.REDSTONE_WALL_TORCH.defaultBlockState().set(BlockRedstoneTorchWall.LIT, true).set(BlockRedstoneTorchWall.b, EnumDirection.EAST);
					case 2:
						return Blocks.REDSTONE_WALL_TORCH.defaultBlockState().set(BlockRedstoneTorchWall.LIT, true).set(BlockRedstoneTorchWall.b, EnumDirection.WEST);
					case 3:
						return Blocks.REDSTONE_WALL_TORCH.defaultBlockState().set(BlockRedstoneTorchWall.LIT, true).set(BlockRedstoneTorchWall.b, EnumDirection.SOUTH);
					case 4:
						return Blocks.REDSTONE_WALL_TORCH.defaultBlockState().set(BlockRedstoneTorchWall.LIT, true).set(BlockRedstoneTorchWall.b, EnumDirection.NORTH);
				}
			default:
				return null;
		}
	}

	private static BlockState getRailsWithData (int material, int data)
	{
		int shape = getBits(data, 0, 3);
		int active = getBit(data, 3);
		switch (material)
		{
			case 0:
				return Blocks.POWERED_RAIL.defaultBlockState().set(BlockPoweredRail.SHAPE, getRailShape(shape)).set(BlockPoweredRail.POWERED, active == 1);
			case 1:
				return Blocks.DETECTOR_RAIL.defaultBlockState().set(BlockMinecartDetector.SHAPE, getRailShape(shape)).set(BlockMinecartDetector.POWERED, active == 1);
			case 2:
				return Blocks.ACTIVATOR_RAIL.defaultBlockState().set(BlockPoweredRail.SHAPE, getRailShape(shape)).set(BlockPoweredRail.POWERED, active == 1);
			default:
				return null;
		}
	}

	private static BlockState getStairsWithData (int material, int data)
	{
		int facing = getBits(data, 0, 2);
		int half = getBit(data, 2);
		BlockState output;
		switch (material)
		{
			case 0:
				output = Blocks.OAK_STAIRS.defaultBlockState();
				break;
			case 1:
				output = Blocks.BIRCH_STAIRS.defaultBlockState();
				break;
			case 2:
				output = Blocks.SPRUCE_STAIRS.defaultBlockState();
				break;
			case 3:
				output = Blocks.JUNGLE_STAIRS.defaultBlockState();
				break;
			case 4:
				output = Blocks.COBBLESTONE_STAIRS.defaultBlockState();
				break;
			case 5:
				output = Blocks.BRICK_STAIRS.defaultBlockState();
				break;
			case 6:
				output = Blocks.STONE_STAIRS.defaultBlockState();
				break;
			case 7:
				output = Blocks.NETHER_BRICK_STAIRS.defaultBlockState();
				break;
			case 8:
				output = Blocks.SANDSTONE_STAIRS.defaultBlockState();
				break;
			case 9:
				output = Blocks.QUARTZ_STAIRS.defaultBlockState();
				break;
			case 10:
				output = Blocks.ACACIA_STAIRS.defaultBlockState();
				break;
			case 11:
				output = Blocks.DARK_OAK_STAIRS.defaultBlockState();
				break;
			case 12:
				output = Blocks.RED_SANDSTONE_STAIRS.defaultBlockState();
				break;
			case 13:
				output = Blocks.PURPUR_STAIRS.defaultBlockState();
				break;
			case 14:
				output = Blocks.STONE_BRICK_STAIRS.defaultBlockState();
				break;
			default:
				return null;
		}
		return output
				.set(StairBlock.FACING, getFacingEastWestSouthNorth(facing))
				.set(StairBlock.HALF, half == 0 ? BlockPropertyHalf.BOTTOM : BlockPropertyHalf.TOP);
	}

	private static BlockState getLeverOrButtonWithData (int material, int data)
	{
		int facing = getBits(data, 0, 3);
		int powered = getBit(data, 3);
		BlockPropertyAttachPosition face = facing == 0 || facing == 7 ? BlockPropertyAttachPosition.CEILING : facing == 1 || facing == 2 || facing == 3 || facing == 4 ? BlockPropertyAttachPosition.WALL : BlockPropertyAttachPosition.FLOOR;

		switch (material)
		{
			case 0:
				return Blocks.LEVER.defaultBlockState().set(BlockLever.FACE, face).set(BlockLever.FACING, getFacingLever(facing)).set(BlockLever.POWERED, powered == 1);
			case 1:
				return Blocks.STONE_BUTTON.defaultBlockState().set(BlockStoneButton.FACE, face).set(BlockStoneButton.FACING, getFacingButton(facing)).set(BlockStoneButton.POWERED, powered == 1);
			case 2:
				return Blocks.OAK_BUTTON.defaultBlockState().set(BlockWoodButton.FACE, face).set(BlockWoodButton.FACING, getFacingButton(facing)).set(BlockWoodButton.POWERED, powered == 1);
			default:
				return null;
		}
	}

	private static BlockState getDoorWithData (int material, int data)
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
		BlockState blockState;
		switch (material)
		{
			case 0:
				blockState = Blocks.OAK_DOOR.defaultBlockState();
				break;
			case 1:
				blockState = Blocks.IRON_DOOR.defaultBlockState();
				break;
			case 2:
				blockState = Blocks.SPRUCE_DOOR.defaultBlockState();
				break;
			case 3:
				blockState = Blocks.BIRCH_DOOR.defaultBlockState();
				break;
			case 4:
				blockState = Blocks.JUNGLE_DOOR.defaultBlockState();
				break;
			case 5:
				blockState = Blocks.ACACIA_DOOR.defaultBlockState();
				break;
			case 6:
				blockState = Blocks.DARK_OAK_DOOR.defaultBlockState();
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

	private static BlockState getSignPostWithData (int data)
	{
		int rotation = getBits(data, 0, 4);
		// TODO: Hopefully rotation is still mapped to the same int values as 1.12..
		return Blocks.OAK_SIGN.defaultBlockState().set(BlockFloorSign.ROTATION, rotation);
	}

	private static BlockState getWallSignWithData (int data)
	{
		int facing = getBits(data, 0, 3);
		return Blocks.OAK_WALL_SIGN.defaultBlockState().set(BlockWallSign.FACING, getFacingNorthSouthWestEast(facing));
	}

	// TODO: Can't find information on 1.12 command block block data, what about facing?
	private static BlockState getCommandBlockWithData (int material, int data)
	{
		BlockState blockState;
		switch (material)
		{
			case 0:
				blockState = Blocks.COMMAND_BLOCK.defaultBlockState();
				break;
			case 1:
				blockState = Blocks.REPEATING_COMMAND_BLOCK.defaultBlockState();
				break;
			case 2:
				blockState = Blocks.CHAIN_COMMAND_BLOCK.defaultBlockState();
				break;
			default:
				return null;
		}
		return blockState;
	}

	private static BlockState getShulkerBoxWithData (int material, int data)
	{
		BlockState blockState;
		switch (material)
		{
			case 0:
				blockState = Blocks.WHITE_SHULKER_BOX.defaultBlockState();
				break;
			case 1:
				blockState = Blocks.ORANGE_SHULKER_BOX.defaultBlockState();
				break;
			case 2:
				blockState = Blocks.MAGENTA_SHULKER_BOX.defaultBlockState();
				break;
			case 3:
				blockState = Blocks.LIGHT_BLUE_SHULKER_BOX.defaultBlockState();
				break;
			case 4:
				blockState = Blocks.YELLOW_SHULKER_BOX.defaultBlockState();
				break;
			case 5:
				blockState = Blocks.LIME_SHULKER_BOX.defaultBlockState();
				break;
			case 6:
				blockState = Blocks.PINK_SHULKER_BOX.defaultBlockState();
				break;
			case 7:
				blockState = Blocks.GRAY_SHULKER_BOX.defaultBlockState();
				break;
			case 8:
				blockState = Blocks.LIGHT_GRAY_SHULKER_BOX.defaultBlockState();
				break;
			case 9:
				blockState = Blocks.CYAN_SHULKER_BOX.defaultBlockState();
				break;
			case 10:
				blockState = Blocks.PURPLE_SHULKER_BOX.defaultBlockState();
				break;
			case 11:
				blockState = Blocks.BLUE_SHULKER_BOX.defaultBlockState();
				break;
			case 12:
				blockState = Blocks.BROWN_SHULKER_BOX.defaultBlockState();
				break;
			case 13:
				blockState = Blocks.GREEN_SHULKER_BOX.defaultBlockState();
				break;
			case 14:
				blockState = Blocks.RED_SHULKER_BOX.defaultBlockState();
				break;
			case 15:
				blockState = Blocks.BLACK_SHULKER_BOX.defaultBlockState();
				break;
			case 16:
				blockState = Blocks.SHULKER_BOX.defaultBlockState();
				break;
			default:
				return null;
		}
		return blockState.set(BlockShulkerBox.a, getFacingDownEastNorthSouthUpWest(data));
	}

	private static BlockState getLadderChestOrFurnaceWithData (int material, int data)
	{
		int facing = getBits(data, 0, 3);
		switch (material)
		{
			case 0:
				return Blocks.LADDER.defaultBlockState().set(BlockLadder.FACING, getFacingNorthSouthWestEast(facing));
			case 1:
				return Blocks.CHEST.defaultBlockState().set(BlockChest.FACING, getFacingNorthSouthWestEast(facing));
			case 2:
				return Blocks.ENDER_CHEST.defaultBlockState().set(BlockEnderChest.FACING, getFacingNorthSouthWestEast(facing));
			case 3:
				return Blocks.TRAPPED_CHEST.defaultBlockState().set(BlockChestTrapped.FACING, getFacingNorthSouthWestEast(facing));
			case 4:
				return Blocks.FURNACE.defaultBlockState().set(BlockFurnace.FACING, getFacingNorthSouthWestEast(facing)).set(BlockFurnace.LIT, false);
			case 5:
				return Blocks.FURNACE.defaultBlockState().set(BlockFurnace.FACING, getFacingNorthSouthWestEast(facing)).set(BlockFurnace.LIT, true);
			default:
				return null;
		}
	}

	private static BlockState getDispenserHopperDropperWithData (int material, int data)
	{
		int facing = getBits(data, 0, 3);
		int active = getBit(data, 3);
		switch (material)
		{
			case 0:
				return Blocks.DISPENSER.defaultBlockState().set(BlockDispenser.FACING, getFacingDownUpNorthSouthWestEast(facing)).set(BlockDispenser.TRIGGERED, active == 1);
			case 1:
				return Blocks.DROPPER.defaultBlockState().set(BlockDropper.FACING, getFacingDownUpNorthSouthWestEast(facing)).set(BlockDropper.TRIGGERED, active == 1);
			case 2:
				return Blocks.HOPPER.defaultBlockState().set(BlockHopper.FACING, getFacingDownUpNorthSouthWestEast(facing)).set(BlockHopper.ENABLED, active == 1);
			default:
				return null;
		}
	}

	private static BlockState getJackOLanternOrPumpkinWithData (int material, int data)
	{
		int facing = getBits(data, 0, 2);
		switch (material)
		{
			case 0:
				return Blocks.CARVED_PUMPKIN.defaultBlockState().set(BlockPumpkinCarved.FACING, getFacingSouthWestNorthEast(facing));
			case 1:
				return Blocks.JACK_O_LANTERN.defaultBlockState().set(BlockPumpkinCarved.FACING, getFacingSouthWestNorthEast(facing));
			default:
				return null;
		}
	}

	private static BlockState getObserverWithData (int data)
	{
		int facing = getBits(data, 0, 3);
		int powered = getBit(data, 3);
		return Blocks.OBSERVER.defaultBlockState().set(BlockObserver.FACING, getFacingDownUpNorthSouthWestEast(facing)).set(BlockObserver.b, powered == 1);
	}

	private static BlockState getRepeaterWithData (int material, int data)
	{
		int facing = getBits(data, 0, 2);
		int delay = getBits(data, 2, 2) + 1;
		BlockState blockState;
		switch (material)
		{
			case 0:
				blockState = Blocks.REPEATER.defaultBlockState().set(BlockRepeater.c, false);
				break;
			case 1:
				blockState = Blocks.REPEATER.defaultBlockState().set(BlockRepeater.c, true);
				break;
			default:
				return null;
		}
		return blockState
				.set(BlockRepeater.DELAY, delay)
				.set(BlockRepeater.FACING, getFacingSouthWestNorthEast(facing))
				;
	}

	private static BlockState getComparatorWithData (int material, int data)
	{
		int facing = getBits(data, 0, 2);
		int mode = getBit(data, 2);
		int powered = material == 1 ? 1 : getBit(data, 3);
		return Blocks.COMPARATOR.defaultBlockState()
				.set(BlockRedstoneComparator.FACING, getFacingSouthWestNorthEast(facing))
				.set(BlockRedstoneComparator.MODE, mode == 0 ? BlockPropertyComparatorMode.COMPARE : BlockPropertyComparatorMode.SUBTRACT)
				.set(BlockRedstoneComparator.c, powered == 1)
				;
	}

	private static BlockState getBedBlockWithData (int data)
	{
		int facing = getBits(data, 0, 2);
		int occupied = getBit(data, 2);
		int part = getBit(data, 3);
		return Blocks.RED_BED.defaultBlockState()
				.set(BlockBed.FACING, getFacingSouthWestNorthEast(facing))
				.set(BlockBed.OCCUPIED, occupied == 1)
				.set(BlockBed.PART, part == 0 ? BlockPropertyBedPart.FOOT : BlockPropertyBedPart.HEAD)
				;
	}

	private static BlockState getTrapDoorBlockWithData (int material, int data)
	{
		int facing = getBits(data, 0, 2);
		int open = getBit(data, 2);
		int half = getBit(data, 3);
		BlockState blockState;
		switch (material)
		{
			case 0:
				blockState = Blocks.OAK_TRAPDOOR.defaultBlockState();
				break;
			case 1:
				blockState = Blocks.IRON_TRAPDOOR.defaultBlockState();
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

	private static BlockState getPistonWithData (int material, int data)
	{
		int facing = getBits(data, 0, 3);
		int extended = getBit(data, 3);
		switch (material)
		{
			case 0:
				return Blocks.PISTON.defaultBlockState().set(BlockPiston.EXTENDED, extended == 1).set(BlockPiston.FACING, getFacingDownUpNorthSouthWestEast(facing));
			case 1:
				return Blocks.STICKY_PISTON.defaultBlockState().set(BlockPiston.EXTENDED, extended == 1).set(BlockPiston.FACING, getFacingDownUpNorthSouthWestEast(facing));
			default:
				return null;
		}
	}

	private static BlockState getPistonHeadWithData (int data)
	{
		int facing = getBits(data, 0, 3);
		int type = getBit(data, 3);
		return Blocks.PISTON_HEAD.defaultBlockState()
				.set(BlockPistonExtension.FACING, getFacingDownUpNorthSouthWestEast(facing))
				.set(BlockPistonExtension.TYPE, type == 0 ? BlockPropertyPistonType.DEFAULT : BlockPropertyPistonType.STICKY)
				;
	}

	private static BlockState getHugeMushroomWithData (int material, int data)
	{
		boolean down = data == 14 || data == 15;
		boolean up = data == 1 || data == 2 || data == 3 || data == 4 || data == 5 || data == 6 || data == 7 || data == 8 || data == 9 || data == 14 || data == 15;
		boolean north = data == 1 || data == 2 || data == 3 || data == 10 || data == 14 || data == 15;
		boolean east = data == 3 || data == 6 || data == 9 || data == 10 || data == 14 || data == 15;
		boolean south = data == 7 || data == 8 || data == 9 || data == 10 || data == 14 || data == 15;
		boolean west = data == 1 || data == 4 || data == 7 || data == 10 || data == 14 || data == 15;
		BlockState blockState;
		if (data == 10 || data == 15)
		{
			blockState = Blocks.MUSHROOM_STEM.defaultBlockState();
		}
		else
		{
			switch (material)
			{
				case 0:
					blockState = Blocks.BROWN_MUSHROOM_BLOCK.defaultBlockState();
					break;
				case 1:
					blockState = Blocks.RED_MUSHROOM_BLOCK.defaultBlockState();
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

	private static BlockState getVineWithData (int data)
	{
		int south = getBit(data, 0);
		int west = getBit(data, 1);
		int north = getBit(data, 2);
		int east = getBit(data, 3);
		int up = data == 0 ? 1 : 0; // Up was not stored in data for 1.12.2 (only rotation), fix via otg update.
		return Blocks.VINE.defaultBlockState()
				.set(BlockVine.EAST, east == 1)
				.set(BlockVine.NORTH, north == 1)
				.set(BlockVine.SOUTH, south == 1)
				.set(BlockVine.WEST, west == 1)
				.set(BlockVine.UP, up == 1)
				;
	}

	private static BlockState getFenceGateWithData (int material, int data)
	{
		int facing = getBits(data, 0, 2);
		int open = getBit(data, 2);
		BlockState blockState;
		switch (material)
		{
			case 0:
				blockState = Blocks.BIRCH_FENCE_GATE.defaultBlockState();
				break;
			case 1:
				blockState = Blocks.OAK_FENCE_GATE.defaultBlockState();
				break;
			case 2:
				blockState = Blocks.SPRUCE_FENCE_GATE.defaultBlockState();
				break;
			case 3:
				blockState = Blocks.JUNGLE_FENCE_GATE.defaultBlockState();
				break;
			case 4:
				blockState = Blocks.DARK_OAK_FENCE_GATE.defaultBlockState();
				break;
			case 5:
				blockState = Blocks.ACACIA_FENCE_GATE.defaultBlockState();
				break;
			default:
				return null;
		}
		return blockState
				.set(FenceGateBlock.FACING, getFacingSouthWestNorthEast(facing))
				.set(FenceGateBlock.OPEN, open == 1)
				;
	}

	private static BlockState getCocoaWithData (int data)
	{
		int facing = getBits(data, 0, 2);
		int age = getBits(data, 2, 2);
		return Blocks.COCOA.defaultBlockState().set(BlockCocoa.FACING, getFacingSouthWestNorthEast(facing)).set(BlockCocoa.AGE, age);
	}

	private static BlockState getTripWireHookWithData (int data)
	{
		int facing = getBits(data, 0, 2);
		int attached = getBit(data, 2);
		int powered = getBit(data, 3);
		return Blocks.TRIPWIRE_HOOK.defaultBlockState()
				.set(BlockTripwireHook.ATTACHED, attached == 1)
				.set(BlockTripwireHook.FACING, getFacingSouthWestNorthEast(facing))
				.set(BlockTripwireHook.POWERED, powered == 1)
				;
	}

	private static BlockState getEndPortalFrameWithData (int data)
	{
		int facing = getBits(data, 0, 2);
		int eye = getBit(data, 2);
		return Blocks.END_PORTAL_FRAME.defaultBlockState().set(BlockEnderPortalFrame.EYE, eye == 1).set(BlockEnderPortalFrame.FACING, getFacingSouthWestNorthEast(facing));
	}

	private static BlockState getStructureBlockWithData (int data)
	{
		BlockPropertyStructureMode structureBlockMode = data == 0 ? BlockPropertyStructureMode.DATA : data == 1 ? BlockPropertyStructureMode.SAVE : data == 2 ? BlockPropertyStructureMode.LOAD : data == 3 ? BlockPropertyStructureMode.LOAD : BlockPropertyStructureMode.DATA;
		return Blocks.STRUCTURE_BLOCK.defaultBlockState().set(BlockStructure.a, structureBlockMode);
	}

	private static BlockState getGlazedTerracottaWithData (int material, int data)
	{
		int facing = getBits(data, 0, 2);
		BlockState blockState;
		switch (material)
		{
			case 0:
				blockState = Blocks.BLACK_GLAZED_TERRACOTTA.defaultBlockState();
				break;
			case 1:
				blockState = Blocks.BLUE_GLAZED_TERRACOTTA.defaultBlockState();
				break;
			case 2:
				blockState = Blocks.BROWN_GLAZED_TERRACOTTA.defaultBlockState();
				break;
			case 3:
				blockState = Blocks.CYAN_GLAZED_TERRACOTTA.defaultBlockState();
				break;
			case 4:
				blockState = Blocks.GRAY_GLAZED_TERRACOTTA.defaultBlockState();
				break;
			case 5:
				blockState = Blocks.GREEN_GLAZED_TERRACOTTA.defaultBlockState();
				break;
			case 6:
				blockState = Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA.defaultBlockState();
				break;
			case 7:
				blockState = Blocks.LIME_GLAZED_TERRACOTTA.defaultBlockState();
				break;
			case 8:
				blockState = Blocks.MAGENTA_GLAZED_TERRACOTTA.defaultBlockState();
				break;
			case 9:
				blockState = Blocks.ORANGE_GLAZED_TERRACOTTA.defaultBlockState();
				break;
			case 10:
				blockState = Blocks.PINK_GLAZED_TERRACOTTA.defaultBlockState();
				break;
			case 11:
				blockState = Blocks.PURPLE_GLAZED_TERRACOTTA.defaultBlockState();
				break;
			case 12:
				blockState = Blocks.RED_GLAZED_TERRACOTTA.defaultBlockState();
				break;
			case 13:
				blockState = Blocks.LIGHT_GRAY_GLAZED_TERRACOTTA.defaultBlockState();
				break;
			case 14:
				blockState = Blocks.WHITE_GLAZED_TERRACOTTA.defaultBlockState();
				break;
			case 15:
				blockState = Blocks.YELLOW_GLAZED_TERRACOTTA.defaultBlockState();
				break;
			default:
				return Blocks.BLACK_GLAZED_TERRACOTTA.defaultBlockState();
		}
		return blockState.set(BlockGlazedTerracotta.FACING, getFacingSouthWestNorthEast(facing));
	}

	private static BlockState getTripWireWithData (int data)
	{
		int active = getBit(data, 0);
		int attached = getBit(data, 2);
		int disarmed = getBit(data, 3);
		return Blocks.TRIPWIRE.defaultBlockState()
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
