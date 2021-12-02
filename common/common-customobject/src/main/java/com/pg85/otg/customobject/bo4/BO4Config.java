package com.pg85.otg.customobject.bo4;

import com.pg85.otg.config.standard.WorldStandardValues;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.constants.SettingsEnums.ConfigMode;
import com.pg85.otg.customobject.CustomObject;
import com.pg85.otg.customobject.CustomObjectManager;
import com.pg85.otg.customobject.bo4.bo4function.BO4BlockFunction;
import com.pg85.otg.customobject.bo4.bo4function.BO4BranchFunction;
import com.pg85.otg.customobject.bo4.bo4function.BO4EntityFunction;
import com.pg85.otg.customobject.bo4.bo4function.BO4RandomBlockFunction;
import com.pg85.otg.customobject.bo4.bo4function.BO4WeightedBranchFunction;
import com.pg85.otg.customobject.bofunctions.BlockFunction;
import com.pg85.otg.customobject.bofunctions.BranchFunction;
import com.pg85.otg.customobject.config.CustomObjectConfigFile;
import com.pg85.otg.customobject.config.CustomObjectConfigFunction;
import com.pg85.otg.customobject.config.CustomObjectErroredFunction;
import com.pg85.otg.customobject.config.CustomObjectResourcesManager;
import com.pg85.otg.customobject.config.io.SettingsReaderBO4;
import com.pg85.otg.customobject.config.io.SettingsWriterBO4;
import com.pg85.otg.customobject.structures.bo4.BO4CustomStructureCoordinate;
import com.pg85.otg.customobject.util.BO3Enums.SpawnHeightEnum;
import com.pg85.otg.customobject.util.BoundingBox;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.interfaces.ICustomObjectManager;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.interfaces.IModLoadedChecker;
import com.pg85.otg.util.nbt.NamedBinaryTag;
import com.pg85.otg.util.bo3.Rotation;
import com.pg85.otg.util.helpers.StreamHelper;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.minecraft.DefaultStructurePart;

import java.io.DataOutput;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.zip.DataFormatException;

public class BO4Config extends CustomObjectConfigFile
{
	// TODO: Split this up into multiple config classes like common-core
	// does for world/biome configs, add getters etc.
	
	public String author;
	public String description;
	public boolean doReplaceBlocks;
	public int frequency;
	public Rotation fixedRotation;
	
	private final int xSize = 16;
	private final int zSize = 16;
	public int minHeight;
	public int maxHeight;

	public SpawnHeightEnum spawnHeight;
	public boolean useCenterForHighestBlock;
	public BoundingBox[] boundingBoxes = new BoundingBox[4];

	private BO4BlockFunction[][] heightMap;
	
	private boolean inheritedBO3Loaded;
	
	// These are used in CustomObjectStructure when determining the minimum area in chunks that
	// this branching structure needs to be able to spawn
	public int minimumSizeTop = -1;
	public int minimumSizeBottom = -1;
	public int minimumSizeLeft = -1;
	public int minimumSizeRight = -1;

	public int timesSpawned = 0;
	
	public int branchFrequency;
	// Define groups that this BO3 belongs to with a range in chunks that members of each group should have to each other
	private String branchFrequencyGroup;
	public HashMap<String, Integer> branchFrequencyGroups;

	private int minX;
	private int maxX;
	private int minY;
	private int maxY;
	private int minZ;
	private int maxZ;

	private ArrayList<String> inheritedBO3s;

	// Adjusts the height by this number before spawning. Handy when using "highestblock" for lowering BO3s that have a lot of ground under them included
	public int heightOffset;
	private Rotation inheritBO3Rotation;
	// If this is set to true then any air blocks in the bo3 will not be spawned
	private boolean removeAir;
	private boolean configRemoveAir;
	// Defaults to false. Set to true if this BO3 should spawn at the player spawn point. When the server starts one of the structures that has IsSpawnPoint set to true is selected randomly and is spawned, the others never get spawned.)
	public boolean isSpawnPoint;

	// Replaces all the non-air blocks that are above this BO3 or its smoothing area with the given block material (should be WATER or AIR or NONE), also applies to smoothing areas although it intentionally leaves some of the terrain above them intact. WATER can be used in combination with SpawnUnderWater to fill any air blocks underneath waterlevel with water (and any above waterlevel with air).
	public String replaceAbove;
	public String configReplaceAbove;
	// Replaces all non-air blocks underneath the BO3 (but not its smoothing area) with the designated material until a solid block is found.
	public String replaceBelow;
	public String configReplaceBelow;
	// Defaults to true. If set to true then every block in the BO3 of the materials defined in ReplaceWithGroundBlock or ReplaceWithSurfaceBlock will be replaced by the GroundBlock or SurfaceBlock materials configured for the biome the block is spawned in.
	public boolean replaceWithBiomeBlocks;
	// Replaces all the blocks of the given material in the BO3 with the GroundBlock configured for the biome it spawns in
	public String replaceWithGroundBlock;
	// Replaces all the blocks of the given material in the BO3 with the SurfaceBlock configured for the biome it spawns in
	public String replaceWithSurfaceBlock;
	// Replaces all the blocks of the given material in the BO3 with the StoneBlock configured for the biome it spawns in
	public String replaceWithStoneBlock;
	// Define a group that this BO3 belongs to and a range in chunks that members of this group should have to each other
	private String bo3Group;
	public HashMap<String, Integer> bo4Groups;
	// If this is set to true then this BO3 can spawn on top of or inside other BO3's
	public boolean canOverride;

	// Copies the blocks and branches of an existing BO3 into this one
	private String inheritBO3;
	// Should the smoothing area go to the top or the bottom blocks in the bo3?
	public boolean smoothStartTop;
	public boolean smoothStartWood;
	// The size of the smoothing area
	public int smoothRadius;
	// The materials used for the smoothing area
	public String smoothingSurfaceBlock;
	public String smoothingGroundBlock;
	// If true then root BO3 smoothing and height settings are used for all children
	public boolean overrideChildSettings;
	public boolean overrideParentHeight;

	// Used to make sure that dungeons can only spawn underneath other structures
	public boolean mustBeBelowOther;
	
	// Used to make sure that dungeons can only spawn inside worldborders
	public boolean mustBeInsideWorldBorders;

	private String replacesBO3;
	public ArrayList<String> replacesBO3Branches;
	private String mustBeInside;
	public ArrayList<String> mustBeInsideBranches;
	private String cannotBeInside;
	public ArrayList<String> cannotBeInsideBranches;

	public int smoothHeightOffset;
	public boolean canSpawnOnWater;
	public boolean spawnOnWaterOnly;
	public boolean spawnUnderWater;
	public boolean spawnAtWaterLevel;

	private String presetFolderName;

	// Store blocks in arrays instead of as BO4BlockFunctions,
	// since that gives way too much overhead memory wise.
	// We may have tens of millions of blocks, java doesn't handle lots of small classes well.
	private short[][][]blocks;
	private LocalMaterialData[]blocksMaterial;
	private String[]blocksMetaDataName;
	private NamedBinaryTag[]blocksMetaDataTag;

	private LocalMaterialData[][] randomBlocksBlocks;
	private byte[][] randomBlocksBlockChances;
	private String[][] randomBlocksMetaDataNames;
	private NamedBinaryTag[][] randomBlocksMetaDataTags;
	private byte[] randomBlocksBlockCount;	
	//
	
	private BO4BranchFunction[] branchesBO4;
	private BO4EntityFunction[] entityDataBO4;
		
	private boolean isCollidable = false;
	boolean isBO4Data = false;
		
	/**
	 * Creates a BO4Config from a file.
	 *
	 * @param reader		The settings of the BO4.
	 */
	public BO4Config(SettingsReaderBO4 reader, boolean init, String presetFolderName, Path otgRootFolder, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker) throws InvalidConfigException
	{
		super(reader);
		if(init)
		{
			init(presetFolderName, otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker);
		}
	}

	private BO4Config(SettingsReaderBO4 reader)
	{
		super(reader);
	}
	
	static int BO4BlocksLoadedFromBO4Data = 0;
	static int accumulatedTime = 0;
	static int accumulatedTime2 = 0;
	private void init(String presetFolderName, Path otgRootFolder, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker) throws InvalidConfigException
	{
		this.minX = Integer.MAX_VALUE;
		this.maxX = Integer.MIN_VALUE;
		this.minY = Integer.MAX_VALUE;
		this.maxY = Integer.MIN_VALUE;
		this.minZ = Integer.MAX_VALUE;
		this.maxZ = Integer.MIN_VALUE;
		if(!this.reader.getFile().getAbsolutePath().toLowerCase().endsWith(".bo4data"))
		{
			//long startTime = System.currentTimeMillis();
			readConfigSettings(presetFolderName, otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker);
			//BO4BlocksLoaded++;
			//long timeTaken = (System.currentTimeMillis() - startTime);
			//accumulatedTime += timeTaken;
			//OTG.log(LogMarker.INFO, "BO4's loaded: " + BO4BlocksLoaded + " in " + accumulatedTime);
			//OTG.log(LogMarker.INFO, ".BO4 loaded in: " + timeTaken + " " + this.getName() + ".BO4");
		} else {
			//long startTime = System.currentTimeMillis();
			this.readFromBO4DataFile(false, logger, materialReader);
			//BO4BlocksLoadedFromBO4Data++;
			//long timeTaken = (System.currentTimeMillis() - startTime);
			//accumulatedTime2 += timeTaken;			
			//OTG.log(LogMarker.INFO, ".BO4Data's loaded: " + BO4BlocksLoadedFromBO4Data + " in " + accumulatedTime2);
			//OTG.log(LogMarker.INFO, ".BO4Data loaded in: " + timeTaken + " " + this.getName()  + ".BO4Data");
		}

		// When writing, we'll need to read some raw data from the file,
		// so can't flush the cache yet. Flush after writing.
		if(this.settingsMode == ConfigMode.WriteDisable)
		{
			this.reader.flushCache();
		}
	}

	public int getXOffset()
	{
		return minX < -8 ? -minX : maxX > 7 ? -minX : 8;
	}

	public int getZOffset()
	{
		return minZ < -7 ? -minZ : maxZ > 8 ? -minZ : 7;
	}

	public int getminX()
	{
		return minX + this.getXOffset(); // + xOffset makes sure that the value returned is never negative which is necessary for the collision detection code for CustomStructures in OTG (it assumes the furthest top and left blocks are at => 0 x or >= 0 z in the BO3)
	}

	public int getmaxX()
	{
		return maxX + this.getXOffset(); // + xOffset makes sure that the value returned is never negative which is necessary for the collision detection code for CustomStructures in OTG (it assumes the furthest top and left blocks are at => 0 x or >= 0 z in the BO3)
	}

	public int getminY()
	{
		return minY;
	}

	public int getmaxY()
	{
		return maxY;
	}

	public int getminZ()
	{
		return minZ + this.getZOffset(); // + zOffset makes sure that the value returned is never negative which is necessary for the collision detection code for CustomStructures in OTG (it assumes the furthest top and left blocks are at => 0 x or >= 0 z in the BO3)
	}

	public int getmaxZ()
	{
		return maxZ + this.getZOffset(); // + zOffset makes sure that the value returned is never negative which is necessary for the collision detection code for CustomStructures in OTG (it assumes the furthest top and left blocks are at => 0 x or >= 0 z in the BO3)
	}
	
	public ArrayList<String> getInheritedBO3s()
	{
		return this.inheritedBO3s;
	}

	public BO4BlockFunction[][] getSmoothingHeightMap(BO4 start, String presetFolderName, Path otgRootFolder, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		return getSmoothingHeightMap(start, true, presetFolderName, otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker);
	}
	
	private BO4BlockFunction[][] getSmoothingHeightMap(BO4 start, boolean fromFile, String presetFolderName, Path otgRootFolder, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		// TODO: Caching the heightmap will mean this BO4 can only be used with 1 master BO4,
		// it won't pick up smoothing area settings if it is also used in another structure.
		if(this.heightMap == null)
		{
			if(this.isBO4Data && fromFile)
			{
				BO4Config bo4Config = null;
				try
				{
					bo4Config = new BO4Config(this.reader, false, presetFolderName, otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker);
				}
				catch (InvalidConfigException e)
				{
					if(logger.getLogCategoryEnabled(LogCategory.CUSTOM_OBJECTS))
					{
						logger.log(LogLevel.ERROR, LogCategory.CUSTOM_OBJECTS, "Error fetching smoothing heightmap for BO4 " + start.getName() + ": " + e.getMessage());
					}
				}
				if(bo4Config != null)
				{
					try {
						bo4Config.readFromBO4DataFile(true, logger, materialReader);
					} catch (InvalidConfigException e) {
						if(logger.getLogCategoryEnabled(LogCategory.CUSTOM_OBJECTS))
						{
							logger.log(LogLevel.ERROR, LogCategory.CUSTOM_OBJECTS, "Error fetching smoothing heightmap for BO4Data " + start.getName() + ": " + e.getMessage());
						}
						this.heightMap = new BO4BlockFunction[16][16];
						return this.heightMap;
					}
					this.heightMap = bo4Config.getSmoothingHeightMap(start, false, presetFolderName, otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker);
					return this.heightMap;
				}
			}
			
			this.heightMap = new BO4BlockFunction[16][16];

			// make heightmap containing the highest or lowest blocks in this chunk
			int blockIndex = 0;
			LocalMaterialData material;
			boolean isSmoothAreaAnchor;
			boolean isRandomBlock;
			int y;
			for(int x = 0; x < xSize; x++)
			{
				for(int z = 0; z < zSize; z++)
				{
					if(blocks[x][z] != null)
					{
						for(int i = 0; i < blocks[x][z].length; i++)
						{
							isSmoothAreaAnchor = false;
							isRandomBlock = this.randomBlocksBlocks[blockIndex] != null;
							y = blocks[x][z][i];
							
							if(isRandomBlock)
							{
								for(LocalMaterialData randomMaterial : this.randomBlocksBlocks[blockIndex])
								{
									// TODO: Material should never be null, fix the code in RandomBlockFunction.load() that causes this.
									if(randomMaterial == null)
									{
										continue;
									}
									if(randomMaterial.isSmoothAreaAnchor(start.getConfig().overrideChildSettings && this.overrideChildSettings ? start.getConfig().smoothStartWood : this.smoothStartWood, start.getConfig().spawnUnderWater))
									{
										isSmoothAreaAnchor = true;
										break;
									}
								}
							}

							material = this.blocksMaterial[blockIndex];
							if(
								isSmoothAreaAnchor ||
								(
									!isRandomBlock &&
									material.isSmoothAreaAnchor(start.getConfig().overrideChildSettings && this.overrideChildSettings ? start.getConfig().smoothStartWood : this.smoothStartWood, start.getConfig().spawnUnderWater)
								)
							)
							{
								if(
									(!(start.getConfig().overrideChildSettings && this.overrideChildSettings ? start.getConfig().smoothStartTop : this.smoothStartTop) && y == getminY()) ||
									((start.getConfig().overrideChildSettings && this.overrideChildSettings ? start.getConfig().smoothStartTop : this.smoothStartTop) && (this.heightMap[x][z] == null || y > this.heightMap[x][z].y))
								)
								{
									BO4BlockFunction blockFunction = null;
									if(isRandomBlock)
									{
										blockFunction = new BO4RandomBlockFunction();
										((BO4RandomBlockFunction)blockFunction).blocks = this.randomBlocksBlocks[blockIndex];
										((BO4RandomBlockFunction)blockFunction).blockChances = this.randomBlocksBlockChances[blockIndex];
										((BO4RandomBlockFunction)blockFunction).metaDataNames = this.randomBlocksMetaDataNames[blockIndex];
										((BO4RandomBlockFunction)blockFunction).metaDataTags = this.randomBlocksMetaDataTags[blockIndex];
										((BO4RandomBlockFunction)blockFunction).blockCount = this.randomBlocksBlockCount[blockIndex];
									} else {
										blockFunction = new BO4BlockFunction();
									}
									blockFunction.material = material;
									blockFunction.x = x;
									blockFunction.y = (short) y;
									blockFunction.z = z;										
									blockFunction.nbtName = this.blocksMetaDataName[blockIndex];
									blockFunction.nbt = this.blocksMetaDataTag[blockIndex];
									
									this.heightMap[x][z] = blockFunction;
								}
							}
							
							blockIndex++;
						}
					}
				}
			}
		}
		return this.heightMap;
	}

	BO4BlockFunction[] getBlocks(String presetFolderName, Path otgRootFolder, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		return getBlocks(true, presetFolderName, otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker);
	}
	
	private BO4BlockFunction[] getBlocks(boolean fromFile, String presetFolderName, Path otgRootFolder, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		if(fromFile && this.isBO4Data)
		{
			BO4Config bo4Config = null;
			try
			{
				bo4Config = new BO4Config(this.reader, false, presetFolderName, otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker);
			}
			catch (InvalidConfigException e)
			{
				if(logger.getLogCategoryEnabled(LogCategory.CUSTOM_OBJECTS))
				{
					logger.log(LogLevel.ERROR, LogCategory.CUSTOM_OBJECTS, " Error fetching blocks for BO4 " + this.getName() + ": " + e.getMessage());
				}
			}
			if(bo4Config != null)
			{
				try {
					bo4Config.readFromBO4DataFile(true, logger, materialReader);
				} catch (InvalidConfigException e) {
					if(logger.getLogCategoryEnabled(LogCategory.CUSTOM_OBJECTS))
					{
						logger.log(LogLevel.ERROR, LogCategory.CUSTOM_OBJECTS, " Error fetching blocks for BO4Data " + this.getName() + ": " + e.getMessage());
					}
					return null;
				}
				return bo4Config.getBlocks(false, presetFolderName, otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker);
			}
		}
		
		BO4BlockFunction[] blocksOTGPlus = new BO4BlockFunction[this.blocksMaterial.length];
		
		BO4BlockFunction block;
		int blockIndex = 0;
		for(int x = 0; x < xSize; x++)
		{
			for(int z = 0; z < zSize; z++)
			{
				if(this.blocks[x][z] != null)
				{
					for(int i = 0; i < this.blocks[x][z].length; i++)
					{
						if(this.randomBlocksBlocks[blockIndex] != null)
						{
							block = new BO4RandomBlockFunction(this);
							((BO4RandomBlockFunction)block).blocks = this.randomBlocksBlocks[blockIndex];
							((BO4RandomBlockFunction)block).blockChances = this.randomBlocksBlockChances[blockIndex];
							((BO4RandomBlockFunction)block).metaDataNames = this.randomBlocksMetaDataNames[blockIndex];
							((BO4RandomBlockFunction)block).metaDataTags = this.randomBlocksMetaDataTags[blockIndex];
							((BO4RandomBlockFunction)block).blockCount = this.randomBlocksBlockCount[blockIndex];
						} else {
							block = new BO4BlockFunction(this);
						}
						
						block.x = x;
						block.y = this.blocks[x][z][i];
						block.z = z;
						block.material = this.blocksMaterial[blockIndex];
						block.nbtName = this.blocksMetaDataName[blockIndex];
						block.nbt = this.blocksMetaDataTag[blockIndex];
												
						blocksOTGPlus[blockIndex] = block;
						blockIndex++;
					} 
				}
			}
		}

		return blocksOTGPlus;
	}

	public BO4BranchFunction[] getbranches()
	{
		return this.branchesBO4;
	}

	public BO4EntityFunction[] getEntityData()
	{
		return this.entityDataBO4;
	}
	
	void loadInheritedBO3(String presetFolderName, Path otgRootFolder, ILogger logger, ICustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		if(this.inheritBO3 != null && this.inheritBO3.trim().length() > 0 && !this.inheritedBO3Loaded)
		{
			File currentFile = this.getFile().getParentFile();
			this.presetFolderName = currentFile.getName();
			while(currentFile.getParentFile() != null && !currentFile.getName().equals(Constants.PRESETS_FOLDER))
			{
				this.presetFolderName = currentFile.getName();
				currentFile = currentFile.getParentFile();
				if(this.presetFolderName.equals(Constants.GLOBAL_OBJECTS_FOLDER))
				{
					this.presetFolderName = null;
					break;
				}
			}
			
			// TODO: Re-wire this so we don't have to cast CustomObjectManager :(
			CustomObjectManager customObjectManager2 = (CustomObjectManager)customObjectManager;			
			CustomObject parentBO3 = customObjectManager2.getGlobalObjects().getObjectByName(this.inheritBO3, this.presetFolderName, otgRootFolder, logger, customObjectManager2, materialReader, manager, modLoadedChecker);
			if(parentBO3 != null)
			{
				BO4BlockFunction[] blocks = getBlocks(this.presetFolderName, otgRootFolder, logger, customObjectManager2, materialReader, manager, modLoadedChecker);
				
				this.inheritedBO3Loaded = true;

				this.inheritedBO3s.addAll(((BO4)parentBO3).getConfig().getInheritedBO3s());

				this.removeAir = ((BO4)parentBO3).getConfig().removeAir;
				this.replaceAbove = this.replaceAbove == null || this.replaceAbove.length() == 0 ? ((BO4)parentBO3).getConfig().replaceAbove : this.replaceAbove;
				this.replaceBelow = this.replaceBelow == null || this.replaceBelow.length() == 0 ? ((BO4)parentBO3).getConfig().replaceBelow : this.replaceBelow;

				BO4CustomStructureCoordinate rotatedParentMaxCoords = BO4CustomStructureCoordinate.getRotatedBO3Coords(((BO4)parentBO3).getConfig().maxX, ((BO4)parentBO3).getConfig().maxY, ((BO4)parentBO3).getConfig().maxZ, this.inheritBO3Rotation);
				BO4CustomStructureCoordinate rotatedParentMinCoords = BO4CustomStructureCoordinate.getRotatedBO3Coords(((BO4)parentBO3).getConfig().minX, ((BO4)parentBO3).getConfig().minY, ((BO4)parentBO3).getConfig().minZ, this.inheritBO3Rotation);

				int parentMaxX = rotatedParentMaxCoords.getX() > rotatedParentMinCoords.getX() ? rotatedParentMaxCoords.getX() : rotatedParentMinCoords.getX();
				int parentMinX = rotatedParentMaxCoords.getX() < rotatedParentMinCoords.getX() ? rotatedParentMaxCoords.getX() : rotatedParentMinCoords.getX();

				int parentMaxY = rotatedParentMaxCoords.getY() > rotatedParentMinCoords.getY() ? rotatedParentMaxCoords.getY() : rotatedParentMinCoords.getY();
				int parentMinY = rotatedParentMaxCoords.getY() < rotatedParentMinCoords.getY() ? rotatedParentMaxCoords.getY() : rotatedParentMinCoords.getY();

				int parentMaxZ = rotatedParentMaxCoords.getZ() > rotatedParentMinCoords.getZ() ? rotatedParentMaxCoords.getZ() : rotatedParentMinCoords.getZ();
				int parentMinZ = rotatedParentMaxCoords.getZ() < rotatedParentMinCoords.getZ() ? rotatedParentMaxCoords.getZ() : rotatedParentMinCoords.getZ();

				if(parentMaxX > this.maxX)
				{
					this.maxX = parentMaxX;
				}
				if(parentMinX < this.minX)
				{
					this.minX = parentMinX;
				}
				if(parentMaxY > this.maxY)
				{
					this.maxY = parentMaxY;
				}
				if(parentMinY < this.minY)
				{
					this.minY = parentMinY;
				}
				if(parentMaxZ > this.maxZ)
				{
					this.maxZ = parentMaxZ;
				}
				if(parentMinZ < this.minZ)
				{
					this.minZ = parentMinZ;
				}

				BO4BlockFunction[] parentBlocks = ((BO4)parentBO3).getConfig().getBlocks(presetFolderName, otgRootFolder, logger, customObjectManager2, materialReader, manager, modLoadedChecker);				
				ArrayList<BlockFunction<?>> newBlocks = new ArrayList<>();				
				newBlocks.addAll(new ArrayList<BO4BlockFunction>(Arrays.asList(parentBlocks)));
				newBlocks.addAll(new ArrayList<BO4BlockFunction>(Arrays.asList(blocks)));
					
				short[][] columnSizes = new short[16][16];
				for(BlockFunction<?> block : newBlocks)
				{
					columnSizes[block.x][block.z]++;
				}
				
				loadBlockArrays(newBlocks, columnSizes);
				
				this.isCollidable = newBlocks.size() > 0;
				
				ArrayList<BO4BranchFunction> newBranches = new ArrayList<BO4BranchFunction>();
				if(this.branchesBO4 != null)
				{
					for(BO4BranchFunction branch : this.branchesBO4)
					{
						newBranches.add(branch);
					}
				}
				for(BO4BranchFunction branch : ((BO4)parentBO3).getConfig().branchesBO4)
				{
					newBranches.add(branch.rotate(this.inheritBO3Rotation, presetFolderName, otgRootFolder, logger, customObjectManager2, materialReader, manager, modLoadedChecker));
				}
				this.branchesBO4 = newBranches.toArray(new BO4BranchFunction[newBranches.size()]);

				ArrayList<BO4EntityFunction> newEntityData = new ArrayList<BO4EntityFunction>();
				if(this.entityDataBO4 != null)
				{
					for(BO4EntityFunction entityData : this.entityDataBO4)
					{
						newEntityData.add(entityData);
					}
				}
				for(BO4EntityFunction entityData : ((BO4)parentBO3).getConfig().entityDataBO4)
				{
					newEntityData.add(entityData.rotate(this.inheritBO3Rotation));
				}
				this.entityDataBO4 = newEntityData.toArray(new BO4EntityFunction[newEntityData.size()]);
	
				this.inheritedBO3s.addAll(((BO4)parentBO3).getConfig().getInheritedBO3s());
			}
			if(!this.inheritedBO3Loaded)
			{
				if(logger.getLogCategoryEnabled(LogCategory.CUSTOM_OBJECTS))
				{
					logger.log(LogLevel.ERROR, LogCategory.CUSTOM_OBJECTS, "could not load BO4 parent for InheritBO3: " + this.inheritBO3 + " in BO4 " + this.getName());
				}
			}
		}
	}

	static int BO4BlocksLoaded = 0;
	private void readResources(ILogger logger, IMaterialReader materialReader, CustomObjectResourcesManager manager) throws InvalidConfigException
	{		
		List<BO4BlockFunction> tempBlocksList = new ArrayList<BO4BlockFunction>();
		List<BO4BranchFunction> tempBranchesList = new ArrayList<BO4BranchFunction>();
		List<BO4EntityFunction> tempEntitiesList = new ArrayList<BO4EntityFunction>();

		short[][] columnSizes = new short[xSize][zSize];
		
		ArrayList<CustomObjectConfigFunction<BO4Config>> resources = new ArrayList<CustomObjectConfigFunction<BO4Config>>();
		int minX = 0;
		int maxX = 0;
		int minZ = 0;
		int maxZ = 0;
		for (CustomObjectConfigFunction<BO4Config> res : reader.getConfigFunctions(this, true, logger, materialReader, manager))
		{
			if (res.isValid())
			{
				resources.add(res);
				if( // TODO: Add interface instead?
					!(res instanceof BranchFunction) &&
					!(res instanceof CustomObjectErroredFunction)
				)
				{
					if(res.x < minX)
					{
						minX = res.x;
					}
					if(res.x > maxX)
					{
						maxX = res.x;
					}
					if(res.z < minZ)
					{
						minZ = res.z;
					}
					if(res.z > maxZ)
					{
						maxZ = res.z;
					}
				}
			}
		}
		
		int xSize = Math.abs(minX - maxX);
		int zSize = Math.abs(minZ - maxZ);
		if(xSize > 15 || zSize > 15)
		{
			if(logger.getLogCategoryEnabled(LogCategory.CUSTOM_OBJECTS))
			{
				logger.log(LogLevel.ERROR, LogCategory.CUSTOM_OBJECTS, "BO4 " + this.getName() + " was too large (" + xSize + "x" + zSize + "), BO4's can be max 16x16 blocks.");
			}
			throw new InvalidConfigException("BO4 " + this.getName() + " was too large, BO4's can be max 16x16 blocks.");
		}
		
		int xOffset = 0;
		int zOffset = 0;
		
		if(minX < -8)
		{
			xOffset = -minX - 8;
		}
		if(maxX > 7)
		{
			xOffset = -(maxX - 7);
		}
		if(minZ < -7)
		{
			zOffset = -minZ - 7;
		}
		if(maxZ > 8)
		{
			zOffset = -(maxZ - 8);
		}		
		
		for (CustomObjectConfigFunction<BO4Config> res : resources)
		{
			if( // TODO: Add interface instead?
				!(res instanceof BranchFunction) &&
				!(res instanceof CustomObjectErroredFunction)
				)
			{
				res.x += xOffset;
				res.z += zOffset;
			}
			
			if (res instanceof BO4BlockFunction)
			{					
				this.isCollidable = true;
				
				if(res instanceof BO4RandomBlockFunction)
				{
					tempBlocksList.add((BO4RandomBlockFunction)res);
					columnSizes[res.x + (this.xSize / 2)][res.z + (this.zSize / 2) - 1]++;
				} else {
					if(!this.removeAir || !((BO4BlockFunction)res).material.isAir())
					{
						tempBlocksList.add((BO4BlockFunction)res);
						columnSizes[res.x + (this.xSize / 2)][res.z + (this.zSize / 2) - 1]++;
					}
				}
				
				// Get the real size of this BO3
				if(res.x < this.minX)
				{
					this.minX = res.x;
				}
				if(res.x > this.maxX)
				{
					this.maxX = res.x;
				}
				if(((BO4BlockFunction)res).y < this.minY)
				{
					this.minY = ((BO4BlockFunction)res).y;
				}
				if(((BO4BlockFunction)res).y > this.maxY)
				{
					this.maxY = ((BO4BlockFunction)res).y;
				}
				if(res.z < this.minZ)
				{
					this.minZ = res.z;
				}
				if(res.z > this.maxZ)
				{
					this.maxZ = res.z;
				}					
			} else {
				if (res instanceof BO4WeightedBranchFunction)
				{
					tempBranchesList.add((BO4WeightedBranchFunction) res);
				}
				else if (res instanceof BO4BranchFunction)
				{
					tempBranchesList.add((BO4BranchFunction) res);
				}
				else if (res instanceof BO4EntityFunction)
				{
					tempEntitiesList.add((BO4EntityFunction) res);
				}
			}
		}

		if(this.minX == Integer.MAX_VALUE)
		{
			this.minX = -8;
		}
		if(this.maxX == Integer.MIN_VALUE)
		{
			this.maxX = -8;
		}
		if(this.minY == Integer.MAX_VALUE)
		{
			this.minY = 0;
		}
		if(this.maxY == Integer.MIN_VALUE)
		{
			this.maxY = 0;
		}
		if(this.minZ == Integer.MAX_VALUE)
		{
			this.minZ = -7;
		}
		if(this.maxZ == Integer.MIN_VALUE)
		{
			this.maxZ = -7;
		}
		
		// TODO: OTG+ Doesn't do CustomObject BO3's, only check for 16x16, not 32x32?
		boolean illegalBlock = false;
		for(BO4BlockFunction block1 : tempBlocksList)
		{
			block1.x += this.getXOffset();
			block1.z += this.getZOffset();

			if(block1.x > 15 || block1.z > 15)
			{
				illegalBlock = true;
			}

			if(block1.x < 0 || block1.z < 0)
			{
				illegalBlock = true;
			}				
		}
		
		this.blocks = new short[this.xSize][this.zSize][];
		this.blocksMaterial = new LocalMaterialData[tempBlocksList.size()];
		this.blocksMetaDataName = new String[tempBlocksList.size()];
		this.blocksMetaDataTag = new NamedBinaryTag[tempBlocksList.size()];
		
		this.randomBlocksBlocks = new LocalMaterialData[tempBlocksList.size()][];
		this.randomBlocksBlockChances = new byte[tempBlocksList.size()][];
		this.randomBlocksMetaDataNames = new String[tempBlocksList.size()][];
		this.randomBlocksMetaDataTags = new NamedBinaryTag[tempBlocksList.size()][];
		this.randomBlocksBlockCount = new byte[tempBlocksList.size()]; 
		
		short[][] columnBlockIndex = new short[this.xSize][this.zSize];
		BO4BlockFunction[] blocksSorted = new BO4BlockFunction[tempBlocksList.size()];
		int blocksSortedIndex = 0;
		for(int x = 0; x < this.xSize; x++)
		{
			for(int z = 0; z < this.zSize; z++)
			{
				for(int h = 0; h < tempBlocksList.size(); h++)
				{
					if(tempBlocksList.get(h).x == x && tempBlocksList.get(h).z == z)
					{
						blocksSorted[blocksSortedIndex] = tempBlocksList.get(h);
						blocksSortedIndex++;
					}
				}
			}
		}
		BO4BlockFunction block;
		for(int blockIndex = 0; blockIndex < blocksSorted.length; blockIndex++)
		{
			block = blocksSorted[blockIndex];
			if(this.blocks[block.x][block.z] == null)
			{
				this.blocks[block.x][block.z] = new short[columnSizes[block.x][block.z]];
			}
				this.blocks[block.x][block.z][columnBlockIndex[block.x][block.z]] = (short) block.y;
			
			this.blocksMaterial[blockIndex] = block.material;
			this.blocksMetaDataName[blockIndex] = block.nbtName;
			this.blocksMetaDataTag[blockIndex] = block.nbt;
			
			if(block instanceof BO4RandomBlockFunction)
			{
				this.randomBlocksBlocks[blockIndex] = ((BO4RandomBlockFunction)block).blocks;
				this.randomBlocksBlockChances[blockIndex] = ((BO4RandomBlockFunction)block).blockChances;
				this.randomBlocksMetaDataNames[blockIndex] = ((BO4RandomBlockFunction)block).metaDataNames;
				this.randomBlocksMetaDataTags[blockIndex] = ((BO4RandomBlockFunction)block).metaDataTags;
				this.randomBlocksBlockCount[blockIndex] = ((BO4RandomBlockFunction)block).blockCount;
			}
			columnBlockIndex[block.x][block.z]++;
		}

		boolean illegalEntityData = false;
		for(BO4EntityFunction entityData : tempEntitiesList)
		{
			entityData.x += this.getXOffset();
			entityData.z += this.getZOffset();

			if(entityData.x > 15 || entityData.z > 15)
			{
				illegalEntityData = true;
			}

			if(entityData.x < 0 || entityData.z < 0)
			{
				illegalEntityData = true;
			}
		}
		this.entityDataBO4 = tempEntitiesList.toArray(new BO4EntityFunction[tempEntitiesList.size()]);

		if(logger.getLogCategoryEnabled(LogCategory.CUSTOM_OBJECTS))
		{
			if(illegalBlock)
			{
				logger.log(LogLevel.WARN, LogCategory.CUSTOM_OBJECTS, "Warning: BO4 contains Blocks or RandomBlocks that are placed outside the chunk(s) that the BO3 will be placed in. This can slow down world generation. BO4: " + this.getName());
			}
			if(illegalEntityData)
			{
				logger.log(LogLevel.WARN, LogCategory.CUSTOM_OBJECTS, "Warning: BO4 contains an Entity() that may be placed outside the chunk(s) that the BO3 will be placed in. This can slow down world generation. BO4: " + this.getName());
			}
		}

		this.branchesBO4 = tempBranchesList.toArray(new BO4BranchFunction[tempBranchesList.size()]);
    }
    
	public void setBranches(List<BranchFunction<?>> branches)
	{
		this.branchesBO4 = branches.toArray(new BO4BranchFunction[branches.size()]);
	}

	/**
	 * Gets the file this config will be written to. May be null if the config
	 * will never be written.
	 * @return The file.
	 */
	public File getFile()
	{
		return this.reader.getFile();
	}

	@Override
	public BlockFunction<?>[] getBlockFunctions(String presetFolderName, Path otgRootFolder, ILogger logger, ICustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		return getBlocks(presetFolderName, otgRootFolder, logger, (CustomObjectManager) customObjectManager, materialReader, manager, modLoadedChecker);
	}

	@Override
	protected void writeConfigSettings(SettingsWriterBO4 writer, ILogger logger, IMaterialReader materialReader, CustomObjectResourcesManager manager) throws IOException
	{
		writeSettings(writer, null, null, logger, materialReader, manager);
	}

	public void writeWithData(SettingsWriterBO4 writer, List<BlockFunction<?>> blocksList, List<BranchFunction<?>> branchesList, ILogger logger, IMaterialReader materialReader, CustomObjectResourcesManager manager) throws IOException
	{
		writer.setConfigMode(ConfigMode.WriteAll);
		try
		{
			writer.open();
			writeSettings(writer, blocksList, branchesList, logger, materialReader, manager);
		} finally {
			writer.close(logger);
		}
	}
	
	private void writeSettings(SettingsWriterBO4 writer, List<BlockFunction<?>> blocksList, List<BranchFunction<?>> branchesList, ILogger logger, IMaterialReader materialReader, CustomObjectResourcesManager manager) throws IOException
	{
		// The object
		writer.bigTitle("BO4 object");
		writer.comment("This is the config file of a custom object.");
		writer.comment("If you add this object correctly to your BiomeConfigs, it will spawn in the world.");
		writer.comment("");
		
		writer.comment("This is the creator of this BO4 object");
		writer.setting(BO4Settings.AUTHOR, this.author);

		writer.comment("A short description of this BO4 object");
		writer.setting(BO4Settings.DESCRIPTION, this.description);

		if(writer.getFile().getName().toUpperCase().endsWith(".BO3"))
		{
			writer.comment("Legacy setting, always true for BO4's. Only used if the file has a .BO3 extension.");
			writer.comment("Rename your file to .BO4 and remove this setting.");
			writer.setting(BO4Settings.ISOTGPLUS, true);
		}
		
		writer.comment("The settings mode, WriteAll, WriteWithoutComments or WriteDisable. See WorldConfig.");
		writer.setting(WorldStandardValues.SETTINGS_MODE_BO3, this.settingsMode);

		// Main settings
		writer.bigTitle("Main settings");

		writer.comment("If this BO4 should spawn with a fixed rotation, set it here.");
		writer.comment("For example: NORTH, EAST, SOUTH or WEST. Empty by default");
		writer.setting(BO4Settings.FIXED_ROTATION, this.fixedRotation == null ? "" : this.fixedRotation.name());
		
		writer.comment("This BO4 can only spawn at least Frequency chunks distance away from any other BO4 with the exact same name.");
		writer.comment("You can use this to make this BO4 spawn in groups or make sure that this BO4 only spawns once every X chunks.");
		writer.setting(BO4Settings.FREQUENCY, this.frequency);

		writer.comment("The spawn height of the BO4: randomY, highestBlock or highestSolidBlock.");
		writer.setting(BO4Settings.SPAWN_HEIGHT, this.spawnHeight);

		writer.comment("When set to true, uses the center of the structure (determined by minimum structure size) when checking the highestBlock to spawn at.");
		writer.setting(BO4Settings.USE_CENTER_FOR_HIGHEST_BLOCK, this.useCenterForHighestBlock);
		
		writer.smallTitle("Height Limits for the BO4.");

		writer.comment("When in randomY mode used as the minimum Y or in atMinY mode as the actual Y to spawn this BO4 at.");
		writer.setting(BO4Settings.MIN_HEIGHT, this.minHeight);

		writer.comment("When in randomY mode used as the maximum Y to spawn this BO4 at.");
		writer.setting(BO4Settings.MAX_HEIGHT, this.maxHeight);

		writer.comment("Copies the blocks and branches of an existing BO4 into this BO4. You can still add blocks and branches in this BO4, they will be added on top of the inherited blocks and branches.");
		writer.setting(BO4Settings.INHERITBO3, this.inheritBO3);
		writer.comment("Rotates the inheritedBO3's resources (blocks, spawners, checks etc) and branches, defaults to NORTH (no rotation).");
		writer.setting(BO4Settings.INHERITBO3ROTATION, this.inheritBO3Rotation);

		writer.comment("Defaults to true, if true and this is the starting BO4 for this branching structure then this BO4's smoothing and height settings are used for all children (branches).");
		writer.setting(BO4Settings.OVERRIDECHILDSETTINGS, this.overrideChildSettings);
		writer.comment("Defaults to false, if true then this branch uses it's own height settings (SpawnHeight, minHeight, maxHeight, spawnAtWaterLevel) instead of those defined in the starting BO4 for this branching structure.");
		writer.setting(BO4Settings.OVERRIDEPARENTHEIGHT, this.overrideParentHeight);
		writer.comment("If this is set to true then this BO4 can spawn on top of or inside an existing BO4. If this is set to false then this BO4 will use a bounding box to detect collisions with other BO4's, if a collision is detected then this BO4 won't spawn and the current branch is rolled back.");
		writer.setting(BO4Settings.CANOVERRIDE, this.canOverride);

		writer.comment("This branch can only spawn at least branchFrequency chunks (x,z) distance away from any other branch with the exact same name.");
		writer.setting(BO4Settings.BRANCH_FREQUENCY, this.branchFrequency);
		writer.comment("Define groups that this branch belongs to along with a minimum (x,z) range in chunks that this branch must have between it and any other members of this group if it is to be allowed to spawn. Syntax is \"GroupName:Frequency, GoupName2:Frequency2\" etc so for example a branch that belongs to 3 groups: \"BranchFrequencyGroup: Ships:10, Vehicles:5, FloatingThings:3\".");
		writer.setting(BO4Settings.BRANCH_FREQUENCY_GROUP, this.branchFrequencyGroup);

		writer.comment("If this is set to true then this BO4 can only spawn underneath an existing BO4. Used to make sure that dungeons only appear underneath buildings.");
		writer.setting(BO4Settings.MUSTBEBELOWOTHER, this.mustBeBelowOther);
		
		writer.comment("Used with CanOverride: true. A comma-seperated list of BO4s, this BO4's bounding box must collide with one of the BO4's in the list or this BO4 fails to spawn and the current branch is rolled back. AND/OR is supported, comma is OR, space is AND, f.e: branch1, branch2 branch3, branch 4.");
		writer.setting(BO4Settings.MUSTBEINSIDE, this.mustBeInside);

		writer.comment("Used with CanOverride: true. A comma-seperated list of BO4s, this BO4's bounding box cannot collide with any of the BO4's in the list or this BO4 fails to spawn and the current branch is rolled back.");
		writer.setting(BO4Settings.CANNOTBEINSIDE, this.cannotBeInside);

		writer.comment("Used with CanOverride: true. A comma-seperated list of BO4s, if this BO4's bounding box collides with any of the BO4's in the list then those BO4's won't spawn any blocks. This does not remove or roll back any BO4's.");
		writer.setting(BO4Settings.REPLACESBO3, this.replacesBO3);

		writer.comment("If this is set to true then this BO4 can only spawn inside world borders. Used to make sure that dungeons only appear inside the world borders.");
		writer.setting(BO4Settings.MUSTBEINSIDEWORLDBORDERS, this.mustBeInsideWorldBorders);		
		
		writer.comment("Defaults to true. Set to false if the BO4 is not allowed to spawn on a water block");
		writer.setting(BO4Settings.CANSPAWNONWATER, this.canSpawnOnWater);

		writer.comment("Defaults to false. Set to true if the BO4 is allowed to spawn only on a water block");
		writer.setting(BO4Settings.SPAWNONWATERONLY, this.spawnOnWaterOnly);

		writer.comment("Defaults to false. Set to true if the BO4 and its smoothing area should ignore water when looking for the highest block to spawn on. Defaults to false (things spawn on top of water)");
		writer.setting(BO4Settings.SPAWNUNDERWATER, this.spawnUnderWater);

		writer.comment("Defaults to false. Set to true if the BO4 should spawn at water level");
		writer.setting(BO4Settings.SPAWNATWATERLEVEL, this.spawnAtWaterLevel);

		writer.comment("Spawns the BO4 at a Y offset of this value. Handy when using highestBlock for lowering BO4s into the surrounding terrain when there are layers of ground included in the BO4, also handy when using SpawnAtWaterLevel to lower objects like ships into the water.");
		writer.setting(BO4Settings.HEIGHT_OFFSET, this.heightOffset);

		writer.comment("If set to true removes all AIR blocks from the BO4 so that it can be flooded or buried.");
		writer.setting(BO4Settings.REMOVEAIR, this.configRemoveAir);

		writer.comment("Replaces all the non-air blocks that are above this BO4 or its smoothing area with the given block material (should be WATER or AIR or NONE), also applies to smoothing areas although OTG intentionally leaves some of the terrain above them intact. WATER can be used in combination with SpawnUnderWater to fill any air blocks underneath waterlevel with water (and any above waterlevel with air).");
		writer.setting(BO4Settings.REPLACEABOVE, this.configReplaceAbove);

		writer.comment("Replaces all air blocks underneath the BO4 (but not its smoothing area) with the specified material until a solid block is found.");
		writer.setting(BO4Settings.REPLACEBELOW, this.configReplaceBelow);

		writer.comment("Defaults to true. If set to true then every block in the BO4 of the materials defined in ReplaceWithGroundBlock or ReplaceWithSurfaceBlock will be replaced by the GroundBlock or SurfaceBlock materials configured for the biome the block is spawned in.");
		writer.setting(BO4Settings.REPLACEWITHBIOMEBLOCKS, this.replaceWithBiomeBlocks);

		writer.comment("Defaults to GRASS, Replaces all the blocks of the given material in the BO4 with the SurfaceBlock configured for the biome it spawns in.");
		writer.setting(BO4Settings.REPLACEWITHSURFACEBLOCK, this.replaceWithSurfaceBlock);		
		
		writer.comment("Defaults to DIRT, Replaces all the blocks of the given material in the BO4 with the GroundBlock configured for the biome it spawns in.");
		writer.setting(BO4Settings.REPLACEWITHGROUNDBLOCK, this.replaceWithGroundBlock);

		writer.comment("Defaults to STONE, Replaces all the blocks of the given material in the BO4 with the StoneBlock configured for the biome it spawns in.");
		writer.setting(BO4Settings.REPLACEWITHSTONEBLOCK, this.replaceWithStoneBlock);
		
		writer.comment("Makes the terrain around the BO4 slope evenly towards the edges of the BO4. The given value is the distance in blocks around the BO4 from where the slope should start and can be any positive number.");
		writer.setting(BO4Settings.SMOOTHRADIUS, this.smoothRadius);

		writer.comment("Moves the smoothing area up or down relative to the BO4 (at the points where the smoothing area is connected to the BO4). Handy when using SmoothStartTop: false and the BO4 has some layers of ground included, in that case we can set the HeightOffset to a negative value to lower the BO4 into the ground and we can set the SmoothHeightOffset to a positive value to move the smoothing area starting height up.");
		writer.setting(BO4Settings.SMOOTH_HEIGHT_OFFSET, this.smoothHeightOffset);

		writer.comment("Should the smoothing area be attached at the bottom or the top of the edges of the BO4? Defaults to false (bottom). Using this setting can make things slower so try to avoid using it and use SmoothHeightOffset instead if for instance you have a BO4 with some ground layers included. The only reason you should need to use this setting is if you have a BO4 with edges that have an irregular height (like some hills).");
		writer.setting(BO4Settings.SMOOTHSTARTTOP, this.smoothStartTop);

		writer.comment("Should the smoothing area attach itself to \"log\" block or ignore them? Defaults to false (ignore logs).");
		writer.setting(BO4Settings.SMOOTHSTARTWOOD, this.smoothStartWood);

		writer.comment("The block used for smoothing area surface blocks, defaults to biome SurfaceBlock.");
		writer.setting(BO4Settings.SMOOTHINGSURFACEBLOCK, this.smoothingSurfaceBlock);

		writer.comment("The block used for smoothing area ground blocks, defaults to biome GroundBlock.");
		writer.setting(BO4Settings.SMOOTHINGGROUNDBLOCK, this.smoothingGroundBlock);

		writer.comment("Define groups that this BO4 belongs to along with a minimum range in chunks that this BO4 must have between it and any other members of this group if it is to be allowed to spawn. Syntax is \"GroupName:Frequency, GoupName2:Frequency2\" etc so for example a BO4 that belongs to 3 groups: \"BO4Group: Ships:10, Vehicles:5, FloatingThings:3\".");
		writer.setting(BO4Settings.BO3GROUP, this.bo3Group);

		writer.comment("Defaults to false. Set to true if this BO4 should spawn at the player spawn point. When the server starts the spawn point is determined and the BO4's for the biome it is in are loaded, one of these BO4s that has IsSpawnPoint set to true (if any) is selected randomly and is spawned at the spawn point regardless of its rarity (so even Rarity:0, IsSpawnPoint: true BO4's can get spawned as the spawn point!).");
		writer.setting(BO4Settings.ISSPAWNPOINT, this.isSpawnPoint);

		writer.comment("Defaults to true. Set to false to make the BO4 ignore any ReplacedBlocks settings in Biome Configs.");
		writer.setting(BO4Settings.DO_REPLACE_BLOCKS, this.doReplaceBlocks);
		
		// Blocks and other things
		writeResources(writer, blocksList, branchesList, logger, materialReader, manager);
		
		if(this.reader != null) // Can be true for BO4Creator?
		{
			this.reader.flushCache();
		}
	}

	@Override
	protected void readConfigSettings(String presetFolderName, Path otgRootFolder, ILogger logger, ICustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker) throws InvalidConfigException
	{
		this.branchFrequency = readSettings(BO4Settings.BRANCH_FREQUENCY, logger, materialReader, manager);
		
		this.branchFrequencyGroup = readSettings(BO4Settings.BRANCH_FREQUENCY_GROUP, logger, materialReader, manager);
		this.branchFrequencyGroups = new HashMap<String, Integer>();
		if(this.branchFrequencyGroup != null && this.branchFrequencyGroup.trim().length() > 0)
		{
			String[] groupStrings = this.branchFrequencyGroup.split(",");
			if(groupStrings != null && groupStrings.length > 0)
			{
				for(int i = 0; i < groupStrings.length; i++)
				{
					String[] groupString = groupStrings[i].trim().length() > 0 ? groupStrings[i].split(":") : null;
					if(groupString != null && groupString.length == 2)
					{
						this.branchFrequencyGroups.put(groupString[0].trim(), Integer.parseInt(groupString[1].trim()));
					}
				}
			}
		}
		
		this.heightOffset = readSettings(BO4Settings.HEIGHT_OFFSET, logger, materialReader, manager);
		this.inheritBO3Rotation = readSettings(BO4Settings.INHERITBO3ROTATION, logger, materialReader, manager);

		this.configRemoveAir = readSettings(BO4Settings.REMOVEAIR, logger, materialReader, manager);
		this.removeAir = this.configRemoveAir;
		this.isSpawnPoint = readSettings(BO4Settings.ISSPAWNPOINT, logger, materialReader, manager);
		this.useCenterForHighestBlock = readSettings(BO4Settings.USE_CENTER_FOR_HIGHEST_BLOCK, logger, materialReader, manager);
		this.configReplaceAbove = readSettings(BO4Settings.REPLACEABOVE, logger, materialReader, manager);
		this.replaceAbove = this.configReplaceAbove;
		this.configReplaceBelow = readSettings(BO4Settings.REPLACEBELOW, logger, materialReader, manager);
		this.replaceBelow = this.configReplaceBelow;
		this.replaceWithBiomeBlocks = readSettings(BO4Settings.REPLACEWITHBIOMEBLOCKS, logger, materialReader, manager);
		this.replaceWithGroundBlock = readSettings(BO4Settings.REPLACEWITHGROUNDBLOCK, logger, materialReader, manager);
		this.replaceWithSurfaceBlock = readSettings(BO4Settings.REPLACEWITHSURFACEBLOCK, logger, materialReader, manager);
		this.replaceWithStoneBlock = readSettings(BO4Settings.REPLACEWITHSTONEBLOCK, logger, materialReader, manager);
		
		this.bo3Group = readSettings(BO4Settings.BO3GROUP, logger, materialReader, manager);
		this.bo4Groups = new HashMap<String, Integer>();
		if(this.bo3Group != null && this.bo3Group.trim().length() > 0)
		{
			String[] groupStrings = this.bo3Group.split(",");
			if(groupStrings != null && groupStrings.length > 0)
			{
				for(int i = 0; i < groupStrings.length; i++)
				{
					String[] groupString = groupStrings[i].trim().length() > 0 ? groupStrings[i].split(":") : null;
					if(groupString != null && groupString.length == 2)
					{
						this.bo4Groups.put(groupString[0].trim(), Integer.parseInt(groupString[1].trim()));
					}
				}
			}
		}
		
		this.canOverride = readSettings(BO4Settings.CANOVERRIDE, logger, materialReader, manager);
		this.mustBeBelowOther = readSettings(BO4Settings.MUSTBEBELOWOTHER, logger, materialReader, manager);
		this.mustBeInsideWorldBorders = readSettings(BO4Settings.MUSTBEINSIDEWORLDBORDERS, logger, materialReader, manager);
		
		this.mustBeInside = readSettings(BO4Settings.MUSTBEINSIDE, logger, materialReader, manager);
		this.mustBeInsideBranches = new ArrayList<String>();
		if(this.mustBeInside != null && this.mustBeInside.trim().length() > 0)
		{
			String[] mustBeInsideStrings = this.mustBeInside.split(",");
			if(mustBeInsideStrings != null && mustBeInsideStrings.length > 0)
			{
				for(int i = 0; i < mustBeInsideStrings.length; i++)
				{
					String mustBeInsideString = mustBeInsideStrings[i].trim();
					if(mustBeInsideString.length() > 0)
					{
						this.mustBeInsideBranches.add(mustBeInsideString);
					}
				}
			}
		}
		
		this.cannotBeInside =  readSettings(BO4Settings.CANNOTBEINSIDE, logger, materialReader, manager);
		this.cannotBeInsideBranches = new ArrayList<String>();
		if(this.cannotBeInside != null && this.cannotBeInside.trim().length() > 0)
		{
			String[] cannotBeInsideStrings = this.cannotBeInside.split(",");
			if(cannotBeInsideStrings != null && cannotBeInsideStrings.length > 0)
			{
				for(int i = 0; i < cannotBeInsideStrings.length; i++)
				{
					String cannotBeInsideString = cannotBeInsideStrings[i].trim();
					if(cannotBeInsideString.length() > 0)
					{
						this.cannotBeInsideBranches.add(cannotBeInsideString);
					}
				}
			}
		}
		
		this.replacesBO3 = readSettings(BO4Settings.REPLACESBO3, logger, materialReader, manager);
		this.replacesBO3Branches = new ArrayList<String>();
		if(this.replacesBO3 != null && this.replacesBO3.trim().length() > 0)
		{
			String[] replacesBO3Strings = replacesBO3.split(",");
			if(replacesBO3Strings != null && replacesBO3Strings.length > 0)
			{
				for(int i = 0; i < replacesBO3Strings.length; i++)
				{
					String replacesBO3String = replacesBO3Strings[i].trim();
					if(replacesBO3String.length() > 0)
					{
						this.replacesBO3Branches.add(replacesBO3String);
					}
				}
			}
		}

		//smoothHeightOffset = readSettings(BO3Settings.SMOOTH_HEIGHT_OFFSET).equals("HeightOffset") ? heightOffset : Integer.parseInt(readSettings(BO3Settings.SMOOTH_HEIGHT_OFFSET));
		this.smoothHeightOffset = readSettings(BO4Settings.SMOOTH_HEIGHT_OFFSET, logger, materialReader, manager);
		this.canSpawnOnWater = readSettings(BO4Settings.CANSPAWNONWATER, logger, materialReader, manager);
		this.spawnOnWaterOnly = readSettings(BO4Settings.SPAWNONWATERONLY, logger, materialReader, manager);
		this.spawnUnderWater = readSettings(BO4Settings.SPAWNUNDERWATER, logger, materialReader, manager);
		this.spawnAtWaterLevel = readSettings(BO4Settings.SPAWNATWATERLEVEL, logger, materialReader, manager);
		this.inheritBO3 = readSettings(BO4Settings.INHERITBO3, logger, materialReader, manager);
		this.overrideChildSettings = readSettings(BO4Settings.OVERRIDECHILDSETTINGS, logger, materialReader, manager);
		this.overrideParentHeight = readSettings(BO4Settings.OVERRIDEPARENTHEIGHT, logger, materialReader, manager);
		this.smoothRadius = readSettings(BO4Settings.SMOOTHRADIUS, logger, materialReader, manager);
		this.smoothStartTop = readSettings(BO4Settings.SMOOTHSTARTTOP, logger, materialReader, manager);
		this.smoothStartWood = readSettings(BO4Settings.SMOOTHSTARTWOOD, logger, materialReader, manager);
		this.smoothingSurfaceBlock = readSettings(BO4Settings.SMOOTHINGSURFACEBLOCK, logger, materialReader, manager);
		this.smoothingGroundBlock = readSettings(BO4Settings.SMOOTHINGGROUNDBLOCK, logger, materialReader, manager);

		// Make sure that the BO3 wont try to spawn below Y 0 because of the height offset
		if(this.heightOffset < 0 && this.minHeight < -this.heightOffset)
		{
			this.minHeight = -this.heightOffset;
		}

		this.inheritedBO3s = new ArrayList<String>();
		this.inheritedBO3s.add(this.getName()); // TODO: Make this cleaner?
		if(this.inheritBO3 != null && this.inheritBO3.trim().length() > 0)
		{
			this.inheritedBO3s.add(this.inheritBO3);
		}

		this.author = readSettings(BO4Settings.AUTHOR, logger, materialReader, manager);
		this.description = readSettings(BO4Settings.DESCRIPTION, logger, materialReader, manager);
		this.settingsMode = readSettings(WorldStandardValues.SETTINGS_MODE_BO3, logger, materialReader, manager);

		this.frequency = readSettings(BO4Settings.FREQUENCY, logger, materialReader, manager);
		this.spawnHeight = readSettings(BO4Settings.SPAWN_HEIGHT, logger, materialReader, manager);
		this.minHeight = readSettings(BO4Settings.MIN_HEIGHT, logger, materialReader, manager);
		this.maxHeight = readSettings(BO4Settings.MAX_HEIGHT, logger, materialReader, manager);
		this.maxHeight = this.maxHeight < this.minHeight ? this.minHeight : this.maxHeight;

		this.doReplaceBlocks = readSettings(BO4Settings.DO_REPLACE_BLOCKS, logger, materialReader, manager);

		String fixedRotation = readSettings(BO4Settings.FIXED_ROTATION, logger, materialReader, manager);
		this.fixedRotation = fixedRotation == null || fixedRotation.trim().length() == 0 ? null : Rotation.FromString(fixedRotation);

		// Read the resources
		readResources(logger, materialReader, manager);
	}
	
	private void writeResources(SettingsWriterBO4 writer, List<BlockFunction<?>> blocksList, List<BranchFunction<?>> branchesList, ILogger logger, IMaterialReader materialReader, CustomObjectResourcesManager manager) throws IOException
	{
		writer.bigTitle("Blocks");
		writer.comment("All the blocks used in the BO4 are listed here. Possible blocks:");
		writer.comment("Block(x,y,z,id[.data][,nbtfile.nbt)");
		writer.comment("RandomBlock(x,y,z,id[:data][,nbtfile.nbt],chance[,id[:data][,nbtfile.nbt],chance[,...]])");
		writer.comment(" So RandomBlock(0,0,0,CHEST,chest.nbt,50,CHEST,anotherchest.nbt,100) will spawn a chest at");
		writer.comment(" the BO4 origin, and give it a 50% chance to have the contents of chest.nbt, or, if that");
		writer.comment(" fails, a 100% percent chance to have the contents of anotherchest.nbt.");
		writer.comment("MinecraftObject(x,y,z,name) (TODO: This may not work anymore and needs to be tested.");
		writer.comment(" Spawns an object in the Mojang NBT structure format. For example, ");
		writer.comment(" MinecraftObject(0,0,0," + DefaultStructurePart.IGLOO_BOTTOM.getPath() + ")");
		writer.comment(" spawns the bottom part of an igloo.");

		ArrayList<BO4EntityFunction> entitiesList = new ArrayList<BO4EntityFunction>();
		
		// Re-read the raw data, if no data was supplied. Don't save any loaded data, since it has been processed/transformed.
		if(blocksList == null || branchesList == null || entitiesList == null)
		{
			blocksList = new ArrayList<>();
			branchesList = new ArrayList<>();
			
			for (CustomObjectConfigFunction<BO4Config> res : reader.getConfigFunctions(this, true, logger, materialReader, manager))
			{
				if (res.isValid())
				{
					if(res instanceof BO4RandomBlockFunction)
					{
						blocksList.add((BO4RandomBlockFunction)res);
					}
					else if(res instanceof BO4BlockFunction)
					{
						blocksList.add((BO4BlockFunction)res);
					}
					else if (res instanceof BO4WeightedBranchFunction)
					{
						branchesList.add((BO4WeightedBranchFunction) res);
					}
					else if (res instanceof BO4BranchFunction)
					{
						branchesList.add((BO4BranchFunction) res);
					}
					else if (res instanceof BO4EntityFunction)
					{
						entitiesList.add((BO4EntityFunction) res);
					}
				}
			}
		}
		else if(blocksList != null)
		{
			// The blockslist passed is not used after this,
			// so it's ok to edit the block objects.
			for(BlockFunction<?> block : blocksList)
			{
				block.x -= this.getXOffset();
				block.z -= this.getZOffset();
			}
		}
		for(BlockFunction<?> block : blocksList)
		{
			writer.function(block);
		}
		
		writer.bigTitle("Branches");
		writer.comment("Branches are child-BO4's that spawn if this BO4 is configured to spawn as a");
		writer.comment("CustomStructure resource in a biome config. Branches can have branches,");
		writer.comment("making complex structures possible. See the wiki for more details.");
		writer.comment("");
		writer.comment("Regular Branches spawn each branch with an independent chance of spawning.");
		writer.comment("Branch(x,y,z,isRequiredBranch,branchName,rotation,chance,branchDepth[,anotherBranchName,rotation,chance,branchDepth[,...]][IndividualChance])");
		writer.comment("branchName - name of the object to spawn.");
		writer.comment("rotation - NORTH, SOUTH, EAST or WEST.");
		writer.comment("IndividualChance - The chance each branch has to spawn, assumed to be 100 when left blank");
		writer.comment("isRequiredBranch - If this is set to true then at least one of the branches in this BO4 must spawn at these x,y,z coordinates. If no branch can spawn there then this BO4 fails to spawn and its branch is rolled back.");
		writer.comment("isRequiredBranch:true branches must spawn or the current branch is rolled back entirely. This is useful for grouping BO4's that must spawn together, for instance a single room made of multiple BO4's/branches.");
		writer.comment("If all parts of the room are connected together via isRequiredBranch:true branches then either the entire room will spawns or no part of it will spawn.");
		writer.comment("*Note: When isRequiredBranch:true only one BO4 can be added per Branch() and it will automatically have a rarity of 100.0.");
		writer.comment("isRequiredBranch:false branches are used to make optional parts of structures, for instance the middle section of a tunnel that has a beginning, middle and end BO4/branch and can have a variable length by repeating the middle BO4/branch.");
		writer.comment("By making the start and end branches isRequiredBranch:true and the middle branch isRequiredbranch:false you can make it so that either:");
		writer.comment("A. A tunnel spawns with at least a beginning and end branch");
		writer.comment("B. A tunnel spawns with a beginning and end branch and as many middle branches as will fit in the available space.");
		writer.comment("C. No tunnel spawns at all because there wasn't enough space to spawn at least a beginning and end branch.");
		writer.comment("branchDepth - When creating a chain of branches that contains optional (isRequiredBranch:false) branches branch depth is configured for the first BO4 in the chain to determine the maximum length of the chain.");
		writer.comment("branchDepth - 1 is inherited by each isRequiredBranch:false branch in the chain. When branchDepth is zero isRequiredBranch:false branches cannot spawn and the chain ends. In the case of the tunnel this means the last middle branch would be");
		writer.comment("rolled back and an IsRequiredBranch:true end branch could be spawned in its place to make sure the tunnel has a proper ending.");
		writer.comment("Instead of inheriting branchDepth - 1 from the parent branchDepth can be overridden by child branches if it is set higher than 0 (the default value).");
		writer.comment("isRequiredBranch:true branches do inherit branchDepth and pass it on to their own branches, however they cannot be prevented from spawning by it and also don't subtract 1 from branchDepth when inheriting it.");
		writer.comment("");
		writer.comment("Weighted Branches spawn branches with a dependent chance of spawning.");
		writer.comment("WeightedBranch(x,y,z,isRequiredBranch,branchName,rotation,chance,branchDepth[,anotherBranchName,rotation,chance,branchDepth[,...]][MaxChanceOutOf])");
		writer.comment("*Note: isRequiredBranch must be set to false. It is not possible to use isRequiredBranch:true with WeightedBranch() since isRequired:true branches must spawn and automatically have a rarity of 100.0.");
		writer.comment("MaxChanceOutOf - The chance all branches have to spawn out of, assumed to be 100 when left blank");
		
		for(BranchFunction<?> func : branchesList)
		{
			writer.function(func);
		}

		writer.bigTitle("Entities");
		writer.comment("Forge only (this may have changed, check for updates).");
		writer.comment("An EntityFunction spawns an entity instead of a block. The entity is spawned only once when the BO4 is spawned.");
		writer.comment("Entities are persistent by default so they don't de-spawn when no player is near, they are only unloaded.");
		writer.comment("Usage: Entity(x,y,z,entityName,groupSize,NameTagOrNBTFileName) or Entity(x,y,z,mobName,groupSize)");
		writer.comment("Use /otg entities to get a list of entities that can be used as entityName, this includes entities added by other mods and non-living entities.");
		writer.comment("NameTagOrNBTFileName can be either a nametag for the mob or an .txt file with nbt data (such as myentityinfo.txt).");
		writer.comment("In the text file you can use the same mob spawning parameters used with the /summon command to equip the");
		writer.comment("entity and give it custom attributes etc. You can copy the DATA part of a summon command including surrounding ");
		writer.comment("curly braces to a .txt file, for instance for: \"/summon Skeleton x y z {DATA}\"");

		for(BO4EntityFunction func : entitiesList)
		{
			writer.function(func);
		}
	}

	private int bo4DataVersion = 3;
	void writeToStream(DataOutput stream, String presetFolderName, Path otgRootFolder, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker) throws IOException
	{		
		stream.writeInt(this.bo4DataVersion);
		// Version 3 added fixedRotation		
		StreamHelper.writeStringToStream(stream, this.fixedRotation == null ? null : this.fixedRotation.toString());
		stream.writeInt(this.minimumSizeTop);
		stream.writeInt(this.minimumSizeBottom);
		stream.writeInt(this.minimumSizeLeft);
		stream.writeInt(this.minimumSizeRight);		
		stream.writeInt(this.minX);
		stream.writeInt(this.maxX);
		stream.writeInt(this.minY);
		stream.writeInt(this.maxY);
		stream.writeInt(this.minZ);
		stream.writeInt(this.maxZ);		
		StreamHelper.writeStringToStream(stream, this.author);
		StreamHelper.writeStringToStream(stream, this.description);
		StreamHelper.writeStringToStream(stream, this.settingsMode.name());
		stream.writeInt(this.frequency);
		StreamHelper.writeStringToStream(stream, this.spawnHeight.name());
		stream.writeInt(this.minHeight);
		stream.writeInt(this.maxHeight);
		stream.writeShort(this.inheritedBO3s.size());
		for(String inheritedBO3 : this.inheritedBO3s) {
			StreamHelper.writeStringToStream(stream, inheritedBO3);
		}
		StreamHelper.writeStringToStream(stream, this.inheritBO3);
		StreamHelper.writeStringToStream(stream, this.inheritBO3Rotation.name());
		stream.writeBoolean(this.overrideChildSettings);
		stream.writeBoolean(this.overrideParentHeight);
		stream.writeBoolean(this.canOverride);
		stream.writeInt(this.branchFrequency);
		StreamHelper.writeStringToStream(stream, this.branchFrequencyGroup);
		stream.writeBoolean(this.mustBeBelowOther);
		stream.writeBoolean(this.mustBeInsideWorldBorders);
		StreamHelper.writeStringToStream(stream, this.mustBeInside);
		StreamHelper.writeStringToStream(stream, this.cannotBeInside);
		StreamHelper.writeStringToStream(stream, this.replacesBO3);
		stream.writeBoolean(this.canSpawnOnWater);
		stream.writeBoolean(this.spawnOnWaterOnly);
		stream.writeBoolean(this.spawnUnderWater);
		stream.writeBoolean(this.spawnAtWaterLevel);
		stream.writeBoolean(this.doReplaceBlocks);
		stream.writeInt(this.heightOffset);
		stream.writeBoolean(this.removeAir);
		StreamHelper.writeStringToStream(stream, this.replaceAbove);
		StreamHelper.writeStringToStream(stream, this.replaceBelow);
		stream.writeBoolean(this.replaceWithBiomeBlocks);
		StreamHelper.writeStringToStream(stream, this.replaceWithSurfaceBlock);
		StreamHelper.writeStringToStream(stream, this.replaceWithGroundBlock);
		StreamHelper.writeStringToStream(stream, this.replaceWithStoneBlock);
		stream.writeInt(this.smoothRadius);
		stream.writeInt(this.smoothHeightOffset);
		stream.writeBoolean(this.smoothStartTop);
		stream.writeBoolean(this.smoothStartWood);
		StreamHelper.writeStringToStream(stream, this.smoothingSurfaceBlock);
		StreamHelper.writeStringToStream(stream, this.smoothingGroundBlock);
		StreamHelper.writeStringToStream(stream, this.bo3Group);
		stream.writeBoolean(this.isSpawnPoint);		
		stream.writeBoolean(this.isCollidable);
		stream.writeBoolean(this.useCenterForHighestBlock);

		stream.writeInt(this.branchesBO4.length);
		for(BO4BranchFunction func : Arrays.asList(this.branchesBO4))
		{
			if(func instanceof BO4WeightedBranchFunction)
			{
				stream.writeBoolean(true); // false For BO4BranchFunction, true for BO4WeightedBranchFunction
			} else {
				stream.writeBoolean(false); // false For BO4BranchFunction, true for BO4WeightedBranchFunction
			}
			func.writeToStream(stream);
		}
		
		stream.writeInt(this.entityDataBO4.length);
		for(BO4EntityFunction func : Arrays.asList(this.entityDataBO4))
		{
			func.writeToStream(stream);
		}
		
		stream.writeInt(0); // Used to be particledata length
		stream.writeInt(0); // Used to be spawnerdata length
		stream.writeInt(0); // Used to be moddata length
		
		ArrayList<LocalMaterialData> materials = new ArrayList<LocalMaterialData>();
		ArrayList<String> metaDataNames = new ArrayList<String>();
		int randomBlockCount = 0;
		int nonRandomBlockCount = 0;
		BO4BlockFunction[] blocks = getBlocks(presetFolderName, otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker);
		for(BO4BlockFunction block : blocks)
		{		
			if(block instanceof BO4RandomBlockFunction)
			{
				randomBlockCount++;
				for(LocalMaterialData material : ((BO4RandomBlockFunction)block).blocks)
				{
					if(!materials.contains(material))
					{
						materials.add(material);
					} 
				}
			} else {
				nonRandomBlockCount++;
			}
						
			if(block.material != null && !materials.contains(block.material))
			{
				materials.add(block.material);
			}			
			if(block.nbtName != null && !metaDataNames.contains(block.nbtName))
			{
				metaDataNames.add(block.nbtName);
			}
		}
		
		String[] metaDataNamesArr = metaDataNames.toArray(new String[metaDataNames.size()]);
		LocalMaterialData[] blocksArr = materials.toArray(new LocalMaterialData[materials.size()]);
		
		stream.writeShort(metaDataNamesArr.length);
		for(int i = 0; i < metaDataNamesArr.length; i++)
		{
			StreamHelper.writeStringToStream(stream, metaDataNamesArr[i]);
		}
		
		stream.writeShort(blocksArr.length);
		for(int i = 0; i < blocksArr.length; i++)
		{
			StreamHelper.writeStringToStream(stream, blocksArr[i].getName());
		}
		
		// TODO: This assumes that loading blocks in a different order won't matter, which may not be true?
		// Anything that spawns on top, entities/spawners etc, should be spawned last tho, so shouldn't be a problem?
		stream.writeInt(nonRandomBlockCount);
		int nonRandomBlockIndex = 0;
		ArrayList<BO4BlockFunction> blocksInColumn;
		if(nonRandomBlockCount > 0)
		{
			for(int x = this.getminX(); x < xSize; x++)
			{
				for(int z = this.getminZ(); z < zSize; z++)
				{
					blocksInColumn = new ArrayList<BO4BlockFunction>();
					for(BO4BlockFunction blockFunction : blocks)
					{
						if(!(blockFunction instanceof BO4RandomBlockFunction))
						{
							if(blockFunction.x == x && blockFunction.z == z)
							{
								blocksInColumn.add(blockFunction);
							}
						}
					}
					stream.writeShort(blocksInColumn.size());
					if(blocksInColumn.size() > 0)
					{
						for(BO4BlockFunction blockFunction : blocksInColumn)
						{
							blockFunction.writeToStream(metaDataNamesArr, blocksArr, stream);
							nonRandomBlockIndex++;
						}
					}
					if(nonRandomBlockIndex == nonRandomBlockCount)
					{
						break;
					}
				}
				if(nonRandomBlockIndex == nonRandomBlockCount)
				{
					break;
				}			
			}
		}

		stream.writeInt(randomBlockCount);
		int randomBlockIndex = 0;
		if(randomBlockCount > 0)
		{
			for(int x = this.getminX(); x < xSize; x++)
			{
				for(int z = this.getminZ(); z < zSize; z++)
				{
					blocksInColumn = new ArrayList<BO4BlockFunction>();
					for(BO4BlockFunction blockFunction : blocks)
					{
						if(blockFunction instanceof BO4RandomBlockFunction)
						{
							if(blockFunction.x == x && blockFunction.z == z)
							{
								blocksInColumn.add(blockFunction);
							}
						}
					}
					stream.writeShort(blocksInColumn.size());
					if(blocksInColumn.size() > 0)
					{
						for(BO4BlockFunction blockFunction : blocksInColumn)
						{
							blockFunction.writeToStream(metaDataNamesArr, blocksArr, stream);
							randomBlockIndex++;
						}
					}
					if(randomBlockIndex == randomBlockCount)
					{
						break;
					}
				}
				if(randomBlockIndex == randomBlockCount)
				{
					break;
				}			
			}
		}
	}

	private BO4Config readFromBO4DataFile(boolean getBlocks, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
	{
		FileInputStream fis;
		ByteBuffer bufferCompressed = null;
		ByteBuffer bufferDecompressed = null;
		try
		{
			fis = new FileInputStream(this.reader.getFile());
			try
			{
				bufferCompressed = fis.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, fis.getChannel().size());
				byte[] compressedBytes = new byte[(int) fis.getChannel().size()];
				bufferCompressed.get(compressedBytes);
				try {
					byte[] decompressedBytes = com.pg85.otg.util.CompressionUtils.decompress(compressedBytes);
					bufferDecompressed = ByteBuffer.wrap(decompressedBytes);
				} catch (DataFormatException e1) {
					e1.printStackTrace();
				}

				//buffer.get(data, 0, remaining);
				// do something with data

				boolean isBO4Data = true;
				boolean inheritedBO3Loaded = true;
				int bo4DataVersion = bufferDecompressed.getInt();
				// Version 2 made breaking changes
				if(bo4DataVersion < 2)
				{
					// TODO: Should only need to close the reader?
					if(bufferCompressed != null)
					{
						bufferCompressed.clear();
					}
					if(bufferDecompressed != null)
					{
						bufferDecompressed.clear();
					}				
					try {
						fis.getChannel().close();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
					try {
						fis.close();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
					throw new InvalidConfigException("Could not read BO4Data file " + this.reader.getName() + ", it is outdated. Delete and re-export BO4Data files to fix this, or delete and reinstall your OTG preset.");
				}
				// Version 3 added fixedRotation
				if(bo4DataVersion > 2)
				{
					String rotationString = StreamHelper.readStringFromBuffer(bufferDecompressed);
					this.fixedRotation = rotationString == null ? null : Rotation.FromString(rotationString);
				}
				int minimumSizeTop = bufferDecompressed.getInt();
				int minimumSizeBottom = bufferDecompressed.getInt();
				int minimumSizeLeft = bufferDecompressed.getInt();
				int minimumSizeRight = bufferDecompressed.getInt();

				int minX = bufferDecompressed.getInt();
				int maxX = bufferDecompressed.getInt();
				int minY = bufferDecompressed.getInt();
				int maxY = bufferDecompressed.getInt();
				int minZ = bufferDecompressed.getInt();
				int maxZ = bufferDecompressed.getInt();

				String author = StreamHelper.readStringFromBuffer(bufferDecompressed);
				String description = StreamHelper.readStringFromBuffer(bufferDecompressed);
				ConfigMode settingsMode = ConfigMode.valueOf(StreamHelper.readStringFromBuffer(bufferDecompressed));
				int frequency = bufferDecompressed.getInt();
				SpawnHeightEnum spawnHeight = SpawnHeightEnum.valueOf(StreamHelper.readStringFromBuffer(bufferDecompressed));
				int minHeight = bufferDecompressed.getInt();
				int maxHeight = bufferDecompressed.getInt();
				short inheritedBO3sSize = bufferDecompressed.getShort();
				ArrayList<String> inheritedBO3s = new ArrayList<String>();
				for(int i = 0; i < inheritedBO3sSize; i++)
				{
					inheritedBO3s.add(StreamHelper.readStringFromBuffer(bufferDecompressed));
				}

				String inheritBO3 = StreamHelper.readStringFromBuffer(bufferDecompressed);
				Rotation inheritBO3Rotation = Rotation.valueOf(StreamHelper.readStringFromBuffer(bufferDecompressed));
				boolean overrideChildSettings = bufferDecompressed.get() != 0;
				boolean overrideParentHeight = bufferDecompressed.get() != 0;
				boolean canOverride = bufferDecompressed.get() != 0;
				int branchFrequency = bufferDecompressed.getInt();
				String branchFrequencyGroup = StreamHelper.readStringFromBuffer(bufferDecompressed);
				boolean mustBeBelowOther = bufferDecompressed.get() != 0;
				boolean mustBeInsideWorldBorders = bufferDecompressed.get() != 0;
				String mustBeInside = StreamHelper.readStringFromBuffer(bufferDecompressed);
				String cannotBeInside = StreamHelper.readStringFromBuffer(bufferDecompressed);
				String replacesBO3 = StreamHelper.readStringFromBuffer(bufferDecompressed);
				boolean canSpawnOnWater = bufferDecompressed.get() != 0;
				boolean spawnOnWaterOnly = bufferDecompressed.get() != 0;
				boolean spawnUnderWater = bufferDecompressed.get() != 0;
				boolean spawnAtWaterLevel = bufferDecompressed.get() != 0;
				boolean doReplaceBlocks = bufferDecompressed.get() != 0;
				int heightOffset = bufferDecompressed.getInt();
				boolean removeAir = bufferDecompressed.get() != 0;
				String replaceAbove = StreamHelper.readStringFromBuffer(bufferDecompressed);
				String replaceBelow = StreamHelper.readStringFromBuffer(bufferDecompressed);
				boolean replaceWithBiomeBlocks = bufferDecompressed.get() != 0;
				String replaceWithSurfaceBlock = StreamHelper.readStringFromBuffer(bufferDecompressed);				
				String replaceWithGroundBlock = StreamHelper.readStringFromBuffer(bufferDecompressed);
				String replaceWithStoneBlock = StreamHelper.readStringFromBuffer(bufferDecompressed);
				int smoothRadius = bufferDecompressed.getInt();
				int smoothHeightOffset = bufferDecompressed.getInt();
				boolean smoothStartTop = bufferDecompressed.get() != 0;
				boolean smoothStartWood = bufferDecompressed.get() != 0;
				String smoothingSurfaceBlock = StreamHelper.readStringFromBuffer(bufferDecompressed);
				String smoothingGroundBlock = StreamHelper.readStringFromBuffer(bufferDecompressed);
				String bo3Group = StreamHelper.readStringFromBuffer(bufferDecompressed);
				boolean isSpawnPoint = bufferDecompressed.get() != 0;		
				boolean isCollidable = bufferDecompressed.get() != 0;
				boolean useCenterForHighestBlock = bufferDecompressed.get() != 0;				
			
				HashMap<String, Integer> branchFrequencyGroups = new HashMap<String, Integer>();
				if(branchFrequencyGroup != null && branchFrequencyGroup.trim().length() > 0)
				{
					String[] groupStrings = branchFrequencyGroup.split(",");
					if(groupStrings != null && groupStrings.length > 0)
					{
						for(int i = 0; i < groupStrings.length; i++)
						{
							String[] groupString = groupStrings[i].trim().length() > 0 ? groupStrings[i].split(":") : null;
							if(groupString != null && groupString.length == 2)
							{
								branchFrequencyGroups.put(groupString[0].trim(), Integer.parseInt(groupString[1].trim()));
							}
						}
					}
				}
				
				HashMap<String, Integer> bo4Groups = new HashMap<String, Integer>();
				if(bo3Group != null && bo3Group.trim().length() > 0)
				{
					String[] groupStrings = bo3Group.split(",");
					if(groupStrings != null && groupStrings.length > 0)
					{
						for(int i = 0; i < groupStrings.length; i++)
						{
							String[] groupString = groupStrings[i].trim().length() > 0 ? groupStrings[i].split(":") : null;
							if(groupString != null && groupString.length == 2)
							{
								bo4Groups.put(groupString[0].trim(), Integer.parseInt(groupString[1].trim()));
							}
						}
					}
				}			
							
				ArrayList<String> mustBeInsideBranches = new ArrayList<String>();
				if(mustBeInside != null && mustBeInside.trim().length() > 0)
				{
					String[] mustBeInsideStrings = mustBeInside.split(",");
					if(mustBeInsideStrings != null && mustBeInsideStrings.length > 0)
					{
						for(int i = 0; i < mustBeInsideStrings.length; i++)
						{
							String mustBeInsideString = mustBeInsideStrings[i].trim();
							if(mustBeInsideString.length() > 0)
							{
								mustBeInsideBranches.add(mustBeInsideString);
							}
						}
					}
				}
				
				ArrayList<String> cannotBeInsideBranches = new ArrayList<String>();
				if(cannotBeInside != null && cannotBeInside.trim().length() > 0)
				{
					String[] cannotBeInsideStrings = cannotBeInside.split(",");
					if(cannotBeInsideStrings != null && cannotBeInsideStrings.length > 0)
					{
						for(int i = 0; i < cannotBeInsideStrings.length; i++)
						{
							String cannotBeInsideString = cannotBeInsideStrings[i].trim();
							if(cannotBeInsideString.length() > 0)
							{
								cannotBeInsideBranches.add(cannotBeInsideString);
							}
						}
					}
				}
				
				ArrayList<String> replacesBO3Branches = new ArrayList<String>();
				if(replacesBO3 != null && replacesBO3.trim().length() > 0)
				{
					String[] replacesBO3Strings = replacesBO3.split(",");
					if(replacesBO3Strings != null && replacesBO3Strings.length > 0)
					{
						for(int i = 0; i < replacesBO3Strings.length; i++)
						{
							String replacesBO3String = replacesBO3Strings[i].trim();
							if(replacesBO3String.length() > 0)
							{
								replacesBO3Branches.add(replacesBO3String);
							}
						}
					}
				}
			
				int branchesOTGPlusLength = bufferDecompressed.getInt();
				boolean branchType;
				BO4BranchFunction branch;
				BO4BranchFunction[] branchesBO4 = new BO4BranchFunction[branchesOTGPlusLength];
				for(int i = 0; i < branchesOTGPlusLength; i++)
				{
					branchType = bufferDecompressed.get() != 0;
					if(branchType)
					{
						branch = BO4WeightedBranchFunction.fromStream(this, bufferDecompressed, logger, materialReader);
					} else {
						branch = BO4BranchFunction.fromStream(this, bufferDecompressed, logger, materialReader);
					}
					branchesBO4[i] = branch;
				}
				
				int entityDataOTGPlusLength = bufferDecompressed.getInt();
				BO4EntityFunction[] entityDataBO4 = new BO4EntityFunction[entityDataOTGPlusLength];
				for(int i = 0; i < entityDataOTGPlusLength; i++)
				{
					entityDataBO4[i] = BO4EntityFunction.fromStream(this, bufferDecompressed, logger);
				}

				// Legacy settings, hoping they were always 0 and noone actually used them :/.
				bufferDecompressed.getInt(); // Used to be particles
				bufferDecompressed.getInt(); // Used to be spawners
				bufferDecompressed.getInt(); // Used to be moddata
				//
					
				ArrayList<BlockFunction<?>> newBlocks = new ArrayList<>();
				short[][] columnSizes = null;
				
				this.minX = minX;
				this.maxX = maxX;
				this.minY = minY;
				this.maxY = maxY;
				this.minZ = minZ;
				this.maxZ = maxZ;
				
				// Reconstruct blocks
				if(getBlocks)
				{
					short metaDataNamesArrLength = bufferDecompressed.getShort();
					String[] metaDataNames = new String[metaDataNamesArrLength];
					for(int i = 0; i < metaDataNamesArrLength; i++)
					{
						metaDataNames[i] = StreamHelper.readStringFromBuffer(bufferDecompressed);
					}
					
					short blocksArrArrLength = bufferDecompressed.getShort();
					LocalMaterialData[] blocksArr = new LocalMaterialData[blocksArrArrLength];
					for(int i = 0; i < blocksArrArrLength; i++)
					{
						String materialName = StreamHelper.readStringFromBuffer(bufferDecompressed);
						try {
							blocksArr[i] = materialReader.readMaterial(materialName);
						} catch (InvalidConfigException e) {
							if(logger.getLogCategoryEnabled(LogCategory.CUSTOM_OBJECTS))
							{
								logger.log(LogLevel.ERROR, LogCategory.CUSTOM_OBJECTS, "Could not read material \"" + materialName + "\" for BO4 \"" + this.getName() + "\"");
								e.printStackTrace();
							}
						}
					}
									
					columnSizes = new short[this.xSize][this.zSize];
							
					// TODO: This assumes that loading blocks in a different order won't matter, which may not be true?
					// Anything that spawns on top, entities/spawners etc, should be spawned last tho, so shouldn't be a problem?
					int nonRandomBlockCount = bufferDecompressed.getInt();
					int nonRandomBlockIndex = 0;
					ArrayList<BO4BlockFunction> nonRandomBlocks = new ArrayList<BO4BlockFunction>();
					if(nonRandomBlockCount > 0)
					{
						for(int x = this.getminX(); x < this.xSize; x++)
						{
							for(int z = this.getminZ(); z < this.zSize; z++)
							{
								short blocksInColumnSize = bufferDecompressed.getShort();
								for(int j = 0; j < blocksInColumnSize; j++)
								{
									columnSizes[x][z]++;
									nonRandomBlocks.add(BO4BlockFunction.fromStream(x, z, metaDataNames, blocksArr, this, bufferDecompressed, logger));
									nonRandomBlockIndex++;
									if(nonRandomBlockCount == nonRandomBlockIndex)
									{
										break;
									}
								}
								if(nonRandomBlockCount == nonRandomBlockIndex)
								{
									break;
								}
							}
							if(nonRandomBlockCount == nonRandomBlockIndex)
							{
								break;
							}
						}
					}				
									
					int randomBlockCount = bufferDecompressed.getInt();
					int randomBlockIndex = 0;
					ArrayList<BO4RandomBlockFunction> randomBlocks = new ArrayList<BO4RandomBlockFunction>();
					if(randomBlockCount > 0)
					{
						for(int x = this.getminX(); x < this.xSize; x++)
						{
							for(int z = this.getminZ(); z < this.zSize; z++)
							{
								short blocksInColumnSize = bufferDecompressed.getShort();
								for(int j = 0; j < blocksInColumnSize; j++)
								{
									columnSizes[x][z]++;
									randomBlocks.add(BO4RandomBlockFunction.fromStream(x, z, metaDataNames, blocksArr, this, bufferDecompressed, logger));
									randomBlockIndex++;
									if(randomBlockCount == randomBlockIndex)
									{
										break;
									}
								}
								if(randomBlockCount == randomBlockIndex)
								{
									break;
								}
							}
							if(randomBlockCount == randomBlockIndex)
							{
								break;
							}
						}
					}
									
					newBlocks = new ArrayList<>();
					newBlocks.addAll(nonRandomBlocks);
					newBlocks.addAll(randomBlocks);				
				}

				this.isBO4Data = isBO4Data;
				this.inheritedBO3Loaded = inheritedBO3Loaded;
				this.minimumSizeTop = minimumSizeTop;
				this.minimumSizeBottom = minimumSizeBottom;
				this.minimumSizeLeft = minimumSizeLeft;
				this.minimumSizeRight = minimumSizeRight;
							
				this.author = author;
				this.description = description;
				this.settingsMode = settingsMode;
				this.frequency = frequency;
				this.spawnHeight = spawnHeight;
				this.minHeight = minHeight;
				this.maxHeight = maxHeight;
				this.inheritedBO3s = inheritedBO3s;
							
				this.inheritBO3 = inheritBO3;
				this.inheritBO3Rotation = inheritBO3Rotation;
				this.overrideChildSettings = overrideChildSettings;
				this.overrideParentHeight = overrideParentHeight;
				this.canOverride = canOverride;
				this.branchFrequency = branchFrequency;
				this.branchFrequencyGroup = branchFrequencyGroup;
				this.mustBeBelowOther = mustBeBelowOther;
				this.mustBeInsideWorldBorders = mustBeInsideWorldBorders;
				this.mustBeInside = mustBeInside;
				this.cannotBeInside = cannotBeInside;
				this.replacesBO3 = replacesBO3;
				this.canSpawnOnWater = canSpawnOnWater;
				this.spawnOnWaterOnly = spawnOnWaterOnly;
				this.spawnUnderWater = spawnUnderWater;
				this.spawnAtWaterLevel = spawnAtWaterLevel;
				this.doReplaceBlocks = doReplaceBlocks;
				this.heightOffset = heightOffset;
				this.removeAir = removeAir;
				this.replaceAbove = replaceAbove;
				this.replaceBelow = replaceBelow;
				this.replaceWithBiomeBlocks = replaceWithBiomeBlocks;
				this.replaceWithSurfaceBlock = replaceWithSurfaceBlock;				
				this.replaceWithGroundBlock = replaceWithGroundBlock;
				this.replaceWithStoneBlock = replaceWithStoneBlock;
				this.smoothRadius = smoothRadius;
				this.smoothHeightOffset = smoothHeightOffset;
				this.smoothStartTop = smoothStartTop;
				this.smoothStartWood = smoothStartWood;
				this.smoothingSurfaceBlock = smoothingSurfaceBlock;
				this.smoothingGroundBlock = smoothingGroundBlock;
				this.bo3Group = bo3Group;
				this.isSpawnPoint = isSpawnPoint;
				this.isCollidable = isCollidable;
				this.useCenterForHighestBlock = useCenterForHighestBlock;
			
				this.branchFrequencyGroups = branchFrequencyGroups;
				this.bo4Groups = bo4Groups;							
				this.mustBeInsideBranches = mustBeInsideBranches;			
				this.cannotBeInsideBranches = cannotBeInsideBranches;			
				this.replacesBO3Branches = replacesBO3Branches;
							
				this.branchesBO4 = branchesBO4;
				this.entityDataBO4 = entityDataBO4;

				// Reconstruct blocks
				if(getBlocks)
				{
					loadBlockArrays(newBlocks, columnSizes);
				}
			}
			catch (Exception | Error e1)
			{
				// TODO: Should only need to close the reader?
				if(bufferCompressed != null)
				{
					bufferCompressed.clear();
				}
				if(bufferDecompressed != null)
				{
					bufferDecompressed.clear();
				}				
				try {
					fis.getChannel().close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
				try {
					fis.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
				
				e1.printStackTrace();
				throw new InvalidConfigException("Could not read BO4Data file " + this.reader.getName() + ", it may be outdated or corrupted. Delete and re-export BO4Data files to fix this, or delete and reinstall your OTG preset.");
			}

			// When finished
			
			// TODO: Should only need to close the reader?
			if(bufferCompressed != null)
			{
				bufferCompressed.clear();
			}
			if(bufferDecompressed != null)
			{
				bufferDecompressed.clear();
			}
			try {
				fis.getChannel().close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			try {
				fis.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		catch (FileNotFoundException e2)
		{
			e2.printStackTrace();
			return null;
		}

		return this;
	}
			
	private void loadBlockArrays(List<BlockFunction<?>> newBlocks, short[][] columnSizes)
	{
		// Store blocks in arrays instead of BO4BlockFunctions,
		// since that gives way too much overhead memory wise.
		// We may have tens of millions of blocks, java doesn't handle lots of small classes well.
		this.blocks = new short[xSize][zSize][];
		this.blocksMaterial = new LocalMaterialData[newBlocks.size()];
		this.blocksMetaDataName = new String[newBlocks.size()];
		this.blocksMetaDataTag = new NamedBinaryTag[newBlocks.size()];
		
		this.randomBlocksBlocks = new LocalMaterialData[newBlocks.size()][];
		this.randomBlocksBlockChances = new byte[newBlocks.size()][];
		this.randomBlocksMetaDataNames = new String[newBlocks.size()][];
		this.randomBlocksMetaDataTags = new NamedBinaryTag[newBlocks.size()][];
		this.randomBlocksBlockCount = new byte[newBlocks.size()]; 
		
		BO4BlockFunction block;
		short[][] columnBlockIndex = new short[xSize][zSize];
		for(int x = 0; x < xSize; x++)
		{
			for(int z = 0; z < zSize; z++)
			{
				if(this.blocks[x][z] == null)
				{
					this.blocks[x ][z] = new short[columnSizes[x][z]];
				}
			}
		}
		for(int i = 0; i < newBlocks.size(); i++)
		{
			block = (BO4BlockFunction) newBlocks.get(i);

			this.blocks[block.x][block.z][columnBlockIndex[block.x][block.z]] = (short) block.y;

			int blockIndex = columnBlockIndex[block.x][block.z] + getColumnBlockIndex(columnSizes, block.x, block.z);

			this.blocksMaterial[blockIndex] = block.material;
			this.blocksMetaDataName[blockIndex] = block.nbtName;
			this.blocksMetaDataTag[blockIndex] = block.nbt;
			
			if(block instanceof BO4RandomBlockFunction)
			{
				this.randomBlocksBlocks[blockIndex] = ((BO4RandomBlockFunction)block).blocks;
				this.randomBlocksBlockChances[blockIndex] = ((BO4RandomBlockFunction)block).blockChances;
				this.randomBlocksMetaDataNames[blockIndex] = ((BO4RandomBlockFunction)block).metaDataNames;
				this.randomBlocksMetaDataTags[blockIndex] = ((BO4RandomBlockFunction)block).metaDataTags;
				this.randomBlocksBlockCount[blockIndex] = ((BO4RandomBlockFunction)block).blockCount;
			}
			columnBlockIndex[block.x][block.z]++;
		}
	}
	
	private int getColumnBlockIndex(short[][] columnSizes, int columnX, int columnZ)
	{
		int blockIndex = 0;
		for(int x = 0; x < 16; x++)
		{
			for(int z = 0; z < 16; z++)
			{
				if(columnX == x && columnZ == z)
				{
					return blockIndex;
				}
				blockIndex += columnSizes[x][z];
			}
		}
		return blockIndex;
	}
	
	@Override
	protected void correctSettings() { }

	@Override
	protected void renameOldSettings() { }

    public boolean isCollidable()
    {
    	return isCollidable;
    }

	public void setBlocks(List<BlockFunction<?>> newBlocks)
	{
		short[][] columnSizes = new short[16][16];
		for(BlockFunction<?> block : newBlocks)
		{
			columnSizes[block.x][block.z]++;
		}

		loadBlockArrays(newBlocks, columnSizes);
	}
}
