package com.pg85.otg.customobject;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.customobject.config.CustomObjectResourcesManager;
import com.pg85.otg.customobject.structures.CustomStructureCache;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.interfaces.IModLoadedChecker;
import com.pg85.otg.interfaces.IWorldGenRegion;
import com.pg85.otg.util.bo3.Rotation;
import com.pg85.otg.util.minecraft.TreeType;

import java.nio.file.Path;
import java.util.Random;

/**
 * A Minecraft tree, viewed as a custom object.
 *
 * <p>For historical reasons, TreeObject implements {@link CustomObject} instead
 * of just {@link SpawnableObject}. We can probably refactor the Tree resource
 * to accept {@link SpawnableObject}s instead of {@link CustomObject}s, so that
 * all the extra methods are no longer needed.
 */
class TreeObject implements CustomObject
{
	private TreeType type;
	private int minHeight = Constants.WORLD_DEPTH;
	private int maxHeight = Constants.WORLD_HEIGHT - 1;

	TreeObject(TreeType type)
	{
		this.type = type;
	}

	@Override
	public boolean onEnable(String presetFolderName, Path otgRootFolder, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		return true;
	}

	@Override
	public String getName()
	{
		return type.name();
	}

	@Override
	public boolean canSpawnAsTree()
	{
		return true;
	}
	
	// Called during decoration.
	@Override
	public boolean process(CustomStructureCache structureCache, IWorldGenRegion worldGenRegion, Random random)
	{
		// A tree has no frequency or rarity, so spawn it once in the chunk
		// Make sure we stay within decoration bounds.
		int x = worldGenRegion.getDecorationArea().getChunkBeingDecoratedCenterX() + random.nextInt(Constants.CHUNK_SIZE);
		int z = worldGenRegion.getDecorationArea().getChunkBeingDecoratedCenterZ() + random.nextInt(Constants.CHUNK_SIZE);
		int y = worldGenRegion.getHighestBlockAboveYAt(x, z);
		if (y < minHeight || y > maxHeight)
		{
			return false;
		}	
		return spawnForced(structureCache, worldGenRegion, random, Rotation.NORTH, x, y, z, false);
	}
	
	@Override
	public boolean spawnFromSapling(IWorldGenRegion worldGenRegion, Random random, Rotation rotation, int x, int y, int z)
	{
		return worldGenRegion.placeTree(type, random, x, y, z);
	}

	@Override
	public boolean spawnForced(CustomStructureCache structureCache, IWorldGenRegion worldGenRegion, Random random, Rotation rotation, int x, int y, int z, boolean allowReplaceBlocks)
	{
		return worldGenRegion.placeTree(type, random, x, y, z);
	}
	
	@Override
	public boolean spawnAsTree(CustomStructureCache structureCache, IWorldGenRegion worldGenRegion, Random random, int x, int z, int minY, int maxY)
	{
		int y = worldGenRegion.getHighestBlockAboveYAt(x, z);
		Rotation rotation = Rotation.getRandomRotation(random);

		if(!(minY == -1 && maxY == -1))
		{
			if (y < minY || y > maxY)
			{
				return false;
			}
		}
		
		if (y < minHeight || y > maxHeight)
		{
			return false;
		}

		return spawnForced(structureCache, worldGenRegion, random, rotation, x, y, z, true);
	}
	
	@Override
	public boolean canRotateRandomly()
	{
		// Trees cannot be rotated
		return false;
	}

	@Override
	public boolean loadChecks(IModLoadedChecker modLoadedChecker)
	{
		return true;
	}

	@Override
	public boolean doReplaceBlocks()
	{
		return false;
	}
}
