package com.pg85.otg.util.materials;

import com.pg85.otg.util.biome.ReplaceBlockMatrix;

/**
 * Represents one of Minecraft's materials.
 * Immutable.
 */
public abstract class LocalMaterialData extends LocalMaterialBase
{
	protected String rawEntry;
	protected boolean isBlank = false;
	protected boolean parsedDefaultMaterial = false;
	protected LocalMaterialData[] rotations = new LocalMaterialData[] {this, null, null, null};
	protected LocalMaterialData rotated = null;

	public abstract <T extends Comparable<T>> LocalMaterialData withProperty(MaterialProperty<T> state, T value);
	
	public abstract String getName();
	
	public abstract String getRegistryName();
	
	public abstract boolean canSnowFallOn();

	public abstract boolean canFall();

	public boolean isBlank()
	{
		return isBlank;
	}

	public abstract boolean isMaterial(LocalMaterialData material);
	
	public abstract boolean isBlockTag(LocalMaterialTag tag);
	
	public abstract boolean isLiquid();

	public abstract boolean isSolid();

	public abstract boolean isEmptyOrAir();
	
	public abstract boolean isNonCaveAir();	
	
	public abstract boolean isAir();

	public abstract boolean isEmpty();
		
	public boolean isLogOrLeaves()
	{
		return this.isLog() || this.isLeaves();
	}
	
	public boolean isDefaultState()
	{
		// TODO: Make this prettier?
		return this.getName().equals(getRegistryName());
	}

	@Override
	public boolean matches(LocalMaterialData material)
	{
		return this.isDefaultState() || material.isDefaultState() ? material.getRegistryName().equals(this.getRegistryName()) : material.hashCode() == this.hashCode();
	}

	/**
	 * Gets whether this material can be used as an anchor point for a smoothing area	
	 * @return True if this material is a solid block, false if it is a tile-entity, half-slab, stairs(?), water, wood or leaves.
	 */
	public boolean isSmoothAreaAnchor(boolean allowWood, boolean ignoreWater)
	{
		return
			(
				(
					// Any materials that are either solid or liquid
					(
						isSolid() &&  
						(!isLog() || allowWood)											
					) || (
						!ignoreWater && isLiquid()
					)
				) || (
					// Whitelist
					isMaterial(LocalMaterials.ICE) ||
					isMaterial(LocalMaterials.PACKED_ICE) ||
					isMaterial(LocalMaterials.FROSTED_ICE) 
				)
			) && (
				// Blacklist
				!isLeaves() &&
				!isMaterial(LocalMaterials.WATER_LILY)
			);
	}
	
	private boolean isOreSet;
	private boolean isOre;
	public boolean isOre()
	{
		// TODO: Use blocktags for this, to pick up all ores?
		if(this.isOreSet)
		{
			return this.isOre;
		}
		this.isOre =
			isMaterial(LocalMaterials.COAL_ORE) ||
			isMaterial(LocalMaterials.DIAMOND_ORE) ||
			isMaterial(LocalMaterials.EMERALD_ORE) ||
			isMaterial(LocalMaterials.GOLD_ORE) ||
			isMaterial(LocalMaterials.IRON_ORE) ||
			isMaterial(LocalMaterials.LAPIS_ORE) ||
			isMaterial(LocalMaterials.QUARTZ_ORE) ||
			isMaterial(LocalMaterials.REDSTONE_ORE)
		;
		this.isOreSet = true;
		return this.isOre;
	}

	private boolean isLeavesSet;
	private boolean isLeaves;
	public boolean isLeaves()
	{
		// TODO: Use blocktags for this, to pick up all leaves?
		if(this.isLeavesSet)
		{
			return this.isLeaves;
		}
		this.isLeaves =
			isMaterial(LocalMaterials.ACACIA_LEAVES) ||
			isMaterial(LocalMaterials.BIRCH_LEAVES) ||
			isMaterial(LocalMaterials.DARK_OAK_LEAVES) ||
			isMaterial(LocalMaterials.JUNGLE_LEAVES) ||
			isMaterial(LocalMaterials.OAK_LEAVES) ||
			isMaterial(LocalMaterials.SPRUCE_LEAVES)
		;
		this.isLeavesSet = true;
		return this.isLeaves;
	}

	private boolean isLogSet;
	private boolean isLog;	
	public boolean isLog()
	{
		// TODO: Use blocktags for this, to pick up all logs?
		if(this.isLogSet)
		{
			return this.isLog;
		}
		this.isLog =
			isMaterial(LocalMaterials.ACACIA_LOG) ||
			isMaterial(LocalMaterials.BIRCH_LOG) ||
			isMaterial(LocalMaterials.DARK_OAK_LOG) ||
			isMaterial(LocalMaterials.OAK_LOG) ||
			isMaterial(LocalMaterials.SPRUCE_LOG) ||
			isMaterial(LocalMaterials.ACACIA_WOOD) ||
			isMaterial(LocalMaterials.BIRCH_WOOD) ||
			isMaterial(LocalMaterials.DARK_OAK_WOOD) ||
			isMaterial(LocalMaterials.OAK_WOOD) ||
			isMaterial(LocalMaterials.SPRUCE_WOOD) ||			
			isMaterial(LocalMaterials.STRIPPED_ACACIA_LOG) ||
			isMaterial(LocalMaterials.STRIPPED_BIRCH_LOG) ||
			isMaterial(LocalMaterials.STRIPPED_DARK_OAK_LOG) ||
			isMaterial(LocalMaterials.STRIPPED_JUNGLE_LOG) ||
			isMaterial(LocalMaterials.STRIPPED_OAK_LOG) ||
			isMaterial(LocalMaterials.STRIPPED_SPRUCE_LOG)
		;
		this.isLogSet = true;
		return this.isLog;
	}
	
	public boolean isSapling()
	{
		// TODO: Use blocktags for this, to pick up all saplings?
		return 
			isMaterial(LocalMaterials.BAMBOO_SAPLING) ||
			isMaterial(LocalMaterials.BIRCH_SAPLING) ||
			isMaterial(LocalMaterials.DARK_OAK_SAPLING) ||
			isMaterial(LocalMaterials.JUNGLE_SAPLING) ||
			isMaterial(LocalMaterials.OAK_SAPLING) ||
			isMaterial(LocalMaterials.SPRUCE_SAPLING)
		;
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
	public abstract LocalMaterialData rotate(int rotateTimes);
	  
	public LocalMaterialData parseWithBiomeAndHeight(boolean biomeConfigsHaveReplacement, ReplaceBlockMatrix replaceBlocks, int y)
	{	
		if (!biomeConfigsHaveReplacement)
		{
			// Don't waste time here, ReplacedBlocks is empty everywhere
			return this;
		}
		return replaceBlocks.replaceBlock(y, this);
	}

	@Override
	public boolean isTag()
	{		
		return false;
	}
	
	@Override
	public String toString()
	{
		return getName();
	}
	
	public abstract boolean equals(Object other);
	
	public abstract int hashCode();

	public abstract LocalMaterialData legalOrPersistentLeaves(boolean leaveIllegalLeaves);
}
