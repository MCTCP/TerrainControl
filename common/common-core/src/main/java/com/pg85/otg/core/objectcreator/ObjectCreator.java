package com.pg85.otg.core.objectcreator;

import com.pg85.otg.constants.SettingsEnums;
import com.pg85.otg.core.OTG;
import com.pg85.otg.core.OTGEngine;
import com.pg85.otg.customobject.CustomObjectManager;
import com.pg85.otg.customobject.bo3.BO3;
import com.pg85.otg.customobject.bo3.BO3Config;
import com.pg85.otg.customobject.bo3.bo3function.BO3BranchFunction;
import com.pg85.otg.customobject.bo4.BO4;
import com.pg85.otg.customobject.bo4.BO4Config;
import com.pg85.otg.customobject.bo4.bo4function.BO4BranchFunction;
import com.pg85.otg.customobject.bo4.bo4function.BO4RandomBlockFunction;
import com.pg85.otg.customobject.bofunctions.BlockFunction;
import com.pg85.otg.customobject.bofunctions.BranchFunction;
import com.pg85.otg.customobject.config.CustomObjectConfigFile;
import com.pg85.otg.customobject.config.CustomObjectConfigFunction;
import com.pg85.otg.customobject.config.CustomObjectResourcesManager;
import com.pg85.otg.customobject.config.io.FileSettingsReaderBO4;
import com.pg85.otg.customobject.config.io.FileSettingsWriterBO4;
import com.pg85.otg.customobject.config.io.SettingsReaderBO4;
import com.pg85.otg.customobject.structures.StructuredCustomObject;
import com.pg85.otg.customobject.util.BoundingBox;
import com.pg85.otg.customobject.util.Corner;
import com.pg85.otg.customobject.util.ObjectType;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.interfaces.IModLoadedChecker;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;
import com.pg85.otg.util.materials.LocalMaterials;
import com.pg85.otg.util.nbt.LocalNBTHelper;
import com.pg85.otg.util.gen.LocalWorldGenRegion;
import com.pg85.otg.util.materials.LocalMaterialData;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ObjectCreator
{
	// Method for creating either object or structure; used by Export to cut down code duplication
	public static StructuredCustomObject create(
		ObjectType type, Corner min, Corner max, Corner center, LocalMaterialData centerBlock, String objectName, boolean isStructure, Path objectPath,
		LocalWorldGenRegion localWorld, LocalNBTHelper nbtHelper, List<BlockFunction<?>> extraBlocks, CustomObjectConfigFile template,
		String presetFolderName,
		Set<LocalMaterialData> filter
	)
	{
		if (filter == null) filter = new HashSet<>(List.of(LocalMaterials.AIR));
		if (isStructure)
		{
			return createStructure(
				type, min, max, center, objectName, objectPath, localWorld, nbtHelper, template, presetFolderName, filter);
		} else {
			return createObject(
				type, min, max, center, centerBlock, objectName, false, objectPath, localWorld, nbtHelper,
				extraBlocks, template, presetFolderName, filter);
		}
	}

	// Method for creating a custom object
	public static StructuredCustomObject createObject(
		ObjectType type, Corner min, Corner max, Corner center, LocalMaterialData centerBlock, String objectName, boolean leaveIllegalLeaves, Path exportPath,
		LocalWorldGenRegion localWorld, LocalNBTHelper nbtHelper, List<BlockFunction<?>> extraBlocks, CustomObjectConfigFile template,
		String presetFolderName, Set<LocalMaterialData> filter
	)
	{
		OTGEngine eng = OTG.getEngine();
		Path rootPath = eng.getOTGRootFolder();
		ILogger logger = eng.getLogger();
		CustomObjectManager boManager = eng.getCustomObjectManager();
		IMaterialReader mr = eng.getPresetLoader().getMaterialReader(presetFolderName);
		CustomObjectResourcesManager manager = eng.getCustomObjectResourcesManager();
		IModLoadedChecker mlc = eng.getModLoadedChecker();

		if (filter == null) filter = new HashSet<>(List.of(LocalMaterials.AIR));

		File exportFolder = exportPath.toFile();
		searchForCenter:
		{
			if (centerBlock != null)
				for (int x = min.x; x <= max.x; x++)
					for (int z = min.z; z <= max.z; z++)
						for (int y = min.y; y <= max.y; y++)
						{
							LocalMaterialData data = localWorld.getMaterial(x, y, z);
							if (data != null && data.isMaterial(centerBlock))
							{
								center = new Corner(x, y, z);
								break searchForCenter;
							}
						}
		}
		// Loop through region, getting the block from the localWorld
		List<BlockFunction<?>> blocks = Extractor.getBlockFunctions(type, min, max, center, localWorld, nbtHelper, leaveIllegalLeaves, objectName, exportFolder, filter);

		// Add extra blocks in (from updating, mainly)
		if (extraBlocks != null) blocks.addAll(extraBlocks);

		Path destinationPath = type.getObjectFilePathFromName(objectName, exportPath);
		// Rename old file, make it .backup
		if (destinationPath.toFile().exists())
		{
			Path backupPath = destinationPath.resolveSibling(objectName+"."+type.getType()+".backup");
			try
			{
				if (backupPath.toFile().exists()) Files.delete(backupPath);
				Files.move(destinationPath, backupPath);
			}
			catch (IOException e)
			{
				logger.log(LogLevel.ERROR, LogCategory.MAIN, "Failed to rename old file "+destinationPath.getFileName());
				logger.printStackTrace(LogLevel.ERROR, LogCategory.MAIN, e);
			}
		}

		// Make new BO with the given blocks
		CustomObjectConfigFile config = makeNewConfig(type, template, objectName, destinationPath,
			max, min, center, blocks, null, presetFolderName, logger, rootPath, boManager, mr, manager, mlc);

		switch (type)
		{
			case BO3:
				FileSettingsWriterBO4.writeToFile(config, config.getFile(), config.settingsMode, logger, mr, manager);
				return new BO3(objectName, type.getObjectFilePathFromName(objectName, exportPath).toFile(), (BO3Config) config);
			case BO4:
				// Don't write the BO4 to file, that's done during this::makeNewConfig
				return new BO4(objectName, type.getObjectFilePathFromName(objectName, exportPath).toFile(), (BO4Config) config);
			default:
				return null;
		}
	}

	// Separate branch for creating a structure, since structures need to create branches
	public static StructuredCustomObject createStructure(
		ObjectType type, Corner min, Corner max, Corner center, String objectName, Path objectPath,
		LocalWorldGenRegion localWorld, LocalNBTHelper nbtHelper, CustomObjectConfigFile template,
		String presetFolderName, Set<LocalMaterialData> filter
	)
	{
		OTGEngine eng = OTG.getEngine();
		Path rootPath = eng.getOTGRootFolder();
		ILogger logger = eng.getLogger();
		CustomObjectManager boManager = eng.getCustomObjectManager();
		IMaterialReader mr = eng.getPresetLoader().getMaterialReader(presetFolderName);
		CustomObjectResourcesManager manager = eng.getCustomObjectResourcesManager();
		IModLoadedChecker mlc = eng.getModLoadedChecker();

		File branchFolder = new File(objectPath.toFile(), objectName);
		branchFolder.mkdirs();

		// Plot out how many sub-objects we need
		int chunksOnXAxis = Math.abs(max.x - min.x) / 16;
		int chunksOnZAxis = Math.abs(max.z - min.z) / 16;
		if ((max.x - min.x) % 16 > 0) chunksOnXAxis++;
		if ((max.z - min.z) % 16 > 0) chunksOnZAxis++;

		// Get the blocks for each branch, put them in a grid
		//  - Make sure empty branches are ignored

		@SuppressWarnings("unchecked")
		List<BlockFunction<?>>[][] branchGrid = (List<BlockFunction<?>>[][]) new ArrayList<?>[chunksOnXAxis][chunksOnZAxis];

		// Array with booleans, saying if a given branch exists
		boolean[][] exists = new boolean[chunksOnXAxis][chunksOnZAxis];

		for (int branchX = 0; branchX < chunksOnXAxis; branchX++)
		{
			for (int branchZ = 0; branchZ < chunksOnZAxis; branchZ++)
			{
				Corner branchMin = new Corner(min.x + (16 * branchX), min.y, min.z + (16 * branchZ));
				// For max corner, we gotta make sure we don't extend over the edge
				Corner branchMax = new Corner(
					branchX == chunksOnXAxis - 1 ? max.x : branchMin.x + 15,
					max.y,
					branchZ == chunksOnZAxis - 1 ? max.z : branchMin.z + 15
				);

				String branchName = objectName + "_C" + branchX + "_R" + branchZ;

				branchGrid[branchX][branchZ] = Extractor.getBlockFunctions(type, branchMin, branchMax, branchMin,
					localWorld, nbtHelper, false, branchName, branchFolder, filter);
				exists[branchX][branchZ] = !branchGrid[branchX][branchZ].isEmpty();
			}
		}

		// Connect the grid in a nice way

		// Array with booleans, saying if a given branch has been found yet
		boolean[][] processed = new boolean[chunksOnXAxis][chunksOnZAxis];

		List<ChunkCoordinate> heads = new ArrayList<>();

		for (int x = 0; x < chunksOnXAxis; x++)
		{
			for (int z = 0; z < chunksOnZAxis; z++)
			{
				if (!exists[x][z])
				{
					continue;
				}

				if (!processed[x][z])
				{
					// This is the head of a graph, save it as such
					processed[x][z] = true;
					//logger.log(LogLevel.INFO, LogCategory.MAIN, "Head branch found at "+x+","+z);
					heads.add(ChunkCoordinate.fromChunkCoords(x, z));
				}

				ArrayList<BranchFunction<?>> branches = new ArrayList<>();

				if (x < exists.length - 1 && exists[x + 1][z] && !processed[x + 1][z])
				{ // East
					processed[x + 1][z] = true;
					addBranch(type, branches, objectName, 16, 0, 0, x+1, z, logger, mr);
				}
				if (z < exists[0].length - 1 && exists[x][z + 1] && !processed[x][z + 1])
				{ // South
					processed[x][z + 1] = true;
					addBranch(type, branches, objectName, 0, 0, 16, x, z+1, logger, mr);
				}
				if (x > 0 && exists[x - 1][z] && !processed[x - 1][z])
				{ // West
					processed[x - 1][z] = true;
					addBranch(type, branches, objectName, -16, 0, 0, x-1, z, logger, mr);
				}
				if (z > 0 && exists[x][z - 1] && !processed[x][z - 1])
				{ // North
					processed[x][z - 1] = true;
					addBranch(type, branches, objectName, 0, 0, -16, x, z-1, logger, mr);
				}
				String branchName = objectName + "_C" + x + "_R" + z;
				Path branchPath = type.getObjectFilePathFromName(branchName, branchFolder.toPath());

				// For BO4's, we need to make fresh BO4 configs for each new branch
				// For BO3's, we pass the template, as it is cloned
				CustomObjectConfigFile branchTemplate;
				try
				{
					branchTemplate = type == ObjectType.BO4 ? new BO4Config(
						new FileSettingsReaderBO4(branchName, branchPath.toFile(), logger),
						true, presetFolderName, rootPath, logger, boManager, mr, manager, mlc
					) : template;
				}
				catch (InvalidConfigException e)
				{
					e.printStackTrace();
					return null;
				}
				Corner localmin = new Corner(min.x + (16 * x), min.y, min.z + (16 * z));
				Corner localmax = new Corner(
					x == chunksOnXAxis - 1 ? max.x : min.x + (16 * x) + 15,
					max.y,
					z == chunksOnZAxis - 1 ? max.z : min.z + (16 * z) + 15);

				CustomObjectConfigFile branchConfig = makeNewConfig(
					type, branchTemplate,  branchName,
					branchPath,
					localmin,
					localmax,
					localmin,
					branchGrid[x][z], branches, presetFolderName, logger, rootPath, boManager, mr, manager, mlc);

				if (type != ObjectType.BO4) // Already written by MakeNewConfig
					FileSettingsWriterBO4.writeToFile(branchConfig, branchConfig.getFile(), branchConfig.settingsMode, logger, mr, manager);
			}
		}

		//Add the heads as branches to the main BO

		List<BranchFunction<?>> branches = new ArrayList<>();

		for (ChunkCoordinate coord : heads)
		{
			addBranch(type, branches, objectName, (coord.getChunkX() * 16), 0, (coord.getChunkZ() * 16),
				coord.getChunkX(), coord.getChunkZ(), logger, mr);
		}
		logger.log(LogLevel.INFO, LogCategory.MAIN, "Creating structure "+objectName+" with "+branches.size()+" direct branches");
		CustomObjectConfigFile config = makeNewConfig(type, template, objectName,
			type.getObjectFilePathFromName(objectName, objectPath),
			min, max, center, null, branches, presetFolderName, logger, rootPath, boManager, mr, manager, mlc);

		switch (type)
		{
			case BO3:
				FileSettingsWriterBO4.writeToFile(config, config.getFile(), config.settingsMode, logger, mr, manager);
				return new BO3(objectName, type.getObjectFilePathFromName(objectName, objectPath).toFile(), (BO3Config) config);
			case BO4:
				return new BO4(objectName, type.getObjectFilePathFromName(objectName, objectPath).toFile(), (BO4Config) config);
			default:
				return null;
		}
	}

	// Method for creating branches; had to be a separate method to avoid a switch statement everywhere this is called.
	private static void addBranch(ObjectType type, List<BranchFunction<?>> branches, String objectName,
								  int x, int y, int z, int chunkX, int chunkZ, ILogger logger, IMaterialReader mr)
	{
		switch (type)
		{
			case BO3 -> branches.add((BO3BranchFunction) CustomObjectConfigFunction.create(
				null, BO3BranchFunction.class, logger, mr,
				x, y, z, (objectName + "_C" + chunkX + "_R" + chunkZ), "NORTH", 100));
			case BO4 -> branches.add(((BO4BranchFunction) CustomObjectConfigFunction.create(
				null, BO4BranchFunction.class, logger, mr,
				x, y, z, true, (objectName + "_C" + chunkX + "_R" + chunkZ), "NORTH", 100, 0)));
			case BO2 -> throw new RuntimeException("Tried adding branch to BO2");
			default -> throw new IllegalStateException("Unexpected value: " + type);
		}
	}

	private static CustomObjectConfigFile makeNewConfig
		(ObjectType type, CustomObjectConfigFile template, String objectName, Path objectFilePath,  Corner max, Corner min, Corner center,
		 List<BlockFunction<?>> blocks, List<BranchFunction<?>> branches, String presetFolderName, ILogger logger, Path rootPath,
		 CustomObjectManager boManager, IMaterialReader mr, CustomObjectResourcesManager manager, IModLoadedChecker mlc)
	{
		SettingsReaderBO4 reader = new FileSettingsReaderBO4(objectName, objectFilePath.toFile(), logger);
		if (blocks == null)
			blocks = new ArrayList<>();

		switch (type)
		{
			case BO3 -> {
				BO3Config config = ((BO3Config) template).cloneConfigValues(reader);
				if (branches != null)
					config.setBranches(branches);
				if (config.settingsMode == SettingsEnums.ConfigMode.WriteDisable)
					config.settingsMode = SettingsEnums.ConfigMode.WriteWithoutComments;
				BoundingBox box = BoundingBox.newEmptyBox();
				box.expandToFit(min.x - center.x, min.y - center.y, min.z - center.z);
				box.expandToFit(max.x - center.x, max.y - center.y, max.z - center.z);

				config.setBoundingBox(box);
				config.extractBlocks(blocks);
				config.rotateBlocksAndChecks(presetFolderName, rootPath, logger, boManager, mr, manager, mlc);
				return config;
			}
			case BO4 -> {
				BO4Config config = (BO4Config) template;
				if (config.settingsMode == SettingsEnums.ConfigMode.WriteDisable)
					config.settingsMode = SettingsEnums.ConfigMode.WriteWithoutComments;
				config.reader = reader;
				// Write and read the file, as otherwise we'd be doing a *lot* of extra work
				if (branches == null && config.getbranches() != null)
				{
					branches = Arrays.asList(config.getbranches());
				}

				// Re-add any AIR blocks that were in the original BO4
				List<BlockFunction<?>> mergedBlocks = new ArrayList<>(blocks);
				BlockFunction<?>[] blocksListOriginal = config.getBlockFunctions(presetFolderName, rootPath, logger, boManager, mr, manager, mlc);
				for (BlockFunction<?> block : blocksListOriginal)
				{
					if (
						!(block instanceof BO4RandomBlockFunction) &&
						block.material != null &&
						block.material.isAir() &&
						blocks.stream().noneMatch(a -> a.x == block.x && a.y == block.y && a.z == block.z)
					)
					{
						mergedBlocks.add(block);
					}
				}

				FileSettingsWriterBO4.writeToFileWithData
					(
						config,
						mergedBlocks,
						branches,
						logger, mr, manager
					)
				;

				BO4 object = (BO4) boManager.getObjectLoaders().get(type.getType().toLowerCase())
					.loadFromFile(
						objectName,
						objectFilePath.toFile(),
						logger
					);

				if (object == null)
				{
					throw new RuntimeException("Could not load BO4 " + objectName + " at " + objectFilePath);
				}

				if (!object.onEnable(presetFolderName, rootPath, logger, boManager, mr, manager, mlc))
				{
					throw new RuntimeException("Could not enable BO4 " + objectName);
				}

				return object.getConfig();
			}
			case BO2 -> throw new RuntimeException("Tried to create BO2 config");
			default -> throw new IllegalStateException("Unexpected value: " + type);
		}
	}
}
