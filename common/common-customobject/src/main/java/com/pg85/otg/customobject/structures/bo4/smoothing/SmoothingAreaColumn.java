package com.pg85.otg.customobject.structures.bo4.smoothing;

import java.util.ArrayList;

import com.pg85.otg.customobject.bo4.BO4Config;
import com.pg85.otg.customobject.structures.bo4.smoothing.SmoothingAreaBlock.enumSmoothingBlockType;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.logging.ILogger;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.interfaces.IBiomeConfig;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.interfaces.IWorldGenRegion;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.LocalMaterials;

class SmoothingAreaColumn
{
	private int x;
	private int z;
	private final ArrayList<SmoothingAreaBlock> blocks = new ArrayList<SmoothingAreaBlock>();
	private SmoothingAreaBlock highestFillingBlock = null;
	private SmoothingAreaBlock lowestCuttingBlock = null;

	SmoothingAreaColumn(int x, int z)
	{
		this.x = x;
		this.z = z;
	}

	void addBlock(SmoothingAreaBlock block)
	{
		this.blocks.add(block);
	}

	void processBlocks(IWorldGenRegion worldGenRegion, ChunkCoordinate chunkBeingPopulated, BO4Config bo4Config, boolean spawnLog, ILogger logger, IMaterialReader materialReader)
	{
		if(this.highestFillingBlock == null && this.lowestCuttingBlock == null)
		{
			// For each column, make sure there is only one cutting line (the lowest cutting block in the column)
			// and one filling line (the highest filling block in the column).	
			for(SmoothingAreaBlock block : this.blocks)
			{
				if(block.smoothingBlockType == enumSmoothingBlockType.FILLING && (this.highestFillingBlock == null || this.highestFillingBlock.y < block.y))
				{
					this.highestFillingBlock = block;
				}
				if(block.smoothingBlockType == enumSmoothingBlockType.CUTTING && (this.lowestCuttingBlock == null || this.lowestCuttingBlock.y > block.y))
				{
					this.lowestCuttingBlock = block;
				}
			}
			
			// Make sure no cutting line spawns below a filling line (fill overrides cut),
			if(
				this.highestFillingBlock != null && this.lowestCuttingBlock != null &&
				this.lowestCuttingBlock.y > this.highestFillingBlock.y
			)
			{
				// TODO: Won't this still cause wonky results for branches with smoothing areas
				// attached at different heights that overlap?
				this.lowestCuttingBlock.y = this.highestFillingBlock.y;
			}

			// TODO: When using SmoothStartTop:true, if a smoothing line is underneath a bo4 block, we can 
			// cancel spawning the rest of the line since we know we won't need it.
		}
		spawn(worldGenRegion, chunkBeingPopulated, bo4Config, spawnLog, logger, materialReader);
	}
	
	private void spawn(IWorldGenRegion worldGenRegion, ChunkCoordinate chunkBeingPopulated, BO4Config bo4Config, boolean spawnLog, ILogger logger, IMaterialReader materialReader)
	{
		IBiomeConfig biomeConfig = worldGenRegion.getBiomeConfigForPopulation(this.x, this.z, chunkBeingPopulated);

		LocalMaterialData replaceAboveMaterial = null;
		try {
			replaceAboveMaterial = materialReader.readMaterial(bo4Config.replaceAbove);
		} catch (InvalidConfigException e) {
			if(spawnLog)
			{
				logger.log(LogMarker.WARN, "ReplaceAbove: " + bo4Config.replaceAbove + " could not be parsed as a material for BO4 " + bo4Config.getName());
			}
		}

		LocalMaterialData smoothingSurfaceBlock = null;
		LocalMaterialData smoothingGroundBlock = null;
		try {
			smoothingSurfaceBlock = materialReader.readMaterial(bo4Config.smoothingSurfaceBlock);
		} catch (InvalidConfigException e) {
			if(spawnLog)
			{
				logger.log(LogMarker.WARN, "SmoothingSurfaceBlock: " + bo4Config.smoothingSurfaceBlock + " could not be parsed as a material for BO4 " + bo4Config.getName());
			}
		}
		try {
			smoothingGroundBlock = materialReader.readMaterial(bo4Config.smoothingGroundBlock);
		} catch (InvalidConfigException e) {
			if(spawnLog)
			{
				logger.log(LogMarker.WARN, "SmoothingGroundBlock: " + bo4Config.smoothingGroundBlock + " could not be parsed as a material for BO4 " + bo4Config.getName());
			}
		}
		boolean needsReplaceBlocks;
		LocalMaterialData surfaceBlock;
		LocalMaterialData groundBlock;
		LocalMaterialData blockAbove;
		
		int highestBlockInWorld = -1;
		// If there is a filling block in this column, fill below and replace above.
		// If there is a cutting block and no filling block in this column, replace above.
		if(this.lowestCuttingBlock != null && this.highestFillingBlock == null)
		{
			// ReplaceAbove 
			// Should be AIR, WATER or none
			if(replaceAboveMaterial != null)
			{
				if(highestBlockInWorld == -1)
				{
					highestBlockInWorld = worldGenRegion.getHighestBlockYAt(this.x, this.z, true, false, true, true, true, null);
				}
				
				for(int y = highestBlockInWorld; y > this.lowestCuttingBlock.y; y--)
				{
					if(y > 0)
					{
						worldGenRegion.setBlock(this.lowestCuttingBlock.x, y, this.lowestCuttingBlock.z, replaceAboveMaterial, null, chunkBeingPopulated, false, false);
					}
				}
				
				// Set the new top block to surface block
				if(highestBlockInWorld > this.lowestCuttingBlock.y && this.lowestCuttingBlock.y > 0)
				{
					// Place the surface block
					surfaceBlock = null;
					needsReplaceBlocks = bo4Config.doReplaceBlocks;
					if(smoothingSurfaceBlock != null && !bo4Config.replaceWithBiomeBlocks)
					{
						surfaceBlock = smoothingSurfaceBlock;
					} else {
						blockAbove = worldGenRegion.getMaterial(this.lowestCuttingBlock.x, this.lowestCuttingBlock.y + 1, this.lowestCuttingBlock.z, chunkBeingPopulated);
						if(blockAbove != null && (blockAbove.isSolid() || blockAbove.isLiquid()))
						{
							surfaceBlock = biomeConfig.getGroundBlockAtHeight(worldGenRegion, this.lowestCuttingBlock.x, this.lowestCuttingBlock.y, this.lowestCuttingBlock.z);																	
						} else {
							surfaceBlock = biomeConfig.getSurfaceBlockAtHeight(worldGenRegion, this.lowestCuttingBlock.x, this.lowestCuttingBlock.y, this.lowestCuttingBlock.z);
						}
						needsReplaceBlocks = false;
						if(surfaceBlock.isAir())
						{
							if(
								this.lowestCuttingBlock.y < (biomeConfig.getWaterLevelMax()) &&
								worldGenRegion.getMaterial(this.lowestCuttingBlock.x, this.lowestCuttingBlock.y, this.lowestCuttingBlock.z, chunkBeingPopulated).isAir()
							)
							{
								surfaceBlock = LocalMaterials.WATER;
							} else {
								surfaceBlock = null;
							}
						}
					}
					if(surfaceBlock != null)
					{						
						worldGenRegion.setBlock(this.lowestCuttingBlock.x, this.lowestCuttingBlock.y, this.lowestCuttingBlock.z, surfaceBlock, null, chunkBeingPopulated, needsReplaceBlocks, false);
					}
				}
			}
		}
		if(this.highestFillingBlock != null)
		{
			if(highestBlockInWorld == -1)
			{
				highestBlockInWorld = worldGenRegion.getHighestBlockYAt(this.x, this.z, true, false, true, true, true, null);
			}
			
			// ReplaceAbove 
			// Should be AIR, WATER or none
			if(replaceAboveMaterial != null)
			{
				for(int y = highestBlockInWorld; y > this.highestFillingBlock.y; y--)
				{
					if(y > 0)
					{
						worldGenRegion.setBlock(this.highestFillingBlock.x, y, this.highestFillingBlock.z, replaceAboveMaterial, null, chunkBeingPopulated, false, false);
					}
				}
			}
			
			// Place the surface block
			surfaceBlock = null;
			needsReplaceBlocks = bo4Config.doReplaceBlocks;
			if(smoothingSurfaceBlock != null && !bo4Config.replaceWithBiomeBlocks)
			{
				surfaceBlock = smoothingSurfaceBlock;
			} else {
				
				blockAbove = worldGenRegion.getMaterial(this.highestFillingBlock.x, this.highestFillingBlock.y + 1, this.highestFillingBlock.z, chunkBeingPopulated);
				if(blockAbove != null && (blockAbove.isSolid() || blockAbove.isLiquid()))
				{
					surfaceBlock = biomeConfig.getGroundBlockAtHeight(worldGenRegion, this.highestFillingBlock.x, this.highestFillingBlock.y, this.highestFillingBlock.z);																	
				} else {
					surfaceBlock = biomeConfig.getSurfaceBlockAtHeight(worldGenRegion, this.highestFillingBlock.x, this.highestFillingBlock.y, this.highestFillingBlock.z);
				}				
				
				needsReplaceBlocks = false;
				if(surfaceBlock.isAir())
				{
					if(
						this.highestFillingBlock.y < biomeConfig.getWaterLevelMax() &&
						worldGenRegion.getMaterial(this.highestFillingBlock.x, this.highestFillingBlock.y, this.highestFillingBlock.z, chunkBeingPopulated).isAir()
					)
					{
						surfaceBlock = LocalMaterials.WATER;
					} else {
						surfaceBlock = null;
					}
				}
			}
			if(surfaceBlock != null)
			{
				if(this.highestFillingBlock.y > 0)
				{
					worldGenRegion.setBlock(this.highestFillingBlock.x, this.highestFillingBlock.y, this.highestFillingBlock.z, surfaceBlock, null, chunkBeingPopulated, needsReplaceBlocks, false);
				}
			}
						
			// ReplaceBelow
			if(smoothingGroundBlock != null && !bo4Config.replaceWithBiomeBlocks)
			{
				for(int y = this.highestFillingBlock.y - 1; y >= highestBlockInWorld; y--)
				{
					if(y > 0)
					{
						groundBlock = smoothingGroundBlock;
						needsReplaceBlocks = bo4Config.doReplaceBlocks;
						if(groundBlock.isAir())
						{
							if(y < biomeConfig.getWaterLevelMax())
							{
								groundBlock = LocalMaterials.WATER;
								needsReplaceBlocks = false;
							}
						}
						worldGenRegion.setBlock(this.highestFillingBlock.x, y, this.highestFillingBlock.z, groundBlock, null, chunkBeingPopulated, needsReplaceBlocks, false);
					}
				}
			} else {
				for(int y = this.highestFillingBlock.y - 1; y >= highestBlockInWorld; y--)
				{
					if(y > 0)
					{
						groundBlock = biomeConfig.getGroundBlockAtHeight(worldGenRegion, this.highestFillingBlock.x, (short)y, this.highestFillingBlock.z);
						if(groundBlock.isAir())
						{
							if(y < biomeConfig.getWaterLevelMax())
							{
								groundBlock = LocalMaterials.WATER;
							}
						}
						worldGenRegion.setBlock(this.highestFillingBlock.x, y, this.highestFillingBlock.z, groundBlock, null, chunkBeingPopulated, false, false);
					}
				}
			}
		}
	}
}
