package com.pg85.otg.paper.materials;

import com.pg85.otg.util.OTGDirection;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.LocalMaterialTag;
import com.pg85.otg.util.materials.MaterialProperties;
import com.pg85.otg.util.materials.MaterialProperty;

import net.minecraft.core.Direction;
import net.minecraft.tags.Tag;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Material;

import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_17_R1.block.data.CraftBlockData;

import java.util.Collection;
import java.util.HashMap;
import java.util.Objects;

/**
 * Implementation of LocalMaterial that wraps one of Minecraft's Blocks.
 */
public class PaperMaterialData extends LocalMaterialData
{
	static final LocalMaterialData blank = new PaperMaterialData(null, null, true);
	private static final HashMap<BlockState, LocalMaterialData> stateToMaterialDataMap = new HashMap<>(); // TODO: Move to SpigotMaterialReader?

	private final BlockState blockData;
	private String name = null;

	private PaperMaterialData(BlockState blockData, String raw)
	{
		this(blockData, raw, false);
	}
	
	private PaperMaterialData(BlockState blockData, String raw, boolean isBlank)
	{
		this.blockData = blockData;
		this.rawEntry = raw;
		this.isBlank = isBlank;
	}

	public static LocalMaterialData ofBlockData(BlockState blockData)
	{
		return ofBlockData(blockData, null);
	}

	public static LocalMaterialData ofBlockData(BlockState blockData, String raw)
	{
		// Create only one LocalMaterialData object for each BlockState		
		if (stateToMaterialDataMap.containsKey(blockData))
		{
			return stateToMaterialDataMap.get(blockData);
		}
		LocalMaterialData data = new PaperMaterialData(blockData, raw);
		stateToMaterialDataMap.put(blockData, data);
		return data;
	}

	public static LocalMaterialData ofSpigotMaterial(org.bukkit.Material type)
	{
		return ofBlockData(((CraftBlockData)type.createBlockData()).getState(), null);
	}

	public BlockState internalBlock()
	{
		return this.blockData;
	}
	
	public BlockData toSpigotBlockData()
	{
		if (this.blockData == null)
		{
			return null;
		}
		return CraftBlockData.fromData(this.blockData);
	}	
	
	@Override
	public String getName()
	{
		if (this.name != null)
		{
			return this.name;
		}
		if (isBlank)
		{
			this.name = "BLANK";
		}
		else if (this.blockData == null)
		{
			if (this.rawEntry != null)
			{
				this.name = this.rawEntry;
			} else {
				this.name = "Unknown";
			}
		} else {
			if(
				this.blockData != this.blockData.getBlock().defaultBlockState() &&
				(
					// We set distance 1 when parsing minecraft:xxx_leaves, so check for default blockstate + distance 1
					!(this.blockData.getBlock() instanceof LeavesBlock) || this.blockData != this.blockData.getBlock().defaultBlockState().setValue(LeavesBlock.DISTANCE, 1)
				)
			)
			{
				this.name = this.blockData.toString().replace("Block{", "").replace("}", "");
			} else {
				this.name = getRegistryName();
			}
		}		
		return this.name;
	}

	@Override
	public String getRegistryName()
	{
		return this.blockData == null ? null : this.blockData.toString().substring(0, this.blockData.toString().indexOf("}")).replace("Block{", "");
	}

	@Override
	public boolean isLiquid()
	{
		return
			this.blockData != null &&
			(
				this.blockData.getMaterial() == Material.WATER ||
				this.blockData.getMaterial() == Material.LAVA
			);
	}

	@Override
	public boolean isSolid()
	{
		// TODO: This should check for isSolid and isSolidBlocking, there is no isSolidBlocking for Spigot tho?
		return this.blockData != null && this.blockData.getMaterial().isSolid();
	}

	@Override
	public boolean isEmptyOrAir()
	{
		return this.blockData == null || this.blockData.getMaterial() == Material.AIR;
	}
	
	@Override
	public boolean isNonCaveAir()
	{
		return this.blockData != null && this.blockData.getBlock() == Blocks.AIR;
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
		// Taken from SnowBlock.canSurvive
		if(
			this.blockData != null &&
			!this.blockData.is(Blocks.ICE) &&
			!this.blockData.is(Blocks.PACKED_ICE) &&
			!this.blockData.is(Blocks.BARRIER)
		) {
			if (
				!this.blockData.is(Blocks.HONEY_BLOCK) &&
				!this.blockData.is(Blocks.SOUL_SAND)
			) {
				// TODO: Vanilla checks faceFull here, we don't since it requires coords.
				//return Block.isFaceFull(this.blockData.getCollisionShape(blockPos, blockPos.below()), Direction.UP) || (this.blockData.is(Blocks.SNOW) && this.blockData.getValue(LAYERS) == 8);
				return this.blockData.getMaterial().isSolid() || (this.blockData.is(Blocks.SNOW) && this.blockData.getValue(SnowLayerBlock.LAYERS) == 8);
			} else {
				return true;
			}
		} else {
			return false;
		}
	}	
	
	@Override
	public boolean isMaterial(LocalMaterialData material)
	{
		return
			(this.isBlank && ((PaperMaterialData) material).isBlank) ||
			(
				!this.isBlank &&
				!((PaperMaterialData) material).isBlank &&
				Objects.equals(this.blockData.getBlock(), ((PaperMaterialData) material).blockData.getBlock())
			)
		;
	}

	@Override
	public LocalMaterialData rotate(int rotateTimes)
	{
		if(this.isBlank)
		{
			return this;
		}

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
			if (state.hasProperty(RotatedPillarBlock.AXIS)) // All pillar blocks (logs, hay, chain(?), basalt, purpur, quartz)
			{
				state = ((RotatedPillarBlock)state.getBlock()).rotate(this.blockData, Rotation.COUNTERCLOCKWISE_90);
			}
			if (state.hasProperty(CrossCollisionBlock.EAST)) // fence or glass pane
			{
				// Cache the east value, before it's overwritten by the rotated south value
				boolean hasEast = state.getValue(CrossCollisionBlock.EAST);
				state = state.setValue(CrossCollisionBlock.EAST, state.getValue(CrossCollisionBlock.SOUTH));
				state = state.setValue(CrossCollisionBlock.SOUTH, state.getValue(CrossCollisionBlock.WEST));
				state = state.setValue(CrossCollisionBlock.WEST, state.getValue(CrossCollisionBlock.NORTH));
				state = state.setValue(CrossCollisionBlock.NORTH, hasEast);
			}
			// Block is rotated, store a pointer to it			
			this.rotated = PaperMaterialData.ofBlockData(state);
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
		} else if (materialProperty == MaterialProperties.SNOWY)
		{
			property = BlockStateProperties.SNOWY;
		} else if (materialProperty == MaterialProperties.HORIZONTAL_DIRECTION)
		{
			// Extremely ugly hack for directions
			property = BlockStateProperties.HORIZONTAL_FACING;
			Direction direction = Direction.values()[((OTGDirection)value).ordinal()];
			return PaperMaterialData.ofBlockData(this.blockData.setValue(property, direction));
		} else {
			throw new IllegalArgumentException("Unknown property: " + materialProperty);
		}

		return PaperMaterialData.ofBlockData(this.blockData.setValue(property, finalVal));
	}
	
	@Override
	public boolean isBlockTag(LocalMaterialTag tag)
	{
		if(this.blockData != null)
		{
			// Since we can't register OTG block tags for Spigot,
			// have to use our own block tags logic.
			// Try OTG tags, then MC tags.

			PaperMaterialTag spigotTag = ((PaperMaterialTag)tag);
			if(spigotTag.isOTGTag(this.blockData.getBlock()))
			{
				return true;
			}
			
			Tag<Block> blockTag = spigotTag.getTag();
			if(blockTag != null)
			{
				return this.blockData.is(blockTag);
			}
		}
		return false;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (!(obj instanceof PaperMaterialData))
		{
			return false;
		}
		PaperMaterialData other = (PaperMaterialData) obj;
		return
			(this.isBlank && other.isBlank) ||
			(
				!this.isBlank &&
				!other.isBlank &&
				Objects.equals(this.blockData.getBlock(), other.blockData.getBlock())
			);
	}

	@Override
	public int hashCode()
	{
		// TODO: Implement this for 1.16
		return this.blockData == null ? -1 : this.blockData.hashCode();
	}

	@Override
	public LocalMaterialData legalOrPersistentLeaves(boolean leaveIllegalLeaves)
	{
		if (!this.isLeaves())
		{
			return this;
		}
		int i = blockData.getValue(LeavesBlock.DISTANCE);
		if (i > 6)
		{
			return PaperMaterialData.ofBlockData(blockData.setValue(LeavesBlock.PERSISTENT, true));
		} else {
			return PaperMaterialData.ofBlockData(blockData.setValue(LeavesBlock.PERSISTENT, false));
		}
	}
}
