package com.pg85.otg.customobject.bo4;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Random;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.constants.SettingsEnums.ConfigMode;
import com.pg85.otg.customobject.CustomObjectManager;
import com.pg85.otg.customobject.bo4.bo4function.BO4BlockFunction;
import com.pg85.otg.customobject.bo4.bo4function.BO4RandomBlockFunction;
import com.pg85.otg.customobject.config.CustomObjectResourcesManager;
import com.pg85.otg.customobject.config.io.FileSettingsReaderBO4;
import com.pg85.otg.customobject.config.io.FileSettingsWriterBO4;
import com.pg85.otg.customobject.creator.ObjectType;
import com.pg85.otg.customobject.structures.Branch;
import com.pg85.otg.customobject.structures.CustomStructureCache;
import com.pg85.otg.customobject.structures.StructuredCustomObject;
import com.pg85.otg.customobject.util.BoundingBox;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.interfaces.IBiomeConfig;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.interfaces.IModLoadedChecker;
import com.pg85.otg.interfaces.IWorldGenRegion;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.biome.ReplaceBlockMatrix;
import com.pg85.otg.util.nbt.NamedBinaryTag;
import com.pg85.otg.util.bo3.Rotation;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.LocalMaterials;

// TODO: Refactor type hierarchy for customobject/structure
public class BO4 implements StructuredCustomObject
{	
	public boolean isInvalidConfig;
	private BO4Config config;
	private final String name;
	private final File file;

	/**
	 * Creates a BO3 from a file.
	 *
	 * @param name Name of the BO3.
	 * @param file File of the BO3. If the file does not exist, a BO3 with the default settings is created.
	 */
	BO4(String name, File file)
	{
		this.name = name;
		this.file = file;
	}
	
	// Used by BO4Creator to export BO4's from WorldEdit selection, prevents instant writing on onEnable.
	public BO4(String name, File file, BO4Config settings)
	{
		this.name = name;
		this.file = file;
		this.config = settings;
	}
	
	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public BO4Config getConfig()
	{
		return config;
	}

	@Override
	public ObjectType getType()
	{
		return ObjectType.BO4;
	}

	@Override
	public boolean onEnable(String presetFolderName, Path otgRootFolder, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		if(isInvalidConfig)
		{
			return false;
		}
		if(this.config != null)
		{
			return true;
		}
		
		try
		{
			this.config = new BO4Config(new FileSettingsReaderBO4(name, file, logger), true, presetFolderName, otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker);
			if(this.config.settingsMode != ConfigMode.WriteDisable && !this.config.isBO4Data)
			{
				FileSettingsWriterBO4.writeToFile(this.config, this.config.settingsMode, logger, materialReader, manager);
			}
			// Merge inherited resources (after writing)
			this.config.loadInheritedBO3(presetFolderName, otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker);
		}
		catch(InvalidConfigException ex)
		{
			if(logger.getLogCategoryEnabled(LogCategory.CUSTOM_OBJECTS))
			{
				logger.log(LogLevel.ERROR, LogCategory.CUSTOM_OBJECTS, "Error occurred while enabling BO4 " + this.getName() + ": " + ex.getMessage());
			}
			isInvalidConfig = true;
			return false;
		}
		return true;
	}

	@Override
	public boolean canSpawnAsTree()
	{
		return false;
	}

	@Override
	public boolean canRotateRandomly()
	{
		return false;
	}	

	@Override
	public boolean loadChecks(IModLoadedChecker modLoadedChecker)
	{
		return true;
	}
	
	@Override
	public boolean spawnFromSapling(IWorldGenRegion worldGenRegion, Random random, Rotation rotation, int x, int y, int z)
	{
		return false;
	}
			
	// BO4 CustomStructures cannot be force-spawned, only plotted in unloaded chunks and then spawned when the chunk is decorated.
	@Override
	public boolean spawnForced(CustomStructureCache structureCache, IWorldGenRegion worldGenRegion, Random random, Rotation rotation, int x, int y, int z, boolean allowReplaceBlocks)
	{
		return false;
	}
	
	@Override
	public boolean spawnAsTree(CustomStructureCache structureCache, IWorldGenRegion worldGenRegion, Random random, int x, int z, int minY, int maxY)
	{
		return false;
	}

	@Override
	public boolean process(CustomStructureCache structureCache, IWorldGenRegion worldGenRegion, Random random)
	{
		return false;
	}

	public Branch[] getBranches()
	{
		return config.getbranches();
	}

	public boolean isCollidable()
	{
		return getConfig().isCollidable();
	}

	@Override
	public BoundingBox getBoundingBox(Rotation north)
	{
		if (this.config.boundingBoxes[0] == null)
		{
			BoundingBox box = BoundingBox.newEmptyBox();
			box.expandToFit(this.config.getminX(), this.config.getminY(), this.config.getminZ());
			box.expandToFit(this.config.getmaxX(), this.config.getmaxY(), this.config.getmaxZ());
			this.config.boundingBoxes[0] = box;
			box = box.rotate();
			this.config.boundingBoxes[1] = box;
			box = box.rotate();
			this.config.boundingBoxes[2] = box;
			box = box.rotate();
			this.config.boundingBoxes[3] = box;
		}

		return this.config.boundingBoxes[north.getRotationId()];
	}

	// BO4's should always spawn within decoration bounds, so there is no SpawnForced, only TrySpawnAt
	public boolean trySpawnAt(String presetFolderName, Path otgRootFolder, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker, IWorldGenRegion worldGenRegion, Random random, Rotation rotation, ChunkCoordinate chunkCoord, int x, int y, int z, String replaceAbove, String replaceBelow, boolean replaceWithBiomeBlocks, String replaceWithSurfaceBlock, String replaceWithGroundBlock, String replaceWithStoneBlock, boolean spawnUnderWater, int waterLevel, boolean isStructureAtSpawn, boolean doReplaceAboveBelowOnly, boolean doBiomeConfigReplaceBlocks)
	{
		//OTG.log(LogMarker.INFO, "Spawning " + this.getName() + " in Chunk X" + chunkCoord.getChunkX() + "Z" + chunkCoord.getChunkZ() + " at pos " + x + " " + y + " " + z);

		LocalMaterialData replaceBelowMaterial = null;
		LocalMaterialData replaceAboveMaterial = null;

		LocalMaterialData bo3SurfaceBlock = null;
		LocalMaterialData bo3GroundBlock = null;
		LocalMaterialData bo3StoneBlock = null;	

		if(config == null)
		{
			logger.log(LogLevel.FATAL, LogCategory.CUSTOM_OBJECTS, "Settings was null for BO4 " + this.getName() + ". This should not be happening, please contact team OTG about this crash.");
			throw new RuntimeException("Settings was null for BO4 " + this.getName() + ". This should not be happening, please contact team OTG about this crash.");
		}

		try {
			bo3SurfaceBlock = replaceWithSurfaceBlock != null && replaceWithSurfaceBlock.length() > 0 ? materialReader.readMaterial(replaceWithSurfaceBlock) : LocalMaterials.GRASS;
		} catch (InvalidConfigException e1) {
			bo3SurfaceBlock = LocalMaterials.GRASS;
			if(logger.getLogCategoryEnabled(LogCategory.CUSTOM_OBJECTS))
			{
				logger.log(LogLevel.ERROR, LogCategory.CUSTOM_OBJECTS, "Value " + replaceWithSurfaceBlock + " for replaceWithSurfaceBlock in BO4 " + this.getName() + " was not recognised. Using GRASS instead.");
			}
		}
		try {
			bo3GroundBlock = replaceWithGroundBlock != null && replaceWithGroundBlock.length() > 0 ? materialReader.readMaterial(replaceWithGroundBlock) : LocalMaterials.DIRT;
		} catch (InvalidConfigException e1) {
			bo3GroundBlock = LocalMaterials.DIRT;
			if(logger.getLogCategoryEnabled(LogCategory.CUSTOM_OBJECTS))
			{
				logger.log(LogLevel.ERROR, LogCategory.CUSTOM_OBJECTS, "Value " + replaceWithGroundBlock + " for replaceWithGroundBlock in BO4 " + this.getName() + " was not recognised. Using DIRT instead.");
			}
		}
		try {
			bo3StoneBlock = replaceWithStoneBlock != null && replaceWithStoneBlock.length() > 0 ? materialReader.readMaterial(replaceWithStoneBlock) : LocalMaterials.STONE;
		} catch (InvalidConfigException e1) {
			bo3StoneBlock = LocalMaterials.STONE;
			if(logger.getLogCategoryEnabled(LogCategory.CUSTOM_OBJECTS))
			{
				logger.log(LogLevel.ERROR, LogCategory.CUSTOM_OBJECTS, "Value " + replaceWithStoneBlock + " for replaceWithStoneBlock in BO4 " + this.getName() + " was not recognised. Using STONE instead.");
			}
		}
		
		try {
			replaceBelowMaterial = config.replaceBelow != null && config.replaceBelow.toLowerCase().equals("none") ? null : replaceBelow != null && replaceBelow.length() > 0 ? materialReader.readMaterial(replaceBelow) : null;
		} catch (InvalidConfigException e1) {
			replaceBelowMaterial = LocalMaterials.DIRT;
			if(logger.getLogCategoryEnabled(LogCategory.CUSTOM_OBJECTS))
			{
				logger.log(LogLevel.ERROR, LogCategory.CUSTOM_OBJECTS, "Value " + config.replaceBelow + " for replaceBelow in BO4 " + this.getName() + " was not recognised. Using DIRT instead.");
			}
		}
		try {
			replaceAboveMaterial = config.replaceAbove != null && config.replaceAbove.toLowerCase().equals("none") ? null : replaceAbove != null && replaceAbove.length() > 0 ? materialReader.readMaterial(replaceAbove) : null;
		} catch (InvalidConfigException e1) {
			replaceAboveMaterial = LocalMaterials.AIR;
			if(logger.getLogCategoryEnabled(LogCategory.CUSTOM_OBJECTS))
			{
				logger.log(LogLevel.ERROR, LogCategory.CUSTOM_OBJECTS, "Value " + config.replaceAbove + " for replaceAbove in BO4 " + this.getName() + " was not recognised. Using AIR instead.");
			}
		}

		boolean isOnBiomeBorder = false;

		IBiomeConfig biomeConfig = null;
		IBiomeConfig biomeConfig2 = null;
		IBiomeConfig biomeConfig3 = null;
		IBiomeConfig biomeConfig4 = null;

		biomeConfig = worldGenRegion.getBiomeConfigForDecoration(x, z);
		if(replaceWithBiomeBlocks)
		{
			biomeConfig2 = worldGenRegion.getBiomeConfigForDecoration(x + 15, z);
			biomeConfig3 = worldGenRegion.getBiomeConfigForDecoration(x, z + 15);
			biomeConfig4 = worldGenRegion.getBiomeConfigForDecoration(x + 15, z + 15);

			if(!(biomeConfig == biomeConfig2 && biomeConfig == biomeConfig3 && biomeConfig == biomeConfig4))
			{
				isOnBiomeBorder = true;
			}
		}

		// Get the right coordinates based on rotation

		ArrayList<Object[]> coordsAboveDone = new ArrayList<Object[]>();
		ArrayList<Object[]> coordsBelowDone = new ArrayList<Object[]>();

		BO4BlockFunction blockToQueueForSpawn = new BO4BlockFunction();
		LocalMaterialData sourceBlockMaterial;

		boolean outOfBounds = false;
		ChunkCoordinate destChunk;
		LocalMaterialData blockAbove;
		BO4RandomBlockFunction randomBlockFunction;		
		BO4BlockFunction newBlock;
		int rotations;
		boolean bFound;
		int blockY;
		int highestBlockToReplace;
		
		// Spawn
		long startTime = System.currentTimeMillis();
		BO4BlockFunction[] blocks = config.getBlocks(presetFolderName, otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker);
		if(blocks != null)
		{
			for (BO4BlockFunction block : config.getBlocks(presetFolderName, otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker))
			{
				if(block instanceof BO4RandomBlockFunction)
				{
					randomBlockFunction = ((BO4RandomBlockFunction)block);
					for (int i = 0; i < randomBlockFunction.blockCount; i++)
					{
						if (random.nextInt(100) < randomBlockFunction.blockChances[i])
						{
							block.nbtName = randomBlockFunction.metaDataNames[i];
							block.nbt = randomBlockFunction.metaDataTags[i];
							block.material = randomBlockFunction.blocks[i];
							break;
						}
					}
				}
	
				if(block.material == null)
				{
					continue;
				}
	
				if(rotation != Rotation.NORTH)
				{
					newBlock = new BO4BlockFunction();
					rotations = 0;
					// How many counter-clockwise rotations have to be applied?
					if(rotation == Rotation.WEST)
					{
						rotations = 1;
					}
					else if(rotation == Rotation.SOUTH)
					{
						rotations = 2;
					}
					else if(rotation == Rotation.EAST)
					{
						rotations = 3;
					}
	
					// Apply rotation
					if(rotations == 0)
					{
						newBlock.x = block.x;
						newBlock.z = block.z;
					}
					if(rotations == 1)
					{
						newBlock.x = block.z;
						newBlock.z = -block.x + 15;
						newBlock.material = block.material.rotate();
					}
					if(rotations == 2)
					{
						newBlock.x = -block.x + 15;
						newBlock.z = -block.z + 15;
						newBlock.material = block.material.rotate();
						newBlock.material = newBlock.material.rotate();
					}
					if(rotations == 3)
					{
						newBlock.x = -block.z + 15;
						newBlock.z = block.x;
						newBlock.material = block.material.rotate();
						newBlock.material = newBlock.material.rotate();
						newBlock.material = newBlock.material.rotate();
					}
					newBlock.y = block.y;
	
					newBlock.nbtName = block.nbtName;
					newBlock.nbt = block.nbt;
	
					if(isOnBiomeBorder)
					{
						biomeConfig = worldGenRegion.getBiomeConfigForDecoration(x + newBlock.x, z + newBlock.z);
					}
					
					// TODO: See BlockFunction.Spawn for what should be done with metadata
	
					if(replaceAboveMaterial != null && doReplaceAboveBelowOnly)
					{
						bFound = false;
						for(Object[] coords : coordsAboveDone)
						{
							if((Integer)coords[0] == x + newBlock.x && (Integer)coords[1] == z + newBlock.z)
							{
								bFound = true;
								break;
							}
						}

						if(!bFound)
						{
							coordsAboveDone.add(new Object[] { x + newBlock.x, z + newBlock.z });
							blockY = y + newBlock.y + 1; // TODO: This is wrong, should be the lowest block in the BO4 at these x-z coordinates. ReplaceAbove should be done before any blocks in this column are placed
							highestBlockToReplace = worldGenRegion.getHighestBlockYAt(x + newBlock.x, z + newBlock.z, true, true, false, false, true);
	
							while(blockY <= highestBlockToReplace && blockY > y + newBlock.y)
							{
								blockToQueueForSpawn = new BO4BlockFunction();
	
								// TODO: Make override leaves and air configurable
								// TODO: Make replaceAbove height configurable
								blockToQueueForSpawn.x = x + newBlock.x;
								blockToQueueForSpawn.y = (short) blockY;
								blockToQueueForSpawn.z = z + newBlock.z;
	
								blockToQueueForSpawn.nbtName = newBlock.nbtName;
								blockToQueueForSpawn.nbt = newBlock.nbt;
	
								destChunk = ChunkCoordinate.fromBlockCoords(blockToQueueForSpawn.x, blockToQueueForSpawn.z);
								if(chunkCoord.equals(destChunk))
								{
									if(spawnUnderWater && blockY >= waterLevel)
									{
										blockToQueueForSpawn.material = LocalMaterials.AIR;
									} else {
										// ReplaceAbove is not affected by sagc
										blockToQueueForSpawn.material = replaceAboveMaterial;								
									}
									setBlock(worldGenRegion, blockToQueueForSpawn.x, blockToQueueForSpawn.y, blockToQueueForSpawn.z, blockToQueueForSpawn.material, blockToQueueForSpawn.nbt, isStructureAtSpawn);
								} else {
									outOfBounds = true;
								}
	
								blockY += 1;
							}
						}
					}
								
					if(replaceBelowMaterial != null && newBlock.y == 0 && !newBlock.material.isEmptyOrAir() && doReplaceAboveBelowOnly)
					{
						bFound = false;
						for(Object[] coords : coordsBelowDone)
						{
							if((Integer)coords[0] == x + newBlock.x && (Integer)coords[1] == z + newBlock.z)
							{
								bFound = true;
								break;
							}
						}
	
						if(!bFound)
						{
							coordsBelowDone.add(new Object[] { x + newBlock.x, z + newBlock.z });
							blockY = y + newBlock.y - 1;
	
							// TODO: Make override leaves and air configurable
							// TODO: Make replaceBelow height configurable
							while(blockY > Constants.WORLD_DEPTH)
							{
								if(blockY < Constants.WORLD_HEIGHT)
								{
									sourceBlockMaterial = worldGenRegion.getMaterial(x + newBlock.x, blockY, z + newBlock.z);
									
									if(sourceBlockMaterial != null)
									{								
										blockToQueueForSpawn = new BO4BlockFunction();
										blockToQueueForSpawn.x = x + newBlock.x;
										blockToQueueForSpawn.y = (short) blockY;
										blockToQueueForSpawn.z = z + newBlock.z;
										blockToQueueForSpawn.material = replaceBelowMaterial;										
										blockToQueueForSpawn.nbtName = newBlock.nbtName;
										blockToQueueForSpawn.nbt = newBlock.nbt;
										
										destChunk = ChunkCoordinate.fromBlockCoords(blockToQueueForSpawn.x, blockToQueueForSpawn.z);
										if(chunkCoord.equals(destChunk))
										{
											// Apply sagc'd biome blocks
											if(replaceWithBiomeBlocks)
											{
												blockToQueueForSpawn.material = biomeConfig.getGroundBlockAtHeight(worldGenRegion, blockToQueueForSpawn.x, blockToQueueForSpawn.y, blockToQueueForSpawn.z);												
											} else {
												blockToQueueForSpawn.material = doBiomeConfigReplaceBlocks ? replaceBelowMaterial.parseWithBiomeAndHeight(biomeConfig.biomeConfigsHaveReplacement(), biomeConfig.getReplaceBlocks(), blockToQueueForSpawn.y) : replaceBelowMaterial;
												if(blockToQueueForSpawn.material == null)
												{
													blockToQueueForSpawn.material = LocalMaterials.DIRT;
												}
											}
											setBlock(worldGenRegion, blockToQueueForSpawn.x, blockToQueueForSpawn.y, blockToQueueForSpawn.z, blockToQueueForSpawn.material, blockToQueueForSpawn.nbt, isStructureAtSpawn);
										} else {
											outOfBounds = true;
										}
										if(sourceBlockMaterial.isSolid())
										{
											break;
										}
									}
								}
								
								blockY -= 1;
							}
						}
					}				
					
					if(y + newBlock.y > 0 && y + newBlock.y < 256 && !doReplaceAboveBelowOnly)
					{
						blockToQueueForSpawn = new BO4BlockFunction();
						blockToQueueForSpawn.x = x + newBlock.x;
						blockToQueueForSpawn.y = (short) (y + newBlock.y);
						blockToQueueForSpawn.z = z + newBlock.z;
						blockToQueueForSpawn.material = newBlock.material;
	
						blockToQueueForSpawn.nbtName = newBlock.nbtName;
						blockToQueueForSpawn.nbt = newBlock.nbt;
	
						destChunk = ChunkCoordinate.fromBlockCoords(blockToQueueForSpawn.x, blockToQueueForSpawn.z);
						if(chunkCoord.equals(destChunk))
						{							
							if(replaceWithBiomeBlocks)
							{
								if(blockToQueueForSpawn.material.equals(bo3GroundBlock))
								{
									blockToQueueForSpawn.material = biomeConfig.getGroundBlockAtHeight(worldGenRegion, blockToQueueForSpawn.x, blockToQueueForSpawn.y, blockToQueueForSpawn.z);
									setBlock(worldGenRegion, blockToQueueForSpawn.x, blockToQueueForSpawn.y, blockToQueueForSpawn.z, blockToQueueForSpawn.material, blockToQueueForSpawn.nbt, isStructureAtSpawn);
									continue;
								}
								else if(blockToQueueForSpawn.material.equals(bo3StoneBlock))
								{
									blockToQueueForSpawn.material = biomeConfig.getStoneBlockReplaced(blockToQueueForSpawn.y);
									setBlock(worldGenRegion, blockToQueueForSpawn.x, blockToQueueForSpawn.y, blockToQueueForSpawn.z, blockToQueueForSpawn.material, blockToQueueForSpawn.nbt, isStructureAtSpawn);
									continue;
								}
								else if(blockToQueueForSpawn.material.equals(bo3SurfaceBlock))
								{
									blockAbove = worldGenRegion.getMaterial(blockToQueueForSpawn.x, blockToQueueForSpawn.y + 1, blockToQueueForSpawn.z);
									if(blockAbove != null && (blockAbove.isSolid() || blockAbove.isLiquid()))
									{
										blockToQueueForSpawn.material = biomeConfig.getGroundBlockAtHeight(worldGenRegion, blockToQueueForSpawn.x, blockToQueueForSpawn.y, blockToQueueForSpawn.z);																	
									} else {
										blockToQueueForSpawn.material = biomeConfig.getSurfaceBlockAtHeight(worldGenRegion, blockToQueueForSpawn.x, blockToQueueForSpawn.y, blockToQueueForSpawn.z);
									}
	
									if(blockToQueueForSpawn.material.isAir())
									{
										if(blockToQueueForSpawn.y < biomeConfig.getWaterLevelMax())
										{
											blockToQueueForSpawn.material = LocalMaterials.WATER;
										} else {
											blockToQueueForSpawn.material = doBiomeConfigReplaceBlocks ? newBlock.material.parseWithBiomeAndHeight(biomeConfig.biomeConfigsHaveReplacement(), biomeConfig.getReplaceBlocks(), blockToQueueForSpawn.y) : newBlock.material;
										}
									}
									setBlock(worldGenRegion, blockToQueueForSpawn.x, blockToQueueForSpawn.y, blockToQueueForSpawn.z, blockToQueueForSpawn.material, blockToQueueForSpawn.nbt, isStructureAtSpawn);
									continue;
								}
							}
							
							// Don't spawn torches underwater
							if(
								spawnUnderWater && 
								blockToQueueForSpawn.material.isMaterial(LocalMaterials.TORCH) && 
								worldGenRegion.getMaterial(blockToQueueForSpawn.x, blockToQueueForSpawn.y, blockToQueueForSpawn.z).isLiquid()
							)
							{
								continue;
							}
							if(doBiomeConfigReplaceBlocks)
							{
								setBlock(worldGenRegion, blockToQueueForSpawn.x, blockToQueueForSpawn.y, blockToQueueForSpawn.z, blockToQueueForSpawn.material, blockToQueueForSpawn.nbt, isStructureAtSpawn, biomeConfig.getReplaceBlocks());
							} else {
								setBlock(worldGenRegion, blockToQueueForSpawn.x, blockToQueueForSpawn.y, blockToQueueForSpawn.z, blockToQueueForSpawn.material, blockToQueueForSpawn.nbt, isStructureAtSpawn);
							}
						} else {
							outOfBounds = true;
						}
					}
				} else {
	
					if(isOnBiomeBorder)
					{
						biomeConfig = worldGenRegion.getBiomeConfigForDecoration(x + block.x, z + block.z);
					}
					
					if(replaceAboveMaterial != null && doReplaceAboveBelowOnly)
					{
						bFound = false;
						for(Object[] coords : coordsAboveDone)
						{
							if((Integer)coords[0] == x + block.x && (Integer)coords[1] == z + block.z)
							{
								bFound = true;
								break;
							}
						}
	
						if(!bFound)
						{
							coordsAboveDone.add(new Object[] { x + block.x, z + block.z });
							blockY = (y + block.y + 1); // TODO: This is wrong, should be the lowest block in the BO3 at these x-z coordinates. replaceAbove should be done before any blocks in this column are placed
							highestBlockToReplace = worldGenRegion.getHighestBlockYAt(x + block.x, z + block.z, true, true, false, false, true);
	
							while(blockY <= highestBlockToReplace && blockY > y + block.y)
							{
								blockToQueueForSpawn = new BO4BlockFunction();
	
								if(spawnUnderWater && blockY >= waterLevel)// && replaceAboveMaterial.isLiquid())
								{
									blockToQueueForSpawn.material = LocalMaterials.AIR;
								} else {
									// ReplaceAbove is not affected by sagc
									blockToQueueForSpawn.material = replaceAboveMaterial;
								}
	
								blockToQueueForSpawn.x = x + block.x;
								blockToQueueForSpawn.y = (short)blockY;
								blockToQueueForSpawn.z = z + block.z;
	
								blockToQueueForSpawn.nbtName = block.nbtName;
								blockToQueueForSpawn.nbt = block.nbt;
	
								destChunk = ChunkCoordinate.fromBlockCoords(blockToQueueForSpawn.x, blockToQueueForSpawn.z);
								if(chunkCoord.equals(destChunk))
								{
										setBlock(worldGenRegion, blockToQueueForSpawn.x, blockToQueueForSpawn.y, blockToQueueForSpawn.z, blockToQueueForSpawn.material, blockToQueueForSpawn.nbt, isStructureAtSpawn);
								} else {
									outOfBounds = true;
								}
								blockY += 1;
							}
						}
					}				
					
					if(replaceBelowMaterial != null && block.y == 0 && !block.material.isEmptyOrAir() && doReplaceAboveBelowOnly)
					{
						bFound = false;
						for(Object[] coords : coordsBelowDone)
						{
							if((Integer)coords[0] == x + block.x && (Integer)coords[1] == z + block.z)
							{
								bFound = true;
								break;
							}
						}
	
						if(!bFound)
						{
							coordsBelowDone.add(new Object[] { x + block.x, z + block.z });
							blockY = y + block.y - 1;
	
							// TODO: Make override leaves and air configurable
							// TODO: Make replaceBelow height configurable
							while(blockY > Constants.WORLD_DEPTH)
							{
								if(blockY < Constants.WORLD_HEIGHT)
								{
									sourceBlockMaterial = worldGenRegion.getMaterial(x + block.x, blockY, z + block.z);
									
									if(sourceBlockMaterial != null)
									{
										blockToQueueForSpawn = new BO4BlockFunction();
										blockToQueueForSpawn.x = x + block.x;
										blockToQueueForSpawn.y = (short) blockY;
										blockToQueueForSpawn.z = z + block.z;
										blockToQueueForSpawn.material = replaceBelowMaterial;										
										blockToQueueForSpawn.nbtName = block.nbtName;
										blockToQueueForSpawn.nbt = block.nbt;
										
										destChunk = ChunkCoordinate.fromBlockCoords(blockToQueueForSpawn.x, blockToQueueForSpawn.z);
										if(chunkCoord.equals(destChunk))
										{
											// Apply sagc'd biome blocks
											if(replaceWithBiomeBlocks)
											{
												blockToQueueForSpawn.material = biomeConfig.getGroundBlockAtHeight(worldGenRegion, blockToQueueForSpawn.x, blockToQueueForSpawn.y, blockToQueueForSpawn.z);											
											} else {
												blockToQueueForSpawn.material = doBiomeConfigReplaceBlocks ? replaceBelowMaterial.parseWithBiomeAndHeight(biomeConfig.biomeConfigsHaveReplacement(), biomeConfig.getReplaceBlocks(), blockToQueueForSpawn.y) : replaceBelowMaterial;
												if(blockToQueueForSpawn.material == null)
												{
													blockToQueueForSpawn.material = LocalMaterials.DIRT;
												}
											}
											setBlock(worldGenRegion, blockToQueueForSpawn.x, blockToQueueForSpawn.y, blockToQueueForSpawn.z, blockToQueueForSpawn.material, blockToQueueForSpawn.nbt, isStructureAtSpawn);
										} else {
											outOfBounds = true;
										}
										if(sourceBlockMaterial.isSolid())
										{
											break;
										}
									}
								}
	
								blockY -= 1;
							}
						}					
					}
					
					if(y + block.y > 0 && y + block.y < 256 && !doReplaceAboveBelowOnly)
					{
						blockToQueueForSpawn = new BO4BlockFunction();
						blockToQueueForSpawn.x = x + block.x;
						blockToQueueForSpawn.y = (short) (y + block.y);
						blockToQueueForSpawn.z = z + block.z;
						blockToQueueForSpawn.material = block.material;
	
						blockToQueueForSpawn.nbtName = block.nbtName;
						blockToQueueForSpawn.nbt = block.nbt;
	
						destChunk = ChunkCoordinate.fromBlockCoords(blockToQueueForSpawn.x, blockToQueueForSpawn.z);
						if(chunkCoord.equals(destChunk))
						{
							if(replaceWithBiomeBlocks)
							{								
								if(blockToQueueForSpawn.material.equals(bo3GroundBlock))
								{
									blockToQueueForSpawn.material = biomeConfig.getGroundBlockAtHeight(worldGenRegion, blockToQueueForSpawn.x, blockToQueueForSpawn.y, blockToQueueForSpawn.z);
									setBlock(worldGenRegion, blockToQueueForSpawn.x, blockToQueueForSpawn.y, blockToQueueForSpawn.z, blockToQueueForSpawn.material, blockToQueueForSpawn.nbt, isStructureAtSpawn);
									continue;
								}
								else if(blockToQueueForSpawn.material.equals(bo3StoneBlock))
								{
									blockToQueueForSpawn.material = biomeConfig.getStoneBlockReplaced(blockToQueueForSpawn.y);
									setBlock(worldGenRegion, blockToQueueForSpawn.x, blockToQueueForSpawn.y, blockToQueueForSpawn.z, blockToQueueForSpawn.material, blockToQueueForSpawn.nbt, isStructureAtSpawn);
									continue;
								}
								else if(blockToQueueForSpawn.material.equals(bo3SurfaceBlock))
								{
									blockAbove = worldGenRegion.getMaterial(blockToQueueForSpawn.x, blockToQueueForSpawn.y + 1, blockToQueueForSpawn.z);
									if(blockAbove != null && (blockAbove.isSolid() || blockAbove.isLiquid()))
									{
										blockToQueueForSpawn.material = biomeConfig.getGroundBlockAtHeight(worldGenRegion, blockToQueueForSpawn.x, blockToQueueForSpawn.y, blockToQueueForSpawn.z);																	
									} else {
										blockToQueueForSpawn.material = biomeConfig.getSurfaceBlockAtHeight(worldGenRegion, blockToQueueForSpawn.x, blockToQueueForSpawn.y, blockToQueueForSpawn.z);
									}
	
									if(blockToQueueForSpawn.material.isAir())
									{
										if(blockToQueueForSpawn.y < biomeConfig.getWaterLevelMax())
										{
											blockToQueueForSpawn.material = LocalMaterials.WATER;
										} else {
											blockToQueueForSpawn.material = doBiomeConfigReplaceBlocks ? block.material.parseWithBiomeAndHeight(biomeConfig.biomeConfigsHaveReplacement(), biomeConfig.getReplaceBlocks(), blockToQueueForSpawn.y) : block.material;
										}
									}
									setBlock(worldGenRegion, blockToQueueForSpawn.x, blockToQueueForSpawn.y, blockToQueueForSpawn.z, blockToQueueForSpawn.material, blockToQueueForSpawn.nbt, isStructureAtSpawn);
									continue;
								}
							}
							
							// Don't spawn torches underwater
							if(
								spawnUnderWater && 
								blockToQueueForSpawn.material.isMaterial(LocalMaterials.TORCH) && 
								worldGenRegion.getMaterial(blockToQueueForSpawn.x, blockToQueueForSpawn.y, blockToQueueForSpawn.z).isLiquid()
							)
							{
								continue;
							}
							if(doBiomeConfigReplaceBlocks)
							{
								setBlock(worldGenRegion, blockToQueueForSpawn.x, blockToQueueForSpawn.y, blockToQueueForSpawn.z, blockToQueueForSpawn.material, blockToQueueForSpawn.nbt, isStructureAtSpawn, biomeConfig.getReplaceBlocks());
							} else {
								setBlock(worldGenRegion, blockToQueueForSpawn.x, blockToQueueForSpawn.y, blockToQueueForSpawn.z, blockToQueueForSpawn.material, blockToQueueForSpawn.nbt, isStructureAtSpawn);
							}
						} else {
							outOfBounds = true;
						}
					}
				}
			}
			if(outOfBounds)
			{
				if(logger.getLogCategoryEnabled(LogCategory.CUSTOM_OBJECTS))
				{
					logger.log(LogLevel.ERROR, LogCategory.CUSTOM_OBJECTS, "BO4 " + this.getName() + " tried to spawn blocks outside of the chunk being decorated, the blocks have been ignored. This can happen if a BO3 is not sliced into 16x16 pieces or has branches positioned in such a way that they cross a chunk border. OTG is more strict than TC in how branching BO4's used as CustomStructures() should be designed, BO4 creators have to design their BO4's and position their branches so that they fit neatly into a 16x16 grid. Hopefully in a future release OTG can be made to automatically slice branching structures instead of forcing the BO4 creator to do it.");
				}
			}
	
			if(logger.getLogCategoryEnabled(LogCategory.PERFORMANCE) && (System.currentTimeMillis() - startTime) > 50)
			{
				logger.log(LogLevel.WARN, LogCategory.PERFORMANCE, "Warning: Spawning BO4 " + this.getName()  + " took " + (System.currentTimeMillis() - startTime) + " Ms.");
			}
		}

		return true;
	}
	
	private void setBlock(IWorldGenRegion worldGenRegion, int x, int y, int z, LocalMaterialData material, NamedBinaryTag metaDataTag, boolean isStructureAtSpawn)
	{
		setBlock(worldGenRegion, x, y, z, material, metaDataTag, isStructureAtSpawn, null);
	}
	
	private void setBlock(IWorldGenRegion worldGenRegion, int x, int y, int z, LocalMaterialData material, NamedBinaryTag metaDataTag, boolean isStructureAtSpawn, ReplaceBlockMatrix replaceBlocks)
	{
		if(worldGenRegion.getPluginConfig().getDeveloperModeEnabled())
		{
			LocalMaterialData worldMaterial = worldGenRegion.getMaterial(x, y, z);
			if(
				worldMaterial.isMaterial(LocalMaterials.GOLD_BLOCK) ||
				worldMaterial.isMaterial(LocalMaterials.IRON_BLOCK) ||
				worldMaterial.isMaterial(LocalMaterials.REDSTONE_BLOCK) ||
				worldMaterial.isMaterial(LocalMaterials.DIAMOND_BLOCK) ||
				worldMaterial.isMaterial(LocalMaterials.LAPIS_BLOCK) ||
				worldMaterial.isMaterial(LocalMaterials.COAL_BLOCK) ||
				worldMaterial.isMaterial(LocalMaterials.QUARTZ_BLOCK) ||
				worldMaterial.isMaterial(LocalMaterials.EMERALD_BLOCK)
			)
			{
				if(
					material.isMaterial(LocalMaterials.GOLD_BLOCK) ||
					material.isMaterial(LocalMaterials.IRON_BLOCK) ||
					material.isMaterial(LocalMaterials.REDSTONE_BLOCK) ||
					material.isMaterial(LocalMaterials.DIAMOND_BLOCK) ||
					material.isMaterial(LocalMaterials.LAPIS_BLOCK) ||
					material.isMaterial(LocalMaterials.COAL_BLOCK) ||
					material.isMaterial(LocalMaterials.QUARTZ_BLOCK) ||
					material.isMaterial(LocalMaterials.EMERALD_BLOCK)
				)
				{
					worldGenRegion.setBlock(x, y, z, LocalMaterials.GLOWSTONE);
					return;
				}
			}
		}
		if(replaceBlocks != null)
		{
			worldGenRegion.setBlock(x, y, z, material, metaDataTag, replaceBlocks);
		} else {
			worldGenRegion.setBlock(x, y, z, material, metaDataTag);
		}
	}

	@Override
	public boolean doReplaceBlocks()
	{
		return this.config.doReplaceBlocks;
	}
}
