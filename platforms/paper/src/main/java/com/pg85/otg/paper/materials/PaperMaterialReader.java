package com.pg85.otg.paper.materials;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.pg85.otg.OTG;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.util.FifoMap;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.LocalMaterialTag;
import com.pg85.otg.util.minecraft.BlockNames;

import net.minecraft.server.v1_17_R1.ArgumentBlock;
import net.minecraft.server.v1_17_R1.Block;
import net.minecraft.server.v1_17_R1.BlockLeaves;
import net.minecraft.server.v1_17_R1.Blocks;
import net.minecraft.server.v1_17_R1.IBlockData;
import net.minecraft.server.v1_17_R1.IRegistry;
import net.minecraft.server.v1_17_R1.MinecraftKey;
import net.minecraft.server.v1_17_R1.ResourceKeyInvalidException;

public class PaperMaterialReader implements IMaterialReader
{
	// TODO: Smaller caches should be ok, only most frequently used should be cached?
	private final FifoMap<String, LocalMaterialData> cachedMaterials = new FifoMap<>(4096);
	private final FifoMap<String, LocalMaterialTag> cachedTags = new FifoMap<>(4096);	

	@Override
	public LocalMaterialData readMaterial(String material) throws InvalidConfigException
	{
		if (material == null)
		{
			return null;
		}

		LocalMaterialData localMaterial = this.cachedMaterials.get(material);
		if (localMaterial != null)
		{
			return localMaterial;
		}
		else if (this.cachedMaterials.containsKey(material))
		{
			throw new InvalidConfigException("Cannot read block: " + material);
		}

		try
		{
			localMaterial = materialFromString(material);
		}
		catch (InvalidConfigException ex)
		{
			// Happens when a non existing block name is used.
			if(OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.CONFIGS))
			{
				OTG.getEngine().getLogger().log(LogLevel.ERROR, LogCategory.CONFIGS, "Invalid material " + material + ". Exception: " + ex.getMessage() + ". Replacing with blank.");
			}
		}

		this.cachedMaterials.put(material, localMaterial);

		return localMaterial;
	}
	
	@Override
	public LocalMaterialTag readTag(String tag) throws InvalidConfigException
	{
		if(tag == null)
		{
			return null;
		}
		
		LocalMaterialTag localTag = this.cachedTags.get(tag);
		if(localTag != null)
		{
			return localTag;
		}

		localTag = PaperMaterialTag.ofString(tag);
		this.cachedTags.put(tag, localTag);	
		return localTag;
	}

	private LocalMaterialData materialFromString(String input) throws InvalidConfigException
	{
		if (input == null || input.trim().isEmpty())
		{
			return null;
		}
		// Allow for xx:xx:xx - Frank
		if (input.matches("minecraft:[A-Za-z_]+:[0-9]+")) input = input.split(":")[1] + ":" + input.split(":")[2];

		// Try parsing as an internal Minecraft name
		// This is so that things like "minecraft:stone" aren't parsed
		// as the block "minecraft" with data "stone", but instead as the
		// block "minecraft:stone" with no block data.

		// Used in BO4's as placeholder/detector block.
		if (input.equalsIgnoreCase("blank"))
		{
			return PaperMaterialData.blank;
		}

		IBlockData blockState;
		String blockNameCorrected = input.trim().toLowerCase();
		// Try parsing as legacy block name / id
		if (!blockNameCorrected.contains(":"))
		{
			blockState = PaperLegacyMaterials.fromLegacyBlockName(blockNameCorrected);
			if (blockState != null)
			{
				return PaperMaterialData.ofBlockData(blockState, input);
			}
			try
			{
				// Deal with pesky accidental floats that parseInt won't recognize
				if (blockNameCorrected.endsWith(".0"))
				{
					blockNameCorrected = blockNameCorrected.substring(0, blockNameCorrected.length() - 2);
				}
				int blockId = Integer.parseInt(blockNameCorrected);
				String fromLegacyIdName = BlockNames.blockNameFromLegacyBlockId(blockId);
				if (fromLegacyIdName != null)
				{
					blockNameCorrected = fromLegacyIdName;
					blockState = PaperLegacyMaterials.fromLegacyBlockName(blockNameCorrected);
					if (blockState != null)
					{
						return PaperMaterialData.ofBlockData(blockState, input);
					}
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
		catch (CommandSyntaxException ignored) { }
		if (blockdata != null)
		{
			// For leaves, add DISTANCE 1 to make them not decay.
			if (blockdata.getBlock() instanceof BlockLeaves)
			{
				return PaperMaterialData.ofBlockData(blockdata.set(BlockLeaves.DISTANCE, 1), input);
			}			
			return PaperMaterialData.ofBlockData(blockdata, input);
		}

		// Try legacy block with data (fe SAND:1 or 12:1)
		if (blockNameCorrected.contains(":"))
		{
			// Try parsing data argument as int.
			String blockNameOrId = blockNameCorrected.substring(0, blockNameCorrected.indexOf(":"));
			try
			{
				int blockId = Integer.parseInt(blockNameOrId);
				blockNameOrId = BlockNames.blockNameFromLegacyBlockId(blockId);
			} catch (NumberFormatException ignored) { }

			try
			{
				int data = Integer.parseInt(blockNameCorrected.substring(blockNameCorrected.indexOf(":") + 1));
				blockState = PaperLegacyMaterials.fromLegacyBlockNameOrIdWithData(blockNameOrId, data);
				if (blockState != null)
				{
					return PaperMaterialData.ofBlockData(blockState, input);
				}
				// Failed to parse data, remove. fe STONE:0 or STONE:1 -> STONE
				blockNameCorrected = blockNameCorrected.substring(0, blockNameCorrected.indexOf(":"));
			} catch (NumberFormatException ignored) { }
		}

		// Try without data
		Block block;

		try
		{
			// This returns AIR if block is not found ><. ----Does it for spigot too?
			block = IRegistry.BLOCK.get(new MinecraftKey(blockNameCorrected));
			if (block != Blocks.AIR || blockNameCorrected.toLowerCase().endsWith("air"))
			{
				// For leaves, add DISTANCE 1 to make them not decay.
				if (block instanceof BlockLeaves)
				{
					return PaperMaterialData.ofBlockData(block.getBlockData().set(BlockLeaves.DISTANCE, 1), input);
				}				
				return PaperMaterialData.ofBlockData(block.getBlockData(), input);
			}
		} catch(ResourceKeyInvalidException ignored) { }

		// Try legacy name again, without data.
		blockState = PaperLegacyMaterials.fromLegacyBlockName(blockNameCorrected.replace("minecraft:", ""));
		if (blockState != null)
		{
			return PaperMaterialData.ofBlockData(blockState, input);
		}

		if(OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.CONFIGS))
		{
			OTG.getEngine().getLogger().log(LogLevel.ERROR, LogCategory.CONFIGS, "Could not parse block: " + input + " (" + blockNameCorrected + "), substituting NOTE_BLOCK.");
		}

		return PaperMaterialData.ofBlockData(Blocks.NOTE_BLOCK.getBlockData(), input);
	}	
}
