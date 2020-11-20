package com.pg85.otg.util.materials;

import com.pg85.otg.util.biome.ReplacedBlocksMatrix;

// TODO: Make this class unmodifiable (parseForWorld modifies atm),
// implement a world-specific materials cache and ensure only one
// instance of each unique material (id+metadata) exists in memory.
// TODO: Do creation of new material instances in one place only?

/**
 * Represents one of Minecraft's materials. Also includes its data value.
 * Immutable.
 * 
 * @see OTGEngine#readMaterial(String)
 * @see OTGEngine#toLocalMaterialData(DefaultMaterial, int)
 */
public abstract class LocalMaterialData
{
	protected String rawEntry;
	protected boolean isBlank = false;
	protected boolean checkedFallbacks = false;
	protected boolean parsedDefaultMaterial = false;
	   
    /**
     * Gets the name of this material. If a {@link #toDefaultMaterial()
     * DefaultMaterial is available,} that name is used, otherwise it's up to
     * the mod that provided this block to name it. Block data is appended to
     * the name, separated with a colon, like "WOOL:2".
     * 
     * @return The name of this material.
     */
	public abstract String getName();
	
    /**
     * Gets whether this material is a liquid, like water or lava.
     * 
     * @return True if this material is a liquid, false otherwise.
     */
    public abstract boolean isLiquid();

    /**
     * Gets whether this material is solid. If there is a
     * {@link #toDefaultMaterial() DefaultMaterial available}, this property is
     * defined by {@link DefaultMaterial#isSolid()}. Otherwise, it's up to the
     * mod that provided this block to say whether it's solid or not.
     * 
     * @return True if this material is solid, false otherwise.
     */
    public abstract boolean isSolid();

    /**
     * Gets whether this material is air. This is functionally equivalent to
     * {@code isMaterial(DefaultMaterial.AIR)}, but may yield better
     * performance.
     * @return True if this material is air, false otherwise.
     */
    public abstract boolean isEmptyOrAir();
    
    public abstract boolean isAir();

    public abstract boolean isEmpty();
    
    /**
     * Gets whether snow can fall on this block.
     * 
     * @return True if snow can fall on this block, false otherwise.
     */
    public abstract boolean canSnowFallOn();

    public abstract boolean isMaterial(LocalMaterialData material);
    
    public boolean isLogOrLeaves()
    {
		return isLog() || isLeaves();
    }
    
    public boolean isLog()
    {
		return
			isMaterial(LocalMaterials.ACACIA_LOG) ||
			isMaterial(LocalMaterials.BIRCH_LOG) ||
			isMaterial(LocalMaterials.DARK_OAK_LOG) ||
			isMaterial(LocalMaterials.OAK_LOG) ||
			isMaterial(LocalMaterials.SPRUCE_LOG) ||
			isMaterial(LocalMaterials.STRIPPED_ACACIA_LOG) ||
			isMaterial(LocalMaterials.STRIPPED_BIRCH_LOG) ||
			isMaterial(LocalMaterials.STRIPPED_DARK_OAK_LOG) ||
			isMaterial(LocalMaterials.STRIPPED_JUNGLE_LOG) ||
			isMaterial(LocalMaterials.STRIPPED_OAK_LOG) ||
			isMaterial(LocalMaterials.STRIPPED_SPRUCE_LOG);
	}
    
    public boolean isLeaves()
    {
		return
			isMaterial(LocalMaterials.ACACIA_LEAVES) ||
			isMaterial(LocalMaterials.BIRCH_LEAVES) ||
			isMaterial(LocalMaterials.DARK_OAK_LEAVES) ||
			isMaterial(LocalMaterials.JUNGLE_LEAVES) ||
			isMaterial(LocalMaterials.OAK_LEAVES) ||
			isMaterial(LocalMaterials.SPRUCE_LEAVES);
    }
    
    /**
     * Gets an instance with the same material as this object, but the default
     * block data of the material. This instance is not modified.
     *
     * @return An instance with the default block data.
     */
    public abstract LocalMaterialData withDefaultBlockData();

    /**
     * Gets whether this material equals another material. The block data is
     * taken into account.
     * 
     * @param other
     *            The other material.
     * @return True if the materials are equal, false otherwise.
     */
    public abstract boolean equals(Object other);
    
    /**
     * Gets the hashCode of the material, based on the block id and block data.
     * The hashCode must be unique, which is possible considering that there are
     * only 4096 * 16 possible materials.
     * 
     * @return The unique hashCode.
     */
    public abstract int hashCode();
    
    public String toString()
    {
    	return getName();
    }   

    /**
     * Gets a new material that is rotated 90 degrees. North -> west -> south ->
     * east. If this material cannot be rotated, the material itself is
     * returned.
     * 
     * @return The rotated material.
     */
    public LocalMaterialData rotate()
    {
    	return rotate(1);
    }
    
    /**
     * Gets a new material that is rotated 90 degrees. North -> west -> south ->
     * east. If this material cannot be rotated, the material itself is
     * returned.
     * 
     * @return The rotated material.
     */
    public LocalMaterialData rotate(int rotateTimes)
    {
    	// TODO: Rotate modded blocks?
    	
    	// TODO: Reimplement this when block data has been implemented
    	/*    	
        // Try to rotate
        DefaultMaterial defaultMaterial = toDefaultMaterial();
        if (defaultMaterial != null)
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
            	return ofDefaultMaterialPrivate(defaultMaterial, newData);
            }
        }
        */

        // No changes, return object itself
        return this;
    }

    /**
     * Parses this material through the fallback system of the world if required.
     * 
     * @param world The world this material will be parsed through, each world may have different fallbacks.
     * @return The parsed material
     */
    //public abstract LocalMaterialData parseForWorld(WorldConfig worldConfig);
    
	public LocalMaterialData parseWithBiomeAndHeight(boolean biomeConfigsHaveReplacement, ReplacedBlocksMatrix replaceBlocks, int y)
	{	
        if (!biomeConfigsHaveReplacement)
        {
            // Don't waste time here, ReplacedBlocks is empty everywhere
            return this;
        }
        return replaceBlocks.replaceBlock(y, this);
	}

    /**
     * Gets whether this material falls down when no other block supports this
     * block, like gravel and sand do.
     * @return True if this material can fall, false otherwise.
     */
    public abstract boolean canFall();
    
    /**
     * Gets whether this material can be used as an anchor point for a smooth area    
     * 
     * @return True if this material is a solid block, false if it is a tile-entity, half-slab, stairs(?), water, wood or leaves
     */    
    public boolean isSmoothAreaAnchor(boolean allowWood, boolean ignoreWater)
    {
    	return
			(
				isSolid() || 
				(
					!ignoreWater && isLiquid()
				)
			) || (
	    		(
					isMaterial(LocalMaterials.ICE) ||
					isMaterial(LocalMaterials.PACKED_ICE) ||
					isMaterial(LocalMaterials.FROSTED_ICE) ||
					(
						isSolid() || 
						(
							!ignoreWater && isLiquid()
						)
					)
				) && (
					allowWood || 
					!(
						isLog()
					)
				) &&
				!isMaterial(LocalMaterials.WATER_LILY)
			);
    }
    
	public boolean isOre()
	{
    	return
			isMaterial(LocalMaterials.COAL_ORE) ||
			isMaterial(LocalMaterials.DIAMOND_ORE) ||
			isMaterial(LocalMaterials.EMERALD_ORE) ||
			isMaterial(LocalMaterials.GLOWING_REDSTONE_ORE) ||
			isMaterial(LocalMaterials.GOLD_ORE) ||
			isMaterial(LocalMaterials.IRON_ORE) ||
			isMaterial(LocalMaterials.LAPIS_ORE) ||
			isMaterial(LocalMaterials.QUARTZ_ORE) ||
			isMaterial(LocalMaterials.REDSTONE_ORE)
		;
	}    

	public abstract boolean hasData();
}
