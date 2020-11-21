package com.pg85.otg.util.biome;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.util.helpers.StringHelper;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.materials.LocalMaterialData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class ReplacedBlocksMatrix
{
	// Redesigned this to use ReplaceBlockEntry instead, since we're now replacing
	// blocks when they're placed, not 4x per chunk at the end of population.
	// TODO: After removing replaceblocks from localworld, clean this up.
	
    private static final String NO_REPLACE = "None";
    
    private class ReplaceBlockEntry
    {
    	public final HashMap<Integer, LocalMaterialData> targetsWithoutBlockData = new HashMap<Integer, LocalMaterialData>();
    	public final HashMap<Integer, LocalMaterialData> targetsWithBlockData = new HashMap<Integer, LocalMaterialData>();
    }
    
    public static class ReplacedBlocksInstruction
    {
        private final LocalMaterialData from;
        private final LocalMaterialData to;
        private final int minHeight;
        private final int maxHeight;
        
        /**
         * Parses the given instruction string.
         * 
         * @param instruction The instruction string.
         * @param maxAllowedY Maximum allowed y height for the replace
         *            setting, inclusive.
         * @throws InvalidConfigException If the instruction is formatted
         *             incorrectly.
         */
        private ReplacedBlocksInstruction(String instruction, int maxAllowedY, IMaterialReader materialReader) throws InvalidConfigException
        {
            String[] values = instruction.split(",");
            if (values.length == 5)
            {
                // Replace in TC 2.3 style found
                values = new String[] {values[0], values[1] + ":" + values[2], values[3], "" + (Integer.parseInt(values[4]) - 1)};
            }

            if (values.length != 2 && values.length != 4)
            {
                throw new InvalidConfigException("Replace parts must be in the format (from,to) or (from,to,minHeight,maxHeight)");
            }

           	from = materialReader.readMaterial(values[0]);
            to = materialReader.readMaterial(values[1]);

            if (values.length == 4)
            {
                minHeight = StringHelper.readInt(values[2], 0, maxAllowedY);
                maxHeight = StringHelper.readInt(values[3], minHeight, maxAllowedY);
            } else
            {
                minHeight = 0;
                maxHeight = maxAllowedY;
            }
        }

        /**
         * Creates a ReplacedBlocksInstruction with the given parameters.
         * Parameters may not be null.
         * 
         * @param from The block that will be replaced.
         * @param to The block that from will be replaced to.
         * @param minHeight Minimum height for this replace, inclusive. Must
         *            be smaller than or equal to 0.
         * @param maxHeight Maximum height for this replace, inclusive. Must
         *            not be larger than
         *            {@link ReplacedBlocksMatrix#maxHeight}.
         */
        public ReplacedBlocksInstruction(LocalMaterialData from, LocalMaterialData to, int minHeight, int maxHeight)
        {
            this.from = from;
            this.to = to;
            this.minHeight = minHeight;
            this.maxHeight = maxHeight;
        }

        public LocalMaterialData getFrom()
        {
            return from;
        }

        public LocalMaterialData getTo()
        {
            return to;
        }

        public int getMinHeight()
        {
            return minHeight;
        }

        public int getMaxHeight()
        {
            return maxHeight;
        }
    }

    /**
     * All {@link ReplacedBlocksInstruction}s must have their
     * {@link ReplacedBlocksInstruction#getMaxHeight() maxHeight} smaller than
     * or equal to this.
     */
    private final int maxHeight;
    private List<ReplacedBlocksInstruction> instructions;
    private final ReplaceBlockEntry[] targetsAtHeights;
    
    public boolean replacesCooledLava = false;
	public boolean replacesIce = false;
	public boolean replacesWater = false;
	public boolean replacesStone = false;
	public boolean replacesGround = false;
	public boolean replacesSurface = false;
	public boolean replacesBedrock = false;
	public boolean replacesSandStone = false;
	public boolean replacesRedSandStone = false;

    public ReplacedBlocksMatrix(String setting, int maxHeight, IMaterialReader reader) throws InvalidConfigException
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
            } else
            {
                throw new InvalidConfigException("One of the parts is missing braces around it.");
            }
        }

        // Set
        setInstructions(instructions);
        
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
        		// Users can chain replacedblocks to replace replacedblocks, instead of actually
        		// replacing the same block to different materials multiple times, we'll calculate
        		// the end result in advance.
        		for(Entry<Integer, LocalMaterialData> entry : targetsAtHeight.targetsWithoutBlockData.entrySet())
        		{
        			if(
            			// BLOCK:X replaces BLOCK:X
    					(instruction.from.hasData() && instruction.from.hashCode() == entry.getValue().hashCode()) //||
            			// BLOCK replaces all BLOCK:X
    					//(!instruction.from.hasData() && instruction.from.hashCodeWithoutBlockData() == entry.getValue().hashCodeWithoutBlockData())
					)
        			{
        				entry.setValue(instruction.to);
        			}
        		}
        		for(Entry<Integer, LocalMaterialData> entry : targetsAtHeight.targetsWithBlockData.entrySet())
        		{
        			if(
            			// BLOCK:X replaces BLOCK:X
    					(instruction.from.hasData() &&  instruction.from.hashCode() == entry.getValue().hashCode()) //||
    					// BLOCK replaces all BLOCK:X
        				//(!instruction.from.hasData() && instruction.from.hashCodeWithoutBlockData() == entry.getValue().hashCodeWithoutBlockData())
    				)
        			{
        				entry.setValue(instruction.to);
        			}
        		}
        		//if(instruction.from.hasData())
        		//{
        			targetsAtHeight.targetsWithBlockData.put(instruction.from.hashCode(), instruction.to);
        		//} else {
        			//targetsAtHeight.targetsWithoutBlockData.put(instruction.from.hashCodeWithoutBlockData(), instruction.to);
        		//}
        	}
        }
    }
    
	public void init(LocalMaterialData biomeCooledLavaBlock, LocalMaterialData biomeIceBlock, LocalMaterialData biomeWaterBlock, LocalMaterialData biomeStoneBlock, LocalMaterialData biomeGroundBlock, LocalMaterialData biomeSurfaceBlock, LocalMaterialData biomeBedrockBlock, LocalMaterialData biomeSandStoneBlock, LocalMaterialData biomeRedSandStoneBlock)
	{
        // Fill maps for faster access
        for(ReplacedBlocksInstruction instruction : this.instructions)
        {     
        	if(
    			//!instruction.from.hasData() ? instruction.from.hashCodeWithoutBlockData() == biomeCooledLavaBlock.hashCodeWithoutBlockData() : 
				instruction.from.hashCode() == biomeCooledLavaBlock.hashCode() 
			)
        	{
        		this.replacesCooledLava = true;
        	}        	
        	if(
    			//!instruction.from.hasData() ? instruction.from.hashCodeWithoutBlockData() == biomeIceBlock.hashCodeWithoutBlockData() : 
				instruction.from.hashCode() == biomeIceBlock.hashCode() 
			)
        	{
        		this.replacesIce = true;
        	}
        	if(
    			//!instruction.from.hasData() ? instruction.from.hashCodeWithoutBlockData() == biomeWaterBlock.hashCodeWithoutBlockData() : 
				instruction.from.hashCode() == biomeWaterBlock.hashCode() 
			)        		
        	{
        		this.replacesWater = true;
        	}
        	if(
    			//!instruction.from.hasData() ? instruction.from.hashCodeWithoutBlockData() == biomeStoneBlock.hashCodeWithoutBlockData() : 
				instruction.from.hashCode() == biomeStoneBlock.hashCode()
			)
        	{
        		this.replacesStone = true;
        	}
        	if(
    			//!instruction.from.hasData() ? instruction.from.hashCodeWithoutBlockData() == biomeGroundBlock.hashCodeWithoutBlockData() : 
				instruction.from.hashCode() == biomeGroundBlock.hashCode()
			)
        	{
        		this.replacesGround = true;
        	}
        	if(
    			//!instruction.from.hasData() ? instruction.from.hashCodeWithoutBlockData() == biomeSurfaceBlock.hashCodeWithoutBlockData() : 
				instruction.from.hashCode() == biomeSurfaceBlock.hashCode()
			)        	
        	{
        		this.replacesSurface = true;
        	}
        	if(
    			//!instruction.from.hasData() ? instruction.from.hashCodeWithoutBlockData() == biomeBedrockBlock.hashCodeWithoutBlockData() : 
				instruction.from.hashCode() == biomeBedrockBlock.hashCode()
			)        	
        	{
        		this.replacesBedrock = true;
        	}
        	if(
    			//!instruction.from.hasData() ? instruction.from.hashCodeWithoutBlockData() == biomeSandStoneBlock.hashCodeWithoutBlockData() : 
				instruction.from.hashCode() == biomeSandStoneBlock.hashCode()
			)        	
        	{
        		this.replacesSandStone = true;
        	}
        	if(
    			//!instruction.from.hasData() ? instruction.from.hashCodeWithoutBlockData() == biomeRedSandStoneBlock.hashCodeWithoutBlockData() : 
				instruction.from.hashCode() == biomeRedSandStoneBlock.hashCode()
			)        	
        	{
        		this.replacesRedSandStone = true;
        	}
        }
	}

	/*
	private boolean parsedFallBacks = false;
	public void parseForWorld(WorldConfig worldConfig)
	{
		if(!parsedFallBacks)
		{
			parsedFallBacks = true;
			for(ReplacedBlocksInstruction instruction : this.instructions)
			{
				if(instruction.from != null)
				{
					instruction.from.parseForWorld(worldConfig);
				}
				if(instruction.to != null)
				{
					instruction.to.parseForWorld(worldConfig);
				}
			}
		}
	}
	*/
   
    public LocalMaterialData replaceBlock(int y, LocalMaterialData material)
    {
    	ReplaceBlockEntry targetsAtHeight = targetsAtHeights[y];
    	if(targetsAtHeight == null)
    	{
    		return material;
    	}
    	//LocalMaterialData replaceToMaterial = targetsAtHeight.targetsWithoutBlockData.get(material.hashCodeWithoutBlockData());
    	//if(replaceToMaterial != null)
    	//{
    		//return replaceToMaterial;
    	//}
    	LocalMaterialData replaceToMaterial = targetsAtHeight.targetsWithBlockData.get(material.hashCode());
    	if(replaceToMaterial != null)
    	{
    		return replaceToMaterial;
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

        if (this.instructions.size() == 0)
        {
            return;
        }
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
    public static ReplacedBlocksMatrix createEmptyMatrix(int maxHeight, IMaterialReader materialReader)
    {
        try
        {
            return new ReplacedBlocksMatrix(NO_REPLACE, maxHeight, materialReader);
        } catch (InvalidConfigException e)
        {
            // Should never happen
            throw new AssertionError(e);
        }
    }

	public boolean replacesBlock(LocalMaterialData surfaceBlock)
	{				
        for(ReplacedBlocksInstruction instruction : this.instructions)
        {
        	if(
    			//instruction.getFrom().hasData() ? 
    			surfaceBlock.hashCode() == instruction.from.hashCode() //: 
        		//surfaceBlock.hashCodeWithoutBlockData() == instruction.from.hashCodeWithoutBlockData()
    		)
        	{
        		return true;
        	}
        }
    	return false;
	}
}
