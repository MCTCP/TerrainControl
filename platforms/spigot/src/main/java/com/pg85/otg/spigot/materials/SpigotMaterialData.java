package com.pg85.otg.spigot.materials;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.pg85.otg.OTG;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.materials.LegacyMaterials;
import com.pg85.otg.util.materials.LocalMaterialData;
import net.minecraft.server.v1_16_R3.*;

import com.pg85.otg.util.materials.MaterialProperties;
import com.pg85.otg.util.materials.MaterialProperty;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_16_R3.block.data.CraftBlockData;

public class SpigotMaterialData extends LocalMaterialData
{
	private final IBlockData blockData;

	private String name;

	private SpigotMaterialData (IBlockData blockData)
	{
		this.blockData = blockData;
	}

	private SpigotMaterialData (IBlockData blockData, String raw)
	{
		if (blockData == null) this.isBlank = true;
		this.blockData = blockData;
		this.rawEntry = raw;
	}

	private SpigotMaterialData (String raw)
	{
		this.blockData = null;
		this.rawEntry = raw;
	}

	public static LocalMaterialData ofString (String input) throws InvalidConfigException
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

		IBlockData blockState = null;
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
				int blockId = Integer.parseInt(blockNameCorrected);
				String fromLegacyIdName = LegacyMaterials.blockNameFromLegacyBlockId(blockId);
				if (fromLegacyIdName != null)
				{
					blockNameCorrected = fromLegacyIdName;
				}
			}
			catch (NumberFormatException ignored)
			{
			}
		}

		// Try blockname[blockdata] / minecraft:blockname[blockdata] syntax

		// Use mc /setblock command logic to parse block string for us <3
		ArgumentTile blockStateArgument = new ArgumentTile();
		ArgumentTileLocation parseResult = null;
		try
		{
			String newInput = blockNameCorrected.contains(":") ? blockNameCorrected : "minecraft:" + blockNameCorrected;
			parseResult = blockStateArgument.parse(new StringReader(newInput));
		}
		catch (CommandSyntaxException ignored)
		{
		}
		if (parseResult != null)
		{
			// For leaves, add DISTANCE 1 to make them not decay.
			if (parseResult.a().getMaterial().equals(Material.LEAVES))
			{
				return new SpigotMaterialData(parseResult.a().set(BlockLeaves.DISTANCE, 1), input);
			}
			return new SpigotMaterialData(parseResult.a(), input);
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
			}
			catch (NumberFormatException ignored)
			{
			}
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
			}
			catch (NumberFormatException ignored)
			{
			}
		}

		// Try without data
		Block block = null;

		// This returns AIR if block is not found ><. ----Does it for spigot too?
		RegistryBlocks<Block> block_registry = IRegistry.BLOCK;
		block = block_registry.get(new MinecraftKey(blockNameCorrected));
		//block = ForgeRegistries.BLOCKS.getValue(new MinecraftKey(blockNameCorrected));
		if (block != null && (block != Blocks.AIR || blockNameCorrected.toLowerCase().endsWith("air")))
		{
			// For leaves, add DISTANCE 1 to make them not decay.
			if (block.getBlockData().getMaterial().equals(Material.LEAVES))
			{
				return new SpigotMaterialData(block.getBlockData().set(BlockLeaves.DISTANCE, 1), input);
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

	private static LocalMaterialData getBlank ()
	{
		SpigotMaterialData material = new SpigotMaterialData(null, null);
		material.isBlank = true;
		return material;
	}

	public static LocalMaterialData ofBlockData (IBlockData blockData)
	{
		return new SpigotMaterialData(blockData, null);
	}

	public static LocalMaterialData ofBlockData (IBlockData blockData, String raw)
	{
		return new SpigotMaterialData(blockData, raw);
	}

	public static LocalMaterialData ofSpigotMaterial (org.bukkit.Material type)
	{
		return new SpigotMaterialData(((CraftBlockData) type.createBlockData()).getState(), null);
	}

	@Override
	public String getName ()
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
			this.name = IRegistry.BLOCK.getKey(this.blockData.getBlock()).toString();
		}
		return this.name;
	}

	@Override
	public boolean isLiquid ()
	{
		return this.blockData != null
			   && (this.blockData.getMaterial() == Material.WATER
				   || this.blockData.getMaterial() == Material.LAVA);
	}

	@Override
	public boolean isSolid ()
	{
		return this.blockData != null && this.blockData.getMaterial().isSolid();
	}

	@Override
	public boolean isEmptyOrAir ()
	{
		return this.blockData == null || this.blockData.getMaterial() == Material.AIR;
	}

	@Override
	public boolean isAir ()
	{
		return this.blockData != null && this.blockData.getMaterial() == Material.AIR;
	}

	@Override
	public boolean isEmpty ()
	{
		return this.blockData == null;
	}

	@Override
	public boolean canSnowFallOn ()
	{
		return this.blockData != null && this.blockData.getMaterial().isSolid();
	}

	@Override
	public boolean isMaterial (LocalMaterialData material)
	{
		return (this.isBlank && ((SpigotMaterialData) material).isBlank) ||
			   (
					   !this.isBlank &&
					   !((SpigotMaterialData) material).isBlank &&
					   this.blockData.getMaterial() == ((SpigotMaterialData) material).blockData.getMaterial());
	}

	@Override
	public LocalMaterialData withDefaultBlockData ()
	{
		if (this.blockData == null)
		{
			return this;
		}
		return new SpigotMaterialData(this.blockData.getBlock().getBlockData(), rawEntry);
	}

	@Override
	public <T extends Comparable<T>> LocalMaterialData withProperty(MaterialProperty<T> materialProperty, T value)
	{
		IBlockState<T> property = null;

		// TODO: This is really bad. We need a way to append properties onto the MaterialProperty
		if (materialProperty == MaterialProperties.AGE_0_25) {
			property = (IBlockState<T>) BlockProperties.ak;
		} else {
			throw new IllegalArgumentException("Bad property: " + materialProperty);
		}

		return SpigotMaterialData.ofBlockData(this.blockData.set(property, value));
	}

	@Override
	public boolean equals (Object obj)
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

		// TODO: Compare registry names?
		return
				(this.isBlank && other.isBlank) ||
				(
						!this.isBlank &&
						!other.isBlank &&
						this.blockData.equals(other.blockData)
				);
	}

	@Override
	public int hashCode ()
	{
		// TODO: Implement this for 1.16
		return this.blockData == null ? -1 : this.blockData.hashCode();
	}

	@Override
	public boolean canFall ()
	{
		return this.blockData != null && this.blockData.getBlock() instanceof BlockFalling;
	}

	@Override
	public boolean hasData ()
	{
		// TODO: Implement this for 1.16
		return false;
	}

	public IBlockData internalBlock ()
	{
		return this.blockData;
	}

	public BlockData toSpigotBlockData ()
	{
		if (this.blockData == null)
			return null;
		return CraftBlockData.fromData(this.blockData);
	}
}
