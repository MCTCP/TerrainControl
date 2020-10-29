package com.pg85.otg.forge.materials;

import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.common.materials.LocalMaterialData;
import com.pg85.otg.exception.InvalidConfigException;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FallingBlock;
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
    	if(input == null)
    	{
    		return null;
    	}

    	// Used in BO4's as placeholder/detector block.
    	if(input.toLowerCase().equals("blank"))
    	{
    		return ForgeMaterialData.getBlank();
    	}
    	
        // Try parsing as an internal Minecraft name
        // This is so that things like "minecraft:stone" aren't parsed
        // as the block "minecraft" with data "stone", but instead as the
        // block "minecraft:stone" with no block data.
    	
    	String newInput = input;

    	net.minecraft.block.Block block = null;
    	try
    	{    
    		block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(newInput.toLowerCase().trim()));
    	} catch(net.minecraft.util.ResourceLocationException ex) {
    		String breakpoint = "";
    	}
        if (block != null)
        {
        	// Some old apps exported schematics/bo3's exported "STAIRS" without metadata (for instance "STAIRS:0").
        	// However, the default rotation has changed so fix this by adding the correct metadata.

        	if(
    			block == Blocks.NETHER_PORTAL ||
				block == Blocks.DISPENSER ||
    			block == Blocks.ACACIA_STAIRS ||
        		block == Blocks.BIRCH_STAIRS ||
        		block == Blocks.BRICK_STAIRS ||
        		block == Blocks.DARK_OAK_STAIRS ||
        		block == Blocks.JUNGLE_STAIRS ||
        		block == Blocks.NETHER_BRICK_STAIRS ||
        		block == Blocks.OAK_STAIRS ||
        		block == Blocks.PURPUR_STAIRS ||
        		block == Blocks.QUARTZ_STAIRS ||
        		block == Blocks.RED_SANDSTONE_STAIRS ||
        		block == Blocks.SANDSTONE_STAIRS ||
        		block == Blocks.SPRUCE_STAIRS ||
        		block == Blocks.STONE_BRICK_STAIRS ||
        		block == Blocks.STONE_STAIRS
    		)
        	{
        		newInput = input + ":0"; // TODO: Shouldn't this be 3? This appears to fix the problem for the dungeon dimension but I still see it in BB, double check?
        	} else {
	            return ForgeMaterialData.ofMinecraftBlock(block, input);
        	}
        }

        // Try block(:data) syntax
        return getMaterial0(newInput);
    }

    private static ForgeMaterialData getMaterial0(String input) throws NumberFormatException, InvalidConfigException
    {
        String blockName = input;
        int blockData = -1;

        // When there is a . or a : in the name, extract block data
        int splitIndex = input.lastIndexOf(":");
        if (splitIndex == -1)
        {
            splitIndex = input.lastIndexOf(".");
        }
        if (splitIndex != -1)
        {
            blockName = input.substring(0, splitIndex);
            try
            {
            	blockData = Integer.parseInt(input.substring(splitIndex + 1));            
            }
            catch (NumberFormatException e)
            {
            	//blockName = input;
            }
        }

        // Parse block name
        Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockName.toLowerCase().trim()));

        // Get the block
        if (block != null)
        {
            //if (blockData == -1)
        	if(1 == 1) // TODO: Reimplement this
            {
                // Use default
                return ForgeMaterialData.ofMinecraftBlock(block, input);
            } else {
                // Use specified data
            	/*
                try
                {
                    return ForgeMaterialData.ofMinecraftBlockState(block.getStateFromMeta(blockData), input);
                }
                catch(java.lang.ArrayIndexOutOfBoundsException e)
                {
                	throw new InvalidConfigException("Illegal meta data for the block type, cannot use " + input);
                }
                catch (IllegalArgumentException e)
                {
                	throw new InvalidConfigException("Illegal block data for the block type, cannot use " + input);
                }
                */
            }
        }

        // Failed, try parsing later as a fallback.
        return new ForgeMaterialData(input);
    }
    
    /**
     * Gets a {@code ForgeMaterialData} of the given id and data.
     * @param id   The block id.
     * @param data The block data.
     * @return The {@code BukkitMateialData} instance.
     */
    @SuppressWarnings("deprecation")
    private static ForgeMaterialData ofIds(int id, int data)
    {
        Block block = Block.getStateById(id).getBlock();
        //BlockState blockData = block.getStateFromMeta(data);
        //return new ForgeMaterialData(blockData, id, data);
        return ForgeMaterialData.ofMinecraftBlock(block, block.getRegistryName().toString());
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
        //return this.withBlockData(block.getMetaFromState(block.getDefaultState()));
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
        return this.blockData.getBlock().equals(((ForgeMaterialData)material).internalBlock().getBlock());
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

	@Override
	public LocalMaterialData parseForWorld(LocalWorld world)
	{
        if (!this.checkedFallbacks && this.isEmpty() && this.rawEntry != null)
		{
			this.checkedFallbacks = true;
			ForgeMaterialData newMaterialData = ((ForgeMaterialData)world.getConfigs().getWorldConfig().parseFallback(this.rawEntry)); 
			if(newMaterialData != null && newMaterialData.blockData != null)
			{
				// TODO: Should blockData be a clone?
				this.blockData = newMaterialData.blockData;
				this.rawEntry = newMaterialData.rawEntry;
			}
		}
		return this;
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
        return this.blockData.equals(other.blockData);
    }
}
