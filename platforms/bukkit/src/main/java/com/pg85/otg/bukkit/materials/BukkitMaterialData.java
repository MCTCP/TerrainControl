package com.pg85.otg.bukkit.materials;

import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.util.minecraft.defaults.DefaultMaterial;

import net.minecraft.server.v1_12_R1.Block;
import net.minecraft.server.v1_12_R1.BlockFalling;
import net.minecraft.server.v1_12_R1.Blocks;
import net.minecraft.server.v1_12_R1.IBlockData;

//TODO: Make this class unmodifiable (parseForWorld modifies atm),
//implement a world-specific materials cache and ensure only one
//instance of each unique material (id+metadata) exists in memory.
//TODO: Do creation of new material instances in one place only?

/**
 * Implementation of LocalMaterial that wraps one of Minecraft's Blocks.
 * 
 */
public class BukkitMaterialData extends LocalMaterialData
{
    /**
     * Block id and data, calculated as {@code blockId << 4 | blockData}, or
     * without binary operators: {@code blockId * 16 + blockData}.
     *
     * <p>Note that Minecraft's Block.getCombinedId uses another format (at
     * least in Minecraft 1.8). However, Minecraft's ChunkSection uses the same
     * format as this field.
     */	
	private int combinedBlockId;
	// Used only for blocks read from settings, so we know whether to append data when writing
	// TODO: Clean up the constructors, hasData is only used for blocks parsed from configs,
	// so we can write them back properly, so may produce unexpected results when used differently.
	private boolean hasData; 
	
    private BukkitMaterialData(int blockId, int blockData, boolean hasData)
    {
        this.combinedBlockId = blockId << 4 | blockData;
        this.hasData = hasData;
    }

    public BukkitMaterialData(String input, boolean hasData)
    {
		this.combinedBlockId = -1;
		this.rawEntry = input;
		this.hasData = hasData;
	}

    public static BukkitMaterialData getBlank()
    {
    	BukkitMaterialData material = new BukkitMaterialData(null, false);
    	material.isBlank = true;
    	return material;
    }

	public static LocalMaterialData ofString(String input) throws InvalidConfigException
	{
        // Try parsing as an internal Minecraft name
        // This is so that things like "minecraft:stone" aren't parsed
        // as the block "minecraft" with data "stone", but instead as the
        // block "minecraft:stone" with no block data.
		
    	// Used in BO4's as placeholder/detector block.
    	if(input.toLowerCase().equals("blank"))
    	{
    		return BukkitMaterialData.getBlank();
    	}
    	
    	// Try blockname / minecraft:blockname syntax
    	String newInput = input;
		
        Block block = Block.getByName(newInput);
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
	            return BukkitMaterialData.ofMinecraftBlock(block, false);
        	}
        }

        // Try block(:data) syntax
        
        String blockName = newInput;
        int blockData = -1;

        // When there is a . or a : in the name, extract block data
        int splitIndex = newInput.lastIndexOf(":");
        if (splitIndex == -1)
        {
            splitIndex = newInput.lastIndexOf(".");
        }
        if (splitIndex != -1)
        {
            blockName = newInput.substring(0, splitIndex);
            try
            {
            	blockData = Integer.parseInt(newInput.substring(splitIndex + 1));            
            }
            catch (NumberFormatException e)
            {
            	blockName = newInput;
            }
        }

        // Try block name without data
        block = Block.getByName(blockName);
        if (block == null)
        {
            DefaultMaterial defaultMaterial = DefaultMaterial.getMaterial(blockName);
            if (defaultMaterial != null)
            {
                block = Block.getById(defaultMaterial.id);
                
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
                return BukkitMaterialData.ofMinecraftBlock(block, false);
            } else {           
                // Use specified data
                try
                {
                    return BukkitMaterialData.ofMinecraftBlockData(block.fromLegacyData(blockData));
                }
                catch(java.lang.ArrayIndexOutOfBoundsException e)
                {
                	throw new InvalidConfigException("Illegal meta data for the block type, cannot use " + newInput);
                }
                catch (IllegalArgumentException e)
                {
                	throw new InvalidConfigException("Illegal block data for the block type, cannot use " + newInput);
                }
            }
        }

        // Failed, try parsing later as a fallback.
        return new BukkitMaterialData(newInput, false); // TODO: Assuming all fallback blocks contain data atm.
    }

    /**
     * Gets a {@code BukkitMaterialData} of the given id and data.
     * @param id   The block id.
     * @param data The block data.
     * @return The {@code BukkitMateialData} instance.
     */
    public static BukkitMaterialData ofIds(int id, int data)
    {
        return new BukkitMaterialData(id, data, true);
    }
    
    /**
     * Gets a {@code BukkitMaterialData} of the given material and data.
     * @param material The material.
     * @param data     The block data.
     * @return The {@code BukkitMateialData} instance.
     */    
    public static BukkitMaterialData ofDefaultMaterial(DefaultMaterial material, int data)
    {
        return ofIds(material.id, data);
    }

    @Override
    protected BukkitMaterialData ofDefaultMaterialPrivate(DefaultMaterial material, int data)
    {
    	return ofDefaultMaterial(material, data);
    }
    
    /**
     * Gets a {@code BukkitMaterialData} of the given Minecraft block. The
     * default block data (usually 0) will be used.
     * @param block The material.
     * @return The {@code BukkitMateialData} instance.
     */
    public static BukkitMaterialData ofMinecraftBlock(Block block, boolean hasData)
    {
    	BukkitMaterialData material = ofIds(Block.getId(block), block.toLegacyData(block.getBlockData()));
    	material.hasData = hasData;
        return material;
    }

    public static BukkitMaterialData ofBukkitBlock(org.bukkit.block.Block block)
    {
        return ofIds(block.getType().getId(), block.getData());
    }
    
    /**
     * Gets a {@code BukkitMaterialData} of the given Minecraft blockData.
     * @param blockData The material an data.
     * @return The {@code BukkitMateialData} instance.
     */
    public static BukkitMaterialData ofMinecraftBlockData(IBlockData blockData)
    {
    	Block block = blockData.getBlock();
        return new BukkitMaterialData(Block.getId(block), block.toLegacyData(blockData), true);
    } 
    
    @SuppressWarnings("deprecation")
    @Override
    public LocalMaterialData withBlockData(int i)
    {
        if (i == getBlockData())
        {
            return this;
        }

        Block block = Block.getById(getBlockId());
        return ofMinecraftBlockData(block.fromLegacyData(i));
    }

    @Override
    public LocalMaterialData withDefaultBlockData()
    {
        Block block = Block.getById(getBlockId());
        byte defaultData = (byte) block.toLegacyData(block.getBlockData());
        return this.withBlockData(defaultData);
    }
    
    @Override
    public byte getBlockData()
    {
        return (byte) (combinedBlockId & 15);
    }

    @Override
    public int getBlockId()
    {
        return combinedBlockId >> 4;
    }
    
    @Override
    public String getName()
    {
    	if(isBlank)
    	{
    		return "BLANK";
    	}
    	else if(this.combinedBlockId == -1)
    	{
    		if(this.rawEntry != null)
    		{
    			return this.rawEntry;
    		} else {
    			return "Unknown";
    		}
    	} else { 	
	        Block block = Block.getById(getBlockId());	
	        byte data = getBlockData();
	        // Note that the above line is not equivalent to data != 0, as for
	        // example pumpkins have a default data value of 2
	
	        DefaultMaterial defaultMaterial = toDefaultMaterial();
	        if (defaultMaterial == null)
	        {
		        boolean nonDefaultData = block.toLegacyData(block.getBlockData()) != data;
		        
	            // Use Minecraft's name
	            if (nonDefaultData)
	            {
	            	return Block.REGISTRY.b(block) + (!this.hasData ? "" : ":" + data);
	            } else {
	            	return Block.REGISTRY.b(block).toString();
	            }
	        } else {
	        	return defaultMaterial.name() + (!this.hasData ? "" : ":" + data);
	        }
    	}
    }   
    
    @SuppressWarnings("deprecation")
	public IBlockData internalBlock()
    {
        return Block.getById(getBlockId()).fromLegacyData(getBlockData());
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
    	
        DefaultMaterial defaultMaterial = toDefaultMaterial();
        if (defaultMaterial != null)
        {
            return defaultMaterial.isLiquid();
        }
    	
        return this.internalBlock().getMaterial().isLiquid();
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

        return this.internalBlock().getMaterial().isSolid();
    }   
    
    @Override
    public boolean isEmptyOrAir()
    {
        return combinedBlockId == -1 || combinedBlockId == 0;
    }
    
    @Override
    public boolean isAir()
    {
        return combinedBlockId == 0;
    }
    
    @Override
    public boolean isEmpty()
    {
        return combinedBlockId == -1;
    }
    
	@Override
    public boolean canFall()
    {
        return Block.getById(getBlockId()) instanceof BlockFalling;
    }
    
	@Override
    public boolean canSnowFallOn()
    {
        DefaultMaterial defaultMaterial = toDefaultMaterial();
        if (defaultMaterial != null)
        {
            return defaultMaterial.canSnowFallOn();
        }

        return this.internalBlock().getMaterial().isSolid();
    }	    
        
    // TODO: Caching result means fallbacks will only work for one world, fix this!
    // TODO: Not returning a copy means that any block parsed is modified, this may
    // unintentionally include things like MaterialHelper blocks. 
    // Redesign this, or at least make sure this doesn't cause problems.
    // TODO: This is only applied for settings using a materialset and (non-bo) resources atm,
    // fix this for bo's and any other material settings (do all use materialset?), will
    // need a world-specific materials cache for readMaterial, should probably just parse
    // materials immediately when reading settings, so pass world info to configs loading 
    // code?
	@Override
	public LocalMaterialData parseForWorld(LocalWorld world)
	{
        if (!this.checkedFallbacks && this.isEmpty() && this.rawEntry != null)
		{
			this.checkedFallbacks = true;
			int newId = ((BukkitMaterialData)world.getConfigs().getWorldConfig().parseFallback(this.rawEntry)).combinedBlockId;
			if(newId != this.combinedBlockId)
			{
				this.combinedBlockId = newId;
				this.defaultMaterial = null;
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
        	if(this.combinedBlockId == -1)
        	{
        		this.defaultMaterial = null;
        	} else {
        		this.defaultMaterial = DefaultMaterial.getMaterial(getBlockId());
        	}
    	}
    	return defaultMaterial;
    }
	
    @Override
    public int hashCode()
    {
        // From 4096 to 69632 when there are 4096 block ids
        return PluginStandardValues.SUPPORTED_BLOCK_IDS + combinedBlockId;
    }
	
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!(obj instanceof BukkitMaterialData))
        {
            return false;
        }      
        BukkitMaterialData other = (BukkitMaterialData) obj;
        if(this.isBlank != other.isBlank)
        {
        	return false;
        }
        else if(this.isBlank)
        {
        	return true;
        }        
        if (combinedBlockId != other.combinedBlockId)
        {
            return false;
        }
        return true;
    }

	@Override
	public boolean hasData()
	{
		return this.hasData;
	}
}
