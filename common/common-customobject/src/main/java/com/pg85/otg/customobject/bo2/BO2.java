package com.pg85.otg.customobject.bo2;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Random;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.customobject.CustomObject;
import com.pg85.otg.customobject.CustomObjectManager;
import com.pg85.otg.customobject.bo3.BO3Config;
import com.pg85.otg.customobject.bo3.bo3function.BO3BlockFunction;
import com.pg85.otg.customobject.bofunctions.BlockFunction;
import com.pg85.otg.customobject.config.CustomObjectConfigFile;
import com.pg85.otg.customobject.config.CustomObjectResourcesManager;
import com.pg85.otg.customobject.config.io.FileSettingsReaderBO4;
import com.pg85.otg.customobject.config.io.SettingsReaderBO4;
import com.pg85.otg.customobject.config.io.SettingsWriterBO4;
import com.pg85.otg.customobject.util.ObjectType;
import com.pg85.otg.customobject.structures.CustomStructureCache;
import com.pg85.otg.customobject.util.BoundingBox;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.interfaces.IBiomeConfig;
import com.pg85.otg.interfaces.ICustomObjectManager;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.interfaces.IModLoadedChecker;
import com.pg85.otg.interfaces.IWorldGenRegion;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.biome.ReplaceBlockMatrix;
import com.pg85.otg.util.bo3.Rotation;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.LocalMaterials;
import com.pg85.otg.util.materials.MaterialSet;

/**
 * The good old BO2.
 */
public class BO2 extends CustomObjectConfigFile implements CustomObject
{	
	private ObjectCoordinate[][] data = new ObjectCoordinate[4][];
	private boolean isEnabled = false;
	public MaterialSet spawnOnBlockType;
	private MaterialSet collisionBlockType;
	private boolean spawnWater;
	private boolean spawnLava;
	public boolean spawnAboveGround;
	public boolean spawnUnderGround;
	private boolean spawnSunlight;
	private boolean spawnDarkness;
	private boolean randomRotation;
	private boolean dig;
	private boolean tree;
	private boolean branch;
	private boolean needsFoundation;
	private boolean doReplaceBlocks;
	private int rarity;
	public double collisionPercentage;
	public int spawnElevationMin;
	public int spawnElevationMax;

	BO2(SettingsReaderBO4 reader)
	{
		super(reader);
	}

	@Override
	public boolean canSpawnAsTree()
	{
		return this.tree;
	}

	@Override
	public boolean doReplaceBlocks()
	{
		return this.doReplaceBlocks;
	}
	
	@Override
	public boolean canRotateRandomly()
	{
		return this.randomRotation;
	}
	
	// Used to safely spawn this object from a grown sapling
	@Override
	public boolean spawnFromSapling(IWorldGenRegion worldGenRegion, Random random, Rotation rotation, int x, int y, int z)
	{
		ObjectCoordinate[] data = this.data[rotation.getRotationId()];
		ArrayList<ObjectCoordinate> blocksToSpawn = new ArrayList<ObjectCoordinate>();

		for (ObjectCoordinate point : data)
		{
			LocalMaterialData material = worldGenRegion.getMaterial(x + point.x, y + point.y, z + point.z);

			// Do not spawn if non-tree blocks are in the way
			if (
				!material.isAir() && 
				!material.isLogOrLeaves()
			)
			{
				return false;
			}

			// Only overwrite air
			if (material.isAir())
			{
				blocksToSpawn.add(point);
			}
		}

		IBiomeConfig biomeConfig = null;
		int lastX = Integer.MAX_VALUE;
		int lastZ = Integer.MAX_VALUE;
		for (ObjectCoordinate point : blocksToSpawn)
		{
			if(this.doReplaceBlocks)
			{
				if(lastX != x + point.x || lastZ != z + point.z)
				{
					// TODO: Calculate area required and fetch biome data for whole chunks instead of per column.
					biomeConfig = worldGenRegion.getCachedBiomeProvider().getBiomeConfig(x + point.x, z + point.z, true);
					lastX = x + point.x;
					lastZ = z + point.z;
				}
				setBlock(worldGenRegion, (x + point.x), y + point.y, z + point.z, point.material, biomeConfig.getReplaceBlocks());				
			} else {
				setBlock(worldGenRegion, (x + point.x), y + point.y, z + point.z, point.material);
			}
		}
		return true;
	}

	public BO3Config getConvertedConfig(String presetFolderName, Path otgRootFolder, ILogger logger,
										CustomObjectManager customObjectManager, IMaterialReader materialReader,
										CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker) throws InvalidConfigException
	{
		BO3Config newConfig = new BO3Config(new FileSettingsReaderBO4(
			this.getName(),
			ObjectType.BO3.getObjectFilePathFromName(this.getName(), this.getFile().getParentFile().toPath()).toFile(),
			logger),
			presetFolderName, otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker);

		// Convert the blocks
		BoundingBox box = BoundingBox.newEmptyBox();
		newConfig.extractBlocks(Arrays.asList(getBlockFunctions(presetFolderName, otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker)));
		newConfig.addBlockCheckFromBO2(this.spawnOnBlockType);
		for (BlockFunction<?> res : newConfig.getBlockFunctions(presetFolderName, otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker))
		{
			if (res instanceof BO3BlockFunction)
			{
				BO3BlockFunction block = (BO3BlockFunction) res;
				box.expandToFit(block.x, block.y, block.z);
			}
		}
		newConfig.setBoundingBox(box);
		newConfig.rotateBlocksAndChecks(presetFolderName, otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker);

		// Convert settings
		newConfig.getSettingsFromBO2(this);

		return newConfig;
	}

	@Override
	public BlockFunction<?>[] getBlockFunctions(String presetFolderName, Path otgRootFolder, ILogger logger, ICustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		ObjectCoordinate[] data = this.data[0];
		BlockFunction<?>[] blockFunctions = new BO3BlockFunction[data.length];
		int i = 0;
		for (ObjectCoordinate point : data)
		{
			BO3BlockFunction block = new BO3BlockFunction();
			block.material = point.material;
			block.nbt = null;
			block.nbtName = "";
			block.x = point.x;
			block.y = (short) point.y;
			block.z = point.z;
			blockFunctions[i++] = block;
		}
		return blockFunctions;
	}

	@Override
	public boolean spawnForced(CustomStructureCache structureCache, IWorldGenRegion worldGenRegion, Random random, Rotation rotation, int x, int y, int z, boolean allowReplaceBlocks)
	{
		ObjectCoordinate[] data = this.data[rotation.getRotationId()];

		LocalMaterialData worldMaterial;
		ReplaceBlockMatrix replaceBlocks = null;
		int lastX = Integer.MAX_VALUE;
		int lastZ = Integer.MAX_VALUE;		
		for (ObjectCoordinate point : data)
		{
			if(this.doReplaceBlocks)
			{
				if(lastX != x + point.x || lastZ != z + point.z)
				{
					// TODO: Calculate area required and fetch biome data for whole chunks instead of per column.
					replaceBlocks = worldGenRegion.getCachedBiomeProvider().getBiomeConfig(x + point.x, z + point.z, true).getReplaceBlocks();
					lastX = x + point.x;
					lastZ = z + point.z;
				}
			}
			if ((worldMaterial = worldGenRegion.getMaterial(x + point.x, y + point.y, z + point.z)) != null)
			{
				if(worldMaterial.isAir())
				{
					if(this.doReplaceBlocks)
					{
						setBlock(worldGenRegion, x + point.x, y + point.y, z + point.z, point.material, replaceBlocks);
					} else {
						setBlock(worldGenRegion, x + point.x, y + point.y, z + point.z, point.material);
					}
				}
				else if (this.dig)
				{
					if(allowReplaceBlocks && this.doReplaceBlocks)
					{
						setBlock(worldGenRegion, x + point.x, y + point.y, z + point.z, point.material, replaceBlocks);
					} else {
						setBlock(worldGenRegion, x + point.x, y + point.y, z + point.z, point.material);
					}
				}
			}
		}
		return true;
	}
	
	private boolean canSpawnAt(IWorldGenRegion worldGenRegion, Rotation rotation, int x, int y, int z)
	{
		// Basic checks
		
		if (y < Constants.WORLD_DEPTH || y >= Constants.WORLD_HEIGHT)  // Isn't this already done before this method is called?
		{
			return false;
		}
		
		if ((y < this.spawnElevationMin) || (y > this.spawnElevationMax))
		{
			return false;
		}

		if (!this.spawnOnBlockType.contains(worldGenRegion.getMaterial(x, y - 1, z)))
		{
			return false;
		}

		LocalMaterialData worldMaterial = worldGenRegion.getMaterial(x, y - 5, z);
		if (this.needsFoundation && worldMaterial == null || worldMaterial.isAir())
		{
			return false;
		}

		LocalMaterialData checkBlock = !this.spawnWater || !this.spawnLava ? worldGenRegion.getMaterial(x, y + 2, z) : null;
		if(checkBlock == null)
		{
			// Tried to spawn in unloaded chunks when decorationBoundsCheck:false.
			return false;
		}
		if (!this.spawnWater)
		{
			if (checkBlock.isMaterial(LocalMaterials.WATER))
			{
				return false;
			}
		}
		if (!this.spawnLava)
		{
			if (checkBlock.isMaterial(LocalMaterials.LAVA))
			{
				return false;
			}
		}

		// TODO: Allow force spawning of BO2's? (/otg spawn), avoid light checks.
		int checkLight = !this.spawnSunlight || !this.spawnDarkness ? worldGenRegion.getLightLevel(x, y + 2, z) : 0;
		if(checkLight == -1)
		{
			// Tried to spawn in unloaded chunk.
			return false;
		}
		if (!this.spawnSunlight)
		{
			if (checkLight > 8)
			{
				return false;
			}
		}
		if (!this.spawnDarkness)
		{
			if (checkLight < 9)
			{
				return false;
			}
		}		

		HashSet<ChunkCoordinate> loadedChunks = new HashSet<ChunkCoordinate>();
		ChunkCoordinate chunkCoord;
		ObjectCoordinate[] objData = this.data[rotation.getRotationId()];
		for (ObjectCoordinate point : objData)
		{
			if (
				y + point.y < Constants.WORLD_DEPTH || 
				y + point.y >= Constants.WORLD_HEIGHT
			)
			{
				return false;
			}
			
			chunkCoord = ChunkCoordinate.fromBlockCoords((x + point.x), (z + point.z));
			
			if(!loadedChunks.contains(chunkCoord))
			{
				if(!worldGenRegion.getDecorationArea().isInAreaBeingDecorated(x + point.x, z + point.z))
				{
					// Cannot spawn BO2, part of world is not loaded
					return false;
				}
				loadedChunks.add(chunkCoord);
			}
		}
		
		if (!this.dig && (int)Math.floor(this.collisionPercentage) < 100)
		{
			// Check all blocks
			int faultCounter = 0;
			int maxBlocksOutsideSourceBlock = (int)Math.ceil(objData.length * (this.collisionPercentage / 100.0));
			LocalMaterialData material;
			for (ObjectCoordinate point : objData)
			{
				if (
					(material = worldGenRegion.getMaterial((x + point.x), (y + point.y), (z + point.z))) == null || 
							this.collisionBlockType.contains(material)
				)
				{
					faultCounter++;
					// Don't spawn if blocks would be cut off.
					if (material == null || faultCounter > maxBlocksOutsideSourceBlock)
					{
						return false;
					}
				}
			}
		}

		// Call event
		//if (!worldGenRegion.fireCanCustomObjectSpawnEvent(this, x, y, z))
		{
			// Cancelled
			//return false;
		}

		return true;
	}

	@Override
	public boolean spawnAsTree(CustomStructureCache structureCache, IWorldGenRegion worldGenRegion, Random random, int x, int z, int minY, int maxY)
	{
		return spawn(worldGenRegion, random, x, z, minY == -1 ? this.spawnElevationMin : minY, maxY == -1 ? this.spawnElevationMax : maxY);
	} 
	
	private boolean spawn(IWorldGenRegion worldGenRegion, Random random, int x, int z, int minY, int maxY)
	{
		int y;
		if (this.spawnAboveGround)
		{
			y = worldGenRegion.getBlockAboveSolidHeight(x, z);
		}
		else if (this.spawnUnderGround)
		{
			int solidHeight = worldGenRegion.getBlockAboveSolidHeight(x, z);
			if (solidHeight < 1 || solidHeight <= minY)
			{
				return false;
			}
			if (solidHeight > maxY)
			{
				solidHeight = maxY;
			}
			y = random.nextInt(solidHeight - minY) + minY;
		} else {
			y = worldGenRegion.getHighestBlockAboveYAt(x, z);
		}

		if (y < 0)
		{
			return false;
		}

		Rotation rotation = this.randomRotation ? Rotation.getRandomRotation(random) : Rotation.NORTH;

		if (!canSpawnAt(worldGenRegion, rotation, x, y, z))
		{
			return false;
		}
		
		ObjectCoordinate[] data = this.data[rotation.getRotationId()];
		
		LocalMaterialData worldMaterial;
		ReplaceBlockMatrix replaceBlocks = null;
		int lastX = Integer.MIN_VALUE;
		int lastZ = Integer.MIN_VALUE;
		for (ObjectCoordinate point : data)
		{
			if ((worldMaterial = worldGenRegion.getMaterial(x + point.x, y + point.y, z + point.z)) != null)
			{
				if(
					this.doReplaceBlocks && 
					(lastX != x + point.x || lastZ != z + point.z))
				{
					replaceBlocks = worldGenRegion.getBiomeConfigForDecoration(x + point.x, z + point.z).getReplaceBlocks();
					lastX = x + point.x;
					lastZ = z + point.z;
				}
				if(worldMaterial.isAir())
				{
					if(this.doReplaceBlocks)
					{
						setBlock(worldGenRegion, x + point.x, y + point.y, z + point.z, point.material, replaceBlocks);
					} else {
						setBlock(worldGenRegion, x + point.x, y + point.y, z + point.z, point.material);
					}
				}
				else if (this.dig)
				{
					if(this.doReplaceBlocks)
					{
						setBlock(worldGenRegion, x + point.x, y + point.y, z + point.z, point.material, replaceBlocks);
					} else {
						setBlock(worldGenRegion, x + point.x, y + point.y, z + point.z, point.material);
					}
				}
			}
		}
		return true;
	}

	// Called during decoration.
	@Override
	public boolean process(CustomStructureCache structureCache, IWorldGenRegion worldGenRegion, Random rand)
	{
		if (this.branch)
		{
			return false;
		}

		int randomRoll = rand.nextInt(100);
		int ObjectRarity = this.rarity;
		boolean objectSpawned = false;
		int x;
		int z;
		while (randomRoll < ObjectRarity)
		{
			ObjectRarity -= 100;

			x = worldGenRegion.getDecorationArea().getChunkBeingDecorated().getBlockX() + rand.nextInt(Constants.CHUNK_SIZE);
			z = worldGenRegion.getDecorationArea().getChunkBeingDecorated().getBlockZ() + rand.nextInt(Constants.CHUNK_SIZE);

			// TODO: Are BO2/BO3 trees ever spawned via this method? If so, then don't replace blocks.
			objectSpawned = spawn(worldGenRegion, rand, x, z, this.spawnElevationMin, this.spawnElevationMax);
		}

		return objectSpawned;
	}

	@Override
	protected void writeConfigSettings(SettingsWriterBO4 writer, ILogger logger, IMaterialReader materialReader, CustomObjectResourcesManager manager) throws IOException
	{
		// It doesn't write.
	}

	@Override
	protected void readConfigSettings(String presetFolderName, Path otgRootFolder, ILogger logger, ICustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		this.spawnOnBlockType = readSettings(BO2Settings.SPAWN_ON_BLOCK_TYPE, logger, materialReader, manager);
		this.collisionBlockType = readSettings(BO2Settings.COLLISION_BLOCK_TYPE, logger, materialReader, manager);
		this.spawnSunlight = readSettings(BO2Settings.SPAWN_SUNLIGHT, logger, materialReader, manager);
		this.spawnDarkness = readSettings(BO2Settings.SPAWN_DARKNESS, logger, materialReader, manager);
		this.spawnWater = readSettings(BO2Settings.SPAWN_WATER, logger, materialReader, manager);
		this.spawnLava = readSettings(BO2Settings.SPAWN_LAVA, logger, materialReader, manager);
		this.spawnAboveGround = readSettings(BO2Settings.SPAWN_ABOVE_GROUND, logger, materialReader, manager);
		this.spawnUnderGround = readSettings(BO2Settings.SPAWN_UNDER_GROUND, logger, materialReader, manager);
		this.randomRotation = readSettings(BO2Settings.RANDON_ROTATION, logger, materialReader, manager);
		this.dig = readSettings(BO2Settings.DIG, logger, materialReader, manager);
		this.tree = readSettings(BO2Settings.TREE, logger, materialReader, manager);
		this.branch = readSettings(BO2Settings.BRANCH, logger, materialReader, manager);
		this.needsFoundation = readSettings(BO2Settings.NEEDS_FOUNDATION, logger, materialReader, manager);
		this.doReplaceBlocks = readSettings(BO2Settings.DO_REPLACE_BLOCKS, logger, materialReader, manager);
		this.rarity = readSettings(BO2Settings.RARITY, logger, materialReader, manager);
		this.collisionPercentage = readSettings(BO2Settings.COLLISION_PERCENTAGE, logger, materialReader, manager);
		this.spawnElevationMin = readSettings(BO2Settings.SPAWN_ELEVATION_MIN, logger, materialReader, manager);
		this.spawnElevationMax = readSettings(BO2Settings.SPAWN_ELEVATION_MAX, logger, materialReader, manager);
		this.readCoordinates(materialReader);
	}

	@Override
	protected void correctSettings() { }

	@Override
	protected void renameOldSettings() { }

	private void readCoordinates(IMaterialReader materialReader)
	{
		ArrayList<ObjectCoordinate> coordinates = new ArrayList<ObjectCoordinate>();
		for (Entry<String, String> line : this.reader.getRawSettings())
		{
			ObjectCoordinate buffer = ObjectCoordinate.getCoordinateFromString(line.getKey(), line.getValue(), materialReader);
			if (buffer != null)
			{
				coordinates.add(buffer);
			}
		}

		this.data[0] = new ObjectCoordinate[coordinates.size()];
		this.data[1] = new ObjectCoordinate[coordinates.size()];
		this.data[2] = new ObjectCoordinate[coordinates.size()];
		this.data[3] = new ObjectCoordinate[coordinates.size()];
		
		ObjectCoordinate coordinate;
		for (int i = 0; i < coordinates.size(); i++)
		{
			coordinate = coordinates.get(i);			
			this.data[0][i] = coordinate;
			coordinate = coordinate.rotate();
			this.data[1][i] = coordinate;
			coordinate = coordinate.rotate();
			this.data[2][i] = coordinate;
			coordinate = coordinate.rotate();
			this.data[3][i] = coordinate;
		}
	}

	private void setBlock(IWorldGenRegion worldGenRegion, int x, int y, int z, LocalMaterialData material)
	{
		worldGenRegion.setBlock(x, y, z, material);
	}
	
	private void setBlock(IWorldGenRegion worldGenRegion, int x, int y, int z, LocalMaterialData material, ReplaceBlockMatrix replacedBlocks)
	{
		worldGenRegion.setBlock(x, y, z, material, replacedBlocks);
	}
	
	@Override
	public boolean onEnable(String presetFolderName, Path otgRootFolder, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		if(!this.isEnabled)
		{
			this.isEnabled = true;
			enable(presetFolderName, otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker);
		}
		return true;
	}

	private void enable(String presetFolderName, Path otgRootFolder, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		readConfigSettings(presetFolderName, otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker);
		correctSettings();
	}

	@Override
	public boolean loadChecks(IModLoadedChecker modLoadedChecker)
	{
		return true;
	}
}
