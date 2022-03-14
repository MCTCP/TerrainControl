package com.pg85.otg.util.biome;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.util.helpers.StringHelper;
import com.pg85.otg.util.materials.LocalMaterialBase;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.LocalMaterialTag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ReplaceBlockMatrix
{
	private static final String NO_REPLACE = "None";
	
	private class ReplaceBlockEntry
	{
		public final List<ReplacedBlocksInstruction> targets = new ArrayList<ReplacedBlocksInstruction>();
	}
	
	public static class ReplacedBlocksInstruction
	{
		private final LocalMaterialBase from;
		private LocalMaterialData to;
		private final int minHeight;
		private final int maxHeight;
		
		/**
		 * Parses the given instruction string.
		 * @param instruction The instruction string.
		 * @param maxAllowedY Maximum allowed y height for the replace setting, inclusive.
		 * @throws InvalidConfigException If the instruction is formatted incorrectly.
		 */
		private ReplacedBlocksInstruction(String instruction, int maxAllowedY, IMaterialReader materialReader) throws InvalidConfigException
		{
			String[] values = instruction.split(",(?![^\\(\\[]*[\\]\\)])"); // Splits on any comma not inside brackets
			if (values.length == 5)
			{
				// Replace in TC 2.3 style found
				values = new String[] {values[0], values[1] + ":" + values[2], values[3], "" + (Integer.parseInt(values[4]) - 1)};
			}

			if (values.length != 2 && values.length != 4)
			{
				throw new InvalidConfigException("Replace parts must be in the format (from,to) or (from,to,minHeight,maxHeight)");
			}
			
			LocalMaterialTag tag = materialReader.readTag(values[0]);
			if(tag != null)
			{
				this.from = tag;
			} else {
				this.from = materialReader.readMaterial(values[0]);	
			}
			this.to = materialReader.readMaterial(values[1]);

			if (values.length == 4)
			{
				this.minHeight = StringHelper.readInt(values[2], 0, maxAllowedY);
				this.maxHeight = StringHelper.readInt(values[3], this.minHeight, maxAllowedY);
			} else {
				this.minHeight = 0;
				this.maxHeight = maxAllowedY;
			}
		}

		/**
		 * Creates a ReplacedBlocksInstruction with the given parameters.
		 * Parameters may not be null.
		 * @param from The block that will be replaced.
		 * @param to The block that from will be replaced to.
		 * @param minHeight Minimum height for this replace, inclusive. Must be smaller than or equal to 0.
		 * @param maxHeight Maximum height for this replace, inclusive. Must not be larger than {@link ReplaceBlockMatrix#maxHeight}.
		 */
		public ReplacedBlocksInstruction(LocalMaterialBase from, LocalMaterialData to, int minHeight, int maxHeight)
		{
			this.from = from;
			this.to = to;
			this.minHeight = minHeight;
			this.maxHeight = maxHeight;
		}

		public ReplacedBlocksInstruction clone()
		{
			return new ReplacedBlocksInstruction(this.from, this.to, this.minHeight, this.maxHeight);
		}
		
		public LocalMaterialBase getFrom()
		{
			return this.from;
		}

		public LocalMaterialData getTo()
		{
			return this.to;
		}

		public int getMinHeight()
		{
			return this.minHeight;
		}

		public int getMaxHeight()
		{
			return this.maxHeight;
		}
	}

	 // All ReplacedBlocksInstructions must have maxHeight smaller than or equal to this.
	private final int maxHeight;
	private List<ReplacedBlocksInstruction> instructions;
	private final ReplaceBlockEntry[] targetsAtHeights;
	
	public boolean replacesCooledLava = false;
	public boolean replacesIce = false;
	public boolean replacesPackedIce = false;
	public boolean replacesSnow = false;
	public boolean replacesWater = false;
	public boolean replacesStone = false;
	public boolean replacesGround = false;
	public boolean replacesSurface = false;
	public boolean replacesUnderWaterSurface = false;
	public boolean replacesBedrock = false;
	public boolean replacesSandStone = false;
	public boolean replacesRedSandStone = false;

	public ReplaceBlockMatrix(String setting, int maxHeight, IMaterialReader reader) throws InvalidConfigException
	{
		this.maxHeight = maxHeight;
		this.targetsAtHeights = (ReplaceBlockEntry[])new ReplaceBlockEntry[256];
		
		// Parse
		if (setting.isEmpty() || setting.equalsIgnoreCase(NO_REPLACE))
		{
			setInstructions(Collections.<ReplacedBlocksInstruction> emptyList());
			return;
		}

		List<ReplacedBlocksInstruction> instructions = new ArrayList<ReplacedBlocksInstruction>();
		String[] keys = StringHelper.readCommaSeperatedString(setting);

		for (String key : keys)
		{
			int start = key.indexOf('(');
			int end = key.lastIndexOf(')');
			if (start != -1 && end != -1)
			{
				String keyWithoutBraces = key.substring(start + 1, end);
				instructions.add(new ReplacedBlocksInstruction(keyWithoutBraces, maxHeight, reader));
			} else {
				throw new InvalidConfigException("One of the parts is missing braces around it.");
			}
		}

		// Set
		setInstructions(instructions);		
	}
	
	public void init(LocalMaterialData biomeCooledLavaBlock, LocalMaterialData biomeIceBlock, LocalMaterialData biomePackedIceBlock, LocalMaterialData biomeSnowBlock, LocalMaterialData biomeWaterBlock, LocalMaterialData biomeStoneBlock, LocalMaterialData biomeGroundBlock, LocalMaterialData biomeSurfaceBlock, LocalMaterialData biomeUnderWaterSurfaceBlock, LocalMaterialData biomeBedrockBlock, LocalMaterialData biomeSandStoneBlock, LocalMaterialData biomeRedSandStoneBlock)
	{
		// Fill maps for faster access
		for(ReplacedBlocksInstruction instruction : this.instructions)
		{
			for(int y = instruction.minHeight; y <= instruction.maxHeight; y++)
			{
				if(y > Constants.WORLD_HEIGHT - 1)
				{
					break;
				}
				if(y < Constants.WORLD_DEPTH)
				{
					continue;
				}
				ReplaceBlockEntry targetsAtHeight = this.targetsAtHeights[y];
				if(targetsAtHeight == null)
				{
					targetsAtHeight = new ReplaceBlockEntry();
					this.targetsAtHeights[y] = targetsAtHeight;
				}
				
				// Users can chain replacedblocks to replace replacedblocks, instead of actually replacing the 
				// same block to different materials multiple times, we'll calculate the end result in advance.
				for(ReplacedBlocksInstruction existing : targetsAtHeight.targets)
				{
					// If this instruction replaces the output of a previously added
					// instruction, override the output of the previous instruction.
					LocalMaterialData existingTo = (LocalMaterialData)existing.to;
					if(!instruction.from.isTag())
					{
						LocalMaterialData newFrom = (LocalMaterialData)instruction.from;
						if(
							(newFrom.isDefaultState() && newFrom.getRegistryName().equals(existingTo.getRegistryName())) ||
							(!newFrom.isDefaultState() && newFrom.hashCode() == existingTo.hashCode())
						)
						{
							existing.to = instruction.to;
						}
					} else {
						LocalMaterialTag newFrom = (LocalMaterialTag)instruction.from;
						if(instruction.from.isTag() && existingTo.isBlockTag(newFrom))
						{
							existing.to = instruction.to;
						}
					}
				}
				targetsAtHeight.targets.add(instruction.clone());
			}
		}
		
		for(ReplacedBlocksInstruction instruction : this.instructions)
		{
			if(instruction.from.matches(biomeCooledLavaBlock))
			{
				this.replacesCooledLava = true;
			}
			if(instruction.from.matches(biomeIceBlock))
			{
				this.replacesIce = true;
			}
			if(instruction.from.matches(biomePackedIceBlock))
			{
				this.replacesPackedIce = true;
			}
			if(instruction.from.matches(biomeSnowBlock))
			{
				this.replacesSnow = true;
			}
			if(instruction.from.matches(biomeWaterBlock))
			{
				this.replacesWater = true;
			}
			if(instruction.from.matches(biomeStoneBlock))
			{
				this.replacesStone = true;
			}
			if(instruction.from.matches(biomeGroundBlock))
			{
				this.replacesGround = true;
			}
			if(instruction.from.matches(biomeSurfaceBlock))
			{
				this.replacesSurface = true;
			}
			if(instruction.from.matches(biomeUnderWaterSurfaceBlock))
			{
				this.replacesUnderWaterSurface = true;
			}
			if(instruction.from.matches(biomeBedrockBlock))
			{
				this.replacesBedrock = true;
			}
			if(instruction.from.matches(biomeSandStoneBlock))
			{
				this.replacesSandStone = true;
			}
			if(instruction.from.matches(biomeRedSandStoneBlock))
			{
				this.replacesRedSandStone = true;
			}
		}
	}

	public boolean replacesBlock(LocalMaterialData targetBlock)
	{
		for(ReplacedBlocksInstruction instruction : this.instructions)
		{
			if(instruction.from.matches(targetBlock))
			{
				return true;
			}
		}
		return false;
	}
	
	public LocalMaterialData replaceBlock(int y, LocalMaterialData material)
	{
		// TODO: simple fix for y being out of bounds, needs a proper fix to figure out why it's happening
		y = Math.max(Math.min(y, 255), 0);

		ReplaceBlockEntry targetsAtHeight = targetsAtHeights[y];
		if(targetsAtHeight != null)
		{
			for(ReplacedBlocksInstruction instruction : targetsAtHeight.targets)
			{			
				if(instruction.from.matches(material))
				{
					return instruction.to;
				}
			}
		}
		return material;
	}

	/**
	 * Gets whether this biome has replace settings set. If this returns true,
	 * the {@link #compiledInstructions} array won't be null.
	 * 
	 * @return Whether this biome has replace settings set.
	 */
	public boolean hasReplaceSettings()
	{
		return this.instructions != null && this.instructions.size() > 0;
	}

	/**
	 * Gets an immutable list of all ReplacedBlocks instructions.
	 * 
	 * @return The ReplacedBlocks instructions.
	 */
	public List<ReplacedBlocksInstruction> getInstructions()
	{
		// Note that the returned list is immutable, see setInstructions
		return instructions;
	}

	/**
	 * Sets the ReplacedBlocks instructions. This method will update the
	 * {@link #compiledInstructions} array.
	 * 
	 * @param instructions The new instructions.
	 */
	public void setInstructions(Collection<ReplacedBlocksInstruction> instructions)
	{
		this.instructions = Collections.unmodifiableList(new ArrayList<ReplacedBlocksInstruction>(instructions));
	}

	public String toString()
	{
		if (!this.hasReplaceSettings())
		{
			// No replace setting
			return NO_REPLACE;
		}

		StringBuilder builder = new StringBuilder();
		for (ReplacedBlocksInstruction instruction : getInstructions())
		{
			builder.append('(');
			builder.append(instruction.from);
			builder.append(',').append(instruction.to);
			if (instruction.getMinHeight() != 0 || instruction.getMaxHeight() != this.maxHeight)
			{
				// Add custom height setting
				builder.append(',').append(instruction.getMinHeight());
				builder.append(',').append(instruction.getMaxHeight());
			}
			builder.append(')').append(',');
		}

		// Remove last ',' and return the result
		return builder.substring(0, builder.length() - 1);
	}

	/**
	 * Creates an empty matrix.
	 * 
	 * @param maxHeight Max height for the replace setting, inclusive.
	 * @return The empty matrix.
	 */
	public static ReplaceBlockMatrix createEmptyMatrix(int maxHeight, IMaterialReader materialReader)
	{
		try {
			return new ReplaceBlockMatrix(NO_REPLACE, maxHeight, materialReader);
		} catch (InvalidConfigException e) {
			throw new AssertionError(e); // Should never happen
		}
	}
}
