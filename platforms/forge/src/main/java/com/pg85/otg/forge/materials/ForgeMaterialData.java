package com.pg85.otg.forge.materials;

import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.forge.blocks.portal.BlockPortalOTG;
import com.pg85.otg.util.minecraft.defaults.DefaultMaterial;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

/**
 * Implementation of LocalMaterial that wraps one of Minecraft's Blocks.
 *
 */
public class ForgeMaterialData extends LocalMaterialData
{
	private IBlockState blockData;
	private boolean metaIdSet = false;
	private byte metaId;
	private boolean materialIdSet = false;
	private int materialId;

    private ForgeMaterialData(IBlockState blockData, int blockId, int blockMetaData)
    {
        this.blockData = blockData;
        this.materialIdSet = true;
        this.materialId = blockId;
        this.metaIdSet = true;
        this.metaId = (byte)blockMetaData;
    }
    
    private ForgeMaterialData(IBlockState blockData, String raw)
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
    	ForgeMaterialData material = new ForgeMaterialData((IBlockState)null, null);
    	material.isBlank = true;
    	return material;
    }

    public static ForgeMaterialData ofString(String input) throws InvalidConfigException
    {
        // Try parsing as an internal Minecraft name
        // This is so that things like "minecraft:stone" aren't parsed
        // as the block "minecraft" with data "stone", but instead as the
        // block "minecraft:stone" with no block data.

    	// Used in BO4's as placeholder/detector block.
    	if(input.toLowerCase().equals("blank"))
    	{
    		return ForgeMaterialData.getBlank();
    	}

    	String newInput = input;

        net.minecraft.block.Block block = net.minecraft.block.Block.getBlockFromName(newInput);
        if (block != null)
        {
        	// Some old apps exported schematics/bo3's exported "STAIRS" without metadata (for instance "STAIRS:0").
        	// However, the default rotation has changed so fix this by adding the correct metadata.

        	if(
    			block == Blocks.PORTAL ||
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
            	blockName = input;
            }
        }

        // Parse block name
        Block block = Block.getBlockFromName(blockName);
        if (block == null)
        {
            DefaultMaterial defaultMaterial = DefaultMaterial.getMaterial(blockName);
            if (defaultMaterial != null)
            {
                block = Block.getBlockById(defaultMaterial.id);

            	// Some old apps exported schematics/bo3's exported "STAIRS" without metadata (for instance "STAIRS:0").
            	// However, the default rotation has changed so fix this by adding the correct metadata.

                // TODO: Check if the block uses the Facing property instead of checking a list of known blocks?
            	if(
        			blockData == -1 &&
        			(
    					block == Blocks.PORTAL ||
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
        		)
            	{
            		blockData = 0; // TODO: Shouldn't this be 3? This appears to fix the problem for the dungeon dimension but I still see it in BB, double check?
            	}
            }
        }

        // Get the block
        if (block != null)
        {
            if (blockData == -1)
            {
                // Use default
                return ForgeMaterialData.ofMinecraftBlock(block, input);
            } else {
                // Use specified data
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
        Block block = Block.getBlockById(id);
        IBlockState blockData = block.getStateFromMeta(data);
        return new ForgeMaterialData(blockData, id, data);
    }

    /**
     * Gets a {@code ForgeMaterialData} of the given material and data.
     * @param material The material.
     * @param data     The block data.
     * @return The {@code ForgeMaterialData} instance.
     */
    public static ForgeMaterialData ofDefaultMaterial(DefaultMaterial material, int data)
    {
        return ofIds(material.id, data);
    }

    @Override
    protected ForgeMaterialData ofDefaultMaterialPrivate(DefaultMaterial material, int data)
    {
        return ofDefaultMaterial(material, data);
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
    public static ForgeMaterialData ofMinecraftBlockState(IBlockState blockData)
    {
        return new ForgeMaterialData(blockData, null);
    }
    
    /**
     * Gets a {@code ForgeMaterialData} of the given Minecraft blockData.
     * @param blockData The material an data.
     * @return The {@code BukkitMateialData} instance.
     */
    public static ForgeMaterialData ofMinecraftBlockState(IBlockState blockData, String raw)
    {
        return new ForgeMaterialData(blockData, raw);
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public LocalMaterialData withBlockData(int i)
    {
    	if(this.blockData == null)
    	{
    		return this;
    	}
        if (i == getBlockData())
        {
            return this;
        }

        Block block = this.blockData.getBlock();
        return ofMinecraftBlockState(block.getStateFromMeta(i), this.rawEntry);
    }

    @Override
    public LocalMaterialData withDefaultBlockData()
    {
    	if(this.blockData == null)
    	{
    		return this;
    	}
        Block block = this.blockData.getBlock();
        return this.withBlockData(block.getMetaFromState(block.getDefaultState()));
    }
    
    @Override
    public byte getBlockData()
    {
    	if(!this.metaIdSet)
    	{
    		this.metaIdSet = true;
    		this.metaId = this.blockData == null ? 0 : (byte) this.blockData.getBlock().getMetaFromState(this.blockData);
    	}
        return this.metaId;
    }

    @Override
    public int getBlockId()
    {
    	if(!this.materialIdSet)
    	{
    		this.materialIdSet = true;
    		this.materialId = this.blockData == null ? 0 : Block.getIdFromBlock(this.blockData.getBlock());
    	}
        return this.materialId;
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
	        byte data = getBlockData();
	        boolean noData = this.blockData.getPropertyKeys().isEmpty();
	        // Note that the above line is not equivalent to data != 0, as for
	        // example pumpkins have a default data value of 2
	
	        DefaultMaterial defaultMaterial = toDefaultMaterial();
	        if (defaultMaterial == null)
	        {
		        boolean nonDefaultData = !block.getDefaultState().equals(this.blockData);
		        
	            // Use Minecraft's name
	            if (nonDefaultData)
	            {
	            	return Block.REGISTRY.getNameForObject(block) + (noData ? "" : ":" + data);
	            } else {
	            	return Block.REGISTRY.getNameForObject(block).toString();
	            }
	        } else {
	            // Use our name
	        	return defaultMaterial.name() + (noData ? "" : ":" + data);
	        }
    	}
    }
    
    public IBlockState internalBlock()
    {
        return this.blockData;
    }

    @Override
    public boolean isMaterial(DefaultMaterial material)
    {
        return material.id == getBlockId();
    }
    
    @Override
    public boolean isLiquid()
    {
    	// For some reason, .isLiquid() appears to be 
    	// really slow, so use defaultMaterial instead.
    	
        // Let us override whether materials are solid
        DefaultMaterial defaultMaterial = toDefaultMaterial();
        if (defaultMaterial != null)
        {
            return defaultMaterial.isLiquid();
        }
        
        return this.blockData == null ? false : this.blockData.getMaterial().isLiquid();
    }

    @Override
    public boolean isSolid()
    {   	
        // Let us override whether materials are solid
        DefaultMaterial defaultMaterial = toDefaultMaterial();
        if (defaultMaterial != null)
        {
            return defaultMaterial.isSolid();
        }

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
        return this.blockData == null ? false : this.blockData.getBlock() instanceof BlockFalling;
    }

    @Override
    public boolean canSnowFallOn()
    {
        DefaultMaterial defaultMaterial = toDefaultMaterial();
        if (defaultMaterial != null)
        {
            return defaultMaterial.canSnowFallOn();
        }

        return this.blockData == null ? false : this.blockData.getMaterial().isSolid();    	
    }     

	@Override
	public LocalMaterialData parseForWorld(LocalWorld world)
	{
		if (!this.checkedFallbacks && this.blockData == null && this.rawEntry != null)
		{
			this.checkedFallbacks = true;
			ForgeMaterialData newMaterialData = ((ForgeMaterialData)world.getConfigs().getWorldConfig().parseFallback(this.rawEntry)); 
			if(newMaterialData != null && newMaterialData.blockData != null)
			{
				this.blockData = newMaterialData.blockData;
				this.metaIdSet = newMaterialData.metaIdSet;
				this.materialIdSet = newMaterialData.materialIdSet;
				this.rawEntry = newMaterialData.rawEntry;
				this.defaultMaterial = newMaterialData.defaultMaterial;
			}
		}
		return this;
	}

    @Override
    public DefaultMaterial toDefaultMaterial()
    {
    	if(this.defaultMaterial == null && !parsedDefaultMaterial)
    	{
    		parsedDefaultMaterial = true;
        	if(this.blockData != null)
        	{
        		this.defaultMaterial = DefaultMaterial.getMaterial(getBlockId());
        		if(this.defaultMaterial == null)
        		{
            		if(this.blockData.getBlock() instanceof BlockPortalOTG) // TODO: avoid using instanceof so much?
            		{
        				this.defaultMaterial = DefaultMaterial.PORTAL;
            		}
        		}
        	}
    	}
    	return defaultMaterial;
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
    
    /**
     * Gets the hashCode of the material, based on the block id and block data.
     * The hashCode must be unique, which is possible considering that there are
     * only 4096 * 16 possible materials.
     * 
     * @return The unique hashCode.
     */
    public int hashCode()
    {
        // From 4096 to 69632 when there are 4096 block ids
        return PluginStandardValues.SUPPORTED_BLOCK_IDS + (getBlockId() * 16) + getBlockData();
    }    
}
