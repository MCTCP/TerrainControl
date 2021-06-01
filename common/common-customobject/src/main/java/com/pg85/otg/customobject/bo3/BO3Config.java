package com.pg85.otg.customobject.bo3;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.pg85.otg.config.standard.WorldStandardValues;
import com.pg85.otg.constants.SettingsEnums.ConfigMode;
import com.pg85.otg.customobject.CustomObjectManager;
import com.pg85.otg.customobject.bo3.bo3function.BO3BlockFunction;
import com.pg85.otg.customobject.bo3.bo3function.BO3BranchFunction;
import com.pg85.otg.customobject.bo3.bo3function.BO3EntityFunction;
import com.pg85.otg.customobject.bo3.bo3function.BO3ModDataFunction;
import com.pg85.otg.customobject.bo3.bo3function.BO3ParticleFunction;
import com.pg85.otg.customobject.bo3.bo3function.BO3RandomBlockFunction;
import com.pg85.otg.customobject.bo3.bo3function.BO3SpawnerFunction;
import com.pg85.otg.customobject.bo3.bo3function.BO3WeightedBranchFunction;
import com.pg85.otg.customobject.bo3.checks.BO3Check;
import com.pg85.otg.customobject.bo3.checks.ModCheck;
import com.pg85.otg.customobject.bo3.checks.ModCheckNot;
import com.pg85.otg.customobject.config.CustomObjectConfigFile;
import com.pg85.otg.customobject.config.CustomObjectConfigFunction;
import com.pg85.otg.customobject.config.CustomObjectResourcesManager;
import com.pg85.otg.customobject.config.io.SettingsReaderBO4;
import com.pg85.otg.customobject.config.io.SettingsWriterBO4;
import com.pg85.otg.customobject.util.BoundingBox;
import com.pg85.otg.customobject.util.BO3Enums.ExtrudeMode;
import com.pg85.otg.customobject.util.BO3Enums.OutsideSourceBlock;
import com.pg85.otg.customobject.util.BO3Enums.SpawnHeightEnum;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.logging.ILogger;
import com.pg85.otg.util.interfaces.ICustomObjectManager;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.interfaces.IModLoadedChecker;
import com.pg85.otg.util.bo3.NamedBinaryTag;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.MaterialSet;
import com.pg85.otg.util.minecraft.DefaultStructurePart;

public class BO3Config extends CustomObjectConfigFile
{
	// TODO: Split this up into multiple config classes like common-core
	// does for world/biome configs, add getters etc.
	
	private boolean isOTGPlus; // Legacy setting
	boolean doReplaceBlocks;
	
	public String author;
	public String description;
	ConfigMode settingsMode;
	boolean tree;
	int frequency;
	double rarity;
	protected boolean rotateRandomly;
	SpawnHeightEnum spawnHeight;
	// Extra spawn height settings
	int spawnHeightOffset;
	int spawnHeightVariance;

	// Extrusion
	ExtrudeMode extrudeMode;
	MaterialSet extrudeThroughBlocks;

	public int minHeight;
	public int maxHeight;
	private List<String> excludedBiomes;
	protected MaterialSet sourceBlocks;
	int maxPercentageOutsideSourceBlock;
	OutsideSourceBlock outsideSourceBlock;

	// Store blocks in arrays instead of as BO3BlockFunctions,
	// since that gives way too much overhead memory wise.
	// We may have tens of millions of blocks, java doesn't handle lots of small
	// classes well.
	private byte[][] blocksX;
	private short[][] blocksY;
	private byte[][] blocksZ;
	private LocalMaterialData[][] blocksMaterial;
	private String[] blocksMetaDataName;
	private NamedBinaryTag[] blocksMetaDataTag;

	private LocalMaterialData[][][] randomBlocksBlocks;
	private byte[][] randomBlocksBlockChances;
	private String[][] randomBlocksMetaDataNames;
	private NamedBinaryTag[][] randomBlocksMetaDataTags;
	private byte[] randomBlocksBlockCount;
	//

	BO3Check[][] bo3Checks = new BO3Check[4][];
	int maxBranchDepth;
	BO3BranchFunction[][] branches = new BO3BranchFunction[4][];

	BoundingBox[] boundingBoxes = new BoundingBox[4];

	BO3ParticleFunction[][] particleFunctions = new BO3ParticleFunction[4][];
	BO3SpawnerFunction[][] spawnerFunctions = new BO3SpawnerFunction[4][];
	BO3ModDataFunction[][] modDataFunctions = new BO3ModDataFunction[4][];
	BO3EntityFunction[][] entityFunctions = new BO3EntityFunction[4][];

	/*
	 * Creates a BO3Config from a file.
	 */
	protected BO3Config(SettingsReaderBO4 reader, String presetName, Path otgRootFolder, boolean spawnLog, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker) throws InvalidConfigException
	{
		super(reader);
		init(presetName, otgRootFolder, spawnLog, logger, customObjectManager, materialReader, manager, modLoadedChecker);
	}

	private BO3Config(SettingsReaderBO4 reader)
	{
		super(reader);
	}

	/**
	 *
	 * @return A clone BO3Config, used for templates in BO3Creator
	 */
	protected BO3Config cloneConfigValues(SettingsReaderBO4 newReader)
	{
		BO3Config clone = new BO3Config(newReader);
		clone.author = author;
		clone.doReplaceBlocks = doReplaceBlocks;
		clone.description = description;
		clone.settingsMode = settingsMode;
		clone.tree = tree;
		clone.frequency = frequency;
		clone.rarity = rarity;
		clone.rotateRandomly = rotateRandomly;
		clone.spawnHeight = spawnHeight;

		clone.spawnHeightOffset = spawnHeightOffset;
		clone.spawnHeightVariance = spawnHeightVariance;

		clone.extrudeMode = extrudeMode;
		clone.extrudeThroughBlocks = extrudeThroughBlocks;

		clone.minHeight = minHeight;
		clone.maxHeight = maxHeight;
		clone.excludedBiomes = excludedBiomes;
		clone.sourceBlocks = sourceBlocks;
		clone.maxPercentageOutsideSourceBlock = maxPercentageOutsideSourceBlock;
		clone.outsideSourceBlock = outsideSourceBlock;
		clone.maxBranchDepth = maxBranchDepth;

		clone.bo3Checks[0] = this.bo3Checks[0].clone(); //new BO3Check[0];
		clone.branches[0] = this.branches[0].clone(); //new BO3BranchFunction[0];
		clone.boundingBoxes[0] = this.boundingBoxes[0].clone(); // BoundingBox.newEmptyBox();
		clone.entityFunctions[0] = this.entityFunctions[0].clone(); // new BO3EntityFunction[0];
		clone.particleFunctions[0] = this.particleFunctions[0].clone(); // new BO3ParticleFunction[0];
		clone.modDataFunctions[0] = this.modDataFunctions[0].clone(); // new BO3ModDataFunction[0];
		clone.spawnerFunctions[0] = this.spawnerFunctions[0].clone(); //new BO3SpawnerFunction[0];

		return clone;
	}

	private void init(String presetName, Path otgRootFolder, boolean spawnLog, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker) throws InvalidConfigException
	{
		this.isOTGPlus = false;
		// Init settings
		readConfigSettings(presetName, otgRootFolder, spawnLog, logger, customObjectManager, materialReader, manager, modLoadedChecker);
		// Read the resources
		readResources(spawnLog, logger, materialReader, manager);

		this.reader.flushCache();
		rotateBlocksAndChecks(presetName, otgRootFolder, spawnLog, logger, customObjectManager, materialReader, manager, modLoadedChecker);
	}

	private void readResources(boolean spawnLog, ILogger logger, IMaterialReader materialReader, CustomObjectResourcesManager manager) throws InvalidConfigException
	{
		List<BO3BlockFunction> tempBlocksList = new ArrayList<BO3BlockFunction>();
		List<BO3Check> tempChecksList = new ArrayList<BO3Check>();
		List<BO3BranchFunction> tempBranchesList = new ArrayList<BO3BranchFunction>();
		List<BO3EntityFunction> tempEntitiesList = new ArrayList<BO3EntityFunction>();
		List<BO3ModDataFunction> tempModDataList = new ArrayList<BO3ModDataFunction>();
		List<BO3ParticleFunction> tempParticlesList = new ArrayList<BO3ParticleFunction>();
		List<BO3SpawnerFunction> tempSpawnerList = new ArrayList<BO3SpawnerFunction>();

		BoundingBox box = BoundingBox.newEmptyBox();

		for (CustomObjectConfigFunction<BO3Config> res : this.reader.getConfigFunctions(this, true, spawnLog, logger, materialReader, manager))
		{
			if (res.isValid())
			{
				if (res instanceof BO3BlockFunction)
				{
					BO3BlockFunction block = (BO3BlockFunction) res;
					box.expandToFit(block.x, block.y, block.z);
					tempBlocksList.add(block);
				} else {
					if (res instanceof BO3Check)
					{
						tempChecksList.add((BO3Check) res);
					}
					else if (res instanceof BO3WeightedBranchFunction)
					{
						tempBranchesList.add((BO3WeightedBranchFunction) res);
					}
					else if (res instanceof BO3BranchFunction)
					{
						tempBranchesList.add((BO3BranchFunction) res);
					}
					else if (res instanceof BO3EntityFunction)
					{
						tempEntitiesList.add((BO3EntityFunction) res);
					}
					else if (res instanceof BO3ParticleFunction)
					{
						tempParticlesList.add((BO3ParticleFunction) res);
					}
					else if (res instanceof BO3ModDataFunction)
					{
						tempModDataList.add((BO3ModDataFunction) res);
					}
					else if (res instanceof BO3SpawnerFunction)
					{
						tempSpawnerList.add((BO3SpawnerFunction) res);
					}
				}
			}
		}

		extractBlocks(tempBlocksList);

		this.bo3Checks[0] = tempChecksList.toArray(new BO3Check[tempChecksList.size()]);
		this.branches[0] = tempBranchesList.toArray(new BO3BranchFunction[tempBranchesList.size()]);
		this.boundingBoxes[0] = box;
		this.entityFunctions[0] = tempEntitiesList.toArray(new BO3EntityFunction[tempEntitiesList.size()]);
		this.particleFunctions[0] = tempParticlesList.toArray(new BO3ParticleFunction[tempParticlesList.size()]);
		this.modDataFunctions[0] = tempModDataList.toArray(new BO3ModDataFunction[tempModDataList.size()]);
		this.spawnerFunctions[0] = tempSpawnerList.toArray(new BO3SpawnerFunction[tempSpawnerList.size()]);
	}

	public void setBranches(List<BO3BranchFunction> branches)
	{
		this.branches[0] = branches.toArray(new BO3BranchFunction[branches.size()]);
	}

	protected void extractBlocks(List<BO3BlockFunction> tempBlocksList)
	{
		this.blocksX = new byte[4][tempBlocksList.size()];
		this.blocksY = new short[4][tempBlocksList.size()];
		this.blocksZ = new byte[4][tempBlocksList.size()];
		this.blocksMaterial = new LocalMaterialData[4][tempBlocksList.size()];
		this.blocksMetaDataName = new String[tempBlocksList.size()];
		this.blocksMetaDataTag = new NamedBinaryTag[tempBlocksList.size()];

		this.randomBlocksBlocks = new LocalMaterialData[4][tempBlocksList.size()][];
		this.randomBlocksBlockChances = new byte[tempBlocksList.size()][];
		this.randomBlocksMetaDataNames = new String[tempBlocksList.size()][];
		this.randomBlocksMetaDataTags = new NamedBinaryTag[tempBlocksList.size()][];
		this.randomBlocksBlockCount = new byte[tempBlocksList.size()];

		for (int i = 0; i < tempBlocksList.size(); i++)
		{
			BO3BlockFunction block = tempBlocksList.get(i);
			// We can probably just break if null?
			if (block != null)
			{
				this.blocksX[0][i] = (byte) block.x;
				this.blocksY[0][i] = (short) block.y;
				this.blocksZ[0][i] = (byte) block.z;
				this.blocksMaterial[0][i] = block.material;
				this.blocksMetaDataName[i] = block.nbtName;
				this.blocksMetaDataTag[i] = block.nbt;

				if (block instanceof BO3RandomBlockFunction)
				{
					this.randomBlocksBlocks[0][i] = ((BO3RandomBlockFunction) block).blocks;
					this.randomBlocksBlockChances[i] = ((BO3RandomBlockFunction) block).blockChances;
					this.randomBlocksMetaDataNames[i] = ((BO3RandomBlockFunction) block).metaDataNames;
					this.randomBlocksMetaDataTags[i] = ((BO3RandomBlockFunction) block).metaDataTags;
					this.randomBlocksBlockCount[i] = ((BO3RandomBlockFunction) block).blockCount;
				}
			}
		}
	}

	/**
	 * Gets the file this config will be written to. May be null if the config will
	 * never be written.
	 * 
	 * @return The file.
	 */
	public File getFile()
	{
		return this.reader.getFile();
	}

	public BO3BlockFunction[] getBlocks(int rotation)
	{
		BO3BlockFunction[] blocksOTGPlus = new BO3BlockFunction[this.blocksX[rotation].length];

		BO3BlockFunction block;
		for (int i = 0; i < this.blocksX[rotation].length; i++)
		{
			if (this.randomBlocksBlocks[rotation][i] != null)
			{
				block = new BO3RandomBlockFunction(this);
				((BO3RandomBlockFunction) block).blocks = this.randomBlocksBlocks[rotation][i];
				((BO3RandomBlockFunction) block).blockChances = this.randomBlocksBlockChances[i];
				((BO3RandomBlockFunction) block).metaDataNames = this.randomBlocksMetaDataNames[i];
				((BO3RandomBlockFunction) block).metaDataTags = this.randomBlocksMetaDataTags[i];
				((BO3RandomBlockFunction) block).blockCount = this.randomBlocksBlockCount[i];
			} else {
				block = new BO3BlockFunction(this);
			}

			block.x = this.blocksX[rotation][i];
			block.y = this.blocksY[rotation][i];
			block.z = this.blocksZ[rotation][i];
			block.material = this.blocksMaterial[rotation][i];
			block.nbtName = this.blocksMetaDataName[i];
			block.nbt = this.blocksMetaDataTag[i];

			blocksOTGPlus[i] = block;
		}

		return blocksOTGPlus;
	}

	protected BO3BranchFunction[] getbranches()
	{
		return this.branches[0];
	}

	public BO3ModDataFunction[] getModData()
	{
		return this.modDataFunctions[0];
	}

	public BO3SpawnerFunction[] getSpawnerData()
	{
		return this.spawnerFunctions[0];
	}

	public BO3ParticleFunction[] getParticleData()
	{
		return this.particleFunctions[0];
	}

	public BO3Check[] getBO3Checks()
	{
		return this.bo3Checks[0];
	}

	public BO3EntityFunction[] getEntityData()
	{
		return this.entityFunctions[0];
	}

	@Override
	protected void writeConfigSettings(SettingsWriterBO4 writer, boolean spawnLog, ILogger logger, IMaterialReader materialReader, CustomObjectResourcesManager manager) throws IOException
	{
		// The object
		writer.bigTitle("BO3 object");
		writer.comment("This is the config file of a custom object.");
		writer.comment("If you add this object correctly to your BiomeConfigs, it will spawn in the world.");
		writer.comment("");

		writer.comment("This is the creator of this BO3 object");
		writer.setting(BO3Settings.AUTHOR, this.author);

		writer.comment("A short description of this BO3 object");
		writer.setting(BO3Settings.DESCRIPTION, this.description);

		writer.comment("The BO3 version, don't change this! It can be used by external applications to do a version check.");
		writer.setting(BO3Settings.VERSION, "3");

		writer.comment("The settings mode, WriteAll, WriteWithoutComments or WriteDisable. See WorldConfig.");
		writer.setting(WorldStandardValues.SETTINGS_MODE_BO3, this.settingsMode);

		// Main settings
		writer.bigTitle("Main settings");

		writer.comment("This needs to be set to true to spawn the object in the Tree and Sapling resources.");
		writer.setting(BO3Settings.TREE, this.tree);

		writer.comment(
				"The frequency of the BO3 from 1 to 200. Tries this many times to spawn this BO3 when using the CustomObject(...) resource.");
		writer.comment("Ignored by Tree(..), Sapling(..) and CustomStructure(..)");
		writer.setting(BO3Settings.FREQUENCY, this.frequency);

		writer.comment(
				"The rarity of the BO3 from 0 to 100. Each spawn attempt has rarity% chance to succeed when using the CustomObject(...) resource.");
		writer.comment("Ignored by Tree(..), Sapling(..) and CustomStructure(..)");
		writer.setting(BO3Settings.RARITY, this.rarity);

		writer.comment("If you set this to true, the BO3 will be placed with a random rotation.");
		writer.setting(BO3Settings.ROTATE_RANDOMLY, this.rotateRandomly);

		writer.comment("The spawn height of the BO3: randomY, highestBlock or highestSolidBlock.");
		writer.setting(BO3Settings.SPAWN_HEIGHT, this.spawnHeight);

		writer.comment("The offset from the spawn height to spawn this BO3");
		writer.comment(
				"Ex. SpawnHeight = highestSolidBlock, SpawnHeightOffset = 3; This object will spawn 3 blocks above the highest solid block");
		writer.setting(BO3Settings.SPAWN_HEIGHT_OFFSET, this.spawnHeightOffset);

		writer.comment("A random amount to offset the spawn location from the spawn offset height");
		writer.comment(
				"Ex. SpawnHeightOffset = 3, SpawnHeightVariance = 3; This object will spawn 3 to 6 blocks above the original spot it would have spawned");
		writer.setting(BO3Settings.SPAWN_HEIGHT_VARIANCE, this.spawnHeightVariance);

		writer.smallTitle("Height Limits for the BO3.");

		writer.comment(
				"When in randomY mode used as the minimum Y or in atMinY mode as the actual Y to spawn this BO3 at.");
		writer.setting(BO3Settings.MIN_HEIGHT, this.minHeight);

		writer.comment("When in randomY mode used as the maximum Y to spawn this BO3 at.");
		writer.setting(BO3Settings.MAX_HEIGHT, this.maxHeight);

		writer.smallTitle("Extrusion settings");

		writer.comment("The style of extrusion you wish to use - BottomDown, TopUp, None (Default)");
		writer.setting(BO3Settings.EXTRUDE_MODE, this.extrudeMode);

		writer.comment("The blocks to extrude your BO3 through");
		writer.setting(BO3Settings.EXTRUDE_THROUGH_BLOCKS, this.extrudeThroughBlocks);

		writer.comment("Objects can have other objects attacthed to it: branches. Branches can also");
		writer.comment("have branches attached to it, which can also have branches, etc. This is the");
		writer.comment("maximum branch depth for this objects.");
		writer.setting(BO3Settings.MAX_BRANCH_DEPTH, this.maxBranchDepth);

		writer.comment("When spawned with the UseWorld keyword, this BO3 should NOT spawn in the following biomes.");
		writer.comment("If you write the BO3 name directly in the BiomeConfigs, this will be ignored.");
		writer.setting(BO3Settings.EXCLUDED_BIOMES, this.excludedBiomes);

		// Sourceblock
		writer.bigTitle("Source block settings");

		writer.comment("The block(s) the BO3 should spawn in.");
		writer.setting(BO3Settings.SOURCE_BLOCKS, this.sourceBlocks);

		writer.comment("The maximum percentage of the BO3 that can be outside the SourceBlock.");
		writer.comment(
				"The BO3 won't be placed on a location with more blocks outside the SourceBlock than this percentage.");
		writer.setting(BO3Settings.MAX_PERCENTAGE_OUTSIDE_SOURCE_BLOCK, this.maxPercentageOutsideSourceBlock);

		writer.comment(
				"What to do when a block is about to be placed outside the SourceBlock? (dontPlace, placeAnyway)");
		writer.setting(BO3Settings.OUTSIDE_SOURCE_BLOCK, this.outsideSourceBlock);

		writer.comment(
				"Disable doReplaceBlocks to make this BO3 ignore any ReplacedBlocks settings in biome configs, improves performance.");
		writer.setting(BO3Settings.DO_REPLACE_BLOCKS, this.doReplaceBlocks);
		
		writer.comment("OTG+ settings #");

		writer.comment(
				"Legacy setting, rename this file to .BO4 instead. Set this to true to enable the advanced customstructure features of OTG+.");
		writer.setting(BO3Settings.IS_OTG_PLUS, this.isOTGPlus);

		// Blocks and other things
		writeResources(writer);
	}

	@Override	
	protected void readConfigSettings(String presetName, Path otgRootFolder, boolean spawnLog, ILogger logger, ICustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker) throws InvalidConfigException
	{
		this.isOTGPlus = readSettings(BO3Settings.IS_OTG_PLUS, spawnLog, logger, materialReader, manager);

		if (this.isOTGPlus)
		{
			throw new InvalidConfigException("isOTGPlus: true for a .bo3 file, file must be .bo4.");
		}

		this.author = readSettings(BO3Settings.AUTHOR, spawnLog, logger, null, null);
		this.description = readSettings(BO3Settings.DESCRIPTION, spawnLog, logger, null, null);
		this.settingsMode = readSettings(WorldStandardValues.SETTINGS_MODE_BO3, spawnLog, logger, null, null);

		this.tree = readSettings(BO3Settings.TREE, spawnLog, logger, null, null);
		this.frequency = readSettings(BO3Settings.FREQUENCY, spawnLog, logger, null, null);
		this.rarity = readSettings(BO3Settings.RARITY, spawnLog, logger, null, null);
		this.rotateRandomly = readSettings(BO3Settings.ROTATE_RANDOMLY, spawnLog, logger, null, null);
		this.spawnHeight = readSettings(BO3Settings.SPAWN_HEIGHT, spawnLog, logger, null, null);
		this.spawnHeightOffset = readSettings(BO3Settings.SPAWN_HEIGHT_OFFSET, spawnLog, logger, null, null);
		this.spawnHeightVariance = readSettings(BO3Settings.SPAWN_HEIGHT_VARIANCE, spawnLog, logger, null, null);
		this.extrudeMode = readSettings(BO3Settings.EXTRUDE_MODE, spawnLog, logger, null, null);
		this.extrudeThroughBlocks = readSettings(BO3Settings.EXTRUDE_THROUGH_BLOCKS, spawnLog, logger, materialReader, manager);
		this.minHeight = readSettings(BO3Settings.MIN_HEIGHT, spawnLog, logger, null, null);
		this.maxHeight = readSettings(BO3Settings.MAX_HEIGHT, spawnLog, logger, null, null);
		this.maxHeight = this.maxHeight < this.minHeight ? this.minHeight : this.maxHeight;
		this.maxBranchDepth = readSettings(BO3Settings.MAX_BRANCH_DEPTH, spawnLog, logger, null, null);
		this.excludedBiomes = new ArrayList<String>(readSettings(BO3Settings.EXCLUDED_BIOMES, spawnLog, logger, null, null));

		this.sourceBlocks = readSettings(BO3Settings.SOURCE_BLOCKS, spawnLog, logger, materialReader, manager);
		this.maxPercentageOutsideSourceBlock = readSettings(BO3Settings.MAX_PERCENTAGE_OUTSIDE_SOURCE_BLOCK, spawnLog, logger, null, null);
		this.outsideSourceBlock = readSettings(BO3Settings.OUTSIDE_SOURCE_BLOCK, spawnLog, logger, null, null);
		this.doReplaceBlocks = readSettings(BO3Settings.DO_REPLACE_BLOCKS, spawnLog, logger, null, null);
	}

	private void writeResources(SettingsWriterBO4 writer) throws IOException
	{
		writer.bigTitle("Blocks");
		writer.comment("All the blocks used in the BO3 are listed here. Possible blocks:");
		writer.comment("Block(x,y,z,id[.data][,nbtfile.nbt)");
		writer.comment("RandomBlock(x,y,z,id[:data][,nbtfile.nbt],chance[,id[:data][,nbtfile.nbt],chance[,...]])");
		writer.comment(" So RandomBlock(0,0,0,CHEST,chest.nbt,50,CHEST,anotherchest.nbt,100) will spawn a chest at");
		writer.comment(" the BO3 origin, and give it a 50% chance to have the contents of chest.nbt, or, if that");
		writer.comment(" fails, a 100% percent chance to have the contents of anotherchest.nbt.");
		writer.comment(
				"*Note: Unlike Entity() and Spawner(), for Block() .txt files don't work, only .nbt files work.");
		writer.comment("MinecraftObject(x,y,z,name) (TODO: This may not work anymore and needs to be tested.");
		writer.comment(" Spawns an object in the Mojang NBT structure format. For example, ");
		writer.comment(" MinecraftObject(0,0,0," + DefaultStructurePart.IGLOO_BOTTOM.getPath() + ")");
		writer.comment(" spawns the bottom part of an igloo.");

		for (int i = 0; i < this.blocksX[0].length; i++)
		{
			BO3BlockFunction blockFunction;

			if (this.randomBlocksBlocks[0][i] != null)
			{
				blockFunction = new BO3RandomBlockFunction(this);
				((BO3RandomBlockFunction) blockFunction).blocks = this.randomBlocksBlocks[0][i];
				((BO3RandomBlockFunction) blockFunction).blockChances = this.randomBlocksBlockChances[i];
				((BO3RandomBlockFunction) blockFunction).metaDataNames = this.randomBlocksMetaDataNames[i];
				((BO3RandomBlockFunction) blockFunction).metaDataTags = this.randomBlocksMetaDataTags[i];
				((BO3RandomBlockFunction) blockFunction).blockCount = this.randomBlocksBlockCount[i];
			} else {
				blockFunction = new BO3BlockFunction(this);
			}

			blockFunction.x = this.blocksX[0][i];
			blockFunction.y = this.blocksY[0][i];
			blockFunction.z = this.blocksZ[0][i];
			blockFunction.material = this.blocksMaterial[0][i];
			blockFunction.nbt = this.blocksMetaDataTag[i];
			blockFunction.nbtName = this.blocksMetaDataName[i];

			writer.function(blockFunction);
		}

		writer.bigTitle("BO3 checks");
		writer.comment("Require a condition at a certain location in order for the BO3 to be spawned.");
		writer.comment("BlockCheck(x,y,z,BlockName[,BlockName[,...]]) - one of the blocks must be at the location");
		writer.comment("BlockCheckNot(x,y,z,BlockName[,BlockName[,...]]) - all the blocks must not be at the location");
		writer.comment("LightCheck(x,y,z,minLightLevel,maxLightLevel) - light must be between min and max (inclusive)");
		writer.comment("ModCheck(ModName[,ModName[,...]]) - all the mods listed must be loaded");
		writer.comment("ModCheckNot(ModName[,ModName[,...]]) - all the mods listed must not be loaded");
		writer.comment("");
		writer.comment(
				"You can use \"Solid\" as a BlockName for matching all solid blocks or \"All\" to match all blocks that aren't air.");
		writer.comment("");
		writer.comment("Examples:");
		writer.comment("  BlockCheck(0,-1,0,GRASS,DIRT)  Require grass or dirt just below the object");
		writer.comment("  BlockCheck(0,-1,0,Solid)       Require any solid block just below the object");
		writer.comment("  BlockCheck(0,-1,0,WOOL)        Require any type of wool just below the object");
		writer.comment("  BlockCheck(0,-1,0,WOOL:0)      Require white wool just below the object");
		writer.comment("  BlockCheckNot(0,-1,0,WOOL:0)   Require that there is no white wool below the object");
		writer.comment("  LightCheck(0,0,0,0,1)          Require almost complete darkness just below the object");

		for (BO3Check func : Arrays.asList(this.bo3Checks[0]))
		{
			writer.function(func);
		}

		writer.bigTitle("Branches");
		writer.comment("Branches are child-BO3's that spawn if this BO3 is configured to spawn as a");
		writer.comment("CustomStructure resource in a biome config. Branches can have branches,");
		writer.comment("making complex structures possible. See the wiki for more details.");
		writer.comment("");
		writer.comment("Regular Branches spawn each branch with an independent chance of spawning.");
		writer.comment(
				"Branch(x,y,z,branchName,rotation,chance[,anotherBranchName,rotation,chance[,...]][IndividualChance])");
		writer.comment("branchName - name of the object to spawn.");
		writer.comment("rotation - NORTH, SOUTH, EAST or WEST.");
		writer.comment("IndividualChance - The chance each branch has to spawn, assumed to be 100 when left blank");
		writer.comment(
				"isRequiredBranch - If this is set to true then at least one of the branches in this BO3 must spawn at these x,y,z coordinates. If no branch can spawn there then this BO3 fails to spawn and its branch is rolled back.");
		writer.comment(
				"isRequiredBranch:true branches must spawn or the current branch is rolled back entirely. This is useful for grouping BO3's that must spawn together, for instance a single room made of multiple BO3's/branches.");
		writer.comment(
				"If all parts of the room are connected together via isRequiredBranch:true branches then either the entire room will spawns or no part of it will spawn.");
		writer.comment(
				"*Note: When isRequiredBranch:true only one BO3 can be added per Branch() and it will automatically have a rarity of 100.0.");
		writer.comment(
				"isRequiredBranch:false branches are used to make optional parts of structures, for instance the middle section of a tunnel that has a beginning, middle and end BO3/branch and can have a variable length by repeating the middle BO3/branch.");
		writer.comment(
				"By making the start and end branches isRequiredBranch:true and the middle branch isRequiredbranch:false you can make it so that either:");
		writer.comment("A. A tunnel spawns with at least a beginning and end branch");
		writer.comment(
				"B. A tunnel spawns with a beginning and end branch and as many middle branches as will fit in the available space.");
		writer.comment(
				"C. No tunnel spawns at all because there wasn't enough space to spawn at least a beginning and end branch.");
		writer.comment(
				"branchDepth - When creating a chain of branches that contains optional (isRequiredBranch:false) branches branch depth is configured for the first BO3 in the chain to determine the maximum length of the chain.");
		writer.comment(
				"branchDepth - 1 is inherited by each isRequiredBranch:false branch in the chain. When branchDepth is zero isRequiredBranch:false branches cannot spawn and the chain ends. In the case of the tunnel this means the last middle branch would be");
		writer.comment(
				"rolled back and an IsRequiredBranch:true end branch could be spawned in its place to make sure the tunnel has a proper ending.");
		writer.comment(
				"Instead of inheriting branchDepth - 1 from the parent branchDepth can be overridden by child branches if it is set higher than 0 (the default value).");
		writer.comment(
				"isRequiredBranch:true branches do inherit branchDepth and pass it on to their own branches, however they cannot be prevented from spawning by it and also don't subtract 1 from branchDepth when inheriting it.");
		writer.comment("");
		writer.comment("Weighted Branches spawn branches with a dependent chance of spawning.");
		writer.comment(
				"WeightedBranch(x,y,z,branchName,rotation,chance[,anotherBranchName,rotation,chance[,...]][MaxChanceOutOf])");
		writer.comment(
				"*Note: isRequiredBranch must be set to false. It is not possible to use isRequiredBranch:true with WeightedBranch() since isRequired:true branches must spawn and automatically have a rarity of 100.0.");
		writer.comment(
				"MaxChanceOutOf - The chance all branches have to spawn out of, assumed to be 100 when left blank");

		for (BO3BranchFunction func : Arrays.asList(this.branches[0]))
		{
			writer.function(func);
		}

		writer.bigTitle("Entities");
		writer.comment("Forge only (this may have changed, check for updates).");
		writer.comment(
				"An EntityFunction spawns an entity instead of a block. The entity is spawned only once when the BO3 is spawned.");
		writer.comment(
				"Entities are persistent by default so they don't de-spawn when no player is near, they are only unloaded.");
		writer.comment(
				"Usage: Entity(x,y,z,entityName,groupSize,NameTagOrNBTFileName) or Entity(x,y,z,mobName,groupSize)");
		writer.comment(
				"Use /otg entities to get a list of entities that can be used as entityName, this includes entities added by other mods and non-living entities.");
		writer.comment(
				"NameTagOrNBTFileName can be either a nametag for the mob or an .txt file with nbt data (such as myentityinfo.txt).");
		writer.comment(
				"In the text file you can use the same mob spawning parameters used with the /summon command to equip the");
		writer.comment(
				"entity and give it custom attributes etc. You can copy the DATA part of a summon command including surrounding ");
		writer.comment("curly braces to a .txt file, for instance for: \"/summon Skeleton x y z {DATA}\"");
		writer.comment("*Note: Unlike Block(), for Entity() .nbt files don't work, only .txt files work.");

		for (BO3EntityFunction func : Arrays.asList(this.entityFunctions[0]))
		{
			writer.function(func);
		}

		writer.bigTitle("Particles");
		writer.comment("Forge only (this may have changed, check for updates).");
		writer.comment(
				"Creates an invisible particle spawner at the given location that spawns particles every x milliseconds.");
		writer.comment("Usage: Particle(x,y,z,particleName,interval,velocityX,velocityY,velocityZ)");
		writer.comment("velocityX, velocityY and velocityZ are optional.");
		writer.comment("Only vanilla particle names can be used, for 1.11.2 these are;");
		writer.comment("explode, largeexplode, hugeexplosion, fireworksSpark, bubble, splash, wake, suspended");
		writer.comment("depthsuspend, crit, magicCrit, smoke, largesmoke, spell, instantSpell, mobSpell");
		writer.comment("mobSpellAmbient, witchMagic, dripWater, dripLava, angryVillager, happyVillager");
		writer.comment("townaura, note, portal, enchantmenttable, flame, lava, footstep, cloud, reddust");
		writer.comment("snowballpoof,  snowshovel, slime, heart, barrier, iconcrack, blockcrack, blockdust");
		writer.comment("droplet, take, mobappearance, dragonbreath, endRod, damageIndicator, sweepAttack");
		writer.comment("fallingdust, totem, spit.");
		writer.comment(
				"velocityX,velocityY,velocityZ - Spawn the enemy with the given velocity. If this is not filled in then a small random velocity is applied.");

		for (BO3ParticleFunction func : Arrays.asList(this.particleFunctions[0]))
		{
			writer.function(func);
		}

		writer.bigTitle("Spawners");
		writer.comment("Forge only (this may have changed, check for updates).");
		writer.comment(
				"Creates an invisible entity spawner at the given location that spawns entities every x seconds.");
		writer.comment(
				"Entities can only spawn if their spawn requirements are met (zombies/skeletons only spawn in the dark etc). Max entity count for the server is ignored, each spawner has its own maxCount setting.");
		writer.comment(
				"Usage: Spawner(x,y,z,entityName,nbtFileName,groupSize,interval,spawnChance,maxCount,despawnTime,velocityX,velocityY,velocityZ,yaw,pitch)");
		writer.comment("nbtFileName, despawnTime, velocityX, velocityY, velocityZ, yaw and pitch are optional");
		writer.comment(
				"Example Spawner(0, 0, 0, Villager, 1, 5, 100, 5) or Spawner(0, 0, 0, Villager, villager1.txt, 1, 5, 100, 5) or Spawner(0, 0, 0, Villager, 1, 5, 100, 5, 30, 1, 1, 1, 0, 0)");
		writer.comment(
				"entityName - Name of the entity to spawn, use /otg entities to get a list of entities that can be used as entityName, this includes entities added by other mods and non-living entities.");
		writer.comment("nbtFileName - A .txt file with nbt data (such as myentityinfo.txt).");
		writer.comment(
				"In the text file you can use the same mob spawning parameters used with the /summon command to equip the");
		writer.comment(
				"entity and give it custom attributes etc. You can copy the DATA part of a summon command including surrounding ");
		writer.comment("curly braces to a .txt file, for instance for: \"/summon Skeleton x y z {DATA}\"");
		writer.comment("*Note: Unlike Block(), for Spawner() .nbt files don't work, only .txt files work.");
		writer.comment("groupSize - Number of entities that should spawn for each successful spawn attempt.");
		writer.comment("interval - Time in seconds between each spawn attempt.");
		writer.comment(
				"spawnChance - For each spawn attempt, the chance between 0-100 that the spawn attempt will succeed.");
		writer.comment(
				"maxCount - The maximum amount of this kind of entity that can exist within 32 blocks. If there are already maxCount or more entities of this type in a 32 radius this spawner will not spawn anything.");
		writer.comment(
				"despawnTime - After despawnTime seconds, if there is no player within 32 blocks of the entity it will despawn..");
		writer.comment(
				"velocityX,velocityY,velocityZ,yaw,pitch - Spawn the enemy with the given velocity and angle, handy for making traps and launchers (shooting arrows and fireballs etc).");

		for (BO3SpawnerFunction func : Arrays.asList(this.spawnerFunctions[0]))
		{
			writer.function(func);
		}

		// ModData
		writer.bigTitle("ModData");
		writer.comment("Forge only.");
		writer.comment("Use the ModData() tag to include data that other mods can use");
		writer.comment(
				"Mod makers can use ModData and the /otg GetModData command to test IMC communications between OTG");
		writer.comment("and their mod.");
		writer.comment("Normal users can use it to spawn some mobs and blocks on command.");
		writer.comment("ModData(x,y,z,\"ModName\", \"MyModDataAsText\"");
		writer.comment("Example: ModData(x,y,z,MyCystomNPCMod,SpawnBobHere/WithAPotato/And50Health)");
		writer.comment(
				"Try not to use exotic/reserved characters, like brackets and comma's etc, this stuff isn't fool-proof.");
		writer.comment(
				"Also, use this only to store IDs/object names etc for your mod, DO NOT include things like character dialogue,");
		writer.comment(
				"messages on signs, loot lists etc in this file. As much as possible just store id's/names here and store all the data related to those id's/names in your own mod.");
		writer.comment("OTG has some built in ModData commands for basic mob and block spawning.");
		writer.comment("These are mostly just a demonstration for mod makers to show how ModData.");
		writer.comment("can be used by other mods.");
		writer.comment("For mob spawning in OTG use: ModData(x,y,z,OTG,mob/MobType/Count/Persistent/Name)");
		writer.comment("mob: Makes OTG recognise this as a mob spawning command.");
		writer.comment("MobType: Lower-case, no spaces. Any vanilla mob like dragon, skeleton, wither, villager etc");
		writer.comment("Count: The number of mobs to spawn");
		writer.comment("Persistent (true/false): Should the mobs never de-spawn? If set to true the mob will get a");
		writer.comment("name-tag ingame so you can recognise it.");
		writer.comment("Name: A name-tag for the monster/npc.");
		writer.comment("Example: ModData(0,0,0,OTG,villager/1/true/Bob)");
		writer.comment("To spawn blocks using ModData use: ModData(x,y,z,OTG,block/material)");
		writer.comment("block: Makes OTG recognise this as a block spawning command.");
		writer.comment("material: id or text, custom blocks can be added using ModName:MaterialName.");
		writer.comment("To send all ModData within a radius in chunks around the player to the specified mod");
		writer.comment("use this console command: /otg GetModData ModName Radius");
		writer.comment("ModName: name of the mod, for OTG commands use OTG ");
		writer.comment("Radius (optional): Radius in chunks around the player.");

		for (BO3ModDataFunction func : Arrays.asList(this.modDataFunctions[0]))
		{
			writer.function(func);
		}
	}

	@Override
	protected void correctSettings()
	{

	}

	@Override
	protected void renameOldSettings()
	{
		// Stub method - there are no old setting to convert yet (:
	}

	/**
	 * Rotates all the blocks and all the checks
	 */
	protected void rotateBlocksAndChecks(String presetName, Path otgRootFolder, boolean spawnLog, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		for (int i = 1; i < 4; i++)
		{
			BO3BlockFunction[] blocks = getBlocks(i);
			BO3BlockFunction[] blocksPreviousRotation = getBlocks(i - 1);

			// Blocks (blocks[i - 1] is previous rotation)
			this.blocksX[i] = new byte[this.blocksX[i - 1].length];
			this.blocksY[i] = new short[this.blocksX[i - 1].length];
			this.blocksZ[i] = new byte[this.blocksX[i - 1].length];
			this.blocksMaterial[i] = new LocalMaterialData[this.blocksX[i - 1].length];

			this.randomBlocksBlocks[i] = new LocalMaterialData[this.blocksX[i - 1].length][];

			for (int j = 0; j < blocks.length; j++)
			{
				blocks[j] = blocksPreviousRotation[j].rotate();
			}
			for (int h = 0; h < blocks.length; h++)
			{
				BO3BlockFunction block = blocks[h];
				this.blocksX[i][h] = (byte) block.x;
				this.blocksY[i][h] = (short) block.y;
				this.blocksZ[i][h] = (byte) block.z;
				this.blocksMaterial[i][h] = block.material;

				if (block instanceof BO3RandomBlockFunction)
				{
					this.randomBlocksBlocks[i][h] = ((BO3RandomBlockFunction) block).blocks;
				}
			}

			// BO3 checks
			this.bo3Checks[i] = new BO3Check[this.bo3Checks[i - 1].length];
			for (int j = 0; j < this.bo3Checks[i].length; j++) 
			{
				this.bo3Checks[i][j] = this.bo3Checks[i - 1][j].rotate();
			}
			// Branches
			this.branches[i] = new BO3BranchFunction[this.branches[i - 1].length];
			for (int j = 0; j < this.branches[i].length; j++)
			{
				this.branches[i][j] = this.branches[i - 1][j].rotate(presetName, otgRootFolder, spawnLog, logger, customObjectManager, materialReader, manager, modLoadedChecker);
			}
			// Bounding box
			this.boundingBoxes[i] = this.boundingBoxes[i - 1].rotate();

			this.entityFunctions[i] = new BO3EntityFunction[this.entityFunctions[i - 1].length];
			for (int j = 0; j < this.entityFunctions[i].length; j++)
			{
				this.entityFunctions[i][j] = this.entityFunctions[i - 1][j].rotate();
			}

			this.particleFunctions[i] = new BO3ParticleFunction[this.particleFunctions[i - 1].length];
			for (int j = 0; j < this.particleFunctions[i].length; j++)
			{
				this.particleFunctions[i][j] = this.particleFunctions[i - 1][j].rotate();
			}

			this.spawnerFunctions[i] = new BO3SpawnerFunction[this.spawnerFunctions[i - 1].length];
			for (int j = 0; j < this.spawnerFunctions[i].length; j++)
			{
				this.spawnerFunctions[i][j] = this.spawnerFunctions[i - 1][j].rotate();
			}

			this.modDataFunctions[i] = new BO3ModDataFunction[this.modDataFunctions[i - 1].length];
			for (int j = 0; j < this.modDataFunctions[i].length; j++)
			{
				this.modDataFunctions[i][j] = this.modDataFunctions[i - 1][j].rotate();
			}
		}
	}

	boolean parseModChecks(IModLoadedChecker modLoadedChecker)
	{
		for (BO3Check check : bo3Checks[0])
		{
			if (check instanceof ModCheck)
			{
				if (!((ModCheck) check).evaluate(modLoadedChecker))
				{
					return false;
				}
			}
			else if (check instanceof ModCheckNot)
			{
				if (!((ModCheckNot) check).evaluate(modLoadedChecker))
				{
					return false;
				}
			}
		}
		return true;
	}
}
