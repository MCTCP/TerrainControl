package com.pg85.otg.forge.materials;

import com.pg85.otg.OTG;
import com.pg85.otg.logging.LogCategory;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.materials.LegacyMaterials;

import net.minecraft.block.AnvilBlock;
import net.minecraft.block.BannerBlock;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BeetrootBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.BrewingStandBlock;
import net.minecraft.block.CactusBlock;
import net.minecraft.block.CakeBlock;
import net.minecraft.block.CarrotBlock;
import net.minecraft.block.CarvedPumpkinBlock;
import net.minecraft.block.CauldronBlock;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.CocoaBlock;
import net.minecraft.block.ComparatorBlock;
import net.minecraft.block.CropsBlock;
import net.minecraft.block.DaylightDetectorBlock;
import net.minecraft.block.DetectorRailBlock;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.DropperBlock;
import net.minecraft.block.EndPortalFrameBlock;
import net.minecraft.block.EndRodBlock;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.FireBlock;
import net.minecraft.block.FurnaceBlock;
import net.minecraft.block.GlazedTerracottaBlock;
import net.minecraft.block.HayBlock;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.HugeMushroomBlock;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.LeverBlock;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.block.NetherWartBlock;
import net.minecraft.block.ObserverBlock;
import net.minecraft.block.PistonBlock;
import net.minecraft.block.PistonHeadBlock;
import net.minecraft.block.PotatoBlock;
import net.minecraft.block.PoweredRailBlock;
import net.minecraft.block.PressurePlateBlock;
import net.minecraft.block.RailBlock;
import net.minecraft.block.RedstoneLampBlock;
import net.minecraft.block.RedstoneWallTorchBlock;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.block.RepeaterBlock;
import net.minecraft.block.RotatedPillarBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.SnowBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.StandingSignBlock;
import net.minecraft.block.StemBlock;
import net.minecraft.block.StoneButtonBlock;
import net.minecraft.block.StructureBlock;
import net.minecraft.block.SugarCaneBlock;
import net.minecraft.block.TrapDoorBlock;
import net.minecraft.block.TrappedChestBlock;
import net.minecraft.block.TripWireBlock;
import net.minecraft.block.TripWireHookBlock;
import net.minecraft.block.VineBlock;
import net.minecraft.block.WallBannerBlock;
import net.minecraft.block.WallSignBlock;
import net.minecraft.block.WallTorchBlock;
import net.minecraft.block.WeightedPressurePlateBlock;
import net.minecraft.block.WoodButtonBlock;
import net.minecraft.state.properties.AttachFace;
import net.minecraft.state.properties.BedPart;
import net.minecraft.state.properties.ComparatorMode;
import net.minecraft.state.properties.DoorHingeSide;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.state.properties.Half;
import net.minecraft.state.properties.PistonType;
import net.minecraft.state.properties.RailShape;
import net.minecraft.state.properties.SlabType;
import net.minecraft.state.properties.StructureMode;
import net.minecraft.util.Direction;

// Converts any (1.12.2 or lower) legacy block names/ids + data to the new (1.16) format.
// TODO: Can probably use some Forge method to convert data to the new format, don't do it all manually?
// ^ At least this way we have full control and can accommodate for any legacy OTG names/data/aliases etc.
// Block.getStateById() says //Forge: Do not use, use GameRegistry. GameRegistry doesn't appear to provide what we need though(?)
class ForgeLegacyMaterials
{
	// TODO: Don't need any names here that match 1.16's
	static BlockState fromLegacyBlockName(String oldBlockName)
	{
		switch(oldBlockName)
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
				return Blocks.WATER.defaultBlockState();
			case "stationary_lava":
				return Blocks.LAVA.defaultBlockState();
			case "stained_clay":
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
				return Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.DISTANCE, 1);
			case "leaves_2":
				return Blocks.ACACIA_LEAVES.defaultBlockState().setValue(LeavesBlock.DISTANCE, 1);
			case "red_rose":
				return Blocks.POPPY.defaultBlockState();
			// TODO: This only spawns the bottom half?
			case "double_plant":
				return Blocks.SUNFLOWER.defaultBlockState();
				
			case "wood_stairs":
			case "oak_stairs":
				return Blocks.OAK_STAIRS.defaultBlockState().setValue(StairsBlock.FACING, Direction.NORTH);
			case "birch_wood_stairs":
			case "birch_stairs":
				return Blocks.BIRCH_STAIRS.defaultBlockState().setValue(StairsBlock.FACING, Direction.NORTH);
			case "spruce_wood_stairs":
			case "spruce_stairs":
				return Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairsBlock.FACING, Direction.NORTH);
			case "jungle_wood_stairs":				
			case "jungle_stairs":
				return Blocks.JUNGLE_STAIRS.defaultBlockState().setValue(StairsBlock.FACING, Direction.NORTH);
			case "cobblestone_stairs":
			case "stone_stairs":
				return Blocks.COBBLESTONE_STAIRS.defaultBlockState().setValue(StairsBlock.FACING, Direction.NORTH);
			case "nether_brick_stairs":
				return Blocks.NETHER_BRICK_STAIRS.defaultBlockState().setValue(StairsBlock.FACING, Direction.NORTH);
			case "sandstone_stairs":
				return Blocks.SANDSTONE_STAIRS.defaultBlockState().setValue(StairsBlock.FACING, Direction.NORTH);
			case "quartz_stairs":
				return Blocks.QUARTZ_STAIRS.defaultBlockState().setValue(StairsBlock.FACING, Direction.NORTH);
			case "acacia_stairs":
				return Blocks.ACACIA_STAIRS.defaultBlockState().setValue(StairsBlock.FACING, Direction.NORTH);
			case "dark_oak_stairs":
				return Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairsBlock.FACING, Direction.NORTH);
			case "red_sandstone_stairs":
				return Blocks.RED_SANDSTONE_STAIRS.defaultBlockState().setValue(StairsBlock.FACING, Direction.NORTH);
			case "purpur_stairs":
				return Blocks.PURPUR_STAIRS.defaultBlockState().setValue(StairsBlock.FACING, Direction.NORTH);
			case "brick_stairs":
				return Blocks.BRICK_STAIRS.defaultBlockState().setValue(StairsBlock.FACING, Direction.NORTH);				
			case "stone_brick_stairs":
			case "smooth_stairs":
				return Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue(StairsBlock.FACING, Direction.NORTH);
			
			case "quartz_ore":
				return Blocks.NETHER_QUARTZ_ORE.defaultBlockState();
			case "yellow_flower":
				return Blocks.DANDELION.defaultBlockState();
			case "web":
				return Blocks.COBWEB.defaultBlockState();
			case "wall_banner":
				return Blocks.WHITE_WALL_BANNER.defaultBlockState();
			case "redstone_lamp_on":
				return Blocks.REDSTONE_LAMP.defaultBlockState().setValue(RedstoneLampBlock.LIT, true);
			case "redstone_lamp_off":
				return Blocks.REDSTONE_LAMP.defaultBlockState().setValue(RedstoneLampBlock.LIT, false);
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
				return Blocks.SMOOTH_STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.DOUBLE);
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
			case "stone_plate":
				return Blocks.STONE_PRESSURE_PLATE.defaultBlockState();
			case "wood_plate":
				return Blocks.OAK_PRESSURE_PLATE.defaultBlockState();
			case "wood_double_step":
				return Blocks.OAK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.DOUBLE);
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
				return Blocks.ZOMBIE_HEAD.defaultBlockState();
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
			default:
				return null;
		}
	}

	static BlockState fromLegacyBlockNameOrIdWithData(String blockName, int data)
	{		
		if(blockName == null || blockName.trim().isEmpty())
		{
			return null;
		}
	
		try
		{
			int blockId = Integer.parseInt(blockName);
			blockName = LegacyMaterials.blockNameFromLegacyBlockId(blockId);
			if(blockName == null)
			{
				return null;
			}
		} catch(NumberFormatException ignored) { }
		
		try
		{
			switch(blockName)
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
					switch(data)
					{
						case 0:
						default:
							return Blocks.BLACK_BANNER.defaultBlockState();
						case 1:
							return Blocks.RED_BANNER.defaultBlockState();
						case 2:
							return Blocks.GREEN_BANNER.defaultBlockState();
						case 3:
							return Blocks.BROWN_BANNER.defaultBlockState();
						case 4:
							return Blocks.BLUE_BANNER.defaultBlockState();
						case 5:
							return Blocks.PURPLE_BANNER.defaultBlockState();
						case 6:
							return Blocks.CYAN_BANNER.defaultBlockState();
						case 7:
							return Blocks.LIGHT_GRAY_BANNER.defaultBlockState();
						case 8:
							return Blocks.GRAY_BANNER.defaultBlockState();
						case 9:
							return Blocks.PINK_BANNER.defaultBlockState();
						case 10:
							return Blocks.LIME_BANNER.defaultBlockState();
						case 11:
							return Blocks.YELLOW_BANNER.defaultBlockState();
						case 12:
							return Blocks.LIGHT_BLUE_BANNER.defaultBlockState();
						case 13:
							return Blocks.MAGENTA_BANNER.defaultBlockState();
						case 14:
							return Blocks.ORANGE_BANNER.defaultBlockState();
						case 15:
							return Blocks.WHITE_BANNER.defaultBlockState();
					}		
				// TODO: How does facing for bed blocks in bo's work for 1.12.2, can only specify color via data?
				case "bed":
				case "white_bed":
					switch(data)
					{
						case 0:
						default:
							return Blocks.WHITE_BED.defaultBlockState();
						case 1:
							return Blocks.ORANGE_BED.defaultBlockState();
						case 2:
							return Blocks.MAGENTA_BED.defaultBlockState();
						case 3:
							return Blocks.LIGHT_BLUE_BED.defaultBlockState();
						case 4:
							return Blocks.YELLOW_BED.defaultBlockState();
						case 5:
							return Blocks.LIME_BED.defaultBlockState();
						case 6:
							return Blocks.PINK_BED.defaultBlockState();
						case 7:
							return Blocks.GRAY_BED.defaultBlockState();
						case 8:
							return Blocks.LIGHT_GRAY_BED.defaultBlockState();
						case 9:
							return Blocks.CYAN_BED.defaultBlockState();
						case 10:
							return Blocks.PURPLE_BED.defaultBlockState();
						case 11:
							return Blocks.BLUE_BED.defaultBlockState();
						case 12:
							return Blocks.BROWN_BED.defaultBlockState();
						case 13:
							return Blocks.GREEN_BED.defaultBlockState();
						case 14:
							return Blocks.RED_BED.defaultBlockState();
						case 15:
							return Blocks.BLACK_BED.defaultBlockState();
					}
				case "carpet":
				case "white_carpet":
					switch(data)
					{
						case 0:
						default:
							return Blocks.WHITE_CARPET.defaultBlockState();
						case 1:
							return Blocks.ORANGE_CARPET.defaultBlockState();
						case 2:
							return Blocks.MAGENTA_CARPET.defaultBlockState();
						case 3:
							return Blocks.LIGHT_BLUE_CARPET.defaultBlockState();
						case 4:
							return Blocks.YELLOW_CARPET.defaultBlockState();
						case 5:
							return Blocks.LIME_CARPET.defaultBlockState();
						case 6:
							return Blocks.PINK_CARPET.defaultBlockState();
						case 7:
							return Blocks.GRAY_CARPET.defaultBlockState();
						case 8:
							return Blocks.LIGHT_GRAY_CARPET.defaultBlockState();
						case 9:
							return Blocks.CYAN_CARPET.defaultBlockState();
						case 10:
							return Blocks.PURPLE_CARPET.defaultBlockState();
						case 11:
							return Blocks.BLUE_CARPET.defaultBlockState();
						case 12:
							return Blocks.BROWN_CARPET.defaultBlockState();
						case 13:
							return Blocks.GREEN_CARPET.defaultBlockState();
						case 14:
							return Blocks.RED_CARPET.defaultBlockState();
						case 15:
							return Blocks.BLACK_CARPET.defaultBlockState();
					}
				case "cobblestone_wall":
				case "cobble_wall":
					switch(data)
					{
						case 0:
						default:
							return Blocks.COBBLESTONE_WALL.defaultBlockState();
						case 1:
							return Blocks.MOSSY_COBBLESTONE_WALL.defaultBlockState();
					}
				case "concrete":
				case "white_concrete":
					switch(data)
					{
						case 0:
						default:
							return Blocks.WHITE_CONCRETE.defaultBlockState();
						case 1:
							return Blocks.ORANGE_CONCRETE.defaultBlockState();
						case 2:
							return Blocks.MAGENTA_CONCRETE.defaultBlockState();
						case 3:
							return Blocks.LIGHT_BLUE_CONCRETE.defaultBlockState();
						case 4:
							return Blocks.YELLOW_CONCRETE.defaultBlockState();
						case 5:
							return Blocks.LIME_CONCRETE.defaultBlockState();
						case 6:
							return Blocks.PINK_CONCRETE.defaultBlockState();
						case 7:
							return Blocks.GRAY_CONCRETE.defaultBlockState();
						case 8:
							return Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState();
						case 9:
							return Blocks.CYAN_CONCRETE.defaultBlockState();
						case 10:
							return Blocks.PURPLE_CONCRETE.defaultBlockState();
						case 11:
							return Blocks.BLUE_CONCRETE.defaultBlockState();
						case 12:
							return Blocks.BROWN_CONCRETE.defaultBlockState();
						case 13:
							return Blocks.GREEN_CONCRETE.defaultBlockState();
						case 14:
							return Blocks.RED_CONCRETE.defaultBlockState();
						case 15:
							return Blocks.BLACK_CONCRETE.defaultBlockState();
					}
				case "concrete_powder":
				case "white_concrete_powder":
					switch(data)
					{
						case 0:
						default:						
							return Blocks.WHITE_CONCRETE_POWDER.defaultBlockState();
						case 1:
							return Blocks.ORANGE_CONCRETE_POWDER.defaultBlockState();
						case 2:
							return Blocks.MAGENTA_CONCRETE_POWDER.defaultBlockState();
						case 3:
							return Blocks.LIGHT_BLUE_CONCRETE_POWDER.defaultBlockState();
						case 4:
							return Blocks.YELLOW_CONCRETE_POWDER.defaultBlockState();
						case 5:
							return Blocks.LIME_CONCRETE_POWDER.defaultBlockState();
						case 6:
							return Blocks.PINK_CONCRETE_POWDER.defaultBlockState();
						case 7:
							return Blocks.GRAY_CONCRETE_POWDER.defaultBlockState();
						case 8:
							return Blocks.LIGHT_GRAY_CONCRETE_POWDER.defaultBlockState();
						case 9:
							return Blocks.CYAN_CONCRETE_POWDER.defaultBlockState();
						case 10:
							return Blocks.PURPLE_CONCRETE_POWDER.defaultBlockState();
						case 11:
							return Blocks.BLUE_CONCRETE_POWDER.defaultBlockState();
						case 12:
							return Blocks.BROWN_CONCRETE_POWDER.defaultBlockState();
						case 13:
							return Blocks.GREEN_CONCRETE_POWDER.defaultBlockState();
						case 14:
							return Blocks.RED_CONCRETE_POWDER.defaultBlockState();
						case 15:
							return Blocks.BLACK_CONCRETE_POWDER.defaultBlockState();
					}
				case "dirt":
					switch(data)
					{
						case 0:
						default:
							return Blocks.DIRT.defaultBlockState();
						case 1:
							return Blocks.COARSE_DIRT.defaultBlockState();
						case 2:
							return Blocks.PODZOL.defaultBlockState();						
					}
				// TODO: This only spawns the bottom half?
				case "double_plant":
				case "sunflower":
				case "rose_bush":
				case "tall_grass":
					switch(data)
					{
						case 0:
						default:
							return Blocks.SUNFLOWER.defaultBlockState();		
						case 1:
							return Blocks.LILAC.defaultBlockState();
						case 2:
							return Blocks.TALL_GRASS.defaultBlockState();
						case 3:
							return Blocks.LARGE_FERN.defaultBlockState();	
						case 4:
							return Blocks.ROSE_BUSH.defaultBlockState();
						case 5:
							return Blocks.PEONY.defaultBlockState();
					}
				case "double_stone_slab":
				case "smooth_stone":
					switch(data)
					{
						case 0:
						default:
							return Blocks.STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.DOUBLE);
						case 1:
							return Blocks.SANDSTONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.DOUBLE);
						//case 2:
						case 3:
							return Blocks.COBBLESTONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.DOUBLE);
						case 4:
							return Blocks.BRICK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.DOUBLE);
						case 5:
							return Blocks.STONE_BRICK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.DOUBLE);
						case 6:
							return Blocks.NETHER_BRICK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.DOUBLE);
						case 7:
							return Blocks.QUARTZ_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.DOUBLE);
					}
				// TODO: Did this even exist for 1.12.2?
				case "double_wooden_slab":
				case "wood_double_step":
					switch(data)
					{
						case 0:
						default:
							return Blocks.OAK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.DOUBLE);
						case 1:
							return Blocks.SPRUCE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.DOUBLE);
						case 2:
							return Blocks.BIRCH_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.DOUBLE);
						case 3:
							return Blocks.JUNGLE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.DOUBLE);
						case 4:
							return Blocks.ACACIA_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.DOUBLE);
						case 5:
							return Blocks.DARK_OAK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.DOUBLE);
					}
				case "leaves":
				case "oak_leaves":
					switch(data)
					{
						case 0:
						case 4:
						case 8:
						case 12:
						default:						
							return Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.DISTANCE, 1);
						case 1:
						case 5:
						case 9:
						case 13:
							return Blocks.SPRUCE_LEAVES.defaultBlockState().setValue(LeavesBlock.DISTANCE, 1);
						case 2:
						case 6:
						case 10:
						case 14:
							return Blocks.BIRCH_LEAVES.defaultBlockState().setValue(LeavesBlock.DISTANCE, 1);
						case 3:
						case 7:
						case 11:
						case 15:
							return Blocks.JUNGLE_LEAVES.defaultBlockState().setValue(LeavesBlock.DISTANCE, 1);
					}
				case "leaves2":
				case "leaves_2":
				case "acacia_leaves":
					switch(data)
					{
						case 0:
						case 4:
						case 8:
						case 12:
						default:
							return Blocks.ACACIA_LEAVES.defaultBlockState().setValue(LeavesBlock.DISTANCE, 1);
						case 1:
						case 5:
						case 9:
						case 13:
							return Blocks.DARK_OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.DISTANCE, 1);
					}
				case "monster_egg":
				case "monster_eggs":
				case "infested_stone":
					switch(data)
					{
						case 0:
						default:
							return Blocks.INFESTED_STONE.defaultBlockState();
						case 1:
							return Blocks.INFESTED_COBBLESTONE.defaultBlockState();
						case 2:
							return Blocks.INFESTED_STONE_BRICKS.defaultBlockState();
						case 3:
							return Blocks.INFESTED_MOSSY_STONE_BRICKS.defaultBlockState();
						case 4:
							return Blocks.INFESTED_CRACKED_STONE_BRICKS.defaultBlockState();
						case 5:
							return Blocks.INFESTED_CHISELED_STONE_BRICKS.defaultBlockState();
					}
				case "planks":
				case "wood":
				case "oak_planks":
					switch(data)
					{
						case 0:
						default:
							return Blocks.OAK_PLANKS.defaultBlockState();
						case 1:
							return Blocks.SPRUCE_PLANKS.defaultBlockState();
						case 2:
							return Blocks.BIRCH_PLANKS.defaultBlockState();
						case 3:
							return Blocks.JUNGLE_PLANKS.defaultBlockState();
						case 4:
							return Blocks.ACACIA_PLANKS.defaultBlockState();
						case 5:
							return Blocks.DARK_OAK_PLANKS.defaultBlockState();
					}
				case "prismarine":
					switch(data)
					{
						// TODO: Docs contradict each other about whether 2 or 3 is bricks/dark, test this.
						case 0:
						default:
							return Blocks.PRISMARINE.defaultBlockState();
						case 1:
							return Blocks.PRISMARINE_BRICKS.defaultBlockState();
						case 2:
							return Blocks.DARK_PRISMARINE.defaultBlockState();
					}
				case "purpur_slab":
					return Blocks.PURPUR_SLAB.defaultBlockState()
						.setValue(SlabBlock.TYPE, data == 0 ? SlabType.BOTTOM : data == 8 ? SlabType.TOP : SlabType.BOTTOM);
				case "purpur_double_slab":
					return Blocks.PURPUR_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.DOUBLE);					
				case "red_flower":
				case "red_rose":
					switch(data)
					{
						case 0:
						default:
							return Blocks.POPPY.defaultBlockState();
						case 1:
							return Blocks.BLUE_ORCHID.defaultBlockState();
						case 2:
							return Blocks.ALLIUM.defaultBlockState();
						case 3:
							return Blocks.AZURE_BLUET.defaultBlockState();
						case 4:
							return Blocks.RED_TULIP.defaultBlockState();
						case 5:
							return Blocks.ORANGE_TULIP.defaultBlockState();
						case 6:
							return Blocks.WHITE_TULIP.defaultBlockState();
						case 7:						
							return Blocks.PINK_TULIP.defaultBlockState();
						case 8:
							return Blocks.OXEYE_DAISY.defaultBlockState();
					}
				case "red_sandstone":
					switch(data)
					{
						case 0:
						default:
							return Blocks.RED_SANDSTONE.defaultBlockState();
						case 1:
							return Blocks.CHISELED_RED_SANDSTONE.defaultBlockState();
						case 2:
							return Blocks.SMOOTH_RED_SANDSTONE.defaultBlockState();
					}
				case "red_sandstone_slab":
				case "stone_slab2": 
					switch(data)
					{
						case 0:
						default:
							return Blocks.RED_SANDSTONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
						case 8:
							return Blocks.RED_SANDSTONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP);
					}
				case "double_red_sandstone_slab":
				case "double_stone_slab2": 
					return Blocks.RED_SANDSTONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.DOUBLE);
				case "sand":
					switch(data)
					{
						case 0:
						default:
							return Blocks.SAND.defaultBlockState();
						case 1:
							return Blocks.RED_SAND.defaultBlockState();
					}
				case "sandstone":
					switch(data)
					{
						case 0:
						default:
							return Blocks.SANDSTONE.defaultBlockState();
						case 1:
							return Blocks.CHISELED_SANDSTONE.defaultBlockState();
						case 2:
							return Blocks.SMOOTH_SANDSTONE.defaultBlockState();
					}
				case "sapling":
				case "oak_sapling":
					switch(data)
					{
						case 0:
						default:
							return Blocks.OAK_SAPLING.defaultBlockState();
						case 1:
							return Blocks.SPRUCE_SAPLING.defaultBlockState();
						case 2:
							return Blocks.BIRCH_SAPLING.defaultBlockState();
						case 3:
							return Blocks.JUNGLE_SAPLING.defaultBlockState();
						case 4:
							return Blocks.ACACIA_SAPLING.defaultBlockState();
						case 5:
							return Blocks.DARK_OAK_SAPLING.defaultBlockState();
					}
				case "skull":
				case "skeleton_skull":
					switch(data)
					{
						case 0:
						default:
							return Blocks.SKELETON_SKULL.defaultBlockState();
						case 1:
							return Blocks.WITHER_SKELETON_SKULL.defaultBlockState();
						case 2:
							return Blocks.ZOMBIE_HEAD.defaultBlockState();
						case 3:
							return Blocks.PLAYER_HEAD.defaultBlockState();
						case 4:
							return Blocks.CREEPER_HEAD.defaultBlockState();
						case 5:
							return Blocks.DRAGON_HEAD.defaultBlockState();
					}
				case "sponge":
					switch(data)
					{
						case 0:
						default:
							return Blocks.SPONGE.defaultBlockState();
						case 1:
							return Blocks.WET_SPONGE.defaultBlockState();
					}
				case "stained_glass":
				case "white_stained_glass":
					switch(data)
					{
						case 0:
						default:
							return Blocks.WHITE_STAINED_GLASS.defaultBlockState();
						case 1:
							return Blocks.ORANGE_STAINED_GLASS.defaultBlockState();
						case 2:
							return Blocks.MAGENTA_STAINED_GLASS.defaultBlockState();
						case 3:
							return Blocks.LIGHT_BLUE_STAINED_GLASS.defaultBlockState();
						case 4:
							return Blocks.YELLOW_STAINED_GLASS.defaultBlockState();
						case 5:
							return Blocks.LIME_STAINED_GLASS.defaultBlockState();
						case 6:
							return Blocks.PINK_STAINED_GLASS.defaultBlockState();
						case 7:
							return Blocks.GRAY_STAINED_GLASS.defaultBlockState();
						case 8:
							return Blocks.LIGHT_GRAY_STAINED_GLASS.defaultBlockState();
						case 9:
							return Blocks.CYAN_STAINED_GLASS.defaultBlockState();
						case 10:
							return Blocks.PURPLE_STAINED_GLASS.defaultBlockState();
						case 11:
							return Blocks.BLUE_STAINED_GLASS.defaultBlockState();
						case 12:
							return Blocks.BROWN_STAINED_GLASS.defaultBlockState();
						case 13:
							return Blocks.GREEN_STAINED_GLASS.defaultBlockState();
						case 14:
							return Blocks.RED_STAINED_GLASS.defaultBlockState();
						case 15:
							return Blocks.BLACK_STAINED_GLASS.defaultBlockState();
					}
				case "stained_glass_pane":
				case "white_stained_glass_pane":
				case "thin_glass":
					switch(data)
					{
						case 0:
						default:
							return Blocks.WHITE_STAINED_GLASS_PANE.defaultBlockState();
						case 1:
							return Blocks.ORANGE_STAINED_GLASS_PANE.defaultBlockState();
						case 2:
							return Blocks.MAGENTA_STAINED_GLASS_PANE.defaultBlockState();
						case 3:
							return Blocks.LIGHT_BLUE_STAINED_GLASS_PANE.defaultBlockState();
						case 4:
							return Blocks.YELLOW_STAINED_GLASS_PANE.defaultBlockState();
						case 5:
							return Blocks.LIME_STAINED_GLASS_PANE.defaultBlockState();
						case 6:
							return Blocks.PINK_STAINED_GLASS_PANE.defaultBlockState();
						case 7:
							return Blocks.GRAY_STAINED_GLASS_PANE.defaultBlockState();
						case 8:
							return Blocks.LIGHT_GRAY_STAINED_GLASS_PANE.defaultBlockState();
						case 9:
							return Blocks.CYAN_STAINED_GLASS_PANE.defaultBlockState();
						case 10:
							return Blocks.PURPLE_STAINED_GLASS_PANE.defaultBlockState();
						case 11:
							return Blocks.BLUE_STAINED_GLASS_PANE.defaultBlockState();
						case 12:
							return Blocks.BROWN_STAINED_GLASS_PANE.defaultBlockState();
						case 13:
							return Blocks.GREEN_STAINED_GLASS_PANE.defaultBlockState();
						case 14:
							return Blocks.RED_STAINED_GLASS_PANE.defaultBlockState();
						case 15:
							return Blocks.BLACK_STAINED_GLASS_PANE.defaultBlockState();
					}
				case "stained_hardened_clay":
				case "stained_clay":
				case "white_terracotta":
					switch(data)
					{
						case 0:
						default:
							return Blocks.WHITE_TERRACOTTA.defaultBlockState();
						case 1:
							return Blocks.ORANGE_TERRACOTTA.defaultBlockState();
						case 2:
							return Blocks.MAGENTA_TERRACOTTA.defaultBlockState();
						case 3:
							return Blocks.LIGHT_BLUE_TERRACOTTA.defaultBlockState();
						case 4:
							return Blocks.YELLOW_TERRACOTTA.defaultBlockState();
						case 5:
							return Blocks.LIME_TERRACOTTA.defaultBlockState();
						case 6:
							return Blocks.PINK_TERRACOTTA.defaultBlockState();
						case 7:
							return Blocks.GRAY_TERRACOTTA.defaultBlockState();
						case 8:
							return Blocks.LIGHT_GRAY_TERRACOTTA.defaultBlockState();
						case 9:
							return Blocks.CYAN_TERRACOTTA.defaultBlockState();
						case 10:
							return Blocks.PURPLE_TERRACOTTA.defaultBlockState();
						case 11:
							return Blocks.BLUE_TERRACOTTA.defaultBlockState();
						case 12:
							return Blocks.BROWN_TERRACOTTA.defaultBlockState();
						case 13:
							return Blocks.GREEN_TERRACOTTA.defaultBlockState();
						case 14:
							return Blocks.RED_TERRACOTTA.defaultBlockState();
						case 15:
							return Blocks.BLACK_TERRACOTTA.defaultBlockState();
					}
				case "stone":
					switch(data)
					{
						case 0:
						default:
							return Blocks.STONE.defaultBlockState();
						case 1:
							return Blocks.GRANITE.defaultBlockState();
						case 2:
							return Blocks.POLISHED_GRANITE.defaultBlockState();
						case 3:
							return Blocks.DIORITE.defaultBlockState();
						case 4:
							return Blocks.POLISHED_DIORITE.defaultBlockState();
						case 5:
							return Blocks.ANDESITE.defaultBlockState();
						case 6:
							return Blocks.POLISHED_ANDESITE.defaultBlockState();
					}
				case "stone_slab":
				case "step":
					switch(data)
					{
						case 0:
						default:
							return Blocks.SMOOTH_STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
						case 1:
							return Blocks.SANDSTONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
						//case 2:
						case 3:
							return Blocks.COBBLESTONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
						case 4:
							return Blocks.BRICK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
						case 5:
							return Blocks.STONE_BRICK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
						case 6:
							return Blocks.NETHER_BRICK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
						case 7:
							return Blocks.QUARTZ_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
						case 8:
							return Blocks.SMOOTH_STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP);
						case 9:
							return Blocks.SANDSTONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP);
						//case 10:
						case 11:
							return Blocks.COBBLESTONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP);
						case 12:
							return Blocks.BRICK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP);
						case 13:
							return Blocks.STONE_BRICK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP);
						case 14:
							return Blocks.NETHER_BRICK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP);
						case 15:
							return Blocks.QUARTZ_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP);
					}
				case "double_step":
					switch(data)
					{
						case 0:
						default:
							return Blocks.SMOOTH_STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.DOUBLE);
						case 1:
							return Blocks.SANDSTONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.DOUBLE);
						//case 2:
						case 3:
							return Blocks.COBBLESTONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.DOUBLE);
						case 4:
							return Blocks.BRICK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.DOUBLE);
						case 5:
							return Blocks.STONE_BRICK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.DOUBLE);
						case 6:
							return Blocks.NETHER_BRICK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.DOUBLE);
						case 7:
							return Blocks.QUARTZ_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.DOUBLE);
					}					
				case "stonebrick":
				case "stone_bricks":
				case "smooth_brick":
					switch(data)
					{
						case 0:
						default:
							return Blocks.STONE_BRICKS.defaultBlockState();
						case 1:
							return Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
						case 2:
							return Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
						case 3:
							return Blocks.CHISELED_STONE_BRICKS.defaultBlockState();
					}
				case "tallgrass":
				case "long_grass":
					switch(data)
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
					switch(data)
					{
						case 0:
						default:
							return Blocks.OAK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
						case 1:
							return Blocks.SPRUCE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
						case 2:
							return Blocks.BIRCH_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
						case 3:
							return Blocks.JUNGLE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
						case 4:
							return Blocks.ACACIA_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
						case 5:
							return Blocks.DARK_OAK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
						case 8:
							return Blocks.OAK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP);
						case 9:
							return Blocks.SPRUCE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP);
						case 10:
							return Blocks.BIRCH_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP);
						case 11:
							return Blocks.JUNGLE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP);
						case 12:
							return Blocks.ACACIA_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP);
						case 13:
							return Blocks.DARK_OAK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP);						
					}
				case "wool":
				case "white_wool":
					switch(data)
					{
						case 0:
						default:
							return Blocks.WHITE_WOOL.defaultBlockState();
						case 1:
							return Blocks.ORANGE_WOOL.defaultBlockState();
						case 2:
							return Blocks.MAGENTA_WOOL.defaultBlockState();
						case 3:
							return Blocks.LIGHT_BLUE_WOOL.defaultBlockState();
						case 4:
							return Blocks.YELLOW_WOOL.defaultBlockState();
						case 5:
							return Blocks.LIME_WOOL.defaultBlockState();
						case 6:
							return Blocks.PINK_WOOL.defaultBlockState();
						case 7:
							return Blocks.GRAY_WOOL.defaultBlockState();
						case 8:
							return Blocks.LIGHT_GRAY_WOOL.defaultBlockState();
						case 9:
							return Blocks.CYAN_WOOL.defaultBlockState();
						case 10:
							return Blocks.PURPLE_WOOL.defaultBlockState();
						case 11:
							return Blocks.BLUE_WOOL.defaultBlockState();
						case 12:
							return Blocks.BROWN_WOOL.defaultBlockState();
						case 13:
							return Blocks.GREEN_WOOL.defaultBlockState();
						case 14:
							return Blocks.RED_WOOL.defaultBlockState();
						case 15:
							return Blocks.BLACK_WOOL.defaultBlockState();
					}
	
				// Blocks with data
				case "fire":
					return Blocks.FIRE.defaultBlockState().setValue(FireBlock.AGE, data);
				case "cake":
				case "cake_block":
					return Blocks.CAKE.defaultBlockState().setValue(CakeBlock.BITES, data); 
				case "stone_pressure_plate":
				case "stone_plate":
					return Blocks.STONE_PRESSURE_PLATE.defaultBlockState().setValue(PressurePlateBlock.POWERED, getBit(data, 0) == 1);
				case "wooden_pressure_plate":
				case "wood_plate":
				case "oak_pressure_plate":
					return Blocks.OAK_PRESSURE_PLATE.defaultBlockState().setValue(PressurePlateBlock.POWERED, getBit(data, 0) == 1);
				case "light_weighted_pressure_plate":
				case "gold_plate":				
					return Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE.defaultBlockState().setValue(WeightedPressurePlateBlock.POWER, data);
				case "heavy_weighted_pressure_plate":
				case "iron_plate":
					return Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE.defaultBlockState().setValue(WeightedPressurePlateBlock.POWER, data);
				case "snow_layer":
					return Blocks.SNOW.defaultBlockState().setValue(SnowBlock.LAYERS, data);
				case "cactus":
					return Blocks.CACTUS.defaultBlockState().setValue(CactusBlock.AGE, data);
				case "reeds":
					return Blocks.SUGAR_CANE.defaultBlockState().setValue(SugarCaneBlock.AGE, data);
				case "jukebox":
					return Blocks.JUKEBOX.defaultBlockState().setValue(JukeboxBlock.HAS_RECORD, data == 1);
				case "wheat":
				case "crops":
					return Blocks.WHEAT.defaultBlockState().setValue(CropsBlock.AGE, data);
				case "carrot":
				case "carrots":
					return Blocks.CARROTS.defaultBlockState().setValue(CarrotBlock.AGE, data);
				case "potato":
				case "potatoes":
					return Blocks.POTATOES.defaultBlockState().setValue(PotatoBlock.AGE, data);
				case "beetroot":
				case "beetroots":
					return Blocks.BEETROOTS.defaultBlockState().setValue(BeetrootBlock.AGE, data);
				case "farmland":
				case "soil":
					return Blocks.FARMLAND.defaultBlockState().setValue(FarmlandBlock.MOISTURE, data);
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
					return Blocks.RAIL.defaultBlockState().setValue(RailBlock.SHAPE, getRailShape(data));
				case "powered_rail":
				case "golden_rail":
					return getRailsWithData(0, data);
				case "detector_rail":
					return getRailsWithData(1, data);
				case "activator_rail":
					return getRailsWithData(2, data);				
				case "hay_block":
					return Blocks.HAY_BLOCK.defaultBlockState().setValue(HayBlock.AXIS, getPillarAxisXYZ(data));
				case "bone_block":
					return Blocks.BONE_BLOCK.defaultBlockState().setValue(RotatedPillarBlock.AXIS, getAxisXYZ(data));			
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
				case "oak_button":
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
					return Blocks.RED_BANNER.defaultBlockState().setValue(BannerBlock.ROTATION, data);
				case "wall_banner":
					return Blocks.WHITE_WALL_BANNER.defaultBlockState().setValue(WallBannerBlock.FACING, getFacingNorthSouthWestEast(data));
				case "end_rod":
					return Blocks.END_ROD.defaultBlockState().setValue(EndRodBlock.FACING, getFacingDownUpNorthSouthWestEast(data));
				case "daylight_detector":
					return Blocks.DAYLIGHT_DETECTOR.defaultBlockState().setValue(DaylightDetectorBlock.POWER, data);
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
					return  getShulkerBoxWithData(16, data);		
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
					return Blocks.PUMPKIN_STEM.defaultBlockState().setValue(StemBlock.AGE, data); 
				case "melon_stem":
					// TODO: Hopefully this auto-updates to ATTACHED_MELON_STEM when placed next to a melon block..
					return Blocks.MELON_STEM.defaultBlockState().setValue(StemBlock.AGE, data);
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
					return Blocks.REDSTONE_WIRE.defaultBlockState().setValue(RedstoneWireBlock.POWER, data);
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
					return Blocks.PURPUR_PILLAR.defaultBlockState().setValue(RotatedPillarBlock.AXIS, getPillarAxisXYZ(data));
				case "nether_wart":
					return Blocks.NETHER_WART.defaultBlockState().setValue(NetherWartBlock.AGE, data);
				case "brewing_stand":
					return Blocks.BREWING_STAND.defaultBlockState()
						.setValue(BrewingStandBlock.HAS_BOTTLE[0], getBit(data, 0) == 1)
						.setValue(BrewingStandBlock.HAS_BOTTLE[1], getBit(data, 1) == 1)
						.setValue(BrewingStandBlock.HAS_BOTTLE[2], getBit(data, 2) == 1)
					;
				case "cauldron":
					return Blocks.CAULDRON.defaultBlockState().setValue(CauldronBlock.LEVEL, data);
				case "portal":
					return Blocks.NETHER_PORTAL.defaultBlockState().setValue(NetherPortalBlock.AXIS, getAxisXZ(data));
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
		} catch(IllegalArgumentException ex) {
			if(OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.CONFIGS))
			{
				OTG.getEngine().getLogger().log(LogMarker.WARN, LogCategory.CONFIGS, "Could not parse block with data, illegal data: " + blockName + ":" + data + ". Exception: " + ex.getMessage());
			}
		}
		return null;
	}

	//
	
	private static BlockState getAnvilWithData(int material, int data)
	{
		Direction orientation = getBit(data, 0) == 0 ? Direction.NORTH : Direction.WEST;
		switch(material)
		{
			case 0:
				// 0x4 0x8 state: regular (0x4 & 0x8 = 0), slightly damaged (0x4 = 1), very damaged (0x8 = 1)				
				if((getBit(data, 2) & getBit(data, 3)) == 0)
				{
					return Blocks.ANVIL.defaultBlockState().setValue(AnvilBlock.FACING, orientation);
				}
				else if(getBit(data, 2) == 1)
				{
					return Blocks.CHIPPED_ANVIL.defaultBlockState().setValue(AnvilBlock.FACING, orientation);
				}
				else if(getBit(data, 3) == 1)
				{
					return Blocks.DAMAGED_ANVIL.defaultBlockState().setValue(AnvilBlock.FACING, orientation);
				}
			case 1:
				return Blocks.CHIPPED_ANVIL.defaultBlockState().setValue(AnvilBlock.FACING, orientation);
			case 2:
				return Blocks.DAMAGED_ANVIL.defaultBlockState().setValue(AnvilBlock.FACING, orientation);
			default:
				return null;
		}
	}

	private static BlockState getLogWithData(int data)
	{
		int material = getBits(data, 0, 2);
		int orientation = getBits(data, 2, 2);
		Direction.Axis axisDirection = orientation == 0 ? Direction.Axis.Y : orientation == 1 ? Direction.Axis.X : orientation == 2 ? Direction.Axis.Z : Direction.Axis.Y;
		boolean bark = orientation == 3;
		switch(material)
		{
			case 0:
				if (bark) return Blocks.OAK_WOOD.defaultBlockState().setValue(RotatedPillarBlock.AXIS, axisDirection);
				return Blocks.OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, axisDirection);
			case 1:
				if (bark) return Blocks.SPRUCE_WOOD.defaultBlockState().setValue(RotatedPillarBlock.AXIS, axisDirection);
				return Blocks.SPRUCE_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, axisDirection);
			case 2:
				if (bark) return Blocks.BIRCH_WOOD.defaultBlockState().setValue(RotatedPillarBlock.AXIS, axisDirection);
				return Blocks.BIRCH_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, axisDirection);
			case 3:
				if (bark) return Blocks.JUNGLE_WOOD.defaultBlockState().setValue(RotatedPillarBlock.AXIS, axisDirection);
				return Blocks.JUNGLE_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, axisDirection);
			default:
				return Blocks.OAK_LOG.defaultBlockState();
		}
	}
	
	private static BlockState getLog2WithData(int data)
	{
		int material = getBits(data, 0, 2);
		int orientation = getBits(data, 2, 2);
		Direction.Axis axisDirection = orientation == 0 ? Direction.Axis.Y : orientation == 1 ? Direction.Axis.X : orientation == 2 ? Direction.Axis.Z : Direction.Axis.Y;
		boolean bark = orientation == 3;
		switch(material)
		{
			case 0:
				if (bark) return Blocks.ACACIA_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, axisDirection);
				return Blocks.ACACIA_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, axisDirection);
			case 1:
				if (bark) return Blocks.DARK_OAK_WOOD.defaultBlockState().setValue(RotatedPillarBlock.AXIS, axisDirection);
				return Blocks.DARK_OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, axisDirection);
			default:
				return Blocks.ACACIA_LOG.defaultBlockState();
		}
	}
	
	private static BlockState getQuartzBlockWithData(int data)
	{
		switch(data)
		{
			case 0:
			default:
				return Blocks.QUARTZ_BLOCK.defaultBlockState();
			case 1:
				return Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState();
			case 2:
				return Blocks.QUARTZ_PILLAR.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
			case 3:
				return Blocks.QUARTZ_PILLAR.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X);
			case 4:
				return Blocks.QUARTZ_PILLAR.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z);
		}
	}

	private static BlockState getTorchWithData(int material, int data)
	{
		switch(material)
		{
			case 0:
				switch(data)
				{
					case 0:
					case 5:
					default:
						return Blocks.TORCH.defaultBlockState();
					case 1:
						return Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.EAST);
					case 2:
						return Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.WEST);
					case 3:
						return Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.SOUTH);
					case 4:
						return Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.NORTH);
				}
			case 1:
				switch(data)
				{
					case 0:
					case 5:
					default:
						return Blocks.REDSTONE_TORCH.defaultBlockState();
					case 1:
						return Blocks.REDSTONE_WALL_TORCH.defaultBlockState().setValue(RedstoneWallTorchBlock.LIT, false).setValue(RedstoneWallTorchBlock.FACING, Direction.EAST);						
					case 2:
						return Blocks.REDSTONE_WALL_TORCH.defaultBlockState().setValue(RedstoneWallTorchBlock.LIT, false).setValue(RedstoneWallTorchBlock.FACING, Direction.WEST);						
					case 3:
						return Blocks.REDSTONE_WALL_TORCH.defaultBlockState().setValue(RedstoneWallTorchBlock.LIT, false).setValue(RedstoneWallTorchBlock.FACING, Direction.SOUTH);
					case 4:
						return Blocks.REDSTONE_WALL_TORCH.defaultBlockState().setValue(RedstoneWallTorchBlock.LIT, false).setValue(RedstoneWallTorchBlock.FACING, Direction.NORTH);
				}
			case 2:
				switch(data)
				{
					case 0:
					case 5:
					default:
						return Blocks.REDSTONE_TORCH.defaultBlockState().setValue(RedstoneWallTorchBlock.LIT, true);
					case 1:
						return Blocks.REDSTONE_WALL_TORCH.defaultBlockState().setValue(RedstoneWallTorchBlock.LIT, true).setValue(RedstoneWallTorchBlock.FACING, Direction.EAST);						
					case 2:
						return Blocks.REDSTONE_WALL_TORCH.defaultBlockState().setValue(RedstoneWallTorchBlock.LIT, true).setValue(RedstoneWallTorchBlock.FACING, Direction.WEST);						
					case 3:
						return Blocks.REDSTONE_WALL_TORCH.defaultBlockState().setValue(RedstoneWallTorchBlock.LIT, true).setValue(RedstoneWallTorchBlock.FACING, Direction.SOUTH);
					case 4:
						return Blocks.REDSTONE_WALL_TORCH.defaultBlockState().setValue(RedstoneWallTorchBlock.LIT, true).setValue(RedstoneWallTorchBlock.FACING, Direction.NORTH);
				}				
			default:
				return null;
		}
	}

	private static BlockState getRailsWithData(int material, int data)
	{
		int shape = getBits(data, 0, 3);
		int active = getBit(data, 3);
		switch(material)
		{
			case 0:
				return Blocks.POWERED_RAIL.defaultBlockState().setValue(PoweredRailBlock.SHAPE, getRailShape(shape)).setValue(PoweredRailBlock.POWERED, active == 1);
			case 1:
				return Blocks.DETECTOR_RAIL.defaultBlockState().setValue(DetectorRailBlock.SHAPE, getRailShape(shape)).setValue(DetectorRailBlock.POWERED, active == 1);
			case 2:
				return Blocks.ACTIVATOR_RAIL.defaultBlockState().setValue(PoweredRailBlock.SHAPE, getRailShape(shape)).setValue(PoweredRailBlock.POWERED, active == 1);
			default:
				return null;
		}
	}
	
	private static BlockState getStairsWithData(int material, int data)
	{
		int facing = getBits(data, 0, 2);
		int half = getBit(data, 2);
		BlockState output = null;
		switch(material)
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
			.setValue(StairsBlock.FACING, getFacingEastWestSouthNorth(facing))
			.setValue(StairsBlock.HALF, half == 0 ? Half.BOTTOM : Half.TOP);
	}
	
	private static BlockState getLeverOrButtonWithData(int material, int data)
	{
		int facing = getBits(data, 0, 3);
		int powered = getBit(data, 3);
		AttachFace face = facing == 0 || facing == 7 ? AttachFace.CEILING : facing == 1 || facing == 2 || facing == 3 || facing == 4 ? AttachFace.WALL : facing == 5 || facing == 6 ? AttachFace.FLOOR : AttachFace.FLOOR;
		
		switch(material)
		{
			case 0:
				return Blocks.LEVER.defaultBlockState().setValue(LeverBlock.FACE, face).setValue(LeverBlock.FACING, getFacingLever(facing)).setValue(LeverBlock.POWERED, powered == 1);
			case 1:
				return Blocks.STONE_BUTTON.defaultBlockState().setValue(StoneButtonBlock.FACE, face).setValue(StoneButtonBlock.FACING, getFacingButton(facing)).setValue(StoneButtonBlock.POWERED, powered == 1);
			case 2:
				return Blocks.OAK_BUTTON.defaultBlockState().setValue(WoodButtonBlock.FACE, face).setValue(WoodButtonBlock.FACING, getFacingButton(facing)).setValue(WoodButtonBlock.POWERED, powered == 1);
			default:
				return null;
		}
	}
	
	private static BlockState getDoorWithData(int material, int data)
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
		switch(material)
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
			.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER)			
			.setValue(DoorBlock.FACING, getFacingEastSouthWestNorth(facing))
			.setValue(DoorBlock.OPEN, open == 1) 
			: blockState
			.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER)
			.setValue(DoorBlock.HINGE, hinge == 0 ? DoorHingeSide.LEFT : DoorHingeSide.RIGHT)
			.setValue(DoorBlock.POWERED, powered == 1)
		;
	}
	
	private static BlockState getSignPostWithData(int data)
	{	
		int rotation = getBits(data, 0, 4);
		// TODO: Hopefully rotation is still mapped to the same int values as 1.12..
		return Blocks.OAK_SIGN.defaultBlockState().setValue(StandingSignBlock.ROTATION, rotation); 
	}

	private static BlockState getWallSignWithData(int data)
	{
		int facing = getBits(data, 0, 3);
		return Blocks.OAK_WALL_SIGN.defaultBlockState().setValue(WallSignBlock.FACING, getFacingNorthSouthWestEast(facing));
	}
	
	// TODO: Can't find information on 1.12 command block block data, what about facing?
	private static BlockState getCommandBlockWithData(int material, int data)
	{
		BlockState blockState;
		switch(material)
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

	private static BlockState getShulkerBoxWithData(int material, int data)
	{
		BlockState blockState;
		switch(material)
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
		return blockState.setValue(ShulkerBoxBlock.FACING, getFacingDownEastNorthSouthUpWest(data));
	}
	
	private static BlockState getLadderChestOrFurnaceWithData(int material, int data)
	{
		int facing = getBits(data, 0, 3);
		switch(material)
		{
			case 0:
				return Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, getFacingNorthSouthWestEast(facing));
			case 1:
				return Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, getFacingNorthSouthWestEast(facing));
			case 2:
				return Blocks.ENDER_CHEST.defaultBlockState().setValue(EnderChestBlock.FACING, getFacingNorthSouthWestEast(facing));
			case 3:
				return Blocks.TRAPPED_CHEST.defaultBlockState().setValue(TrappedChestBlock.FACING, getFacingNorthSouthWestEast(facing));
			case 4:
				return Blocks.FURNACE.defaultBlockState().setValue(FurnaceBlock.FACING, getFacingNorthSouthWestEast(facing)).setValue(FurnaceBlock.LIT, false);
			case 5:
				return Blocks.FURNACE.defaultBlockState().setValue(FurnaceBlock.FACING, getFacingNorthSouthWestEast(facing)).setValue(FurnaceBlock.LIT, true);
			default:
				return null;
		}
	}
	
	private static BlockState getDispenserHopperDropperWithData(int material, int data)
	{
		int facing = getBits(data, 0, 3);
		int active = getBit(data, 3);
		switch(material)
		{
			case 0:
				return Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING, getFacingDownUpNorthSouthWestEast(facing)).setValue(DispenserBlock.TRIGGERED, active == 1);
			case 1:
				return Blocks.DROPPER.defaultBlockState().setValue(DropperBlock.FACING, getFacingDownUpNorthSouthWestEast(facing)).setValue(DropperBlock.TRIGGERED, active == 1);
			case 2:
				return Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING, getFacingDownUpNorthSouthWestEast(facing)).setValue(HopperBlock.ENABLED, active == 1);
			default:
				return null;
		}
	}

	private static BlockState getJackOLanternOrPumpkinWithData(int material, int data)
	{
		int facing = getBits(data, 0, 2);
		switch(material)
		{
			case 0:
				return Blocks.CARVED_PUMPKIN.defaultBlockState().setValue(CarvedPumpkinBlock.FACING, getFacingSouthWestNorthEast(facing));
			case 1:
				return Blocks.JACK_O_LANTERN.defaultBlockState().setValue(CarvedPumpkinBlock.FACING, getFacingSouthWestNorthEast(facing));
			default:
				return null;
		}
	}
	
	private static BlockState getObserverWithData(int data)
	{
		int facing = getBits(data, 0, 3);
		int powered = getBit(data, 3);
		return Blocks.OBSERVER.defaultBlockState().setValue(ObserverBlock.FACING, getFacingDownUpNorthSouthWestEast(facing)).setValue(ObserverBlock.POWERED, powered == 1);
	}
	
	private static BlockState getRepeaterWithData(int material, int data)
	{
		int facing = getBits(data, 0, 2);
		int delay = getBits(data, 2, 2) + 1;
		BlockState blockState;
		switch(material)
		{
			case 0:
				blockState = Blocks.REPEATER.defaultBlockState().setValue(RepeaterBlock.POWERED, false);
				break;
			case 1:
				blockState = Blocks.REPEATER.defaultBlockState().setValue(RepeaterBlock.POWERED, true);
				break;
			default:
				return null;
		}
		return blockState
			.setValue(RepeaterBlock.DELAY, delay)
			.setValue(RepeaterBlock.FACING, getFacingSouthWestNorthEast(facing))
		;
	}
	
	private static BlockState getComparatorWithData(int material, int data)
	{
		int facing = getBits(data, 0, 2);
		int mode = getBit(data, 2);
		int powered = material == 1 ? 1 : getBit(data, 3);
		return Blocks.COMPARATOR.defaultBlockState()
			.setValue(ComparatorBlock.FACING, getFacingSouthWestNorthEast(facing))
			.setValue(ComparatorBlock.MODE, mode == 0 ?	ComparatorMode.COMPARE : ComparatorMode.SUBTRACT)
			.setValue(ComparatorBlock.POWERED, powered == 1)
		;
	}
	
	private static BlockState getBedBlockWithData(int data)
	{
		int facing = getBits(data, 0, 2);
		int occupied = getBit(data, 2);
		int part = getBit(data, 3);
		return Blocks.RED_BED.defaultBlockState()
			.setValue(BedBlock.FACING, getFacingSouthWestNorthEast(facing))
			.setValue(BedBlock.OCCUPIED, occupied == 1)
			.setValue(BedBlock.PART, part == 0 ? BedPart.FOOT : BedPart.HEAD)
		;
	}
	
	private static BlockState getTrapDoorBlockWithData(int material, int data)
	{
		int facing = getBits(data, 0, 2);
		int open = getBit(data, 2);
		int half = getBit(data, 3);		
		BlockState blockState;
		switch(material)
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
			.setValue(TrapDoorBlock.FACING, getFacingSouthNorthEastWest(facing))
			.setValue(TrapDoorBlock.HALF, half == 0 ? Half.BOTTOM : Half.TOP)
			.setValue(TrapDoorBlock.OPEN, open == 1)
		;
	}
	
	private static BlockState getPistonWithData(int material, int data)
	{
		int facing = getBits(data, 0, 3);
		int extended = getBit(data, 3);
		switch(material)
		{
			case 0:
				return Blocks.PISTON.defaultBlockState().setValue(PistonBlock.EXTENDED, extended == 1).setValue(PistonBlock.FACING, getFacingDownUpNorthSouthWestEast(facing));
			case 1:				
				return Blocks.STICKY_PISTON.defaultBlockState().setValue(PistonBlock.EXTENDED, extended == 1).setValue(PistonBlock.FACING, getFacingDownUpNorthSouthWestEast(facing));
			default:
				return null;
		}
	}
	
	private static BlockState getPistonHeadWithData(int data)
	{
		int facing = getBits(data, 0, 3);
		int type = getBit(data, 3);
		return Blocks.PISTON_HEAD.defaultBlockState()
			.setValue(PistonHeadBlock.FACING, getFacingDownUpNorthSouthWestEast(facing))
			.setValue(PistonHeadBlock.TYPE, type == 0 ? PistonType.DEFAULT : PistonType.STICKY)			
		;
	}

	private static BlockState getHugeMushroomWithData(int material, int data)
	{
		boolean down = data == 14 || data == 15;
		boolean up = data == 1 || data == 2 || data == 3 || data == 4 || data == 5 || data == 6 || data == 7 || data == 8 || data == 9 || data == 14 || data == 15;
		boolean north = data == 1 || data == 2 || data == 3 || data == 10 || data == 14 || data == 15;
		boolean east = data == 3 || data == 6 || data == 9 || data == 10 || data == 14 || data == 15;
		boolean south = data == 7 || data == 8 || data == 9 || data == 10 || data == 14 || data == 15;
		boolean west = data == 1 || data == 4 || data == 7 || data == 10 || data == 14 || data == 15;
		BlockState blockState;
		if(data == 10 || data == 15)
		{
			blockState = Blocks.MUSHROOM_STEM.defaultBlockState();
		} else {
			switch(material)
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
			.setValue(HugeMushroomBlock.DOWN, down)
			.setValue(HugeMushroomBlock.UP, up)
			.setValue(HugeMushroomBlock.NORTH, north)
			.setValue(HugeMushroomBlock.EAST, east)
			.setValue(HugeMushroomBlock.SOUTH, south)
			.setValue(HugeMushroomBlock.WEST, west)
		;
	}
	
	private static BlockState getVineWithData(int data)
	{
		int south = getBit(data, 0);
		int west = getBit(data, 1);
		int north = getBit(data, 2);
		int east = getBit(data, 3);
		int up = data == 0 ? 1 : 0; // TODO: Should also be true if there's a block above, test if this is done dynamically.
		return Blocks.VINE.defaultBlockState()
			.setValue(VineBlock.EAST, east == 1)
			.setValue(VineBlock.NORTH, north == 1)
			.setValue(VineBlock.SOUTH, south == 1)
			.setValue(VineBlock.WEST, west == 1)
			.setValue(VineBlock.UP, up == 1)
		;
	}
	
	private static BlockState getFenceGateWithData(int material, int data)
	{
		int facing = getBits(data, 0, 2);
		int open = getBit(data, 2);
		BlockState blockState;
		switch(material)
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
			.setValue(FenceGateBlock.FACING, getFacingSouthWestNorthEast(facing))
			.setValue(FenceGateBlock.OPEN, open == 1)
		;
	}
	
	private static BlockState getCocoaWithData(int data)
	{
		int facing = getBits(data, 0, 2);
		int age = getBits(data, 2, 2);
		return Blocks.COCOA.defaultBlockState().setValue(CocoaBlock.FACING, getFacingNorthEastSouthWest(facing)).setValue(CocoaBlock.AGE, age);
	}
	
	private static BlockState getTripWireHookWithData(int data)
	{
		int facing = getBits(data, 0, 2);
		int attached = getBit(data, 2);
		int powered = getBit(data, 3);
		return Blocks.TRIPWIRE_HOOK.defaultBlockState()
			.setValue(TripWireHookBlock.ATTACHED, attached == 1)
			.setValue(TripWireHookBlock.FACING, getFacingSouthWestNorthEast(facing))
			.setValue(TripWireHookBlock.POWERED, powered == 1)
		;
	}
	
	private static BlockState getEndPortalFrameWithData(int data)
	{
		int facing = getBits(data, 0, 2);
		int eye = getBit(data, 2);
		return Blocks.END_PORTAL_FRAME.defaultBlockState().setValue(EndPortalFrameBlock.HAS_EYE, eye == 1).setValue(EndPortalFrameBlock.FACING, getFacingSouthWestNorthEast(facing));
	}	
	
	private static BlockState getStructureBlockWithData(int data)
	{
		StructureMode structureBlockMode = data == 0 ? StructureMode.DATA : data == 1 ? StructureMode.SAVE : data == 2 ? StructureMode.LOAD : data == 3 ? StructureMode.LOAD : StructureMode.DATA;
		return Blocks.STRUCTURE_BLOCK.defaultBlockState().setValue(StructureBlock.MODE, structureBlockMode);
	}
	
	private static BlockState getGlazedTerracottaWithData(int material, int data)
	{
		int facing = getBits(data, 0, 2);
		BlockState blockState;
		switch(material)
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
		return blockState.setValue(GlazedTerracottaBlock.FACING, getFacingSouthWestNorthEast(facing));
	}
	
	private static BlockState getTripWireWithData(int data)
	{
		int active = getBit(data, 0);
		int attached = getBit(data, 2);
		int disarmed = getBit(data, 3);
		return Blocks.TRIPWIRE.defaultBlockState()
			.setValue(TripWireBlock.POWERED, active == 1)
			.setValue(TripWireBlock.ATTACHED, attached == 1)
			.setValue(TripWireBlock.DISARMED, disarmed == 1)
		;
	}
	
	//	

	private static Direction.Axis getAxisXYZ(int data)
	{
		switch(data)
		{
			case 0:
				return Direction.Axis.X;
			case 1:
				return Direction.Axis.Y;
			case 2:
				return Direction.Axis.Z;
			default:
				return Direction.Axis.Y;
		}
	}
	
	private static Direction.Axis getAxisXZ(int data)
	{
		switch(data)
		{
			case 1:
				return Direction.Axis.X;
			case 2:
				return Direction.Axis.Z;
			default:
				return Direction.Axis.X;
		}
	}
	
	private static Direction getFacingSouthWestNorthEast(int data)
	{
		switch(data)
		{
			case 0:
				return Direction.SOUTH;
			case 1:
				return Direction.WEST;
			case 2:
				return Direction.NORTH;
			case 3:
				return Direction.EAST;
			default:
				return Direction.SOUTH;
		}
	}
	
	private static Direction getFacingNorthSouthWestEast(int data)
	{
		switch(data)
		{
			case 2:
				return Direction.NORTH;
			case 3:
				return Direction.SOUTH;
			case 4:
				return Direction.WEST;
			case 5:
				return Direction.EAST;				
			default:
				return Direction.NORTH;
		}
	}
	
	private static Direction getFacingNorthEastSouthWest(int data)
	{
		switch(data)
		{
			case 0:
				return Direction.NORTH;
			case 1:
				return Direction.EAST;
			case 2:
				return Direction.SOUTH;
			case 3:
				return Direction.WEST;
			default:
				return Direction.NORTH;
		}
	}
	
	private static Direction getFacingDownUpNorthSouthWestEast(int data)
	{
		switch(data)
		{
			case 0:
				return Direction.DOWN;
			case 1:
				return Direction.UP;
			case 2:
				return Direction.NORTH;
			case 3:
				return Direction.SOUTH;
			case 4:
				return Direction.WEST;
			case 5:
				return Direction.EAST;
			default:
				return Direction.DOWN;
		}
	}	
	
	private static Direction getFacingSouthNorthEastWest(int data)
	{
		switch(data)
		{
			case 0:
				return Direction.SOUTH;
			case 1:
				return Direction.NORTH;
			case 2:
				return Direction.EAST;
			case 3:
				return Direction.WEST;
			default:
				return Direction.SOUTH;
		}
	}	
	
	private static Direction getFacingEastSouthWestNorth(int data)
	{
		switch(data)
		{
			case 0:
				return Direction.EAST;
			case 1:
				return Direction.SOUTH;
			case 2:
				return Direction.WEST;				
			case 3:
				return Direction.NORTH;
			default:
				return Direction.EAST;
		}
	}			
	
	private static Direction getFacingEastWestSouthNorth(int data)
	{
		switch(data)
		{
			case 0:
				return Direction.EAST;
			case 1:
				return Direction.WEST;
			case 2:
				return Direction.SOUTH;
			case 3:
				return Direction.NORTH;
			default:
				return Direction.EAST;
		}
	}
	
	// TODO: Couldn't find docs for 1.12.2 shulker box	
	// data values, these rotations may be incorrect.
	private static Direction getFacingDownEastNorthSouthUpWest(int data)
	{
		switch(data)
		{
			case 0:
				return Direction.DOWN;
			case 1:
				return Direction.EAST;
			case 2:
				return Direction.NORTH;
			case 3:
				return Direction.SOUTH;
			case 4:
				return Direction.UP;
			case 5:
				return Direction.WEST;
			default:
				return Direction.UP;
		}
	}
		
	private static Direction.Axis getPillarAxisXYZ(int data)
	{
		switch(data)
		{
			case 0:
				return Direction.Axis.Y;
			case 4:
				return Direction.Axis.X;
			case 8:
				return Direction.Axis.Z;
			default:
				return Direction.Axis.Y;
		}
	}
	
	// TODO: Test this
	private static Direction getFacingLever(int data)
	{
		switch(data)
		{
			case 0:
			case 1:
			case 6:
				return Direction.EAST;
			case 2:
				return Direction.WEST;
			case 3:
			case 5:
			case 7:
				return Direction.SOUTH;
			case 4:
				return Direction.NORTH;
			default:
				return Direction.EAST;
		}
	}
	
	private static Direction getFacingButton(int data)
	{
		switch(data)
		{
			case 1:
				return Direction.EAST;
			case 2:
				return Direction.WEST;
			case 3:
				return Direction.SOUTH;
			case 4:
				return Direction.NORTH;
			default:
				return Direction.EAST;
		}
	}
	
	private static RailShape getRailShape(int shape)
	{
		switch (shape)
		{
			case 0:
				return RailShape.NORTH_SOUTH;
			case 1:
				return RailShape.EAST_WEST;
			case 2:
				return RailShape.ASCENDING_EAST;
			case 3:
				return RailShape.ASCENDING_WEST;
			case 4:
				return RailShape.ASCENDING_NORTH;
			case 5:
				return RailShape.ASCENDING_SOUTH;
			case 6:
				return RailShape.SOUTH_EAST;
			case 7:
				return RailShape.SOUTH_WEST;
			case 8:
				return RailShape.NORTH_WEST;
			case 9:
				return RailShape.NORTH_EAST;
			default:
				return RailShape.NORTH_SOUTH;
		}
	}	
	
	private static int getBits(int source, int index, int length)
	{
		int bits = 0;
		for(int i = 0; i < length; i++)
		{
			bits = bits | (getBit(source, index + i) << i);
		}
		return bits;
	}
	
	private static int getBit(int source, int index)
	{
		return ((source & (1 << index)) >> index);
	}
}
