package com.pg85.otg.customobject.bo3;

import com.pg85.otg.constants.SettingsEnums;
import com.pg85.otg.customobject.BOCreator;
import com.pg85.otg.customobject.bofunctions.BlockFunction;
import com.pg85.otg.customobject.bofunctions.BranchFunction;
import com.pg85.otg.customobject.creator.Extractor;
import com.pg85.otg.customobject.util.Corner;
import com.pg85.otg.customobject.CustomObjectManager;
import com.pg85.otg.customobject.bo3.bo3function.BO3BlockFunction;
import com.pg85.otg.customobject.bo3.bo3function.BO3BranchFunction;
import com.pg85.otg.customobject.config.CustomObjectConfigFunction;
import com.pg85.otg.customobject.config.CustomObjectResourcesManager;
import com.pg85.otg.customobject.config.io.FileSettingsReaderBO4;
import com.pg85.otg.customobject.config.io.FileSettingsWriterBO4;
import com.pg85.otg.customobject.util.BoundingBox;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.interfaces.IModLoadedChecker;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.bo3.LocalNBTHelper;
import com.pg85.otg.util.bo3.NamedBinaryTag;
import com.pg85.otg.util.gen.LocalWorldGenRegion;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.LocalMaterials;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Deprecated
public class BO3Creator extends BOCreator
{
	public static BO3 create(
		Corner min, Corner max, Corner center, LocalMaterialData centerBlock, String objectName, boolean includeAir, Path objectPath,
		LocalWorldGenRegion localWorld, LocalNBTHelper nbtHelper, List<BO3BlockFunction> extraBlocks, BO3Config template,
		String presetFolderName, Path rootPath, ILogger logger, CustomObjectManager boManager,
		IMaterialReader mr, CustomObjectResourcesManager manager, IModLoadedChecker mlc)
	{

		File objectFolder = objectPath.toFile();
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
		List<BlockFunction<?>> blocks = getBlockFunctions(min, max, center, localWorld, nbtHelper, includeAir, objectName, objectFolder);

		// Add extra blocks in from the fixbo3 command
		if (extraBlocks != null) blocks.addAll(extraBlocks);

		// Make new BO3 with the given blocks
		BO3Config config = makeBO3Config(template, objectName, new File(objectFolder, objectName + ".bo3"), logger, max, min, blocks, null, presetFolderName, rootPath, boManager, mr, manager, mlc);

		FileSettingsWriterBO4.writeToFile(config, config.getFile(), config.settingsMode, logger, mr, manager);

		// Return BO3
		return new BO3(objectName, new File(objectFolder, objectName + ".bo3"), config);
	}

	// Use this to make the BO3 config
	private static BO3Config makeBO3Config(BO3Config template, String objectName, File bo3File, ILogger logger, Corner max, Corner min,
										   List<BlockFunction<?>> blocks, List<BranchFunction<?>> branches, String presetFolderName, Path rootPath,
										   CustomObjectManager boManager, IMaterialReader mr, CustomObjectResourcesManager manager, IModLoadedChecker mlc)
	{
		BO3Config config = template.cloneConfigValues(new FileSettingsReaderBO4(objectName, bo3File, logger));
		if (branches != null)
			config.setBranches(branches);
		if (config.settingsMode == SettingsEnums.ConfigMode.WriteDisable)
			config.settingsMode = SettingsEnums.ConfigMode.WriteWithoutComments;
		BoundingBox box = BoundingBox.newEmptyBox();
		box.expandToFit(0, 0, 0);
		box.expandToFit(max.x - min.x + 1, max.y - min.x + 1, max.z - min.x + 1);
		config.boundingBoxes[0] = box;
		config.extractBlocks(blocks);
		config.rotateBlocksAndChecks(presetFolderName, rootPath, logger, boManager, mr, manager, mlc);
		return config;
	}

	private static List<BlockFunction<?>> getBlockFunctions(
		Corner min, Corner max, Corner center, LocalWorldGenRegion localWorld, LocalNBTHelper nbtHelper, boolean includeAir, String objectName, File objectFolder)
	{
		// Loop through the defined region and make BO3BlockFunctions of all the found blocks
		// Also includes TileEntities, which are saved to their own sub-folder

		File nbtFolder = new File(objectFolder, objectName);
		ArrayList<BlockFunction<?>> blocks = new ArrayList<>();
		for (int x = min.x; x <= max.x; x++)
		{
			for (int z = min.z; z <= max.z; z++)
			{
				for (int y = min.y; y <= max.y; y++)
				{
					LocalMaterialData materialData = localWorld.getMaterial(x, y, z);
					if (materialData == null || (!includeAir && materialData.isMaterial(LocalMaterials.AIR)))
						continue;

					if (materialData.isLeaves())
						materialData = materialData.legalOrPersistentLeaves();

					BO3BlockFunction block = new BO3BlockFunction();
					block.material = materialData;
					block.nbt = null;
					block.nbtName = "";
					block.x = x - center.x;
					block.y = (short) (y - center.y);
					block.z = z - center.z;

					NamedBinaryTag nbt = nbtHelper.getNBTFromLocation(localWorld, x, y, z);
					if (nbt != null)
					{
						try
						{
							if (!nbtFolder.exists()) nbtFolder.mkdirs();
							String tileName = Extractor.getTileEntityName(nbt);
							String nbtName = objectName + "/" + tileName + "_" + block.x + "_" + block.y + "_" + block.z + ".nbt";
							block.nbt = nbt;
							block.nbtName = nbtName;
							File nbtFile = new File(objectFolder, nbtName);
							nbtFile.delete(); // Make sure there is no leftover file here from before
							nbtFile.createNewFile(); // Make the new file
							FileOutputStream stream = new FileOutputStream(nbtFile);
							nbt.writeTo(stream); // Write the new file to disk
							stream.flush();
							stream.close();
						}
						catch (IOException e)
						{
							e.printStackTrace();
						}
					}
					blocks.add(block);
				}
			}
		}
		return blocks;
	}

	public static BO3 createStructure(
		Corner min, Corner max, Corner center, String objectName, boolean includeAir, Path objectPath,
		LocalWorldGenRegion localWorld, LocalNBTHelper nbtHelper, BO3Config template,
		String presetFolderName, Path rootPath, ILogger logger, CustomObjectManager boManager,
		IMaterialReader mr, CustomObjectResourcesManager manager, IModLoadedChecker mlc)
	{
		File branchFolder = new File(objectPath.toFile(), objectName);
		branchFolder.mkdirs();

		// Plot out how many sub-objects we need

		int chunksOnXAxis = Math.abs(max.x - min.x) / 16;
		int chunksOnZAxis = Math.abs(max.z - min.z) / 16;
		if ((max.x - min.x) % 16 > 0) chunksOnXAxis++;
		if ((max.z - min.z) % 16 > 0) chunksOnZAxis++;

		// Get the blocks for each branch, put them in a grid
		//  - Make sure empty branches are ignored

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
				String branchName = objectName + "_C" + branchX + "_r" + branchZ;

				branchGrid[branchX][branchZ] = getBlockFunctions(branchMin, branchMax, branchMin, localWorld, nbtHelper,
					includeAir, branchName, branchFolder);
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
				if (exists[x][z] && !processed[x][z])
				{
					// This is the head of a graph, save it as such
					processed[x][z] = true;
					heads.add(ChunkCoordinate.fromChunkCoords(x, z));
				}
				// Create config for this BO3
				ArrayList<BranchFunction<?>> branches = new ArrayList<>();

				if (x < exists.length - 1 && exists[x + 1][z] && !processed[x + 1][z])
				{ // East
					processed[x + 1][z] = true;
					branches.add((BO3BranchFunction) CustomObjectConfigFunction.create(null, BO3BranchFunction.class, logger, mr,
						16, 0, 0, (objectName + "_C" + (x + 1) + "_R" + z), "NORTH", 100));
				}
				if (z < exists[0].length - 1 && exists[x][z + 1] && !processed[x][z + 1])
				{ // South
					processed[x][z + 1] = true;
					branches.add((BO3BranchFunction) CustomObjectConfigFunction.create(null, BO3BranchFunction.class, logger, mr,
						0, 0, 16, (objectName + "_C" + x + "_R" + (z + 1)), "NORTH", 100));
				}
				if (x > 0 && exists[x - 1][z] && !processed[x - 1][z])
				{ // West
					processed[x - 1][z] = true;
					branches.add((BO3BranchFunction) CustomObjectConfigFunction.create(null, BO3BranchFunction.class, logger, mr,
						0, 0, -16, (objectName + "_C" + (x - 1) + "_R" + z), "NORTH", 100));
				}
				if (z > 0 && exists[x][z - 1] && !processed[x][z - 1])
				{ // North
					processed[x][z - 1] = true;
					branches.add((BO3BranchFunction) CustomObjectConfigFunction.create(null, BO3BranchFunction.class, logger, mr,
						-16, 0, 0, (objectName + "_C" + x + "_R" + (z - 1)), "NORTH", 100));
				}
				String branchName = objectName + "_C" + x + "_R" + z;

				BO3Config branchConfig = makeBO3Config(template, branchName, new File(branchFolder, branchName + ".bo3"), logger,
					new Corner(min.x + (16 * x), min.y, min.z + (16 * z)),
					new Corner(x == chunksOnXAxis - 1 ? max.x : min.x + (16 * x) + 15, max.y, z == chunksOnZAxis - 1 ? max.z : min.z + (16 * z) + 15),
					branchGrid[x][z], branches, presetFolderName, rootPath, boManager, mr, manager, mlc);

				FileSettingsWriterBO4.writeToFile(branchConfig, branchConfig.getFile(), branchConfig.settingsMode, logger, mr, manager);
			}
		}

		//Add the heads as branches to the main BO3

		List<BranchFunction<?>> branches = new ArrayList<>();

		for (ChunkCoordinate coord : heads)
		{
			branches.add((BO3BranchFunction) CustomObjectConfigFunction.create(null, BO3BranchFunction.class, logger, mr,
				(coord.getChunkX() * 16), 0, (coord.getChunkZ() * 16), (objectName + "_C" + coord.getChunkX() + "_R" + coord.getChunkZ()), "NORTH", 100));
		}

		BO3Config mainConfig = makeBO3Config(
			template,
			objectName,
			new File(objectPath.toFile(), objectName + ".bo3"),
			logger,
			min,
			max,
			new ArrayList<>(),
			branches,
			presetFolderName,
			rootPath,
			boManager,
			mr,
			manager,
			mlc
		);

		FileSettingsWriterBO4.writeToFile(mainConfig, mainConfig.getFile(), mainConfig.settingsMode, logger, mr, manager);

		// Return the main BO3

		return new BO3(objectName, mainConfig.getFile(), mainConfig);
	}
}
