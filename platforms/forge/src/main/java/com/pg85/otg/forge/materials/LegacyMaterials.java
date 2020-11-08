package com.pg85.otg.forge.materials;

import com.pg85.otg.OTG;
import com.pg85.otg.logging.LogMarker;

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
public class LegacyMaterials
{
    static BlockState fromLegacyBlockName(String oldBlockName)
    {
    	switch(oldBlockName.replace("minecraft:", ""))
    	{
    		case "stationary_water":
    			return Blocks.WATER.getDefaultState();
    		case "stationary_lava":
    			return Blocks.LAVA.getDefaultState();
    		case "stained_clay":
    			return Blocks.WHITE_TERRACOTTA.getDefaultState();
    		case "hard_clay":
    			return Blocks.TERRACOTTA.getDefaultState();
    		case "step":
    			return Blocks.STONE_SLAB.getDefaultState();
    		case "sugar_cane_block":
    			return Blocks.SUGAR_CANE.getDefaultState();
    		case "melon_block":
    			return Blocks.MELON.getDefaultState();
    		case "water_lily":
    			return Blocks.LILY_PAD.getDefaultState();
    		case "soil":
    			return Blocks.FARMLAND.getDefaultState();
    		case "long_grass":
    			return Blocks.TALL_GRASS.getDefaultState();
    		case "mycel":
    			return Blocks.MYCELIUM.getDefaultState();
    		case "snow_layer":
    			return Blocks.SNOW.getDefaultState();
    		case "leaves":
    			return Blocks.OAK_LEAVES.getDefaultState().with(LeavesBlock.DISTANCE, 1);
    		case "red_rose":
    			return Blocks.ROSE_BUSH.getDefaultState();
    		case "smooth_stairs":
    			return Blocks.STONE_STAIRS.getDefaultState();
    		case "yellow_flower":
    			return Blocks.DANDELION.getDefaultState();
    		case "web":
    			return Blocks.COBWEB.getDefaultState();
    		case "wall_banner":
    			return Blocks.WHITE_WALL_BANNER.getDefaultState();
    		case "redstone_lamp_on":
    			return Blocks.REDSTONE_LAMP.getDefaultState().with(RedstoneLampBlock.LIT, true);
    		case "redstone_lamp_off":
    			return Blocks.REDSTONE_LAMP.getDefaultState().with(RedstoneLampBlock.LIT, false);
    		case "wool":
    			return Blocks.WHITE_WOOL.getDefaultState();
    		case "log":
    		case "wood":
    			return Blocks.OAK_LOG.getDefaultState();
    		case "spruce_wood_stairs":
    			return Blocks.SPRUCE_STAIRS.getDefaultState();
    		case "magma":
    			return Blocks.MAGMA_BLOCK.getDefaultState();
    		case "tallgrass":
				return Blocks.GRASS.getDefaultState();
    		case "cobble_wall":
    			return Blocks.COBBLESTONE_WALL.getDefaultState();
    		case "iron_fence":
    			return Blocks.IRON_BARS.getDefaultState();
    		case "workbench":
    			return Blocks.CRAFTING_TABLE.getDefaultState();
    		case "mob_spawner":
    			return Blocks.INFESTED_STONE.getDefaultState();
    		case "double_step":
    			return Blocks.SMOOTH_STONE.getDefaultState();
    		case "smooth_brick":
    			return Blocks.STONE_BRICKS.getDefaultState();
    		case "rails":
    			return Blocks.RAIL.getDefaultState();
    		case "fence":
    			return Blocks.OAK_FENCE.getDefaultState();
    		case "wood_step":
    			return Blocks.OAK_SLAB.getDefaultState();
    		case "wood_stairs":
    			return Blocks.OAK_STAIRS.getDefaultState();
    		case "thin_glass":
    			return Blocks.GLASS_PANE.getDefaultState();
    		case "stone_plate":
    			return Blocks.STONE_PRESSURE_PLATE.getDefaultState();
    		case "wood_plate":
    			return Blocks.OAK_PRESSURE_PLATE.getDefaultState();
    		case "wood_double_step":
    			return Blocks.OAK_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.DOUBLE);
    		case "brick":
    			return Blocks.BRICKS.getDefaultState();
    		case "iron_door_block":
    			return Blocks.IRON_DOOR.getDefaultState();
    		case "carpet":
    			return Blocks.WHITE_CARPET.getDefaultState();
    		case "carrot":
    			return Blocks.CARROTS.getDefaultState();
    		case "skull":
    			return Blocks.SKELETON_SKULL.getDefaultState();
    		case "mcpitman":
    			return Blocks.CREEPER_HEAD.getDefaultState();
    		case "pg85":
    			return Blocks.ZOMBIE_HEAD.getDefaultState();
    		case "supercoder":
    			return Blocks.CAKE.getDefaultState();
    		case "authvin":
				return Blocks.WET_SPONGE.getDefaultState();
    		case "josh":
				return Blocks.BARREL.getDefaultState();
    		case "wahrheit":
				return Blocks.LECTERN.getDefaultState();
    		case "lordsmellypants":
				return Blocks.FLOWER_POT.getDefaultState();
			default:
				return null;
    	}
    }

	static String blockNameFromLegacyBlockId(int id)
	{
    	switch(id)
    	{
	    	case 0:
	    		return "air";
	    	case 1:
	    		return "stone";
	    	case 2:
	    		return "grass_block";
	    	case 3:
	    		return "dirt";
	    	case 4:
	    		return "cobblestone";
	    	case 5:
	    		return "oak_planks";
	    	case 6:
	    		return "oak_sapling";
	    	case 7:
	    		return "bedrock";
	    	case 8:
	    		return "water";
	    	case 9:
	    		return "water";
	    	case 10:
	    		return "lava";
	    	case 11:
	    		return "lava";
	    	case 12:
	    		return "sand";
	    	case 13:
	    		return "gravel";
	    	case 14:
	    		return "gold_ore";
	    	case 15:
	    		return "iron_ore";
	    	case 16:
	    		return "coal_ore";
	    	case 17:
	    		return "oak_log";
	    	case 18:
	    		return "oak_leaves";
	    	case 19:
	    		return "sponge";
	    	case 20:
	    		return "glass";
	    	case 21:
	    		return "lapis_ore";
	    	case 22:
	    		return "lapis_block";
	    	case 23:
	    		return "dispenser";
	    	case 24:
	    		return "sandstone";
	    	case 25:
	    		return "note_block";
	    	case 26:
	    		return "white_bed";
	    	case 27:
	    		return "powered_rail";
	    	case 28:
	    		return "detector_rail";
	    	case 29:
	    		return "sticky_piston";
	    	case 30:
	    		return "cobweb";
	    	case 31:
	    		return "tall_grass";
	    	case 32:
	    		return "dead_bush";
	    	case 33:
	    		return "piston";
	    	case 34:
	    		return "piston_head";
	    	case 35:
	    		return "white_wool";
	    	case 36:
	    		return "piston_extension";
	    	case 37:
	    		return "dandelion";
	    	case 38:
	    		return "rose_bush";
	    	case 39:
	    		return "brown_mushroom";
	    	case 40:
	    		return "red_mushroom";
	    	case 41:
	    		return "gold_block";
	    	case 42:
	    		return "iron_block";
	    	case 43:
	    		return "smooth_stone"; // Double-slab
	    	case 44:
	    		return "stone_slab";
	    	case 45:
	    		return "bricks";
	    	case 46:
	    		return "tnt";
	    	case 47:
	    		return "bookshelf";
	    	case 48:
	    		return "mossy_cobblestone";
	    	case 49:
	    		return "obsidian";
	    	case 50:
	    		return "torch";
	    	case 51:
	    		return "fire";
	    	case 52:
	    		return "spawner";
	    	case 53:
	    		return "oak_stairs";
	    	case 54:
	    		return "chest";
	    	case 55:
	    		return "redstone_wire";
	    	case 56:
	    		return "diamond_ore";
	    	case 57:
	    		return "diamond_block";
	    	case 58:
	    		return "crafting_table";
	    	case 59:
	    		return "wheat";
	    	case 60:
	    		return "farmland";
	    	case 61:
	    		return "furnace";
	    	case 62:
	    		return "furnace"; // TODO: Should be lit
	    	case 63:
	    		return "oak_sign";
	    	case 64:
	    		return "oak_door";
	    	case 65:
	    		return "ladder";
	    	case 66:
	    		return "rail";
	    	case 67:
	    		return "stone_stairs";
	    	case 68:
	    		return "oak_wall_sign";
	    	case 69:
	    		return "lever";
	    	case 70:
	    		return "stone_pressure_plate";
	    	case 71:
	    		return "iron_door";
	    	case 72:
	    		return "oak_pressure_plate";
	    	case 73:
	    		return "redstone_ore";
	    	case 74:
	    		return "redstone_ore"; // TODO: Should be lit
	    	case 75:
	    		return "redstone_torch"; // TODO: Should be unlit
	    	case 76:
	    		return "redstone_torch"; // TODO: Should be lit
	    	case 77:
	    		return "stone_button";
	    	case 78:
	    		return "snow";
	    	case 79:
	    		return "ice";
	    	case 80:
	    		return "snow_block";
	    	case 81:
	    		return "cactus";
	    	case 82:
	    		return "clay";
	    	case 83:
	    		return "sugar_cane";
	    	case 84:
	    		return "jukebox";
	    	case 85:
	    		return "oak_fence";
	    	case 86:
	    		return "pumpkin";
	    	case 87:
	    		return "netherrack";
	    	case 88:
	    		return "soul_sand";
	    	case 89:
	    		return "glowstone";
	    	case 90:
	    		return "nether_portal";
	    	case 91:
	    		return "jack_o_lantern";
	    	case 92:
	    		return "cake";
	    	case 93:
	    		return "unpowered_repeater";
	    	case 94:
	    		return "powered_repeater";
	    	case 95:
	    		return "white_stained_glass";
	    	case 96:
	    		return "oak_trapdoor";
	    	case 97:
	    		return "infested_stone";
	    	case 98:
	    		return "stone_bricks";
	    	case 99:
	    		return "brown_mushroom_block";
	    	case 100:
	    		return "red_mushroom_block";
	    	case 101:
	    		return "iron_bars";
	    	case 102:
	    		return "glass_pane";
	    	case 103:
	    		return "melon";
	    	case 104:
	    		return "pumpkin_stem";
	    	case 105:
	    		return "melon_stem";
	    	case 106:
	    		return "vine";
	    	case 107:
	    		return "oak_fence_gate";
	    	case 108:
	    		return "brick_stairs";
	    	case 109:
	    		return "stone_brick_stairs";
	    	case 110:
	    		return "mycelium";
	    	case 111:
	    		return "lily_pad";
	    	case 112:
	    		return "nether_bricks";
	    	case 113:
	    		return "nether_brick_fence";
	    	case 114:
	    		return "nether_brick_stairs";
	    	case 115:
	    		return "nether_wart";
	    	case 116:
	    		return "enchanting_table";
	    	case 117:
	    		return "brewing_stand";
	    	case 118:
	    		return "cauldron";
	    	case 119:
	    		return "end_portal";
	    	case 120:
	    		return "end_portal_frame";
	    	case 121:
	    		return "end_stone";
	    	case 122:
	    		return "dragon_egg";
	    	case 123:
	    		return "redstone_lamp";
	    	case 124:
	    		return "redstone_lamp"; // TODO: Should be lit
	    	case 125:
	    		return "oak_planks"; // Double-slab
	    	case 126:
	    		return "oak_slab";
	    	case 127:
	    		return "cocoa";
	    	case 128:
	    		return "sandstone_stairs";
	    	case 129:
	    		return "emerald_ore";
	    	case 130:
	    		return "ender_chest";
	    	case 131:
	    		return "tripwire_hook";
	    	case 132:
	    		return "tripwire";
	    	case 133:
	    		return "emerald_block";
	    	case 134:
	    		return "spruce_stairs";
	    	case 135:
	    		return "birch_stairs";
	    	case 136:
	    		return "jungle_stairs";
	    	case 137:
	    		return "command_block";
	    	case 138:
	    		return "beacon";
	    	case 139:
	    		return "cobblestone_wall";
	    	case 140:
	    		return "flower_pot";
	    	case 141:
	    		return "carrots";
	    	case 142:
	    		return "potatoes";
	    	case 143:
	    		return "oak_button";
	    	case 144:
	    		return "skeleton_skull";
	    	case 145:
	    		return "anvil";
	    	case 146:
	    		return "trapped_chest";
	    	case 147:
	    		return "light_weighted_pressure_plate";
	    	case 148:
	    		return "heavy_weighted_pressure_plate";
	    	case 149:
	    		return "comparator"; // TODO: Should be unpowered 
	    	case 150:
	    		return "comparator"; // TODO: Should be powered
	    	case 151:
	    		return "daylight_detector";
	    	case 152:
	    		return "redstone_block";
	    	case 153:
	    		return "nether_quartz_ore";
	    	case 154:
	    		return "hopper";
	    	case 155:
	    		return "quartz_block";
	    	case 156:
	    		return "quartz_stairs";
	    	case 157:
	    		return "activator_rail";
	    	case 158:
	    		return "dropper";
	    	case 159:
	    		return "white_terracotta";
	    	case 160:
	    		return "white_stained_glass_pane";
	    	case 161:
	    		return "acacia_leaves";
	    	case 162:
	    		return "acacia_log";
	    	case 163:
	    		return "acacia_stairs";
	    	case 164:
	    		return "dark_oak_stairs";
	    	case 165:
	    		return "slime_block";
	    	case 166:
	    		return "barrier";
	    	case 167:
	    		return "iron_trapdoor";
	    	case 168:
	    		return "prismarine";
	    	case 169:
	    		return "sea_lantern";
	    	case 170:
	    		return "hay_block";
	    	case 171:
	    		return "white_carpet";
	    	case 172:
	    		return "white_terracotta";
	    	case 173:
	    		return "coal_block";
	    	case 174:
	    		return "packed_ice";
	    	case 175:
	    		return "sunflower";
	    	case 176:
	    		return "standing_banner";
	    	case 177:
	    		return "wall_banner";
	    	case 178:
	    		return "daylight_detector_inverted";
	    	case 179:
	    		return "red_sandstone";
	    	case 180:
	    		return "red_sandstone_stairs";
	    	case 181:
	    		return "red_sandstone"; // Double-slab
	    	case 182:
	    		return "red_sandstone_slab";
	    	case 183:
	    		return "spruce_fence_gate";
	    	case 184:
	    		return "birch_fence_gate";
	    	case 185:
	    		return "jungle_fence_gate";
	    	case 186:
	    		return "dark_oak_fence_gate";
	    	case 187:
	    		return "acacia_fence_gate";
	    	case 188:
	    		return "spruce_fence";
	    	case 189:
	    		return "birch_fence";
	    	case 190:
	    		return "jungle_fence";
	    	case 191:
	    		return "dark_oak_fence";
	    	case 192:
	    		return "acacia_fence";
	    	case 193:
	    		return "spruce_door";
	    	case 194:
	    		return "birch_door";
	    	case 195:
	    		return "jungle_door";
	    	case 196:
	    		return "acacia_door";
	    	case 197:
	    		return "dark_oak_door";
	    	case 198:
	    		return "end_rod";
	    	case 199:
	    		return "chorus_plant";
	    	case 200:
	    		return "chorus_flower";
	    	case 201:
	    		return "purpur_block";
	    	case 202:
	    		return "purpur_pillar";
	    	case 203:
	    		return "purpur_stairs";
	    	case 204:
	    		return "purpur_double_slab";
	    	case 205:
	    		return "purpur_slab";
	    	case 206:
	    		return "end_stone_bricks";
	    	case 207:
	    		return "beetroots";
	    	case 208:
	    		return "grass_path";
	    	case 209:
	    		return "end_gateway";
	    	case 210:
	    		return "repeating_command_block";
	    	case 211:
	    		return "chain_command_block";
	    	case 212:
	    		return "frosted_ice";
	    	case 213:
	    		return "magma_block";
	    	case 214:
	    		return "nether_wart_block";
	    	case 215:
	    		return "red_nether_bricks";
	    	case 216:
	    		return "bone_block";
	    	case 217:
	    		return "structure_void";
	    	case 218:
	    		return "observer";
	    	case 219:
	    		return "white_shulker_box";
	    	case 220:
	    		return "orange_shulker_box";
	    	case 221:
	    		return "magenta_shulker_box";
	    	case 222:
	    		return "light_blue_shulker_box";
	    	case 223:
	    		return "yellow_shulker_box";
	    	case 224:
	    		return "lime_shulker_box";
	    	case 225:
	    		return "pink_shulker_box";
	    	case 226:
	    		return "gray_shulker_box";
	    	case 227:
	    		return "light_gray_shulker_box";
	    	case 228:
	    		return "cyan_shulker_box";
	    	case 229:
	    		return "purple_shulker_box";
	    	case 230:
	    		return "blue_shulker_box";
	    	case 231:
	    		return "brown_shulker_box";
	    	case 232:
	    		return "green_shulker_box";
	    	case 233:
	    		return "red_shulker_box";
	    	case 234:
	    		return "black_shulker_box";
	    	case 235:
	    		return "white_glazed_terracotta";
	    	case 236:
	    		return "orange_glazed_terracotta";
	    	case 237:
	    		return "magenta_glazed_terracotta";
	    	case 238:
	    		return "light_blue_glazed_terracotta";
	    	case 239:
	    		return "yellow_glazed_terracotta";
	    	case 240:
	    		return "lime_glazed_terracotta";
	    	case 241:
	    		return "pink_glazed_terracotta";
	    	case 242:
	    		return "gray_glazed_terracotta";
	    	case 243:
	    		return "light_gray_glazed_terracotta";
	    	case 244:
	    		return "cyan_glazed_terracotta";
	    	case 245:
	    		return "purple_glazed_terracotta";
	    	case 246:
	    		return "blue_glazed_terracotta";
	    	case 247:
	    		return "brown_glazed_terracotta";
	    	case 248:
	    		return "green_glazed_terracotta";
	    	case 249:
	    		return "red_glazed_terracotta";
	    	case 250:
	    		return "black_glazed_terracotta";
	    	case 251:
	    		return "white_concrete";
	    	case 252:
	    		return "white_concrete_powder";
	    	case 255:
	    		return "structure_block";
	    	case 342:
	    		return "chest_minecart";
	    	case 343:
	    		return "furnace_minecart";
	    	case 397:
	    		return "skeleton_skull";
	    	case 404:
	    		return "comparator";
	    	case 405:
	    		return "nether_bricks";
	    	case 427:
	    		return "spruce_door";
	    	case 428:
	    		return "birch_door";
	    	case 429:
	    		return "jungle_door";
	    	case 430:
	    		return "acacia_door";
	    	case 431:
	    		return "dark_oak_door";
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
    		blockName = blockNameFromLegacyBlockId(blockId);
        	if(blockName == null)
        	{
        		return null;
        	}
    	} catch(NumberFormatException ex) { }
    	
    	try
    	{
	    	switch(blockName)
	    	{
				// Legacy blocks with block data that are now their own block
	    		case "banner":
	    		case "white_banner":
	    			switch(data)
	    			{
	    				case 0:
	    				default:
	    					return Blocks.BLACK_BANNER.getDefaultState();
	    				case 1:
	    					return Blocks.RED_BANNER.getDefaultState();
	    				case 2:
	    					return Blocks.GREEN_BANNER.getDefaultState();
	    				case 3:
	    					return Blocks.BROWN_BANNER.getDefaultState();
	    				case 4:
	    					return Blocks.BLUE_BANNER.getDefaultState();
	    				case 5:
	    					return Blocks.PURPLE_BANNER.getDefaultState();
	    				case 6:
	    					return Blocks.CYAN_BANNER.getDefaultState();
	    				case 7:
	    					return Blocks.LIGHT_GRAY_BANNER.getDefaultState();
	    				case 8:
	    					return Blocks.GRAY_BANNER.getDefaultState();
	    				case 9:
	    					return Blocks.PINK_BANNER.getDefaultState();
	    				case 10:
	    					return Blocks.LIME_BANNER.getDefaultState();
	    				case 11:
	    					return Blocks.YELLOW_BANNER.getDefaultState();
	    				case 12:
	    					return Blocks.LIGHT_BLUE_BANNER.getDefaultState();
	    				case 13:
	    					return Blocks.MAGENTA_BANNER.getDefaultState();
	    				case 14:
	    					return Blocks.ORANGE_BANNER.getDefaultState();
	    				case 15:
	    					return Blocks.WHITE_BANNER.getDefaultState();
	    			}		
				// TODO: How does facing for bed blocks in bo's work for 1.12.2, can only specify color via data?
	    		case "bed":
	    		case "white_bed":
	    			switch(data)
	    			{
	    				case 0:
	    				default:
	    					return Blocks.WHITE_BED.getDefaultState();
	    				case 1:
	    					return Blocks.ORANGE_BED.getDefaultState();
	    				case 2:
	    					return Blocks.MAGENTA_BED.getDefaultState();
	    				case 3:
	    					return Blocks.LIGHT_BLUE_BED.getDefaultState();
	    				case 4:
	    					return Blocks.YELLOW_BED.getDefaultState();
	    				case 5:
	    					return Blocks.LIME_BED.getDefaultState();
	    				case 6:
	    					return Blocks.PINK_BED.getDefaultState();
	    				case 7:
	    					return Blocks.GRAY_BED.getDefaultState();
	    				case 8:
	    					return Blocks.LIGHT_GRAY_BED.getDefaultState();
	    				case 9:
	    					return Blocks.CYAN_BED.getDefaultState();
	    				case 10:
	    					return Blocks.PURPLE_BED.getDefaultState();
	    				case 11:
	    					return Blocks.BLUE_BED.getDefaultState();
	    				case 12:
	    					return Blocks.BROWN_BED.getDefaultState();
	    				case 13:
	    					return Blocks.GREEN_BED.getDefaultState();
	    				case 14:
	    					return Blocks.RED_BED.getDefaultState();
	    				case 15:
	    					return Blocks.BLACK_BED.getDefaultState();
	    			}
	    		case "carpet":
	    		case "white_carpet":
	    			switch(data)
	    			{
	    				case 0:
	    				default:
	    					return Blocks.WHITE_CARPET.getDefaultState();
	    				case 1:
	    					return Blocks.ORANGE_CARPET.getDefaultState();
	    				case 2:
	    					return Blocks.MAGENTA_CARPET.getDefaultState();
	    				case 3:
	    					return Blocks.LIGHT_BLUE_CARPET.getDefaultState();
	    				case 4:
	    					return Blocks.YELLOW_CARPET.getDefaultState();
	    				case 5:
	    					return Blocks.LIME_CARPET.getDefaultState();
	    				case 6:
	    					return Blocks.PINK_CARPET.getDefaultState();
	    				case 7:
	    					return Blocks.GRAY_CARPET.getDefaultState();
	    				case 8:
	    					return Blocks.LIGHT_GRAY_CARPET.getDefaultState();
	    				case 9:
	    					return Blocks.CYAN_CARPET.getDefaultState();
	    				case 10:
	    					return Blocks.PURPLE_CARPET.getDefaultState();
	    				case 11:
	    					return Blocks.BLUE_CARPET.getDefaultState();
	    				case 12:
	    					return Blocks.BROWN_CARPET.getDefaultState();
	    				case 13:
	    					return Blocks.GREEN_CARPET.getDefaultState();
	    				case 14:
	    					return Blocks.RED_CARPET.getDefaultState();
	    				case 15:
	    					return Blocks.BLACK_CARPET.getDefaultState();
	    			}
	    		case "cobblestone_wall":
	    		case "cobble_wall":
	    			switch(data)
	    			{
	    				case 0:
	    				default:
	    					return Blocks.COBBLESTONE_WALL.getDefaultState();
	    				case 1:
	    					return Blocks.MOSSY_COBBLESTONE_WALL.getDefaultState();
	    			}
	    		case "concrete":
	    		case "white_concrete":
	    			switch(data)
	    			{
	    				case 0:
	    				default:
	    					return Blocks.WHITE_CONCRETE.getDefaultState();
	    				case 1:
	    					return Blocks.ORANGE_CONCRETE.getDefaultState();
	    				case 2:
	    					return Blocks.MAGENTA_CONCRETE.getDefaultState();
	    				case 3:
	    					return Blocks.LIGHT_BLUE_CONCRETE.getDefaultState();
	    				case 4:
	    					return Blocks.YELLOW_CONCRETE.getDefaultState();
	    				case 5:
	    					return Blocks.LIME_CONCRETE.getDefaultState();
	    				case 6:
	    					return Blocks.PINK_CONCRETE.getDefaultState();
	    				case 7:
	    					return Blocks.GRAY_CONCRETE.getDefaultState();
	    				case 8:
	    					return Blocks.LIGHT_GRAY_CONCRETE.getDefaultState();
	    				case 9:
	    					return Blocks.CYAN_CONCRETE.getDefaultState();
	    				case 10:
	    					return Blocks.PURPLE_CONCRETE.getDefaultState();
	    				case 11:
	    					return Blocks.BLUE_CONCRETE.getDefaultState();
	    				case 12:
	    					return Blocks.BROWN_CONCRETE.getDefaultState();
	    				case 13:
	    					return Blocks.GREEN_CONCRETE.getDefaultState();
	    				case 14:
	    					return Blocks.RED_CONCRETE.getDefaultState();
	    				case 15:
	    					return Blocks.BLACK_CONCRETE.getDefaultState();
	    			}
	    		case "concrete_powder":
	    		case "white_concrete_powder":
	    			switch(data)
	    			{
	    				case 0:
	    				default:    					
	    					return Blocks.WHITE_CONCRETE_POWDER.getDefaultState();
	    				case 1:
	    					return Blocks.ORANGE_CONCRETE_POWDER.getDefaultState();
	    				case 2:
	    					return Blocks.MAGENTA_CONCRETE_POWDER.getDefaultState();
	    				case 3:
	    					return Blocks.LIGHT_BLUE_CONCRETE_POWDER.getDefaultState();
	    				case 4:
	    					return Blocks.YELLOW_CONCRETE_POWDER.getDefaultState();
	    				case 5:
	    					return Blocks.LIME_CONCRETE_POWDER.getDefaultState();
	    				case 6:
	    					return Blocks.PINK_CONCRETE_POWDER.getDefaultState();
	    				case 7:
	    					return Blocks.GRAY_CONCRETE_POWDER.getDefaultState();
	    				case 8:
	    					return Blocks.LIGHT_GRAY_CONCRETE_POWDER.getDefaultState();
	    				case 9:
	    					return Blocks.CYAN_CONCRETE_POWDER.getDefaultState();
	    				case 10:
	    					return Blocks.PURPLE_CONCRETE_POWDER.getDefaultState();
	    				case 11:
	    					return Blocks.BLUE_CONCRETE_POWDER.getDefaultState();
	    				case 12:
	    					return Blocks.BROWN_CONCRETE_POWDER.getDefaultState();
	    				case 13:
	    					return Blocks.GREEN_CONCRETE_POWDER.getDefaultState();
	    				case 14:
	    					return Blocks.RED_CONCRETE_POWDER.getDefaultState();
	    				case 15:
	    					return Blocks.BLACK_CONCRETE_POWDER.getDefaultState();
	    			}
	    		case "dirt":
	    			switch(data)
	    			{
	    				case 0:
	    				default:
	    					return Blocks.DIRT.getDefaultState();
	    				case 1:
	    					return Blocks.COARSE_DIRT.getDefaultState();
	    				case 2:
	    					return Blocks.PODZOL.getDefaultState();    					
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
	    					return Blocks.SUNFLOWER.getDefaultState();		
	    				case 1:
	    					return Blocks.LILAC.getDefaultState();
	    				case 2:
	    					return Blocks.TALL_GRASS.getDefaultState();
	    				case 3:
	    					return Blocks.LARGE_FERN.getDefaultState();	
	    				case 4:
	    					return Blocks.ROSE_BUSH.getDefaultState();
	    				case 5:
	    					return Blocks.PEONY.getDefaultState();
	    			}
	    		case "double_stone_slab":
	    		case "smooth_stone":
	    			switch(data)
	    			{
						case 0:
						default:
							return Blocks.STONE_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.DOUBLE);
						case 1:
							return Blocks.SANDSTONE_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.DOUBLE);
						//case 2:
						case 3:
							return Blocks.COBBLESTONE_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.DOUBLE);
						case 4:
							return Blocks.BRICK_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.DOUBLE);
						case 5:
							return Blocks.STONE_BRICK_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.DOUBLE);
						case 6:
							return Blocks.NETHER_BRICK_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.DOUBLE);
						case 7:
							return Blocks.QUARTZ_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.DOUBLE);
	    			}
	    		// TODO: Did this even exist for 1.12.2?
	    		case "double_wooden_slab":
	    		case "wood_double_step":
	    			switch(data)
	    			{
						case 0:
						default:
							return Blocks.OAK_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.DOUBLE);
						case 1:
							return Blocks.SPRUCE_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.DOUBLE);
						case 2:
							return Blocks.BIRCH_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.DOUBLE);
						case 3:
							return Blocks.JUNGLE_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.DOUBLE);
						case 4:
							return Blocks.ACACIA_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.DOUBLE);
						case 5:
							return Blocks.DARK_OAK_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.DOUBLE);
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
	    					return Blocks.OAK_LEAVES.getDefaultState().with(LeavesBlock.DISTANCE, 1);
	    				case 1:
	    				case 5:
	    				case 9:
	    				case 13:
	    					return Blocks.SPRUCE_LEAVES.getDefaultState().with(LeavesBlock.DISTANCE, 1);
	    				case 2:
	    				case 6:
	    				case 10:
	    				case 14:
	    					return Blocks.BIRCH_LEAVES.getDefaultState().with(LeavesBlock.DISTANCE, 1);
	    				case 3:
	    				case 7:
	    				case 11:
	    				case 15:
	    					return Blocks.JUNGLE_LEAVES.getDefaultState().with(LeavesBlock.DISTANCE, 1);
	    			}
	    		case "leaves2":
	    		case "acacia_leaves":
	    			switch(data)
	    			{
	    				case 0:
	    				case 4:
	    				case 8:
	    				case 12:
	    				default:
	    					return Blocks.ACACIA_LEAVES.getDefaultState().with(LeavesBlock.DISTANCE, 1);
	    				case 1:
	    				case 5:
	    				case 9:
	    				case 13:
	    					return Blocks.DARK_OAK_LEAVES.getDefaultState().with(LeavesBlock.DISTANCE, 1);
	    			}
	    		case "monster_egg":
	    		case "monster_eggs":
	    		case "infested_stone":
	    			switch(data)
	    			{
	    				case 0:
	    				default:
	    					return Blocks.INFESTED_STONE.getDefaultState();
	    				case 1:
	    					return Blocks.INFESTED_COBBLESTONE.getDefaultState();
	    				case 2:
	    					return Blocks.INFESTED_STONE_BRICKS.getDefaultState();
	    				case 3:
	    					return Blocks.INFESTED_MOSSY_STONE_BRICKS.getDefaultState();
	    				case 4:
	    					return Blocks.INFESTED_CRACKED_STONE_BRICKS.getDefaultState();
	    				case 5:
	    					return Blocks.INFESTED_CHISELED_STONE_BRICKS.getDefaultState();
	    			}
	    		case "planks":
	    		case "oak_planks":
	    			switch(data)
	    			{
	    				case 0:
	    				default:
	    					return Blocks.OAK_PLANKS.getDefaultState();
	    				case 1:
	    					return Blocks.SPRUCE_PLANKS.getDefaultState();
	    				case 2:
	    					return Blocks.BIRCH_PLANKS.getDefaultState();
	    				case 3:
	    					return Blocks.JUNGLE_PLANKS.getDefaultState();
	    				case 4:
	    					return Blocks.ACACIA_PLANKS.getDefaultState();
	    				case 5:
	    					return Blocks.DARK_OAK_PLANKS.getDefaultState();
	    			}
	    		case "prismarine":
	    			switch(data)
	    			{
	    				// TODO: Docs contradict each other about whether 2 or 3 is bricks/dark, test this.
	    				case 0:
	    				default:
	    					return Blocks.PRISMARINE.getDefaultState();
	    				case 1:
	    					return Blocks.PRISMARINE_BRICKS.getDefaultState();
	    				case 2:
	    					return Blocks.DARK_PRISMARINE.getDefaultState();
	    			}
	    		case "purpur_slab":
	    			return Blocks.PURPUR_SLAB.getDefaultState()
						.with(SlabBlock.TYPE, data == 2 ? SlabType.BOTTOM : data == 10 ? SlabType.TOP : SlabType.BOTTOM);
	    		case "red_flower":
	    			switch(data)
	    			{
	    				case 0:
	    				default:
	    					return Blocks.POPPY.getDefaultState();
	    				case 1:
	    					return Blocks.BLUE_ORCHID.getDefaultState();
	    				case 2:
	    					return Blocks.ALLIUM.getDefaultState();
	    				case 3:
	    					return Blocks.AZURE_BLUET.getDefaultState();
	    				case 4:
	    					return Blocks.RED_TULIP.getDefaultState();
	    				case 5:
	    					return Blocks.ORANGE_TULIP.getDefaultState();
	    				case 6:
	    					return Blocks.WHITE_TULIP.getDefaultState();
	    				case 7:    					
	    					return Blocks.PINK_TULIP.getDefaultState();
	    				case 8:
	    					return Blocks.OXEYE_DAISY.getDefaultState();
	    			}
	    		case "red_sandstone":
	    			switch(data)
	    			{
	    				case 0:
	    				default:
	    					return Blocks.RED_SANDSTONE.getDefaultState();
	    				case 1:
	    					return Blocks.CHISELED_RED_SANDSTONE.getDefaultState();
	    				case 2:
	    					return Blocks.SMOOTH_RED_SANDSTONE.getDefaultState();
	    			}
	    		case "red_sandstone_slab":
	    		case "stone_slab2": 
	    			switch(data)
	    			{
	    				case 0:
	    				default:
	    					return Blocks.RED_SANDSTONE_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.BOTTOM);
	    				case 8:
	    					return Blocks.CHISELED_RED_SANDSTONE.getDefaultState().with(SlabBlock.TYPE, SlabType.TOP);
	    			}
	    		case "sand":
	    			switch(data)
	    			{
	    				case 0:
	    				default:
	    					return Blocks.SAND.getDefaultState();
	    				case 1:
	    					return Blocks.RED_SAND.getDefaultState();
	    			}
	    		case "sandstone":
	    			switch(data)
	    			{
	    				case 0:
	    				default:
	    					return Blocks.SANDSTONE.getDefaultState();
	    				case 1:
	    					return Blocks.CHISELED_SANDSTONE.getDefaultState();
	    				case 2:
	    					return Blocks.SMOOTH_SANDSTONE.getDefaultState();
	    			}
	    		case "sapling":
	    		case "oak_sapling":
	    			switch(data)
	    			{
	    				case 0:
	    				default:
	    					return Blocks.OAK_SAPLING.getDefaultState();
	    				case 1:
	    					return Blocks.SPRUCE_SAPLING.getDefaultState();
	    				case 2:
	    					return Blocks.BIRCH_SAPLING.getDefaultState();
	    				case 3:
	    					return Blocks.JUNGLE_SAPLING.getDefaultState();
	    				case 4:
	    					return Blocks.ACACIA_SAPLING.getDefaultState();
	    				case 5:
	    					return Blocks.DARK_OAK_SAPLING.getDefaultState();
	    			}
	    		case "skull":
	    		case "skeleton_skull":
	    			switch(data)
	    			{
	    				case 0:
	    				default:
	    					return Blocks.SKELETON_SKULL.getDefaultState();
	    				case 1:
	    					return Blocks.WITHER_SKELETON_SKULL.getDefaultState();
	    				case 2:
	    					return Blocks.ZOMBIE_HEAD.getDefaultState();
	    				case 3:
	    					return Blocks.PLAYER_HEAD.getDefaultState();
	    				case 4:
	    					return Blocks.CREEPER_HEAD.getDefaultState();
	    				case 5:
	    					return Blocks.DRAGON_HEAD.getDefaultState();
	    			}
	    		case "sponge":
	    			switch(data)
	    			{
	    				case 0:
	    				default:
	    					return Blocks.SPONGE.getDefaultState();
	    				case 1:
	    					return Blocks.WET_SPONGE.getDefaultState();
	    			}
	    		case "stained_glass":
	    		case "white_stained_glass":
	    			switch(data)
	    			{
	    				case 0:
	    				default:
	    					return Blocks.WHITE_STAINED_GLASS.getDefaultState();
	    				case 1:
	    					return Blocks.ORANGE_STAINED_GLASS.getDefaultState();
	    				case 2:
	    					return Blocks.MAGENTA_STAINED_GLASS.getDefaultState();
	    				case 3:
	    					return Blocks.LIGHT_BLUE_STAINED_GLASS.getDefaultState();
	    				case 4:
	    					return Blocks.YELLOW_STAINED_GLASS.getDefaultState();
	    				case 5:
	    					return Blocks.LIME_STAINED_GLASS.getDefaultState();
	    				case 6:
	    					return Blocks.PINK_STAINED_GLASS.getDefaultState();
	    				case 7:
	    					return Blocks.GRAY_STAINED_GLASS.getDefaultState();
	    				case 8:
	    					return Blocks.LIGHT_GRAY_STAINED_GLASS.getDefaultState();
	    				case 9:
	    					return Blocks.CYAN_STAINED_GLASS.getDefaultState();
	    				case 10:
	    					return Blocks.PURPLE_STAINED_GLASS.getDefaultState();
	    				case 11:
	    					return Blocks.BLUE_STAINED_GLASS.getDefaultState();
	    				case 12:
	    					return Blocks.BROWN_STAINED_GLASS.getDefaultState();
	    				case 13:
	    					return Blocks.GREEN_STAINED_GLASS.getDefaultState();
	    				case 14:
	    					return Blocks.RED_STAINED_GLASS.getDefaultState();
	    				case 15:
	    					return Blocks.BLACK_STAINED_GLASS.getDefaultState();
	    			}
	    		case "stained_glass_pane":
	    		case "white_stained_glass_pane":
	    		case "thing_glass":
	    			switch(data)
	    			{
	    				case 0:
	    				default:
	    					return Blocks.WHITE_STAINED_GLASS_PANE.getDefaultState();
	    				case 1:
	    					return Blocks.ORANGE_STAINED_GLASS_PANE.getDefaultState();
	    				case 2:
	    					return Blocks.MAGENTA_STAINED_GLASS_PANE.getDefaultState();
	    				case 3:
	    					return Blocks.LIGHT_BLUE_STAINED_GLASS_PANE.getDefaultState();
	    				case 4:
	    					return Blocks.YELLOW_STAINED_GLASS_PANE.getDefaultState();
	    				case 5:
	    					return Blocks.LIME_STAINED_GLASS_PANE.getDefaultState();
	    				case 6:
	    					return Blocks.PINK_STAINED_GLASS_PANE.getDefaultState();
	    				case 7:
	    					return Blocks.GRAY_STAINED_GLASS_PANE.getDefaultState();
	    				case 8:
	    					return Blocks.LIGHT_GRAY_STAINED_GLASS_PANE.getDefaultState();
	    				case 9:
	    					return Blocks.CYAN_STAINED_GLASS_PANE.getDefaultState();
	    				case 10:
	    					return Blocks.PURPLE_STAINED_GLASS_PANE.getDefaultState();
	    				case 11:
	    					return Blocks.BLUE_STAINED_GLASS_PANE.getDefaultState();
	    				case 12:
	    					return Blocks.BROWN_STAINED_GLASS_PANE.getDefaultState();
	    				case 13:
	    					return Blocks.GREEN_STAINED_GLASS_PANE.getDefaultState();
	    				case 14:
	    					return Blocks.RED_STAINED_GLASS_PANE.getDefaultState();
	    				case 15:
	    					return Blocks.BLACK_STAINED_GLASS_PANE.getDefaultState();
	    			}
	    		case "stained_hardened_clay":
	    		case "stained_clay":
	    		case "hard_clay":
	    		case "white_terracotta":
	    			switch(data)
	    			{
	    				case 0:
	    				default:
	    					return Blocks.WHITE_TERRACOTTA.getDefaultState();
	    				case 1:
	    					return Blocks.ORANGE_TERRACOTTA.getDefaultState();
	    				case 2:
	    					return Blocks.MAGENTA_TERRACOTTA.getDefaultState();
	    				case 3:
	    					return Blocks.LIGHT_BLUE_TERRACOTTA.getDefaultState();
	    				case 4:
	    					return Blocks.YELLOW_TERRACOTTA.getDefaultState();
	    				case 5:
	    					return Blocks.LIME_TERRACOTTA.getDefaultState();
	    				case 6:
	    					return Blocks.PINK_TERRACOTTA.getDefaultState();
	    				case 7:
	    					return Blocks.GRAY_TERRACOTTA.getDefaultState();
	    				case 8:
	    					return Blocks.LIGHT_GRAY_TERRACOTTA.getDefaultState();
	    				case 9:
	    					return Blocks.CYAN_TERRACOTTA.getDefaultState();
	    				case 10:
	    					return Blocks.PURPLE_TERRACOTTA.getDefaultState();
	    				case 11:
	    					return Blocks.BLUE_TERRACOTTA.getDefaultState();
	    				case 12:
	    					return Blocks.BROWN_TERRACOTTA.getDefaultState();
	    				case 13:
	    					return Blocks.GREEN_TERRACOTTA.getDefaultState();
	    				case 14:
	    					return Blocks.RED_TERRACOTTA.getDefaultState();
	    				case 15:
	    					return Blocks.BLACK_TERRACOTTA.getDefaultState();
	    			}
	    		case "stone":
	    			switch(data)
	    			{
	    				case 0:
	    				default:
	    					return Blocks.STONE.getDefaultState();
	    				case 1:
	    					return Blocks.GRANITE.getDefaultState();
	    				case 2:
	    					return Blocks.POLISHED_GRANITE.getDefaultState();
	    				case 3:
	    					return Blocks.DIORITE.getDefaultState();
	    				case 4:
	    					return Blocks.POLISHED_DIORITE.getDefaultState();
	    				case 5:
	    					return Blocks.ANDESITE.getDefaultState();
	    				case 6:
	    					return Blocks.POLISHED_ANDESITE.getDefaultState();
	    			}
	    		case "stone_slab":
				case "step":
	    			switch(data)
	    			{
	    				case 0:
	    				default:
	    					return Blocks.STONE_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.BOTTOM);
	    				case 1:
	    					return Blocks.SANDSTONE_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.BOTTOM);
	    				//case 2:
	    				case 3:
	    					return Blocks.COBBLESTONE_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.BOTTOM);
	    				case 4:
	    					return Blocks.BRICK_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.BOTTOM);
	    				case 5:
	    					return Blocks.STONE_BRICK_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.BOTTOM);
	    				case 6:
	    					return Blocks.NETHER_BRICK_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.BOTTOM);
	    				case 7:
	    					return Blocks.QUARTZ_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.BOTTOM);
	    				case 8:
	    					return Blocks.STONE_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.TOP);
	    				case 9:
	    					return Blocks.SANDSTONE_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.TOP);
	    				//case 10:
	    				case 11:
	    					return Blocks.COBBLESTONE_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.TOP);
	    				case 12:
	    					return Blocks.BRICK_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.TOP);
	    				case 13:
	    					return Blocks.STONE_BRICK_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.TOP);
	    				case 14:
	    					return Blocks.NETHER_BRICK_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.TOP);
	    				case 15:
	    					return Blocks.QUARTZ_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.TOP);
	    			}
	    		case "stonebrick":
	    		case "stone_bricks":
	    		case "smooth_brick":
	    			switch(data)
	    			{
	    				case 0:
	    				default:
	    					return Blocks.STONE_BRICKS.getDefaultState();
	    				case 1:
	    					return Blocks.MOSSY_STONE_BRICKS.getDefaultState();
	    				case 2:
	    					return Blocks.CRACKED_STONE_BRICKS.getDefaultState();
	    				case 3:
	    					return Blocks.CHISELED_STONE_BRICKS.getDefaultState();
	    			}
	    		case "tallgrass":
	    		case "long_grass":
	    			switch(data)
	    			{
	    				case 0:
	    				default:
	    					return Blocks.GRASS.getDefaultState();
	    				case 1:
	    					return Blocks.FERN.getDefaultState();
	    			}
	    		case "wooden_slab":
	    		case "wood_step":
	    		case "oak_slab":
	    			switch(data)
	    			{
	    				case 0:
	    				default:
	    					return Blocks.OAK_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.BOTTOM);
	    				case 1:
	    					return Blocks.SPRUCE_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.BOTTOM);
	    				case 2:
	    					return Blocks.BIRCH_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.BOTTOM);
	    				case 3:
	    					return Blocks.JUNGLE_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.BOTTOM);
	    				case 4:
	    					return Blocks.ACACIA_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.BOTTOM);
	    				case 5:
	    					return Blocks.DARK_OAK_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.BOTTOM);
	    				case 8:
	    					return Blocks.OAK_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.TOP);
	    				case 9:
	    					return Blocks.SPRUCE_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.TOP);
	    				case 10:
	    					return Blocks.BIRCH_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.TOP);
	    				case 11:
	    					return Blocks.JUNGLE_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.TOP);
	    				case 12:
	    					return Blocks.ACACIA_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.TOP);
	    				case 13:
	    					return Blocks.DARK_OAK_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.TOP);    					
	    			}
	    		case "wool":
	    		case "white_wool":
	    			switch(data)
	    			{
	    				case 0:
	    				default:
	    	    			return Blocks.WHITE_WOOL.getDefaultState();
	    				case 1:
	    					return Blocks.ORANGE_WOOL.getDefaultState();
	    				case 2:
	    					return Blocks.MAGENTA_WOOL.getDefaultState();
	    				case 3:
	    					return Blocks.LIGHT_BLUE_WOOL.getDefaultState();
	    				case 4:
	    					return Blocks.YELLOW_WOOL.getDefaultState();
	    				case 5:
	    					return Blocks.LIME_WOOL.getDefaultState();
	    				case 6:
	    					return Blocks.PINK_WOOL.getDefaultState();
	    				case 7:
	    					return Blocks.GRAY_WOOL.getDefaultState();
	    				case 8:
	    					return Blocks.LIGHT_GRAY_WOOL.getDefaultState();
	    				case 9:
	    					return Blocks.CYAN_WOOL.getDefaultState();
	    				case 10:
	    					return Blocks.PURPLE_WOOL.getDefaultState();
	    				case 11:
	    					return Blocks.BLUE_WOOL.getDefaultState();
	    				case 12:
	    					return Blocks.BROWN_WOOL.getDefaultState();
	    				case 13:
	    					return Blocks.GREEN_WOOL.getDefaultState();
	    				case 14:
	    					return Blocks.RED_WOOL.getDefaultState();
	    				case 15:
	    					return Blocks.BLACK_WOOL.getDefaultState();
	    			}
	
	    		// Blocks with data
	    		case "fire":
	    			return Blocks.FIRE.getDefaultState().with(FireBlock.AGE, data);
	    		case "cake":
	    		case "cake_block":
	    			return Blocks.CAKE.getDefaultState().with(CakeBlock.BITES, data); 
	    		case "stone_pressure_plate":
	    		case "stone_plate":
	    			return Blocks.STONE_PRESSURE_PLATE.getDefaultState().with(PressurePlateBlock.POWERED, getBit(data, 0) == 1);
	    		case "wooden_pressure_plate":
	    		case "wood_plate":
	    			return Blocks.OAK_PRESSURE_PLATE.getDefaultState().with(PressurePlateBlock.POWERED, getBit(data, 0) == 1);
	    		case "light_weighted_pressure_plate":
	    			return Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE.getDefaultState().with(WeightedPressurePlateBlock.POWER, data);
	    		case "heavy_weighted_pressure_plate":
	    			return Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE.getDefaultState().with(WeightedPressurePlateBlock.POWER, data);
	    		case "snow_layer":
	    			return Blocks.SNOW.getDefaultState().with(SnowBlock.LAYERS, data);
	    		case "cactus":
	    			return Blocks.CACTUS.getDefaultState().with(CactusBlock.AGE, data);
	    		case "reeds":
	    			return Blocks.SUGAR_CANE.getDefaultState().with(SugarCaneBlock.AGE, data);
	    		case "jukebox":
	    			return Blocks.JUKEBOX.getDefaultState().with(JukeboxBlock.HAS_RECORD, data == 1);
	    		case "wheat":
	    		case "crops":
	    			return Blocks.WHEAT.getDefaultState().with(CropsBlock.AGE, data);
	    		case "carrot":
	    			return Blocks.CARROTS.getDefaultState().with(CarrotBlock.AGE, data);
	    		case "potato":
	    			return Blocks.POTATOES.getDefaultState().with(PotatoBlock.AGE, data);
	    		case "beetroot":
	    			return Blocks.BEETROOTS.getDefaultState().with(BeetrootBlock.BEETROOT_AGE, data);
	    		case "farmland":
	    		case "soil":
	    			return Blocks.FARMLAND.getDefaultState().with(FarmlandBlock.MOISTURE, data);
	    		case "anvil":
	    			return getAnvilWithData(0, data);
				case "log":
				case "wood":
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
					return Blocks.RAIL.getDefaultState().with(RailBlock.SHAPE, getRailShape(data));
				case "powered_rail":
				case "golden_rail":
					return getRailsWithData(0, data);
				case "detector_rail":
					return getRailsWithData(1, data);
				case "activator_rail":
					return getRailsWithData(2, data);				
				case "hay_block":
					return Blocks.HAY_BLOCK.getDefaultState().with(HayBlock.AXIS, getAxisXYZ(data));
				case "bone_block":
					return Blocks.BONE_BLOCK.getDefaultState().with(RotatedPillarBlock.AXIS, getAxisXYZ(data));			
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
				// TODO: Stone stairs didn't exist in 1.12? OTG had a smooth_stairs DefaultMaterial tho :/
				case "smooth_stairs":
					return getStairsWithData(6, data);
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
				case "sign_post":
				case "sign": // TODO: This will also pick up wall signs
					return getSignPostWithData(data);
				case "standing_banner":
					return Blocks.BLACK_BANNER.getDefaultState().with(BannerBlock.ROTATION, data);
				case "wall_banner":
					return Blocks.WHITE_WALL_BANNER.getDefaultState().with(WallBannerBlock.HORIZONTAL_FACING, getFacingNorthSouthWestEast(data));
				case "end_rod":
					return Blocks.END_ROD.getDefaultState().with(EndRodBlock.FACING, getFacingDownUpNorthSouthWestEast(data));
				case "daylight_detector":
					return Blocks.DAYLIGHT_DETECTOR.getDefaultState().with(DaylightDetectorBlock.POWER, data);
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
					return Blocks.PUMPKIN_STEM.getDefaultState().with(StemBlock.AGE, data); 
				case "melon_stem":
					// TODO: Hopefully this auto-updates to ATTACHED_MELON_STEM when placed next to a melon block..
					return Blocks.MELON_STEM.getDefaultState().with(StemBlock.AGE, data);
				case "carved_pumpkin":
					return getJackOLanternOrPumpkinWithData(0, data);
				case "jack_o_lantern":
				case "lit_pumpkin":
					return getJackOLanternOrPumpkinWithData(1, data);
				case "diode_block_off":
				case "repeater":
					return getRepeaterWithData(0, data);
				case "diode_block_on":
					return getRepeaterWithData(1, data);
				case "redstone":
				case "redstone_wire":
					return Blocks.REDSTONE_WIRE.getDefaultState().with(RedstoneWireBlock.POWER, data);
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
					return Blocks.PURPUR_PILLAR.getDefaultState().with(RotatedPillarBlock.AXIS, getAxisXYZ(data));
				case "nether_wart":
					return Blocks.NETHER_WART.getDefaultState().with(NetherWartBlock.AGE, data);
				case "brewing_stand":
					return Blocks.BREWING_STAND.getDefaultState()
						.with(BrewingStandBlock.HAS_BOTTLE[0], Boolean.valueOf(getBit(data, 0) == 1))
						.with(BrewingStandBlock.HAS_BOTTLE[1], Boolean.valueOf(getBit(data, 1) == 1))
						.with(BrewingStandBlock.HAS_BOTTLE[2], Boolean.valueOf(getBit(data, 2) == 1))
					;
				case "cauldron":
					return Blocks.CAULDRON.getDefaultState().with(CauldronBlock.LEVEL, data);
				case "portal":
					return Blocks.NETHER_PORTAL.getDefaultState().with(NetherPortalBlock.AXIS, getAxisXZ(data));
				case "end_portal_frame":
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
    		OTG.log(LogMarker.INFO, "Could not parse block with data, illegal data: " + blockName + ":" + data + ". Exception: " + ex.getMessage());
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
    				return Blocks.ANVIL.getDefaultState().with(AnvilBlock.FACING, orientation);
	    		}
	    		else if(getBit(data, 2) == 1)
	    		{
    				return Blocks.CHIPPED_ANVIL.getDefaultState().with(AnvilBlock.FACING, orientation);
	    		}
	    		else if(getBit(data, 3) == 1)
	    		{
    				return Blocks.DAMAGED_ANVIL.getDefaultState().with(AnvilBlock.FACING, orientation);
	    		}
			case 1:
				return Blocks.CHIPPED_ANVIL.getDefaultState().with(AnvilBlock.FACING, orientation);
			case 2:
				return Blocks.DAMAGED_ANVIL.getDefaultState().with(AnvilBlock.FACING, orientation);
			default:
				return null;
    	}
	}

	private static BlockState getLogWithData(int data)
    {
		int material = getBits(data, 0, 2);
		int orientation = getBits(data, 2, 2);
		Direction.Axis axisDirection = orientation == 0 ? Direction.Axis.Y : orientation == 1 ? Direction.Axis.X : orientation == 2 ? Direction.Axis.Z : Direction.Axis.Y;
		switch(material)
		{
			case 0:
				return Blocks.OAK_LOG.getDefaultState().with(RotatedPillarBlock.AXIS, axisDirection);
			case 1:
				return Blocks.SPRUCE_LOG.getDefaultState().with(RotatedPillarBlock.AXIS, axisDirection);
			case 2:
				return Blocks.BIRCH_LOG.getDefaultState().with(RotatedPillarBlock.AXIS, axisDirection);
			case 3:
				return Blocks.JUNGLE_LOG.getDefaultState().with(RotatedPillarBlock.AXIS, axisDirection);
			default:
				return Blocks.OAK_LOG.getDefaultState();
		}
	}
    
    private static BlockState getLog2WithData(int data)
    {
		int material = getBits(data, 0, 2);
		int orientation = getBits(data, 2, 2);
		Direction.Axis axisDirection = orientation == 0 ? Direction.Axis.Y : orientation == 1 ? Direction.Axis.X : orientation == 2 ? Direction.Axis.Z : Direction.Axis.Y;		
		switch(material)
		{
			case 0:
				return Blocks.ACACIA_LOG.getDefaultState().with(RotatedPillarBlock.AXIS, axisDirection);
			case 1:
				return Blocks.DARK_OAK_LOG.getDefaultState().with(RotatedPillarBlock.AXIS, axisDirection);
			default:
				return Blocks.ACACIA_LOG.getDefaultState();
		}
	}
	
    private static BlockState getQuartzBlockWithData(int data)
    {
		switch(data)
		{
			case 0:
			default:
				return Blocks.QUARTZ_BLOCK.getDefaultState();
			case 1:
				return Blocks.CHISELED_QUARTZ_BLOCK.getDefaultState();
			case 2:
				return Blocks.QUARTZ_PILLAR.getDefaultState().with(RotatedPillarBlock.AXIS, Direction.Axis.Y);
			case 3:
				return Blocks.QUARTZ_PILLAR.getDefaultState().with(RotatedPillarBlock.AXIS, Direction.Axis.X);
			case 4:
				return Blocks.QUARTZ_PILLAR.getDefaultState().with(RotatedPillarBlock.AXIS, Direction.Axis.Z);
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
						return Blocks.TORCH.getDefaultState();
					case 1:
						return Blocks.WALL_TORCH.getDefaultState().with(WallTorchBlock.HORIZONTAL_FACING, Direction.EAST);
					case 2:
						return Blocks.WALL_TORCH.getDefaultState().with(WallTorchBlock.HORIZONTAL_FACING, Direction.WEST);
					case 3:
						return Blocks.WALL_TORCH.getDefaultState().with(WallTorchBlock.HORIZONTAL_FACING, Direction.SOUTH);
					case 4:
						return Blocks.WALL_TORCH.getDefaultState().with(WallTorchBlock.HORIZONTAL_FACING, Direction.NORTH);
				}
			case 1:
				switch(data)
				{
					case 0:
					case 5:
					default:
						return Blocks.TORCH.getDefaultState();
					case 1:
						return Blocks.REDSTONE_WALL_TORCH.getDefaultState().with(RedstoneWallTorchBlock.REDSTONE_TORCH_LIT, false).with(RedstoneWallTorchBlock.FACING, Direction.EAST);						
					case 2:
						return Blocks.REDSTONE_WALL_TORCH.getDefaultState().with(RedstoneWallTorchBlock.REDSTONE_TORCH_LIT, false).with(RedstoneWallTorchBlock.FACING, Direction.WEST);						
					case 3:
						return Blocks.REDSTONE_WALL_TORCH.getDefaultState().with(RedstoneWallTorchBlock.REDSTONE_TORCH_LIT, false).with(RedstoneWallTorchBlock.FACING, Direction.SOUTH);						
					case 4:
						return Blocks.REDSTONE_WALL_TORCH.getDefaultState().with(RedstoneWallTorchBlock.REDSTONE_TORCH_LIT, false).with(RedstoneWallTorchBlock.FACING, Direction.NORTH);						
				}
			case 2:
				switch(data)
				{
					case 0:
					case 5:
					default:
						return Blocks.TORCH.getDefaultState();
					case 1:
						return Blocks.REDSTONE_WALL_TORCH.getDefaultState().with(RedstoneWallTorchBlock.REDSTONE_TORCH_LIT, true).with(RedstoneWallTorchBlock.FACING, Direction.EAST);						
					case 2:
						return Blocks.REDSTONE_WALL_TORCH.getDefaultState().with(RedstoneWallTorchBlock.REDSTONE_TORCH_LIT, true).with(RedstoneWallTorchBlock.FACING, Direction.WEST);						
					case 3:
						return Blocks.REDSTONE_WALL_TORCH.getDefaultState().with(RedstoneWallTorchBlock.REDSTONE_TORCH_LIT, true).with(RedstoneWallTorchBlock.FACING, Direction.SOUTH);						
					case 4:
						return Blocks.REDSTONE_WALL_TORCH.getDefaultState().with(RedstoneWallTorchBlock.REDSTONE_TORCH_LIT, true).with(RedstoneWallTorchBlock.FACING, Direction.NORTH);						
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
				return Blocks.POWERED_RAIL.getDefaultState().with(PoweredRailBlock.SHAPE, getRailShape(shape)).with(PoweredRailBlock.POWERED, active == 1);
			case 1:
				return Blocks.DETECTOR_RAIL.getDefaultState().with(DetectorRailBlock.SHAPE, getRailShape(shape)).with(DetectorRailBlock.POWERED, active == 1);
			case 2:
				return Blocks.ACTIVATOR_RAIL.getDefaultState().with(PoweredRailBlock.SHAPE, getRailShape(shape)).with(PoweredRailBlock.POWERED, active == 1);
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
				output = Blocks.OAK_STAIRS.getDefaultState();
				break;
			case 1:				
				output = Blocks.BIRCH_STAIRS.getDefaultState();
				break;
			case 2:			
				output = Blocks.SPRUCE_STAIRS.getDefaultState();
				break;
			case 3:			
				output = Blocks.JUNGLE_STAIRS.getDefaultState();
				break;
			case 4:			
				output = Blocks.COBBLESTONE_STAIRS.getDefaultState();
				break;
			case 5:
				output = Blocks.BRICK_STAIRS.getDefaultState();
				break;
			case 6:
				output = Blocks.STONE_STAIRS.getDefaultState();
				break;
			case 7:			
				output = Blocks.NETHER_BRICK_STAIRS.getDefaultState();
				break;
			case 8:			
				output = Blocks.SANDSTONE_STAIRS.getDefaultState();
				break;
			case 9:
				output = Blocks.QUARTZ_STAIRS.getDefaultState();
				break;
			case 10:			
				output = Blocks.ACACIA_STAIRS.getDefaultState();
				break;
			case 11:			
				output = Blocks.DARK_OAK_STAIRS.getDefaultState();
				break;
			case 12:			
				output = Blocks.RED_SANDSTONE_STAIRS.getDefaultState();
				break;				
			case 13:			
				output = Blocks.PURPUR_STAIRS.getDefaultState();
				break;
			case 14:
				output = Blocks.STONE_BRICK_STAIRS.getDefaultState();
				break;
			default:
				return null;
		}
		return output
			.with(StairsBlock.FACING, getFacingEastWestSouthNorth(facing))
			.with(StairsBlock.HALF, half == 0 ? Half.BOTTOM : Half.TOP);
	}
	
	private static BlockState getLeverOrButtonWithData(int material, int data)
	{
		int facing = getBits(data, 0, 3);
		int powered = getBit(data, 3);
		AttachFace face = facing == 0 || facing == 7 ? AttachFace.CEILING : facing == 1 || facing == 2 || facing == 3 || facing == 4 ? AttachFace.WALL : facing == 5 || facing == 6 ? AttachFace.FLOOR : AttachFace.FLOOR;
		
		switch(material)
		{
			case 0:
				return Blocks.LEVER.getDefaultState().with(LeverBlock.FACE, face).with(LeverBlock.HORIZONTAL_FACING, getFacingLever(facing)).with(LeverBlock.POWERED, powered == 1);
			case 1:
				return Blocks.STONE_BUTTON.getDefaultState().with(StoneButtonBlock.FACE, face).with(StoneButtonBlock.HORIZONTAL_FACING, getFacingButton(facing)).with(StoneButtonBlock.POWERED, powered == 1);
			case 2:
				return Blocks.OAK_BUTTON.getDefaultState().with(WoodButtonBlock.FACE, face).with(WoodButtonBlock.HORIZONTAL_FACING, getFacingButton(facing)).with(WoodButtonBlock.POWERED, powered == 1);
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
				blockState = Blocks.OAK_DOOR.getDefaultState();
				break;
			case 1:
				blockState = Blocks.IRON_DOOR.getDefaultState();
				break;
			case 2:
				blockState = Blocks.SPRUCE_DOOR.getDefaultState();
				break;
			case 3:
				blockState = Blocks.BIRCH_DOOR.getDefaultState();
				break;
			case 4:
				blockState = Blocks.JUNGLE_DOOR.getDefaultState();
				break;
			case 5:
				blockState = Blocks.ACACIA_DOOR.getDefaultState();
				break;
			case 6:
				blockState = Blocks.DARK_OAK_DOOR.getDefaultState();
				break;
			default:
				return null;
		}
		return half == 0 ? 
			blockState
			.with(DoorBlock.HALF, DoubleBlockHalf.LOWER)			
			.with(DoorBlock.FACING, getFacingEastSouthWestNorth(facing))
			.with(DoorBlock.OPEN, open == 1) 
			: blockState
			.with(DoorBlock.HALF, DoubleBlockHalf.UPPER)
			.with(DoorBlock.HINGE, hinge == 0 ? DoorHingeSide.LEFT : DoorHingeSide.RIGHT)
			.with(DoorBlock.POWERED, powered == 1)
		;
	}
	
	private static BlockState getSignPostWithData(int data)
	{	
		int rotation = getBits(data, 0, 4);
		// TODO: Hopefully rotation is still mapped to the same int values as 1.12..
		return Blocks.OAK_SIGN.getDefaultState().with(StandingSignBlock.ROTATION, rotation); 
	}

	private static BlockState getWallSignWithData(int data)
	{
		int facing = getBits(data, 0, 3);
		return Blocks.OAK_WALL_SIGN.getDefaultState().with(WallSignBlock.FACING, getFacingNorthSouthWestEast(facing));
	}
	
	// TODO: Can't find information on 1.12 command block block data, what about facing?
	private static BlockState getCommandBlockWithData(int material, int data)
	{
		BlockState blockState;
		switch(material)
		{
			case 0:
				blockState = Blocks.COMMAND_BLOCK.getDefaultState();
				break;
			case 1:
				blockState = Blocks.REPEATING_COMMAND_BLOCK.getDefaultState();
				break;
			case 2:
				blockState = Blocks.CHAIN_COMMAND_BLOCK.getDefaultState();
				break;
			default:
				return null;
		}
		return blockState;
	}
	
	// TODO: Can't find information on 1.12 command block block data, what about facing?
	private static BlockState getShulkerBoxWithData(int material, int data)
	{
		BlockState blockState;
		switch(material)
		{
			case 0:
				blockState = Blocks.WHITE_SHULKER_BOX.getDefaultState();
				break;
			case 1:
				blockState = Blocks.ORANGE_SHULKER_BOX.getDefaultState();
				break;
			case 2:
				blockState = Blocks.MAGENTA_SHULKER_BOX.getDefaultState();
				break;
			case 3:
				blockState = Blocks.LIGHT_BLUE_SHULKER_BOX.getDefaultState();;
				break;
			case 4:
				blockState = Blocks.YELLOW_SHULKER_BOX.getDefaultState();
				break;
			case 5:
				blockState = Blocks.YELLOW_SHULKER_BOX.getDefaultState();
				break;
			case 6:
				blockState = Blocks.LIME_SHULKER_BOX.getDefaultState();
				break;
			case 7:
				blockState = Blocks.PINK_SHULKER_BOX.getDefaultState();
				break;
			case 8:
				blockState = Blocks.GRAY_SHULKER_BOX.getDefaultState();
				break;
			case 9:
				blockState = Blocks.SHULKER_BOX.getDefaultState();
				break;
			case 10:
				blockState = Blocks.CYAN_SHULKER_BOX.getDefaultState();
				break;
			case 11:
				blockState = Blocks.PURPLE_SHULKER_BOX.getDefaultState();
				break;
			case 12:
				blockState = Blocks.BLUE_SHULKER_BOX.getDefaultState();
				break;
			case 13:
				blockState = Blocks.BROWN_SHULKER_BOX.getDefaultState();
				break;
			case 14:
				blockState = Blocks.GREEN_SHULKER_BOX.getDefaultState();
				break;
			case 15:
				blockState = Blocks.RED_SHULKER_BOX.getDefaultState();
				break;
			case 16:
				blockState = Blocks.BLACK_SHULKER_BOX.getDefaultState();
				break;
			default:
				return null;
		}		
		return blockState.with(ShulkerBoxBlock.FACING, getFacingDownEastNorthSouthUpWest(data));
	}
	
	private static BlockState getLadderChestOrFurnaceWithData(int material, int data)
	{
		int facing = getBits(data, 0, 3);
		switch(material)
		{
			case 0:
				return Blocks.LADDER.getDefaultState().with(LadderBlock.FACING, getFacingNorthSouthWestEast(facing));
			case 1:
				return Blocks.CHEST.getDefaultState().with(ChestBlock.FACING, getFacingNorthSouthWestEast(facing));
			case 2:
				return Blocks.ENDER_CHEST.getDefaultState().with(EnderChestBlock.FACING, getFacingNorthSouthWestEast(facing));
			case 3:
				return Blocks.TRAPPED_CHEST.getDefaultState().with(TrappedChestBlock.FACING, getFacingNorthSouthWestEast(facing));
			case 4:
				return Blocks.FURNACE.getDefaultState().with(FurnaceBlock.FACING, getFacingNorthSouthWestEast(facing)).with(FurnaceBlock.LIT, false);
			case 5:
				return Blocks.FURNACE.getDefaultState().with(FurnaceBlock.FACING, getFacingNorthSouthWestEast(facing)).with(FurnaceBlock.LIT, true);
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
				return Blocks.DISPENSER.getDefaultState().with(DispenserBlock.FACING, getFacingDownUpNorthSouthWestEast(facing)).with(DispenserBlock.TRIGGERED, active == 1);
			case 1:
				return Blocks.DROPPER.getDefaultState().with(DropperBlock.FACING, getFacingDownUpNorthSouthWestEast(facing)).with(DropperBlock.TRIGGERED, active == 1);
			case 2:
				return Blocks.HOPPER.getDefaultState().with(HopperBlock.FACING, getFacingDownUpNorthSouthWestEast(facing)).with(HopperBlock.ENABLED, active == 1);
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
    			return Blocks.CARVED_PUMPKIN.getDefaultState().with(CarvedPumpkinBlock.FACING, getFacingSouthWestNorthEast(facing));
    		case 1:
    			return Blocks.JACK_O_LANTERN.getDefaultState().with(CarvedPumpkinBlock.FACING, getFacingSouthWestNorthEast(facing));
    		default:
    			return null;
    	}
	}
    
	private static BlockState getObserverWithData(int data)
	{
		int facing = getBits(data, 0, 3);
		int powered = getBit(data, 3);
		return Blocks.OBSERVER.getDefaultState().with(ObserverBlock.FACING, getFacingDownUpNorthSouthWestEast(facing)).with(ObserverBlock.POWERED, powered == 1);
	}
    
	private static BlockState getRepeaterWithData(int material, int data)
	{
		int facing = getBits(data, 0, 2);
		int delay = getBits(data, 2, 2) + 1;
		BlockState blockState;
		switch(material)
		{
			case 0:
				blockState = Blocks.REPEATER.getDefaultState().with(RepeaterBlock.POWERED, false);
				break;
			case 1:
				blockState = Blocks.REPEATER.getDefaultState().with(RepeaterBlock.POWERED, true);
				break;
			default:
				return null;
		}
		return blockState
			.with(RepeaterBlock.DELAY, delay)
			.with(RepeaterBlock.HORIZONTAL_FACING, getFacingNorthEastSouthWest(facing))
		;
	}
	
	private static BlockState getComparatorWithData(int material, int data)
	{
		int facing = getBits(data, 0, 2);
		int mode = getBit(data, 2);
		int powered = material == 1 ? 1 : getBit(data, 3);
		return Blocks.COMPARATOR.getDefaultState()
			.with(ComparatorBlock.HORIZONTAL_FACING, getFacingNorthEastSouthWest(facing))
			.with(ComparatorBlock.MODE, mode == 0 ?  ComparatorMode.COMPARE : ComparatorMode.SUBTRACT)
			.with(ComparatorBlock.POWERED, powered == 1)
		;
	}
	
	private static BlockState getBedBlockWithData(int data)
	{
		int facing = getBits(data, 0, 2);
		int occupied = getBit(data, 2);
		int part = getBit(data, 3);
		return Blocks.WHITE_BED.getDefaultState()
			.with(BedBlock.HORIZONTAL_FACING, getFacingSouthWestNorthEast(facing))
			.with(BedBlock.OCCUPIED, occupied == 1)
			.with(BedBlock.PART, part == 0 ? BedPart.FOOT : BedPart.HEAD)
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
				blockState = Blocks.OAK_TRAPDOOR.getDefaultState();
				break;
			case 1:
				blockState = Blocks.IRON_TRAPDOOR.getDefaultState();
				break;
			default:
				return null;
		}
		return blockState
			.with(TrapDoorBlock.HORIZONTAL_FACING, getFacingSouthNorthEastWest(facing))
			.with(TrapDoorBlock.HALF, half == 0 ? Half.BOTTOM : Half.TOP)
			.with(TrapDoorBlock.OPEN, open == 1)
		;
	}
	
	private static BlockState getPistonWithData(int material, int data)
	{
		int facing = getBits(data, 0, 3);
		int extended = getBit(data, 3);
		switch(material)
		{
			case 0:
				return Blocks.PISTON.getDefaultState().with(PistonBlock.EXTENDED, extended == 1).with(PistonBlock.FACING, getFacingDownUpNorthSouthWestEast(facing));
			case 1:				
				return Blocks.STICKY_PISTON.getDefaultState().with(PistonBlock.EXTENDED, extended == 1).with(PistonBlock.FACING, getFacingDownUpNorthSouthWestEast(facing));
			default:
				return null;
		}
	}
	
	private static BlockState getPistonHeadWithData(int data)
	{
		int facing = getBits(data, 0, 3);
		int type = getBit(data, 3);
		return Blocks.PISTON_HEAD.getDefaultState()
			.with(PistonHeadBlock.FACING, getFacingDownUpNorthSouthWestEast(facing))
			.with(PistonHeadBlock.TYPE, type == 0 ? PistonType.DEFAULT : PistonType.STICKY)			
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
			blockState = Blocks.MUSHROOM_STEM.getDefaultState();
		} else {
			switch(material)
			{
				case 0:
					blockState = Blocks.BROWN_MUSHROOM_BLOCK.getDefaultState();
					break;
				case 1:
					blockState = Blocks.RED_MUSHROOM_BLOCK.getDefaultState();
					break;
				default:
					return null;
			}
		}
		return blockState
			.with(HugeMushroomBlock.DOWN, down)
			.with(HugeMushroomBlock.UP, up)
			.with(HugeMushroomBlock.NORTH, north)
			.with(HugeMushroomBlock.EAST, east)
			.with(HugeMushroomBlock.SOUTH, south)
			.with(HugeMushroomBlock.WEST, west)
		;
	}
	
	private static BlockState getVineWithData(int data)
	{
		int south = getBit(data, 0);
		int west = getBit(data, 1);
		int north = getBit(data, 2);
		int east = getBit(data, 3);
		int up = data == 0 ? 1 : 0; // TODO: Should also be true if there's a block above, test if this is done dynamically.
		return Blocks.VINE.getDefaultState()
			.with(VineBlock.EAST, east == 1)
			.with(VineBlock.NORTH, north == 1)
			.with(VineBlock.SOUTH, south == 1)
			.with(VineBlock.WEST, west == 1)
			.with(VineBlock.UP, up == 1)
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
				blockState = Blocks.BIRCH_FENCE_GATE.getDefaultState();
				break;
			case 1:
				blockState = Blocks.OAK_FENCE_GATE.getDefaultState();
				break;
			case 2:
				blockState = Blocks.SPRUCE_FENCE_GATE.getDefaultState();
				break;
			case 3:
				blockState = Blocks.JUNGLE_FENCE_GATE.getDefaultState();
				break;
			case 4:
				blockState = Blocks.DARK_OAK_FENCE_GATE.getDefaultState();
				break;
			case 5:
				blockState = Blocks.ACACIA_FENCE_GATE.getDefaultState();
				break;
			default:
				return null;
		}
		return blockState
			.with(FenceGateBlock.HORIZONTAL_FACING, getFacingSouthWestNorthEast(facing))
			.with(FenceGateBlock.OPEN, open == 1)
		;
	}
	
	private static BlockState getCocoaWithData(int data)
	{
		int facing = getBits(data, 0, 2);
		int age = getBits(data, 2, 2);
		return Blocks.COCOA.getDefaultState().with(CocoaBlock.HORIZONTAL_FACING, getFacingNorthEastSouthWest(facing)).with(CocoaBlock.AGE, age);
	}
	
    private static BlockState getTripWireHookWithData(int data)
    {
    	int facing = getBits(data, 0, 2);
    	int attached = getBit(data, 2);
    	int powered = getBit(data, 3);
		return Blocks.TRIPWIRE_HOOK.getDefaultState()
			.with(TripWireHookBlock.ATTACHED, attached == 1)
			.with(TripWireHookBlock.FACING, getFacingSouthWestNorthEast(facing))
			.with(TripWireHookBlock.POWERED, powered == 1)			
		;
	}
    
	private static BlockState getEndPortalFrameWithData(int data)
	{
		int facing = getBits(data, 0, 2);
		int eye = getBit(data, 2);
		return Blocks.END_PORTAL_FRAME.getDefaultState().with(EndPortalFrameBlock.EYE, eye == 1).with(EndPortalFrameBlock.FACING, getFacingSouthWestNorthEast(facing));
	}    
    
	private static BlockState getStructureBlockWithData(int data)
	{
		StructureMode structureBlockMode = data == 0 ? StructureMode.DATA : data == 1 ? StructureMode.SAVE : data == 2 ? StructureMode.LOAD : data == 3 ? StructureMode.LOAD : StructureMode.DATA;
		return Blocks.STRUCTURE_BLOCK.getDefaultState().with(StructureBlock.MODE, structureBlockMode);
	}
	
	private static BlockState getGlazedTerracottaWithData(int material, int data)
	{
		int facing = getBits(data, 0, 2);
		BlockState blockState;
		switch(material)
		{
			case 0:
				blockState = Blocks.BLACK_GLAZED_TERRACOTTA.getDefaultState();
				break;
			case 1:
				blockState = Blocks.BLUE_GLAZED_TERRACOTTA.getDefaultState();
				break;
			case 2:
				blockState = Blocks.BROWN_GLAZED_TERRACOTTA.getDefaultState();
				break;
			case 3:
				blockState = Blocks.CYAN_GLAZED_TERRACOTTA.getDefaultState();
				break;
			case 4:
				blockState = Blocks.GRAY_GLAZED_TERRACOTTA.getDefaultState();
				break;
			case 5:
				blockState = Blocks.GREEN_GLAZED_TERRACOTTA.getDefaultState();
				break;
			case 6:
				blockState = Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA.getDefaultState();
				break;
			case 7:
				blockState = Blocks.LIME_GLAZED_TERRACOTTA.getDefaultState();
				break;
			case 8:
				blockState = Blocks.MAGENTA_GLAZED_TERRACOTTA.getDefaultState();
				break;
			case 9:
				blockState = Blocks.ORANGE_GLAZED_TERRACOTTA.getDefaultState();
				break;
			case 10:
				blockState = Blocks.PINK_GLAZED_TERRACOTTA.getDefaultState();
				break;
			case 11:
				blockState = Blocks.PURPLE_GLAZED_TERRACOTTA.getDefaultState();
				break;
			case 12:
				blockState = Blocks.RED_GLAZED_TERRACOTTA.getDefaultState();
				break;
			case 13:
				blockState = Blocks.LIGHT_GRAY_GLAZED_TERRACOTTA.getDefaultState();
				break;
			case 14:
				blockState = Blocks.WHITE_GLAZED_TERRACOTTA.getDefaultState();
				break;
			case 15:
				blockState = Blocks.YELLOW_GLAZED_TERRACOTTA.getDefaultState();
				break;
			default:
				return Blocks.BLACK_GLAZED_TERRACOTTA.getDefaultState();
		}
		return blockState.with(GlazedTerracottaBlock.HORIZONTAL_FACING, getFacingSouthWestNorthEast(facing));
	}
	
	private static BlockState getTripWireWithData(int data)
	{
		int active = getBit(data, 0);
		int attached = getBit(data, 2);
		int disarmed = getBit(data, 3);
		return Blocks.TRIPWIRE.getDefaultState()
			.with(TripWireBlock.POWERED, active == 1)
			.with(TripWireBlock.ATTACHED, attached == 1)
			.with(TripWireBlock.DISARMED, disarmed == 1)
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
	
    //

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
		return (source & (1 << index));
	}
}
