package com.pg85.otg.spigot.materials;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.pg85.otg.OTG;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.OTGDirection;
import com.pg85.otg.util.materials.LegacyMaterials;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.MaterialProperties;
import com.pg85.otg.util.materials.MaterialProperty;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_16_R3.block.data.CraftBlockData;

import java.util.Collection;
import java.util.HashMap;

public class SpigotMaterialData extends LocalMaterialData
{
	private static final LocalMaterialData blank;

	static
	{
		SpigotMaterialData data = new SpigotMaterialData(null, null);
		data.isBlank = true;
		blank = data;
	}

	private static final HashMap<IBlockData, LocalMaterialData> stateToMaterialDataMap = new HashMap<>();

	private final IBlockData blockData;

	private String name = null;

	private SpigotMaterialData(IBlockData blockData, String raw)
	{
		if (blockData == null) this.isBlank = true;
		this.blockData = blockData;
		this.rawEntry = raw;
	}


	private static LocalMaterialData getBlank()
	{
		return blank;
	}

	public static LocalMaterialData ofString(String input) throws InvalidConfigException
	{
		if (input == null || input.trim().isEmpty())
		{
			return null;
		}

		// Try parsing as an internal Minecraft name
		// This is so that things like "minecraft:stone" aren't parsed
		// as the block "minecraft" with data "stone", but instead as the
		// block "minecraft:stone" with no block data.

		// Used in BO4's as placeholder/detector block.
		if (input.equalsIgnoreCase("blank"))
		{
			return SpigotMaterialData.getBlank();
		}

		IBlockData blockState;
		String blockNameCorrected = input.trim().toLowerCase();
		// Try parsing as legacy block name / id
		if (!blockNameCorrected.contains(":"))
		{
			blockState = SpigotLegacyMaterials.fromLegacyBlockName(blockNameCorrected);
			if (blockState != null)
			{
				return ofBlockData(blockState, input);
			}
			try
			{
				// Deal with pesky accidental floats that parseInt won't recognize
				if (blockNameCorrected.endsWith(".0"))
				{
					blockNameCorrected = blockNameCorrected.substring(0, blockNameCorrected.length() - 2);
				}
				int blockId = Integer.parseInt(blockNameCorrected);
				String fromLegacyIdName = LegacyMaterials.blockNameFromLegacyBlockId(blockId);
				if (fromLegacyIdName != null)
				{
					blockNameCorrected = fromLegacyIdName;
				}
			}
			catch (NumberFormatException ignored) { }
		}

		// Try blockname[blockdata] / minecraft:blockname[blockdata] syntax

		// Use mc /setblock command logic to parse block string for us <3
		IBlockData blockdata = null;
		try
		{
			String newInput = blockNameCorrected.contains(":") ? blockNameCorrected : "minecraft:" + blockNameCorrected;
			blockdata = new ArgumentBlock(new StringReader(newInput), true).a(true).getBlockData();
		}
		catch (CommandSyntaxException ignored)
		{
		}
		if (blockdata != null)
		{
			// For leaves, add DISTANCE 1 to make them not decay.
			if (blockdata.getMaterial().equals(Material.LEAVES))
			{
				return ofBlockData(blockdata.set(BlockLeaves.DISTANCE, 1), input);
			}
			return ofBlockData(blockdata, input);
		}

		// Try legacy block with data (fe SAND:1 or 12:1)
		if (blockNameCorrected.contains(":"))
		{
			// Try parsing data argument as int.
			String blockNameOrId = blockNameCorrected.substring(0, blockNameCorrected.indexOf(":"));
			try
			{
				int blockId = Integer.parseInt(blockNameOrId);
				blockNameOrId = LegacyMaterials.blockNameFromLegacyBlockId(blockId);
			} catch (NumberFormatException ignored) { }

			try
			{
				int data = Integer.parseInt(blockNameCorrected.substring(blockNameCorrected.indexOf(":") + 1));
				blockState = SpigotLegacyMaterials.fromLegacyBlockNameOrIdWithData(blockNameOrId, data);
				if (blockState != null)
				{
					return ofBlockData(blockState, input);
				}
				// Failed to parse data, remove. fe STONE:0 or STONE:1 -> STONE
				blockNameCorrected = blockNameCorrected.substring(0, blockNameCorrected.indexOf(":"));
			} catch (NumberFormatException ignored) { }
		}

		// Try without data
		Block block;

		// This returns AIR if block is not found ><. ----Does it for spigot too?
		block = IRegistry.BLOCK.get(new MinecraftKey(blockNameCorrected));
		if (block != Blocks.AIR || blockNameCorrected.toLowerCase().endsWith("air"))
		{
			// For leaves, add DISTANCE 1 to make them not decay.
			if (block.getBlockData().getMaterial().equals(Material.LEAVES))
			{
				return ofBlockData(block.getBlockData().set(BlockLeaves.DISTANCE, 1), input);
			}
			return ofBlockData(block.getBlockData(), input);
		}

		// Try legacy name again, without data.
		blockState = SpigotLegacyMaterials.fromLegacyBlockName(blockNameCorrected.replace("minecraft:", ""));
		if (blockState != null)
		{
			return ofBlockData(blockState, input);
		}

		OTG.log(LogMarker.INFO, "Could not parse block: " + input + " (" + blockNameCorrected + "), substituting NOTE_BLOCK.");

		return ofBlockData(Blocks.NOTE_BLOCK.getBlockData(), input);
	}

	public static LocalMaterialData ofBlockData(IBlockData blockData)
	{
		return ofBlockData(blockData, null);
	}

	public static LocalMaterialData ofBlockData(IBlockData blockData, String raw)
	{
		if (stateToMaterialDataMap.containsKey(blockData))
			return stateToMaterialDataMap.get(blockData);
		LocalMaterialData data = new SpigotMaterialData(blockData, raw);
		stateToMaterialDataMap.put(blockData, data);
		return data;
	}

	public static LocalMaterialData ofSpigotMaterial(org.bukkit.Material type)
	{
		return ofBlockData(((CraftBlockData) type.createBlockData()).getState(), null);
	}

	@Override
	public String getName()
	{
		if (this.name != null)
			return this.name;
		if (isBlank)
		{
			this.name = "BLANK";
		}
		else if (this.blockData == null)
		{
			if (this.rawEntry != null)
			{
				this.name = this.rawEntry;
			}
			else
			{
				this.name = "Unknown";
			}
		}
		else
		{
			this.name = this.blockData.toString()
				.replace("Block{", "")
				.replace("}", "");
		}
		return this.name;
	}

	@Override
	public boolean isLiquid()
	{
		return this.blockData != null
			   && (this.blockData.getMaterial() == Material.WATER
				   || this.blockData.getMaterial() == Material.LAVA);
	}

	@Override
	public boolean isSolid()
	{
		return this.blockData != null && this.blockData.getMaterial().isSolid();
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
	public boolean canSnowFallOn()
	{
		return this.blockData != null && this.blockData.getMaterial().isSolid();
	}

	@Override
	public boolean isMaterial(LocalMaterialData material)
	{
		return (this.isBlank && ((SpigotMaterialData) material).isBlank) ||
			   (
				   !this.isBlank &&
				   !((SpigotMaterialData) material).isBlank &&
				   this.blockData.getBlock().r().equals(((SpigotMaterialData) material).blockData.getBlock().r()));
	}

	@Override
	public LocalMaterialData withDefaultBlockData()
	{
		if (this.blockData == null)
		{
			return this;
		}
		return ofBlockData(this.blockData.getBlock().getBlockData(), rawEntry);
	}

	@Override
	public <T extends Comparable<T>> LocalMaterialData withProperty(MaterialProperty<T> materialProperty, T value)
	{
		IBlockState property = null;
		T finalVal = value;

		// TODO: This is really bad. We need a way to append properties onto the MaterialProperty
		if (materialProperty == MaterialProperties.AGE_0_25)
		{
			property = BlockProperties.ak;
		}
		else if (materialProperty == MaterialProperties.PICKLES_1_4)
		{
			property = BlockProperties.ay;
		} else if (materialProperty == MaterialProperties.HORIZONTAL_DIRECTION)
		{
			// Extremely ugly hack for directions
			property = BlockProperties.O;
			EnumDirection direction = EnumDirection.values()[((OTGDirection)value).ordinal()];
			return SpigotMaterialData.ofBlockData(this.blockData.set(property, direction));
		} else
		{
			throw new IllegalArgumentException("Unknown property: " + materialProperty);
		}

		return SpigotMaterialData.ofBlockData(this.blockData.set(property, finalVal));
	}

	@Override
	public LocalMaterialData rotate(int rotateTimes)
	{
		if (rotated == null)
		{

			IBlockData state = this.blockData;

			Collection<IBlockState<?>> properties = state.r();
			for (IBlockState<?> property : properties)
			{
				if (property instanceof BlockStateDirection) // Anything with a direction
				{
					EnumDirection direction = (EnumDirection) state.get(property);
					switch (direction)
					{
						case DOWN:
						case UP:
							break;
						case NORTH:
							state = state.set((BlockStateDirection) property, EnumDirection.WEST);
							break;
						case SOUTH:
							state = state.set((BlockStateDirection) property, EnumDirection.EAST);
							break;
						case WEST:
							state = state.set((BlockStateDirection) property, EnumDirection.SOUTH);
							break;
						case EAST:
							state = state.set((BlockStateDirection) property, EnumDirection.NORTH);
							break;
					}
				}
			}

			if (state.b(BlockTall.EAST)) // fence or glass pane
			{
				boolean hasEast = state.get(BlockTall.EAST);
				state = state.set(BlockTall.EAST, state.get(BlockTall.SOUTH));
				state = state.set(BlockTall.SOUTH, state.get(BlockTall.WEST));
				state = state.set(BlockTall.WEST, state.get(BlockTall.NORTH));
				state = state.set(BlockTall.NORTH, hasEast);
			}
			this.rotated = SpigotMaterialData.ofBlockData(state);
		}

		if (rotateTimes > 1) {
			return rotated.rotate(rotateTimes-1);
		}

		return this.rotated;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (!(obj instanceof SpigotMaterialData))
		{
			return false;
		}
		SpigotMaterialData other = (SpigotMaterialData) obj;
		return
			(this.isBlank && other.isBlank) ||
			(
				!this.isBlank &&
				!other.isBlank &&
				this.blockData.equals(other.blockData)
			);
	}

	@Override
	public int hashCode()
	{
		// TODO: Implement this for 1.16
		return this.blockData == null ? -1 : this.blockData.hashCode();
	}

	@Override
	public boolean canFall()
	{
		return this.blockData != null && this.blockData.getBlock() instanceof BlockFalling;
	}

	@Override
	public boolean hasData()
	{
		// TODO: Implement this for 1.16
		return false;
	}

	public IBlockData internalBlock()
	{
		return this.blockData;
	}

	public BlockData toSpigotBlockData()
	{
		if (this.blockData == null)
			return null;
		return CraftBlockData.fromData(this.blockData);
	}
}
