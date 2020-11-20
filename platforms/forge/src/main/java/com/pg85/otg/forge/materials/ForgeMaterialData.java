package com.pg85.otg.forge.materials;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.pg85.otg.OTG;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.materials.LocalMaterialData;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.material.Material;
import net.minecraft.command.arguments.BlockStateArgument;
import net.minecraft.command.arguments.BlockStateInput;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Implementation of LocalMaterial that wraps one of Minecraft's Blocks.
 *
 */
public class ForgeMaterialData extends LocalMaterialData
{
	private BlockState blockData;

    private ForgeMaterialData(BlockState blockData)
    {
        this.blockData = blockData;
    }
    
    private ForgeMaterialData(BlockState blockData, String raw)
    {
        this.blockData = blockData;
        this.rawEntry = raw;
    }
    
    private ForgeMaterialData(String raw)
    {
    	this.blockData = null;
    	this.rawEntry = raw;
    }
    
    public static ForgeMaterialData getBlank()
    {
    	// TODO: this null should probably be replaced with air
    	ForgeMaterialData material = new ForgeMaterialData((BlockState)null, null);
    	material.isBlank = true;
    	return material;
    }

    public static ForgeMaterialData ofString(String input) throws InvalidConfigException
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
    	if(input.toLowerCase().equals("blank"))
    	{
    		return ForgeMaterialData.getBlank();
    	}

    	BlockState blockState = null;
    	String blockNameCorrected = input.trim().toLowerCase();
    	// Try parsing as legacy block name / id
    	if(!blockNameCorrected.contains(":"))
    	{
    		blockState = LegacyMaterials.fromLegacyBlockName(blockNameCorrected);
			if(blockState != null)
			{
				return ofMinecraftBlockState(blockState, input);
			}
	    	try
	    	{
	    		int blockId = Integer.parseInt(blockNameCorrected);
	    		String fromLegacyIdName = LegacyMaterials.blockNameFromLegacyBlockId(blockId);
	    		if(fromLegacyIdName != null)
	    		{
	    			blockNameCorrected = fromLegacyIdName;
	    		}
	    	} catch(NumberFormatException ex) { }	    		    
    	}
    	
    	// Try blockname[blockdata] / minecraft:blockname[blockdata] syntax
    	
    	// Use mc /setblock command logic to parse block string for us <3
		BlockStateArgument blockStateArgument = new BlockStateArgument();
		BlockStateInput parseResult = null;
		try {
			String newInput = blockNameCorrected.contains(":") ? blockNameCorrected : "minecraft:" + blockNameCorrected;
			parseResult = blockStateArgument.parse(new StringReader(newInput));
		} catch (CommandSyntaxException e) { }		
		if(parseResult != null)
		{
			// For leaves, add DISTANCE 1 to make them not decay.
			if(parseResult.getState().getMaterial().equals(Material.LEAVES))
			{
				return new ForgeMaterialData(parseResult.getState().with(LeavesBlock.DISTANCE, 1), input);	
			}
			return new ForgeMaterialData(parseResult.getState(), input);
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
	    	} catch(NumberFormatException ex) { }	    	
	    	try
	    	{
	    		int data = Integer.parseInt(blockNameCorrected.substring(blockNameCorrected.indexOf(":") + 1));
	    		blockState = LegacyMaterials.fromLegacyBlockNameOrIdWithData(blockNameOrId, data);
				if(blockState != null)
				{
					return ofMinecraftBlockState(blockState, input);
				}
				// Failed to parse data, remove. fe STONE:0 or STONE:1 -> STONE
				blockNameCorrected = blockNameCorrected.substring(0, blockNameCorrected.indexOf(":"));				
	    	} catch(NumberFormatException ex) { }	    	
    	}

		// Try without data
		Block block = null;
    	try
    	{
    		// This returns AIR if block is not found ><.
    		block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockNameCorrected));
        	if(block != null && (block != Blocks.AIR || blockNameCorrected.toLowerCase().endsWith("air")))
        	{
    			// For leaves, add DISTANCE 1 to make them not decay.
    			if(block.getDefaultState().getMaterial().equals(Material.LEAVES))
    			{
    				return new ForgeMaterialData(block.getDefaultState().with(LeavesBlock.DISTANCE, 1), input);	
    			}
        		return ofMinecraftBlock(block, input);
        	}
    	} catch(net.minecraft.util.ResourceLocationException ex) { }

    	// Try legacy name again, without data.
    	blockState = LegacyMaterials.fromLegacyBlockName(blockNameCorrected);
		if(blockState != null)
		{
			return ofMinecraftBlockState(blockState, input);
		}
    	
		OTG.log(LogMarker.INFO, "Could not parse block: " + input + ", substituting AIR.");
		
		return ofMinecraftBlock(Blocks.AIR, input);
    }
        
    /**
     * Gets a {@code BukkitMaterialData} of the given Minecraft block. The
     * default block data (usually 0) will be used.
     * @param block The material.
     * @return The {@code BukkitMateialData} instance.
     */
    public static ForgeMaterialData ofMinecraftBlock(Block block, String raw)
    {
        return ofMinecraftBlockState(block.getDefaultState(), raw);
    }

    /**
     * Gets a {@code ForgeMaterialData} of the given Minecraft blockData.
     * @param blockData The material an data.
     * @return The {@code BukkitMateialData} instance.
     */
    public static ForgeMaterialData ofMinecraftBlockState(BlockState blockData)
    {
        return new ForgeMaterialData(blockData, null);
    }
    
    /**
     * Gets a {@code ForgeMaterialData} of the given Minecraft blockData.
     * @param blockData The material an data.
     * @return The {@code BukkitMateialData} instance.
     */
    public static ForgeMaterialData ofMinecraftBlockState(BlockState blockData, String raw)
    {
        return new ForgeMaterialData(blockData, raw);
    }   

    @Override
    public LocalMaterialData withDefaultBlockData()
    {
    	if(this.blockData == null)
    	{
    		return this;
    	}
        Block block = this.blockData.getBlock();
        return ofMinecraftBlock(block, this.rawEntry);
    }
    
    @Override
    public String getName()
    {
    	if(isBlank)
    	{
    		return "BLANK";
    	}
    	else if(this.blockData == null)
    	{
    		if(this.rawEntry != null)
    		{
    			return this.rawEntry;
    		} else {
    			return "Unknown";
    		}
    	} else {
	        Block block = this.blockData.getBlock();	
	        //byte data = getBlockData();
	        //boolean noData = this.blockData.getPropertyKeys().isEmpty();
	        // Note that the above line is not equivalent to data != 0, as for
	        // example pumpkins have a default data value of 2
	
	        //boolean nonDefaultData = !block.getDefaultState().equals(this.blockData);
	        
            // Use Minecraft's name
            //if (nonDefaultData)
            //{
            	//return Block.REGISTRY.getNameForObject(block) + (noData ? "" : ":" + data);
            //} else {
            	return block.getRegistryName().toString();
            //}
    	}
    }
    
    public BlockState internalBlock()
    {
        return this.blockData;
    }

    @Override
    public boolean isMaterial(LocalMaterialData material)
    {
    	// TODO: Compare registry names?
        return 
    		(this.isBlank && ((ForgeMaterialData)material).isBlank) || 
        	(
    			!this.isBlank && 
    			!((ForgeMaterialData)material).isBlank &&
    			this.blockData.getBlock().equals(((ForgeMaterialData)material).internalBlock().getBlock())
			)
    	;
    }
    
    @Override
    public boolean isLiquid()
    {
        return this.blockData == null ? false : this.blockData.getMaterial().isLiquid();
    }

    @Override
    public boolean isSolid()
    {   	
        return this.blockData == null ? false : this.blockData.getMaterial().isSolid();
    }
    
    @Override
    public boolean isEmptyOrAir()
    {
        return this.blockData == null ? true : this.blockData.getBlock() == Blocks.AIR;
    }
    
    @Override
    public boolean isAir()
    {
        return this.blockData != null && this.blockData.getBlock() == Blocks.AIR;
    }
    
    @Override
    public boolean isEmpty()
    {
        return this.blockData == null;
    }

    @Override
    public boolean canFall()
    {
        return this.blockData == null ? false : this.blockData.getBlock() instanceof FallingBlock;
    }

    @Override
    public boolean canSnowFallOn()
    {
        return this.blockData == null ? false : this.blockData.getMaterial().isSolid();    	
    }

    /*
	@Override
	public LocalMaterialData parseForWorld(WorldConfig worldConfig)
	{
        if (!this.checkedFallbacks && this.isEmpty() && this.rawEntry != null)
		{
			this.checkedFallbacks = true;
			ForgeMaterialData newMaterialData = ((ForgeMaterialData)worldConfig.parseFallback(this.rawEntry)); 
			if(newMaterialData != null && newMaterialData.blockData != null)
			{
				// TODO: Should blockData be a clone?
				this.blockData = newMaterialData.blockData;
				this.rawEntry = newMaterialData.rawEntry;
			}
		}
		return this;
	}
	*/
	
    @Override
    public boolean hasData()
    {
    	// TODO: Implement this for 1.16    	
    	return false;
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
        
    	// TODO: Compare registry names?
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
