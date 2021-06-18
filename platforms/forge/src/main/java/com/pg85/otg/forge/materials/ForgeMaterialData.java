package com.pg85.otg.forge.materials;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.pg85.otg.OTG;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.OTGDirection;
import com.pg85.otg.util.materials.LegacyMaterials;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.LocalMaterialTag;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.command.arguments.BlockStateParser;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.Property;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;

import com.pg85.otg.util.materials.MaterialProperty;
import com.pg85.otg.util.materials.MaterialProperties;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collection;
import java.util.HashMap;
import java.util.Objects;

/**
 * Implementation of LocalMaterial that wraps one of Minecraft's Blocks.
 */
public class ForgeMaterialData extends LocalMaterialData
{
	private static final LocalMaterialData blank = new ForgeMaterialData(null, null, true);
	private static final HashMap<BlockState, ForgeMaterialData> stateToMaterialDataMap = new HashMap<>();

	private final BlockState blockData;
	private String name = null;

	private ForgeMaterialData(BlockState blockData, String raw)
	{
		this(blockData, raw, false);
	}
	
	private ForgeMaterialData(BlockState blockData, String raw, boolean isBlank)
	{
		this.blockData = blockData;
		this.rawEntry = raw;
		this.isBlank = isBlank;
	}

	public static LocalMaterialData ofString(String input) throws InvalidConfigException
	{
		if(input == null || input.trim().isEmpty())
		{
			return null;
		}
		
		// Try parsing as an internal Minecraft name
		// This is so that things like "minecraft:stone" aren't parsed
		// as the block "minecraft" with data "stone", but instead as the
		// block "minecraft:stone" with no block data.
		
		// Used in BO4's as placeholder/detector block.
		if(input.equalsIgnoreCase("blank"))
		{
			return ForgeMaterialData.blank;
		}

		BlockState blockState;
		String blockNameCorrected = input.trim().toLowerCase();
		// Try parsing as legacy block name / id
		if(!blockNameCorrected.contains(":"))
		{
			blockState = ForgeLegacyMaterials.fromLegacyBlockName(blockNameCorrected);
			if(blockState != null)
			{
				return ofBlockState(blockState, input);
			}
			try
			{
				// Deal with pesky accidental floats that parseInt won't recognize
				if (blockNameCorrected.endsWith(".0"))
				{
					blockNameCorrected = blockNameCorrected.substring(0, blockNameCorrected.length()-2);
				}
				int blockId = Integer.parseInt(blockNameCorrected);
				String fromLegacyIdName = LegacyMaterials.blockNameFromLegacyBlockId(blockId);
				if(fromLegacyIdName != null)
				{
					blockNameCorrected = fromLegacyIdName;
				}
			} catch(NumberFormatException ignored) { }
		}
		
		// Try blockname[blockdata] / minecraft:blockname[blockdata] syntax
		
		// Use mc /setblock command logic to parse block string for us <3
		BlockState state = null;
		try {
			String newInput = blockNameCorrected.contains(":") ? blockNameCorrected : "minecraft:" + blockNameCorrected;
			state = new BlockStateParser(new StringReader(newInput), true).parse(true).getState();
		} catch (CommandSyntaxException ignored) { }
		if(state != null)
		{
			// For leaves, add DISTANCE 1 to make them not decay.
			if(state.getMaterial().equals(Material.LEAVES))
			{
				return ofBlockState(state.setValue(LeavesBlock.DISTANCE, 1), input);
			}
			return ofBlockState(state, input);
		}
		
		// Try legacy block with data (fe SAND:1 or 12:1)
		if(blockNameCorrected.contains(":"))
		{
			// Try parsing data argument as int.
			String blockNameOrId = blockNameCorrected.substring(0, blockNameCorrected.indexOf(":"));
			try
			{
				int blockId = Integer.parseInt(blockNameOrId);
				blockNameOrId = LegacyMaterials.blockNameFromLegacyBlockId(blockId);
			} catch(NumberFormatException ignored) { }

			try
			{
				int data = Integer.parseInt(blockNameCorrected.substring(blockNameCorrected.indexOf(":") + 1));
				blockState = ForgeLegacyMaterials.fromLegacyBlockNameOrIdWithData(blockNameOrId, data);
				if(blockState != null)
				{
					return ofBlockState(blockState, input);
				}
				// Failed to parse data, remove. fe STONE:0 or STONE:1 -> STONE
				blockNameCorrected = blockNameCorrected.substring(0, blockNameCorrected.indexOf(":"));				
			} catch(NumberFormatException ignored) { }
		}

		// Try without data
		Block block;
		try
		{
			// This returns AIR if block is not found ><.
			block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockNameCorrected));
			if(block != null && (block != Blocks.AIR || blockNameCorrected.toLowerCase().endsWith("air")))
			{
				// For leaves, add DISTANCE 1 to make them not decay.
				if(block.defaultBlockState().getMaterial().equals(Material.LEAVES))
				{
					return ofBlockState(block.defaultBlockState().setValue(LeavesBlock.DISTANCE, 1), input);
				}
				return ofBlock(block, input);
			}
		} catch(net.minecraft.util.ResourceLocationException ignored) { }

		// Try legacy name again, without data.
		blockState = ForgeLegacyMaterials.fromLegacyBlockName(blockNameCorrected.replace("minecraft:", ""));
		if(blockState != null)
		{
			return ofBlockState(blockState, input);
		}
		
		OTG.log(LogMarker.INFO, "Could not parse block: " + input + ", substituting AIR.");
		
		return ofBlock(Blocks.AIR, input);
	}
		
	private static ForgeMaterialData ofBlock(Block block, String raw)
	{
		return ofBlockState(block.defaultBlockState(), raw);
	}

	public static ForgeMaterialData ofBlockState(BlockState blockData)
	{
		return ofBlockState(blockData, null);
	}
	
	private static ForgeMaterialData ofBlockState(BlockState blockState, String raw)
	{
		// Create only one LocalMaterialData object for each BlockState
		if (stateToMaterialDataMap.containsKey(blockState))
		{
			return stateToMaterialDataMap.get(blockState);
		}
		ForgeMaterialData data = new ForgeMaterialData(blockState, raw);
		stateToMaterialDataMap.put(blockState, data);
		return data;
	}	
	
	public BlockState internalBlock()
	{
		return this.blockData;
	}
	
	@Override
	public String getName()
	{
		if (this.name != null)
		{
			return this.name;
		}
		if(isBlank)
		{
			return "BLANK";
		}
		else if(this.blockData == null)
		{
			if(this.rawEntry != null)
			{
				this.name = this.rawEntry;
			} else {
				this.name = "Unknown";
			}
		} else {
			this.name = this.blockData.toString()
				.replace("Block{", "")
				.replace("}", "");
		}
		return this.name;
	}
		
	@Override
	public boolean isLiquid()
	{
		return this.blockData != null && this.blockData.getMaterial().isLiquid();
	}

	@Override
	public boolean isSolid()
	{
		return this.blockData != null && this.blockData.getMaterial().isSolid() && this.blockData.getMaterial().isSolidBlocking();
	}
	
	@Override
	public boolean isEmptyOrAir()
	{
		return this.blockData == null || this.blockData.getMaterial() == Material.AIR;
	}
	
	@Override
	public boolean isAir()
	{
		return this.blockData != null && this.blockData.getMaterial() == Material.AIR;
	}
	
	@Override
	public boolean isEmpty()
	{
		return this.blockData == null;
	}

	@Override
	public boolean canFall()
	{
		return this.blockData != null && this.blockData.getBlock() instanceof FallingBlock;
	}

	@Override
	public boolean canSnowFallOn()
	{
		return this.blockData != null && this.blockData.getMaterial().isSolid();
	}

	@Override
	public boolean isMaterial(LocalMaterialData material)
	{
		return 
			(this.isBlank && ((ForgeMaterialData)material).isBlank) ||
			(
				!this.isBlank && 
				!((ForgeMaterialData)material).isBlank &&
				Objects.equals(this.blockData.getBlock().getRegistryName(), ((ForgeMaterialData) material).internalBlock().getBlock().getRegistryName())
			)
		;
	}
	
	@Override
	public LocalMaterialData rotate(int rotateTimes)
	{
		// Get the rotation if we haven't stored the rotation yet
		if (rotated == null)
		{
			BlockState state = this.blockData;
			Collection<Property<?>> properties = state.getProperties();
			// Loop through the blocks properties
			for (Property<?> property : properties)
			{
				// Anything with a direction
				if (property instanceof DirectionProperty)
				{
					Direction direction = (Direction) state.getValue(property);
					switch (direction)
					{
						case DOWN:
						case UP:
							break;
						case NORTH:
							state = state.setValue((DirectionProperty) property, Direction.WEST);
							break;
						case SOUTH:
							state = state.setValue((DirectionProperty) property, Direction.EAST);
							break;
						case WEST:
							state = state.setValue((DirectionProperty) property, Direction.SOUTH);
							break;
						case EAST:
							state = state.setValue((DirectionProperty) property, Direction.NORTH);
							break;
					}
				}
			}
			if (state.hasProperty(SixWayBlock.EAST)) // fence or glass pane
			{
				// Cache the east value, before it's overwritten by the rotated south value
				boolean hasEast = state.getValue(SixWayBlock.EAST);
				state = state.setValue(SixWayBlock.EAST, state.getValue(SixWayBlock.SOUTH));
				state = state.setValue(SixWayBlock.SOUTH, state.getValue(SixWayBlock.WEST));
				state = state.setValue(SixWayBlock.WEST, state.getValue(SixWayBlock.NORTH));
				state = state.setValue(SixWayBlock.NORTH, hasEast);
			}
			// Block is rotated, store a pointer to it
			this.rotated = ForgeMaterialData.ofBlockState(state);
		}

		if (rotateTimes > 1) {
			return rotated.rotate(rotateTimes-1);
		}

		return this.rotated;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Comparable<T>> LocalMaterialData withProperty(MaterialProperty<T> materialProperty, T value)
	{
		@SuppressWarnings("rawtypes")
		Property property = null;
		T finalVal = value;

		// TODO: This is really bad. We need a way to append properties onto the MaterialProperty
		if (materialProperty == MaterialProperties.AGE_0_25)
		{
			property = BlockStateProperties.AGE_25;
		}
		else if (materialProperty == MaterialProperties.PICKLES_1_4)
		{
			property = BlockStateProperties.PICKLES;
		}
		else if (materialProperty == MaterialProperties.SNOWY)
		{
			property = BlockStateProperties.SNOWY;
		}
		else if (materialProperty == MaterialProperties.HORIZONTAL_DIRECTION)
		{
			// Extremely ugly hack for directions
			property = BlockStateProperties.HORIZONTAL_FACING;
			Direction direction = Direction.values()[((OTGDirection)value).ordinal()];
			return ForgeMaterialData.ofBlockState(this.blockData.setValue(property, direction));
		} else {
			throw new IllegalArgumentException("Unknown property: " + materialProperty);
		}

		return ForgeMaterialData.ofBlockState(this.blockData.setValue(property, finalVal));
	}

	@Override
	public boolean isBlockTag(LocalMaterialTag tag)
	{
		return this.blockData == null ? false : this.blockData.is(((ForgeMaterialTag)tag).getTag());
	}	
	
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (!(obj instanceof ForgeMaterialData))
		{
			return false;
		}
		ForgeMaterialData other = (ForgeMaterialData) obj;
		return 
			(this.isBlank && other.isBlank) || 
			(
				!this.isBlank && 
				!other.isBlank &&
				this.blockData.getBlock().equals(other.internalBlock().getBlock())
			)
		;
	}
	
	/**
	 * Gets the hashCode of the material, based on the block id and block data.
	 * The hashCode must be unique, which is possible considering that there are
	 * only 4096 * 16 possible materials.
	 * 
	 * @return The unique hashCode.
	 */
	@Override
	public int hashCode()
	{
		// TODO: Implement this for 1.16
		return this.blockData == null ? -1 : this.blockData.hashCode();
	}	
}
