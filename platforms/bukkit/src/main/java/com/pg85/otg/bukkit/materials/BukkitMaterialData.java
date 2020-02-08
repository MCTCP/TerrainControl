package com.pg85.otg.bukkit.materials;

import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.util.helpers.BlockHelper;
import com.pg85.otg.util.materials.MaterialHelper;
import com.pg85.otg.util.minecraft.defaults.DefaultMaterial;

import net.minecraft.server.v1_12_R1.Block;
import net.minecraft.server.v1_12_R1.BlockFalling;
import net.minecraft.server.v1_12_R1.Blocks;
import net.minecraft.server.v1_12_R1.IBlockData;

//TODO: Clean up and optimise ForgeMaterialData/BukkitMaterialData/LocalMaterialData/MaterialHelper/OTGEngine.readMaterial
/**
 * Implementation of LocalMaterial that wraps one of Minecraft's Blocks.
 * 
 */
public class BukkitMaterialData implements LocalMaterialData
{
    /**
     * Block id and data, calculated as {@code blockId << 4 | blockData}, or
     * without binary operators: {@code blockId * 16 + blockData}.
     *
     * <p>Note that Minecraft's Block.getCombinedId uses another format (at
     * least in Minecraft 1.8). However, Minecraft's ChunkSection uses the same
     * format as this field.
     */	
	DefaultMaterial defaultMaterial;
	private int combinedBlockId;
	private String rawEntry;
	private boolean isBlank = false;
	private boolean checkFallbacks;
	private String name;
	
    private BukkitMaterialData(int blockId, int blockData)
    {
        this.combinedBlockId = blockId << 4 | blockData;
    }

    public BukkitMaterialData(String input)
    {
		this.combinedBlockId = -1;
		this.rawEntry = input;
		this.checkFallbacks = true;
	}
	      
    public static BukkitMaterialData getBlank()
    {
    	BukkitMaterialData material = new BukkitMaterialData(null);
    	material.isBlank = true;
    	material.checkFallbacks = false;
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
	            return BukkitMaterialData.ofMinecraftBlock(block);
        	}
        }

        try
        {
            // Try block(:data) syntax
            return getMaterial0(newInput);
        } catch (NumberFormatException e)
        {
            throw new InvalidConfigException("Unknown material: " + input);
        }    
    }

    @SuppressWarnings("deprecation")
    private static BukkitMaterialData getMaterial0(String input) throws NumberFormatException, InvalidConfigException
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
            blockData = Integer.parseInt(input.substring(splitIndex + 1));
        }

        // Parse block name
        Block block = Block.getByName(blockName);
        if (block == null)
        {
            DefaultMaterial defaultMaterial = DefaultMaterial.getMaterial(blockName);
            if (defaultMaterial != DefaultMaterial.UNKNOWN_BLOCK)
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
                return BukkitMaterialData.ofMinecraftBlock(block);
            } else
            {               
                // Use specified data
                try
                {
                    //return ForgeMaterialData.ofMinecraftBlockState(block.getStateFromMeta(blockData));
                    return BukkitMaterialData.ofMinecraftBlockData(block.fromLegacyData(blockData));
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
        return new BukkitMaterialData(input);
    }

    /**
     * Gets a {@code BukkitMaterialData} of the given id and data.
     * @param id   The block id.
     * @param data The block data.
     * @return The {@code BukkitMateialData} instance.
     */
    public static BukkitMaterialData ofIds(int id, int data)
    {
        return new BukkitMaterialData(id, data);
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

    /**
     * Gets a {@code BukkitMaterialData} of the given Minecraft block. The
     * default block data (usually 0) will be used.
     * @param block The material.
     * @return The {@code BukkitMateialData} instance.
     */
    public static BukkitMaterialData ofMinecraftBlock(Block block)
    {
        return ofIds(Block.getId(block), block.toLegacyData(block.getBlockData()));
    }

    /**
     * Gets a {@code BukkitMaterialData} of the given Minecraft blockData.
     * @param blockData The material an data.
     * @return The {@code BukkitMateialData} instance.
     */
    public static BukkitMaterialData ofMinecraftBlockData(IBlockData blockData)
    {
        Block block = blockData.getBlock();
        return new BukkitMaterialData(Block.getId(block), block.toLegacyData(blockData));
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
    	if(this.name == null)
    	{
	    	if(isBlank)
	    	{
	    		this.name = "BLANK";
	    	}
	    	else if(this.combinedBlockId == -1)
	    	{
	    		this.name = "Unknown";
	    	} else { 	
		        Block block = Block.getById(getBlockId());
		        DefaultMaterial defaultMaterial = toDefaultMaterial();
		
		        byte data = getBlockData();
		        boolean nonDefaultData = block.toLegacyData(block.getBlockData()) != data;
		        boolean noData = block.getBlockData().s().isEmpty(); //s == getPropertyKeys
		        // Note that the above line is not equivalent to data != 0, as for
		        // example pumpkins have a default data value of 2
		
		        if (defaultMaterial == DefaultMaterial.UNKNOWN_BLOCK)
		        {
		            // Use Minecraft's name
		            if (nonDefaultData)
		            {
		            	this.name = Block.REGISTRY.b(block) + (noData ? "" : ":" + data);
		            }
		            this.name = Block.REGISTRY.b(block).toString();
		        } else {
		        	this.name = defaultMaterial.name() + (noData ? "" : ":" + data);
		        }
	    	}
    	}
    	return this.name;
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
        if (defaultMaterial != DefaultMaterial.UNKNOWN_BLOCK)
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
        if (defaultMaterial != DefaultMaterial.UNKNOWN_BLOCK)
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
        if (defaultMaterial != DefaultMaterial.UNKNOWN_BLOCK)
        {
            return defaultMaterial.canSnowFallOn();
        }

        return this.internalBlock().getMaterial().isSolid();
    }

    @Override
    public boolean isSmoothAreaAnchor(boolean allowWood, boolean ignoreWater)
    {
    	DefaultMaterial defaultMaterial = this.toDefaultMaterial();
    	return
    		(
				defaultMaterial.equals(DefaultMaterial.ICE) ||
				defaultMaterial.equals(DefaultMaterial.PACKED_ICE) ||
				defaultMaterial.equals(DefaultMaterial.FROSTED_ICE) ||
				(
					isSolid() || 
					(
						!ignoreWater && isLiquid()
					)
				)
			) &&
			(
				allowWood || 
				!(
					defaultMaterial.equals(DefaultMaterial.LOG) || 
					defaultMaterial.equals(DefaultMaterial.LOG_2)
				)
			) &&
			!defaultMaterial.equals(DefaultMaterial.WATER_LILY);
    }
	    
    @Override
    public LocalMaterialData rotate()
    {
        // Try to rotate
        DefaultMaterial defaultMaterial = toDefaultMaterial();
        if (defaultMaterial != DefaultMaterial.UNKNOWN_BLOCK)
        {
            // We only know how to rotate vanilla blocks
            byte blockDataByte = getBlockData();
            int newData = BlockHelper.rotateData(defaultMaterial, blockDataByte);
            if (newData != blockDataByte)
            {
            	// Don't return a copy, return a cached object. OTG should only use forgematerialdata for BO2's/BO3's/BO4's and materialset,
            	// and shouldn't need to edit them, so they can be re-used. TODO: Make sure this won't cause problems.
            	try
            	{
					return MaterialHelper.readMaterial(defaultMaterial.name() + ":" + newData);
				}
            	catch (InvalidConfigException e)
            	{
					e.printStackTrace();
					return null;
				}
                //return ofDefaultMaterial(defaultMaterial, newData);
            }
        }

        // No changes, return object itself
        return this;
    }
    
    @Override
    public LocalMaterialData rotate(int rotateTimes)
    {
        // Try to rotate
        DefaultMaterial defaultMaterial = toDefaultMaterial();
        if (defaultMaterial != DefaultMaterial.UNKNOWN_BLOCK)
        {
            // We only know how to rotate vanilla blocks
        	byte blockDataByte = 0;
            int newData = 0;
            for(int i = 0; i < rotateTimes; i++)
            {
            	blockDataByte = getBlockData();
            	newData = BlockHelper.rotateData(defaultMaterial, blockDataByte);	
            }
            if (newData != blockDataByte)
            {
            	return ofDefaultMaterial(defaultMaterial, newData);
            }
        }

        // No changes, return object itself
        return this;
    }
    
	@Override
	public LocalMaterialData parseForWorld(LocalWorld world)
	{
		if (this.checkFallbacks)
		{
			this.checkFallbacks = false;
			int newId = ((BukkitMaterialData)world.getConfigs().getWorldConfig().parseFallback(this.rawEntry)).combinedBlockId;
			if(newId != this.combinedBlockId)
			{
				this.combinedBlockId = newId;
				this.defaultMaterial = null;
				this.name = null;
			}
		}
		return this;
	}
	   
    @Override
    public DefaultMaterial toDefaultMaterial()
    {
    	if(this.defaultMaterial == null)
    	{
        	if(this.combinedBlockId == -1)
        	{
        		this.defaultMaterial = DefaultMaterial.UNKNOWN_BLOCK;
        	} else {
        		this.defaultMaterial = DefaultMaterial.getMaterial(getBlockId());
        	}
    	}
    	return defaultMaterial;
    }

	@Override
	public boolean isParsed()
	{
		return !checkFallbacks;
	}
	
    @Override
    public int hashCode()
    {
        // From 4096 to 69632 when there are 4096 block ids
        return PluginStandardValues.SUPPORTED_BLOCK_IDS + combinedBlockId;
    }

    @Override
    public int hashCodeWithoutBlockData()
    {
        // From 0 to 4095 when there are 4096 block ids
        return getBlockId();
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
        if (combinedBlockId != other.combinedBlockId)
        {
            return false;
        }
        return true;
    }
	
    @Override
    public String toString()
    {
        return getName();
    }
}
