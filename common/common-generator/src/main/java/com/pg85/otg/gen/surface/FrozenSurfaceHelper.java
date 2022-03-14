package com.pg85.otg.gen.surface;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.interfaces.IBiome;
import com.pg85.otg.interfaces.IBiomeConfig;
import com.pg85.otg.interfaces.IWorldGenRegion;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.gen.DecorationArea;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.LocalMaterials;
import com.pg85.otg.util.materials.MaterialProperties;

public class FrozenSurfaceHelper
{
	private static final int MAX_LAYERS_ON_LEAVES = 3;

	public FrozenSurfaceHelper() { }

	/**
	 * Freezes and Applied snow to an offset chunkCoordinate
	 * @param chunkCoord The chunk to freeze and snow on
	 */
	public static void freezeChunk(IWorldGenRegion worldGenRegion, ChunkCoordinate chunkCoord)
	{
		int x = chunkCoord.getChunkX() * Constants.CHUNK_SIZE + DecorationArea.DECORATION_OFFSET;
		int z = chunkCoord.getChunkZ() * Constants.CHUNK_SIZE + DecorationArea.DECORATION_OFFSET;
		int blockToFreezeX;
		int blockToFreezeZ;
		for (int i = 0; i < Constants.CHUNK_SIZE; i++)
		{
			for (int j = 0; j < Constants.CHUNK_SIZE; j++)
			{
				blockToFreezeX = x + i;
				blockToFreezeZ = z + j;
				freezeColumn(worldGenRegion, blockToFreezeX, blockToFreezeZ);
			}
		}
	}

	/**
	 * Performs a liquid freeze and lays down a layer of snow on a Chunk column
	 * @param x Location X
	 * @param z Location Z
	 */
	private static void freezeColumn(IWorldGenRegion worldGenRegion, int x, int z)
	{
		IBiome biome = worldGenRegion.getBiomeForDecoration(x, z);
		if (biome != null)
		{
			IBiomeConfig biomeConfig = biome.getBiomeConfig();			
			int blockToFreezeY = worldGenRegion.getHighestBlockAboveYAt(x, z);
			float tempAtBlockToFreeze = biome.getTemperatureAt(x, blockToFreezeY, z);
			if (blockToFreezeY > 0 && tempAtBlockToFreeze < Constants.SNOW_AND_ICE_TEMP)
			{
				// Start to freeze liquids
				if (!freezeLiquid(biomeConfig, worldGenRegion, x, blockToFreezeY -1, z, 0))
				{
					// Snow has to be placed on an empty space on a block that accepts snow in the world
					startSnowFall(biomeConfig, worldGenRegion, x, blockToFreezeY, z, biome);
				}
			}
		}
	}

	/**
	 * Attempts to freeze liquids at the given location
	 * @param x Location X
	 * @param y Location Y
	 * @param z Location Z
	 * @return If a liquid was present at the given location (not necessarily successful in freezing)
	 */
	private static boolean freezeLiquid(IBiomeConfig biomeConfig, IWorldGenRegion worldGenRegion, int x, int y, int z, int currentPropagationSize)
	{
		if (biomeConfig != null)
		{
			LocalMaterialData materialToFreeze = worldGenRegion.getMaterial(x, y, z);
			if (materialToFreeze.isLiquid())
			{
				// Water & Stationary Water => IceBlock
				LocalMaterialData iceBlock = biomeConfig.getIceBlockReplaced(y);
				if(shouldFreeze(x, y, z, materialToFreeze, iceBlock, LocalMaterials.WATER))
				{
					worldGenRegion.setBlock(x, y, z, iceBlock);
				} else {
					LocalMaterialData cooledLavaBlock = biomeConfig.getCooledLavaBlockReplaced(y);
					// Lava & Stationary Lava => CooledLavaBlock
					if(shouldFreeze(x, y, z, materialToFreeze, cooledLavaBlock, LocalMaterials.LAVA))
					{
						worldGenRegion.setBlock(x, y, z, cooledLavaBlock);
					}
				}
				return true;
			}
		}
		return false;
	}

	/**
	 * Freezes two types of blocks to a third type at a specific location.
	 *
	 * Example: WATER and STATIONARY_WATER to ICE
	 * @param x Location X
	 * @param y Location Y
	 * @param z Location Z
	 * @param thawedMaterial The material to be checked and if passed, frozen
	 * @param frozenMaterial The material to freeze the thawed material to if checks pass
	 * @param check1 The first material to check for
	 */
	private static boolean shouldFreeze(int x, int y, int z, LocalMaterialData thawedMaterial, LocalMaterialData frozenMaterial, LocalMaterialData source)
	{
		return (thawedMaterial.isMaterial(source) && !frozenMaterial.isMaterial(source));
	}

	/**
	 * Determines all Y locations that need snow, and how much snow in each Y location based on temperature
	 * and transparent block pass-through. Sets snow to determined height for each Y location applicable.
	 * @param x Location X
	 * @param y Location Y
	 * @param z Location Z
	 * @param biome The biome associated with the chunk column
	 */
	private static void startSnowFall(IBiomeConfig biomeConfig, IWorldGenRegion worldGenRegion, int x, int y, int z, IBiome biome)
	{
		int decreaseFactor = 0;
		float tempAtBlockToFreeze;
		int snowHeight;
		LocalMaterialData materialToSnowAt = null;
		LocalMaterialData materialToSnowOn = null;
		if(worldGenRegion.getWorldConfig().isBetterSnowFall())
		{
			tempAtBlockToFreeze = biome.getTemperatureAt(x, y, z);
			snowHeight = biomeConfig.getSnowHeight(tempAtBlockToFreeze);
		} else {
			snowHeight = 0;
		}
		while (
			y > Constants.WORLD_DEPTH + 1 && 
			decreaseFactor < 8 &&
			snowHeight - decreaseFactor >= 0
		)
		{
			materialToSnowAt = worldGenRegion.getMaterial(x, y, z);
			materialToSnowOn = worldGenRegion.getMaterial(x, y - 1, z);			
			if (
				materialToSnowAt != null &&
				materialToSnowOn != null &&
				materialToSnowAt.isAir() &&
				materialToSnowOn.canSnowFallOn()
			)
			{
				// If we've spawned all snow layers, exit.
				if(setSnowFallAtLocation(worldGenRegion, x, y, z, snowHeight - decreaseFactor, materialToSnowOn))
				{
					break;
				}
				// Spawned on leaves, which can only carry MAX_LAYERS_ON_LEAVES snow layers.
				// We have more snow layers to spawn.
				decreaseFactor += MAX_LAYERS_ON_LEAVES;
			}
			if(materialToSnowOn == null || materialToSnowOn.isSolid())
			{
				break;
			}
			y--;
		}
	}

	/**
	 * Applied snow to a location
	 * @param x Location X
	 * @param y Location Y
	 * @param z Location Z
	 * @param baseSnowHeight The base height snow should be
	 * @param materialToSnowOn The material that might have snow applied
	 */
	private static boolean setSnowFallAtLocation(IWorldGenRegion worldGenRegion, int x, int y, int z, int baseSnowHeight, LocalMaterialData materialToSnowOn)
	{
		LocalMaterialData snowMass;
		if (materialToSnowOn.isLeaves())
		{
			// Snow Layer(s) for trees, let each leaf carry MAX_LAYERS_ON_LEAVES or less layers of snow,
			// any remaining layers will fall through.
			// TODO: Reimplement this when block data works
			snowMass = LocalMaterials.SNOW;
			//snowMass = LocalMaterialManager.toLocalMaterialData(LocalMaterialManager.SNOW, baseSnowHeight <= MAX_LAYERS_ON_LEAVES - 1 ? baseSnowHeight : MAX_LAYERS_ON_LEAVES - 1);
			worldGenRegion.setBlock(x, y, z, snowMass);

			return baseSnowHeight <= MAX_LAYERS_ON_LEAVES - 1;
		}
		
		// Basic Snow Layer(s)
		// TODO: Reimplement this when block data works
		//snowMass = LocalMaterialManager.toLocalMaterialData(DefaultMaterial.SNOW, baseSnowHeight);
		snowMass = LocalMaterials.SNOW;
		worldGenRegion.setBlock(x, y, z, snowMass);

		// If we placed snow on grass or podzol, we need to update the snowy property of the grass.
		if (materialToSnowOn.isMaterial(LocalMaterials.GRASS) || materialToSnowOn.isMaterial(LocalMaterials.PODZOL))
		{
			worldGenRegion.setBlock(x, y - 1, z, materialToSnowOn.withProperty(MaterialProperties.SNOWY, true));
		}

		return true;
	}
}
