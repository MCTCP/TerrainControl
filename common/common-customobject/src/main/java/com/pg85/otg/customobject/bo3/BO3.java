package com.pg85.otg.customobject.bo3;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.constants.SettingsEnums.ConfigMode;
import com.pg85.otg.customobject.CustomObjectManager;
import com.pg85.otg.customobject.bo3.bo3function.BO3BlockFunction;
import com.pg85.otg.customobject.bo3.bo3function.BO3EntityFunction;
import com.pg85.otg.customobject.bo3.checks.BO3Check;
import com.pg85.otg.customobject.config.CustomObjectResourcesManager;
import com.pg85.otg.customobject.config.io.FileSettingsReaderBO4;
import com.pg85.otg.customobject.config.io.FileSettingsWriterBO4;
import com.pg85.otg.customobject.util.ObjectType;
import com.pg85.otg.customobject.structures.Branch;
import com.pg85.otg.customobject.structures.CustomStructure;
import com.pg85.otg.customobject.structures.CustomStructureCache;
import com.pg85.otg.customobject.structures.CustomStructureCoordinate;
import com.pg85.otg.customobject.structures.StructuredCustomObject;
import com.pg85.otg.customobject.structures.bo3.BO3CustomStructure;
import com.pg85.otg.customobject.structures.bo3.BO3CustomStructureCoordinate;
import com.pg85.otg.customobject.util.BoundingBox;
import com.pg85.otg.customobject.util.BO3Enums.OutsideSourceBlock;
import com.pg85.otg.customobject.util.BO3Enums.SpawnHeightEnum;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.interfaces.IModLoadedChecker;
import com.pg85.otg.interfaces.IWorldGenRegion;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.biome.ReplaceBlockMatrix;
import com.pg85.otg.util.bo3.Rotation;
import com.pg85.otg.util.gen.DecorationArea;
import com.pg85.otg.util.helpers.MathHelper;
import com.pg85.otg.util.helpers.RandomHelper;
import com.pg85.otg.util.materials.LocalMaterialData;

public class BO3 implements StructuredCustomObject
{
	private BO3Config settings;
	private final String name;
	private final File file;
	private boolean isInvalidConfig;

	/**
	 * Creates a BO3 from a file.
	 *
	 * @param name Name of the BO3.
	 * @param file File of the BO3. If the file does not exist, a BO3 with the default settings is created.
	 */
	public BO3(String name, File file)
	{
		this.name = name;
		this.file = file;
	}

	public BO3(String name, File file, BO3Config settings)
	{
		this.name = name;
		this.file = file;
		this.settings = settings;
	}

	@Override
	public String getName()
	{
		return this.name;
	}

	@Override
	public ObjectType getType()
	{
		return ObjectType.BO3;
	}

	@Override
	public BO3Config getConfig()
	{
		return this.settings;
	}

	@Override
	public boolean onEnable(String presetFolderName, Path otgRootFolder, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		if(this.isInvalidConfig)
		{
			return false;
		}
		if(this.settings != null)
		{
			return true;
		}
		try
		{
			this.settings = new BO3Config(new FileSettingsReaderBO4(this.name, this.file, logger), presetFolderName, otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker);
			if (this.settings.settingsMode != ConfigMode.WriteDisable)
			{
				FileSettingsWriterBO4.writeToFile(this.settings, this.settings.settingsMode, logger, materialReader, manager);
			}
		}
		catch (InvalidConfigException ex)
		{
			this.isInvalidConfig = true;
			return false;
		}
		return true;
	}

	@Override
	public boolean canSpawnAsTree()
	{
		return this.settings.tree;
	}

	// Used for saplings
	@Override
	public boolean canRotateRandomly()
	{
		return this.settings.rotateRandomly;
	}
	
	@Override
	public boolean loadChecks(IModLoadedChecker modLoadedChecker)
	{
		return this.settings != null && this.settings.parseModChecks(modLoadedChecker);
	}

	// Used to safely spawn this object from a grown sapling
	@Override
	public boolean spawnFromSapling(IWorldGenRegion worldGenRegion, Random random, Rotation rotation, int x, int y, int z)
	{
		BO3BlockFunction[] blocks = this.settings.getBlocks(rotation.getRotationId());

		ArrayList<BO3BlockFunction> blocksToSpawn = new ArrayList<BO3BlockFunction>();

		ObjectExtrusionHelper oeh = new ObjectExtrusionHelper(this.settings.extrudeMode, this.settings.extrudeThroughBlocks);
		HashSet<ChunkCoordinate> chunks = new HashSet<ChunkCoordinate>();

		LocalMaterialData localMaterial;
		for (BO3BlockFunction block : blocks)
		{
			localMaterial = worldGenRegion.getMaterial(x + block.x, y + block.y, z + block.z);

			// Ignore blocks in the ground when checking spawn conditions
			if (block.y >= 0)
			{
				// Do not spawn if non-tree blocks are in the way
				if (
					!localMaterial.isAir() && 
					!localMaterial.isLogOrLeaves() && 
					!localMaterial.isSapling()
				)
				{
					return false;
				}
			}

			// Only overwrite air
			if (localMaterial.isAir())
			{
				chunks.add(ChunkCoordinate.fromBlockCoords(x + block.x, z + block.z));
				blocksToSpawn.add(block);
			}

			oeh.addBlock((BO3BlockFunction) block);
		}
		
		ReplaceBlockMatrix replaceBlocks = null;
		int lastX = Integer.MIN_VALUE;
		int lastZ = Integer.MIN_VALUE;		
		for (BO3BlockFunction block : blocksToSpawn)
		{
			if(this.doReplaceBlocks())
			{
				if(lastX != x + block.x || lastZ != z + block.z)
				{
					// TODO: Calculate area required and fetch biome data for whole chunks instead of per column.
					replaceBlocks = worldGenRegion.getCachedBiomeProvider().getBiomeConfig(x + block.x, z + block.z, true).getReplaceBlocks();
					lastX = x + block.x;
					lastZ = z + z + block.z;
				}
				block.spawn(worldGenRegion, random, x + block.x, y + block.y, z + block.z, replaceBlocks);
			} else {
				block.spawn(worldGenRegion, random, x + block.x, y + block.y, z + block.z);
			}
		}
		oeh.extrude(worldGenRegion, random, x, y, z, doReplaceBlocks(), true);
		handleBO3Functions(null, null, worldGenRegion, random, rotation, x, y, z, chunks);

		return true;
	}

	public int getXOffset(Rotation rotation)
	{
		
		return -(this.settings.boundingBoxes[rotation.ordinal()].getMinX() + (int)Math.floor((this.settings.boundingBoxes[rotation.ordinal()].getWidth() / 2f)));
	}
	
	public int getZOffset(Rotation rotation)
	{
		return -(this.settings.boundingBoxes[rotation.ordinal()].getMinZ() + (int)Math.floor((this.settings.boundingBoxes[rotation.ordinal()].getDepth() / 2f)));
	}
	
	// Force spawns a BO3 object. Used by /otg spawn and bo3AtSpawn.
	// This method ignores the maxPercentageOutsideBlock setting
	@Override
	public boolean spawnForced(CustomStructureCache structureCache, IWorldGenRegion worldGenRegion, Random random, Rotation rotation, int x, int y, int z, boolean allowReplaceBlocks)
	{
		BO3BlockFunction[] blocks = this.settings.getBlocks(rotation.getRotationId());
		ObjectExtrusionHelper oeh = new ObjectExtrusionHelper(this.settings.extrudeMode, this.settings.extrudeThroughBlocks);
		HashSet<ChunkCoordinate> chunks = new HashSet<ChunkCoordinate>();

		ReplaceBlockMatrix replaceBlocks = null;
		int lastX = Integer.MIN_VALUE;
		int lastZ = Integer.MIN_VALUE;
		for (BO3BlockFunction block : blocks)
		{
			if(allowReplaceBlocks && doReplaceBlocks())
			{
				if(lastX != x + block.x || lastZ != z + block.z)
				{
					// TODO: Calculate area required and fetch biome data for whole chunks instead of per column.
					replaceBlocks = worldGenRegion.getCachedBiomeProvider().getBiomeConfig(x + block.x, z + block.z, true).getReplaceBlocks();
					lastX = x + block.x;
					lastZ = z + block.z;
				}
				block.spawn(worldGenRegion, random, x + block.x, y + block.y, z + block.z, replaceBlocks);
			} else {
				block.spawn(worldGenRegion, random, x + block.x, y + block.y, z + block.z);
			}
			oeh.addBlock(block);
			chunks.add(ChunkCoordinate.fromBlockCoords(x + block.x, z + block.z));
		}
		oeh.extrude(worldGenRegion, random, x, y, z, doReplaceBlocks(), true);
		handleBO3Functions(null, structureCache, worldGenRegion, random, rotation, x, y, z, chunks);

		return true;
	}

	// This method is only used to spawn CustomObject.
	// Called during decoration.
	@Override
	public boolean process(CustomStructureCache structureCache, IWorldGenRegion worldGenRegion, Random random)
	{
		boolean atLeastOneObjectHasSpawned = false;

		// TODO: Remove this offset for 1.16?
		int chunkMiddleX = worldGenRegion.getDecorationArea().getChunkBeingDecoratedCenterX();
		int chunkMiddleZ = worldGenRegion.getDecorationArea().getChunkBeingDecoratedCenterZ();
		int spawned = 0;
		for (int i = 0; i < this.settings.frequency; i++)
		{
			if (this.settings.rarity > random.nextDouble() * 100.0)
			{
				int x = chunkMiddleX + random.nextInt(Constants.CHUNK_SIZE);
				int z = chunkMiddleZ + random.nextInt(Constants.CHUNK_SIZE);
				if (spawn(structureCache, worldGenRegion, random, x, z, this.settings.minHeight, this.settings.maxHeight))
				{
					spawned++;
					atLeastOneObjectHasSpawned = true;
				}
			}
			if(this.settings.maxSpawn > 0 && spawned == this.settings.maxSpawn)
			{
				break; 
			}
		}

		return atLeastOneObjectHasSpawned;
	}

	// Used for trees during decoration
	@Override
	public boolean spawnAsTree(CustomStructureCache structureCache, IWorldGenRegion worldGenRegion, Random random, int x, int z, int minY, int maxY)
	{
		// A bit ugly, but avoids having to create and implement another spawnAsTree method.
		if(minY == -1)
		{
			minY = this.getConfig().minHeight;
		}
		if(maxY == -1)
		{
			maxY = this.getConfig().maxHeight;
		}
		return spawn(structureCache, worldGenRegion, random, x, z, minY, maxY);
	}

	// Used for customobject and trees during decoration
	private boolean spawn(CustomStructureCache structureCache, IWorldGenRegion worldGenRegion, Random random, int x, int z, int minY, int maxY)
	{
		Rotation rotation = this.settings.rotateRandomly ? Rotation.getRandomRotation(random) : Rotation.NORTH;
		int offsetY = 0;
		int baseY = 0;
		if (this.settings.getSpawnHeight() == SpawnHeightEnum.randomY)
		{
			baseY = minY == maxY ? minY : RandomHelper.numberInRange(random, minY, maxY);
		}
		if (this.settings.getSpawnHeight() == SpawnHeightEnum.highestBlock)
		{
			baseY = worldGenRegion.getHighestBlockAboveYAt(x, z);
		}
		if (this.settings.getSpawnHeight() == SpawnHeightEnum.highestSolidBlock)
		{
			baseY = worldGenRegion.getBlockAboveSolidHeight(x, z);
		}
		// Offset by static and random settings values
		// TODO: This is pointless used with randomY?
		offsetY = baseY + this.getOffsetAndVariance(random, this.settings.getSpawnHeightOffset(), this.settings.spawnHeightVariance);
		return trySpawnAt(null, structureCache, worldGenRegion, random, rotation, x, offsetY, z, minY, maxY, baseY);
	}
	
	// Used for trees, customobjects and customstructures during decoration.
	public boolean trySpawnAt(CustomStructure structure, CustomStructureCache structureCache, IWorldGenRegion worldGenRegion, Random random, Rotation rotation, int x, int y, int z, int minY, int maxY, int baseY)
	{
		if (y < Constants.WORLD_DEPTH || y >= Constants.WORLD_HEIGHT) // Isn't this already done before this method is called?
		{
			return false;
		}

		// Height check
		if (y < minY || y > maxY)
		{
			return false;
		}

		BO3Check[] checks = this.settings.bo3Checks[rotation.getRotationId()];

		// Check for spawning
		// TODO: Allow force spawning of BO3's for /otg spawn etc, avoid light checks.
		for (BO3Check check : checks)
		{
			// Don't apply spawn height offset/variance to block checks,
			// they should only be used with highestBlock/highestSolidBlock,
			// and need to check for things like grass at the original spawn y.
			if (check.preventsSpawn(worldGenRegion, x + check.x, baseY + check.y, z + check.z))
			{
				// A check failed
				return false;
			}
		}

		BO3BlockFunction[] blocks = this.settings.getBlocks(rotation.getRotationId());
		HashSet<ChunkCoordinate> loadedChunks = new HashSet<ChunkCoordinate>();
		ChunkCoordinate chunkCoord;
		for (BO3BlockFunction block : blocks)
		{
			if (y + block.y < Constants.WORLD_DEPTH || y + block.y >= Constants.WORLD_HEIGHT)
			{
				return false;
			}

			chunkCoord = ChunkCoordinate.fromBlockCoords(x + block.x, z + block.z);
			if(!loadedChunks.contains(chunkCoord))
			{
				if(!worldGenRegion.getDecorationArea().isInAreaBeingDecorated(x + block.x, z + block.z))
				{
					// Cannot spawn BO3, part of world is not loaded
					return false;
				}
				loadedChunks.add(chunkCoord);
			}
		}

		ArrayList<BO3BlockFunction> blocksToSpawn = new ArrayList<BO3BlockFunction>();
		ObjectExtrusionHelper oeh = new ObjectExtrusionHelper(this.settings.extrudeMode, this.settings.extrudeThroughBlocks);
		HashSet<ChunkCoordinate> chunks = new HashSet<ChunkCoordinate>();

		int blocksOutsideSourceBlock = 0;
		int maxBlocksOutsideSourceBlock = (int) Math.ceil(blocks.length * (this.settings.maxPercentageOutsideSourceBlock / 100.0));
		for (BO3BlockFunction block : blocks)
		{
			if (
				(
					(
						this.settings.maxPercentageOutsideSourceBlock < 100 && 
						blocksOutsideSourceBlock <= maxBlocksOutsideSourceBlock
					) || 
					this.settings.outsideSourceBlock == OutsideSourceBlock.dontPlace
				) && 
				!this.settings.sourceBlocks.contains(worldGenRegion.getMaterial(x + block.x, y + block.y, z + block.z))
			)
			{
				blocksOutsideSourceBlock++;
				if (blocksOutsideSourceBlock > maxBlocksOutsideSourceBlock)
				{
					// Too many blocks outside source block
					return false;
				}

				if (this.settings.outsideSourceBlock == OutsideSourceBlock.placeAnyway)
				{
					chunks.add(ChunkCoordinate.fromBlockCoords(x + block.x, z + block.z));
					blocksToSpawn.add(block);
				}
			} else {
				chunks.add(ChunkCoordinate.fromBlockCoords(x + block.x, z + block.z));
				blocksToSpawn.add(block);
			}
			if (block instanceof BO3BlockFunction)
			{
				oeh.addBlock((BO3BlockFunction) block);
			}
		}

		// Call event
		//if (!worldGenRegion.fireCanCustomObjectSpawnEvent(this, x, y, z))
		{
			// Cancelled
			//return false;
		}

		ReplaceBlockMatrix replaceBlocks = null;
		int lastX = Integer.MIN_VALUE;
		int lastZ = Integer.MIN_VALUE;		
		for (BO3BlockFunction block : blocksToSpawn)
		{
			if(doReplaceBlocks())
			{
				if(lastX != x + block.x || lastZ != z + block.z)
				{
					replaceBlocks = worldGenRegion.getBiomeConfigForDecoration(x + block.x, z + block.z).getReplaceBlocks();
					lastX = x + block.x;
					lastZ = z + z + block.z;					
				}				
				block.spawn(worldGenRegion, random, x + block.x, y + block.y, z + block.z, replaceBlocks);
			} else {
				block.spawn(worldGenRegion, random, x + block.x, y + block.y, z + block.z);
			}
		}
		oeh.extrude(worldGenRegion, random, x, y, z, doReplaceBlocks(), false);
		handleBO3Functions(structure, structureCache, worldGenRegion, random, rotation, x, y, z, chunks);

		return true;
	}

	private void handleBO3Functions(CustomStructure structure, CustomStructureCache structureCache, IWorldGenRegion worldGenRegion, Random random, Rotation rotation, int x, int y, int z, HashSet<ChunkCoordinate> chunks)
	{
		HashSet<ChunkCoordinate> chunksCustomObject = new HashSet<ChunkCoordinate>();

		// StructureCache can be null for non-otg worlds, when using /otg spawn/edit/export.
		if (structure != null && structureCache != null)
		{
			for (ChunkCoordinate structureCoord : chunks)
			{
				structureCache.addBo3ToStructureCache(structureCoord, structure, true);
			}
		} else if (structureCache != null)  {
			CustomStructure placeHolderStructure = new BO3CustomStructure(new BO3CustomStructureCoordinate(worldGenRegion.getPresetFolderName(), this, this.getName(), Rotation.NORTH, x, (short) 0, z));
			for (ChunkCoordinate structureCoord : chunksCustomObject)
			{
				structureCache.addBo3ToStructureCache(structureCoord, placeHolderStructure, false);			
			}
		}

		BO3EntityFunction[] entityDataInObject = this.settings.entityFunctions[rotation.getRotationId()];
		for (BO3EntityFunction entity : entityDataInObject)
		{
			BO3EntityFunction newEntityData = new BO3EntityFunction();

			newEntityData.y = y + entity.y;
			newEntityData.x = x + entity.x;
			newEntityData.z = z + entity.z;

			newEntityData.name = entity.name;
			newEntityData.resourceLocation = entity.resourceLocation;
			newEntityData.groupSize = entity.groupSize;
			newEntityData.nameTagOrNBTFileName = entity.nameTagOrNBTFileName;
			newEntityData.originalNameTagOrNBTFileName = entity.originalNameTagOrNBTFileName;
			newEntityData.namedBinaryTag = entity.namedBinaryTag;
			newEntityData.rotation = entity.rotation;

			worldGenRegion.spawnEntity(newEntityData);
		}
	}

	public Branch[] getBranches(Rotation rotation)
	{
		return this.settings.branches[rotation.getRotationId()];
	}
	
	public SpawnHeightEnum getStructurePartSpawnHeight()
	{
		return this.settings.getSpawnHeight();
	}

	// TODO: Use BoundingBox for BO4's?
	public BoundingBox getBoundingBox(Rotation rotation)
	{
		return this.settings.boundingBoxes[rotation.getRotationId()];
	}

	public int getMaxBranchDepth() // This used to be in CustomObject?
	{
		return this.settings.maxBranchDepth;
	}

	/**
	 * Computes the offset and variance for spawning a bo3
	 *
	 * @param random	Random number generator.
	 * @param offset	Base spawn offset.
	 * @param variance Max variance from this offset.
	 *
	 * @return The sum of the offset and variance.
	 */
	private int getOffsetAndVariance(Random random, int offset, int variance)
	{
		if (variance == 0)
		{
			return offset;
		} else if (variance < 0)
		{
			variance = -random.nextInt(MathHelper.abs(variance) + 1);
		} else {
			variance = random.nextInt(variance + 1);
		}
		return MathHelper.clamp(offset + variance, Constants.WORLD_DEPTH, Constants.WORLD_HEIGHT - 1);
	}
	
	public CustomStructureCoordinate makeCustomStructureCoordinate(String presetFolderName, boolean useOldBO3StructureRarity, Random random, int chunkX, int chunkZ)
	{
		// For 1.12.2 v9.0_r11 and earlier, BO3 customstructures used 2 rarity rolls,
		// one for the rarity in the CustomStructure() tag, one for the rarity in the BO3 itself.
		// TODO: Remove oldBO3StructureRarity after presets have updated. 
        if (!useOldBO3StructureRarity || this.settings.rarity > random.nextDouble() * 100.0)
        {
    		Rotation rotation = this.settings.rotateRandomly ? Rotation.getRandomRotation(random) : Rotation.NORTH;
    		int height = RandomHelper.numberInRange(random, this.settings.minHeight, this.settings.maxHeight);
    		return new BO3CustomStructureCoordinate(presetFolderName, this, this.getName(), rotation, chunkX * 16 + DecorationArea.BO_CHUNK_CENTER_X + random.nextInt(16), (short)height, chunkZ * 16 + DecorationArea.BO_CHUNK_CENTER_Z + random.nextInt(16));
        }
        return null;
	}

	@Override
	public boolean doReplaceBlocks()
	{
		return this.settings.doReplaceBlocks;
	}
}
