package com.pg85.otg.forge.materials;

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

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.command.arguments.BlockStateParser;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class ForgeMaterialReader implements IMaterialReader
{
	// TODO: Smaller caches should be ok, only most frequently used should be cached?
	private final FifoMap<String, LocalMaterialData> cachedMaterials = new FifoMap<>(4096);
	private final FifoMap<String, LocalMaterialTag> cachedTags = new FifoMap<>(4096);	
	
	@Override
	public LocalMaterialData readMaterial(String material) throws InvalidConfigException
	{
		if(material == null)
		{
			return null;
		}
		
		LocalMaterialData localMaterial = this.cachedMaterials.get(material);
		if(localMaterial != null)
		{
			return localMaterial;
		}
		else if(this.cachedMaterials.containsKey(material))
		{
			throw new InvalidConfigException("Cannot read block: " + material);
		}

		try
		{
			localMaterial = materialFromString(material);
		}
		catch(InvalidConfigException ex)
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

		localTag = ForgeMaterialTag.ofString(tag);
		this.cachedTags.put(tag, localTag);
		return localTag;
	}

	private LocalMaterialData materialFromString(String input) throws InvalidConfigException
	{
		if(input == null || input.trim().isEmpty())
		{
			return null;
		}

		if (input.matches("minecraft:[A-Za-z_]+:[0-9]+")) input = input.split(":")[1] + ":" + input.split(":")[2];
		
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
				return ForgeMaterialData.ofBlockState(blockState, input);
			}
			try
			{
				// Deal with pesky accidental floats that parseInt won't recognize
				if (blockNameCorrected.endsWith(".0"))
				{
					blockNameCorrected = blockNameCorrected.substring(0, blockNameCorrected.length()-2);
				}
				int blockId = Integer.parseInt(blockNameCorrected);
				String fromLegacyIdName = BlockNames.blockNameFromLegacyBlockId(blockId);
				if(fromLegacyIdName != null)
				{
					blockNameCorrected = fromLegacyIdName;
					blockState = ForgeLegacyMaterials.fromLegacyBlockName(blockNameCorrected);
					if(blockState != null)
					{
						return ForgeMaterialData.ofBlockState(blockState, input);
					}					
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
			// TODO: Maybe only do this for leaves that don't already have distance set to a different value? -auth
			if(state.getBlock() instanceof LeavesBlock)
			{
				return ForgeMaterialData.ofBlockState(state.setValue(LeavesBlock.DISTANCE, 1), input);
			}			
			return ForgeMaterialData.ofBlockState(state, input);
		}
		
		// Try legacy block with data (fe SAND:1 or 12:1)
		if(blockNameCorrected.contains(":"))
		{
			// Try parsing data argument as int.
			String blockNameOrId = blockNameCorrected.substring(0, blockNameCorrected.indexOf(":"));
			try
			{
				int blockId = Integer.parseInt(blockNameOrId);
				blockNameOrId = BlockNames.blockNameFromLegacyBlockId(blockId);
			} catch(NumberFormatException ignored) { }

			try
			{
				int data = Integer.parseInt(blockNameCorrected.substring(blockNameCorrected.indexOf(":") + 1));
				blockState = ForgeLegacyMaterials.fromLegacyBlockNameOrIdWithData(blockNameOrId, data);
				if(blockState != null)
				{
					return ForgeMaterialData.ofBlockState(blockState, input);
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
				if(block instanceof LeavesBlock)
				{
					return ForgeMaterialData.ofBlockState(block.defaultBlockState().setValue(LeavesBlock.DISTANCE, 1), input);
				}				
				return ForgeMaterialData.ofBlock(block, input);
			}
		} catch(net.minecraft.util.ResourceLocationException ignored) { }

		// Try legacy name again, without data.
		blockState = ForgeLegacyMaterials.fromLegacyBlockName(blockNameCorrected.replace("minecraft:", ""));
		if(blockState != null)
		{
			return ForgeMaterialData.ofBlockState(blockState, input);
		}
		
		if(OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.CONFIGS))
		{
			OTG.getEngine().getLogger().log(LogLevel.ERROR, LogCategory.CONFIGS, "Could not parse block: " + input + ", substituting AIR.");
		}
		
		return ForgeMaterialData.ofBlock(Blocks.AIR, input);
	}	
}
