package com.pg85.otg.customobject.bo3;

import com.pg85.otg.customobject.bo3.bo3function.BO3BlockFunction;
import com.pg85.otg.customobject.util.BO3Enums.ExtrudeMode;
import com.pg85.otg.interfaces.IBiomeConfig;
import com.pg85.otg.interfaces.IWorldGenRegion;
import com.pg85.otg.util.materials.MaterialSet;

import java.util.ArrayList;
import java.util.Random;

/**
 * This class aids in the task of finding the blocks at the top or bottom of a collection of blocks
 */
class ObjectExtrusionHelper
{
	/**
	 * The Y coordinate of the appropriate level to be extruding blocks from
	 */
	private int blockExtrusionY;

	/**
	 * The style to use for extruding; Currently either BottomDown or TopUp
	 */
	private ExtrudeMode extrudeMode;

	/**
	 * These materials are the set of materials that are allow to be extruded through; That is, as soon as we find a
	 * block in the world that is not in this list, we will stop extruding the BO3
	 */
	private MaterialSet extrudeThroughBlocks;

	/**
	 * These blocks are the blocks that are found to be at the location dictated by the extrudeMode, and will be
	 * extruded until hitting a material not listed in extrudeThroughBlocks
	 */
	private ArrayList<BO3BlockFunction> blocksToExtrude = new ArrayList<BO3BlockFunction>();

	/**
	 * Constructor
	 *
	 * @param extrudeMode		  The style of extrusion to perform
	 * @param extrudeThroughBlocks The types of materials to allow extrusion to act upon
	 */
	ObjectExtrusionHelper(ExtrudeMode extrudeMode, MaterialSet extrudeThroughBlocks)
	{
		this.extrudeMode = extrudeMode;
		this.extrudeThroughBlocks = extrudeThroughBlocks;
		blockExtrusionY = extrudeMode.getStartingHeight();
	}

	/**
	 * Determines if the block is one we wish to add to the list of blocks to be extruded. If it is, it will be added
	 * otherwise, nothing happens. Any blocks added to the list that are on a level not optimal to the current level
	 * will be purged to create the optimal list of blocks to extrude
	 *
	 * @param block The block to add.
	 */
	void addBlock(BO3BlockFunction block)
	{
		if (extrudeMode != ExtrudeMode.None)
		{
			if (extrudeMode == ExtrudeMode.BottomDown && block.y < blockExtrusionY)
			{
				blocksToExtrude.clear();
				blockExtrusionY = block.y;
			} else if (extrudeMode == ExtrudeMode.TopUp && block.y > blockExtrusionY)
			{
				blocksToExtrude.clear();
				blockExtrusionY = block.y;
			}
			if (block.y == blockExtrusionY)
			{
				blocksToExtrude.add(block);
			}
		}
	}

	/**
	 * This method takes the blocks that have been added to this and extrudes them individually until a block outside
	 * of the extrudeThroughBlocks has been hit
	 *
	 * @param worldGenRegion  The LocalWorld to extrude block in
	 * @param random The random generator to use to spawning
	 * @param x	  The BO3 base X spawn location
	 * @param y	  The BO3 base Y spawn location
	 * @param z	  The BO3 base Z spawn location
	 */
	void extrude(IWorldGenRegion worldGenRegion, Random random, int x, int y, int z, boolean replaceBlock, boolean forceSpawn)
	{
		for (BO3BlockFunction block : blocksToExtrude)
		{
			// TODO: Calculate area required and fetch biome data for whole chunks instead of per column.
			IBiomeConfig biomeConfig = forceSpawn ? worldGenRegion.getCachedBiomeProvider().getBiomeConfig(x + block.x, z + block.z, true) : worldGenRegion.getBiomeConfigForDecoration(x + block.x, z + block.z);
			if (extrudeMode == ExtrudeMode.BottomDown)
			{
				for (int yi = y + block.y - 1;
					 yi > extrudeMode.getEndingHeight() && extrudeThroughBlocks.contains(worldGenRegion.getMaterial(x + block.x, yi, z + block.z));
					 --yi)
				{
					if(replaceBlock)
					{
						worldGenRegion.setBlock(x + block.x, yi, z + block.z, block.material, block.nbt, biomeConfig.getReplaceBlocks());
					} else {
						worldGenRegion.setBlock(x + block.x, yi, z + block.z, block.material, block.nbt);
					}
				}
			}
			else if (extrudeMode == ExtrudeMode.TopUp)
			{
				for (int yi = y + block.y + 1;
					 yi < extrudeMode.getEndingHeight() && extrudeThroughBlocks.contains(worldGenRegion.getMaterial(x + block.x, yi, z + block.z));
					 ++yi)
				{
					if(replaceBlock)
					{
						worldGenRegion.setBlock(x + block.x, yi, z + block.z, block.material, block.nbt, biomeConfig.getReplaceBlocks());
					} else {
						worldGenRegion.setBlock(x + block.x, yi, z + block.z, block.material, block.nbt);
					}
				}
			}
		}
	}
}
