package com.pg85.otg.forge.commands;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.core.OTG;
import com.pg85.otg.core.presets.Preset;
import com.pg85.otg.customobject.CustomObject;
import com.pg85.otg.customobject.bofunctions.BlockFunction;
import com.pg85.otg.core.objectcreator.ObjectCreator;
import com.pg85.otg.customobject.util.ObjectType;
import com.pg85.otg.customobject.structures.StructuredCustomObject;
import com.pg85.otg.customobject.util.BoundingBox;
import com.pg85.otg.customobject.util.Corner;
import com.pg85.otg.forge.gen.ForgeWorldGenRegion;
import com.pg85.otg.forge.gen.MCWorldGenRegion;
import com.pg85.otg.forge.gen.OTGNoiseChunkGenerator;
import com.pg85.otg.forge.util.ForgeNBTHelper;
import com.pg85.otg.util.bo3.Rotation;
import com.pg85.otg.util.gen.LocalWorldGenRegion;
import com.pg85.otg.util.materials.LocalMaterials;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;

import java.nio.file.Path;
import java.util.List;

public class ObjectUtils
{
	/** Get the path to the object folder, either Objects, WorldObjects, or GlobalObjects,
	 * whichever is appropriate.
	 *
	 * @param presetFolder The path to the preset in question, or null if global
	 * @return The path to the object folder
	 */
	public static Path getObjectFolderPath(Path presetFolder)
	{
		Path objectPath;
		if (presetFolder == null)
		{
			objectPath = OTG.getEngine().getGlobalObjectsFolder();
		} else {
			objectPath = presetFolder.resolve(Constants.WORLD_OBJECTS_FOLDER);
		}

		if (!objectPath.toFile().exists() && objectPath.resolve("..").resolve("WorldObjects").toFile().exists())
		{
			objectPath = objectPath.resolve("..").resolve("WorldObjects");
		}
		return objectPath;
	}

	/** Fetches the preset from its name. If given a null string, returns the default preset.
	 *
	 * @param presetName The name of the preset to fetch
	 * @return The preset
	 */
	public static Preset getPresetOrDefault(String presetName)
	{
		if (presetName == null)
		{
			return OTG.getEngine().getPresetLoader().getPresetByShortNameOrFolderName(OTG.getEngine().getPresetLoader().getDefaultPresetFolderName());
		} else {
			return OTG.getEngine().getPresetLoader().getPresetByShortNameOrFolderName(presetName);
		}
	}

	/** Gets what folder a given object is stored in, relative to the base object folder
	 *
	 * @param object The object in question
	 * @return The folders from the Object folder to the given Object
	 */
	public static String getFoldersFromObject(StructuredCustomObject object)
	{
		Path filePath = object.getConfig().getFile().toPath();
		Path parent = filePath.getParent();
		StringBuilder sb = new StringBuilder();
		// Search down to the Objects folder
		while (
			!parent.getFileName().toString().equalsIgnoreCase("WorldObjects") &&
			!parent.getFileName().toString().equalsIgnoreCase("Objects") &&
			!parent.getFileName().toString().equalsIgnoreCase("GlobalObjects")
		) {
			sb.insert(0, "/");
			sb.insert(0, parent.getFileName());
			parent = parent.getParent();
		}
		return sb.toString();
	}

	/** Cleans an area to prepare it for spawning an object for editing, or cleans up after it is done editing.
	 *
	 * @param min The min X,Y,Z of the area
	 * @param max The max X,Y,Z of the area
	 * @param preparing When preparing, the area is filled with structure void. If not, fills with air.
	 */
	public static void cleanArea(LocalWorldGenRegion region, Corner min, Corner max, boolean preparing)
	{
		for (int x1 = min.x-1; x1 <= max.x+1; x1++)
		{
			for (int z1 = min.z-1; z1 <= max.z+1; z1++)
			{
				for (int y1 = max.y+1; y1 >= min.y-1; y1--)
				{
					if (preparing)
					{
						region.setBlock(x1, y1, z1, LocalMaterials.STRUCTURE_VOID);
					} else {
						region.setBlock(x1, y1, z1, LocalMaterials.AIR);
					}
				}
			}
		}
	}

	/** Method to check if an object is larger than the bounds of its type
	 *
	 * @param region The region containing an object to be exported
	 * @param type The type of the object in question
	 * @return Wether it is outside of the bounds of the object type
	 */
	public static boolean isOutsideBounds(RegionCommand.Region region, ObjectType type)
	{
		Corner min = region.getMin();
		Corner max = region.getMax();
		int xlen = Math.abs(max.x - min.x);
		int zlen = Math.abs(max.z - min.z);
		return switch (type)
			{
				case BO3 -> xlen > 31 || zlen > 31;
				case BO4 -> xlen > 15 || zlen > 15;
				case BO2 -> false;
			};
	}

	/** Gets a region large enough to fit an object, positioned away from the player
	 *
	 * @param pos The position of the player
	 * @param object The object to be spawned
	 */
	protected static RegionCommand.Region getRegionFromObject(BlockPos pos, StructuredCustomObject object)
	{
		RegionCommand.Region region = new RegionCommand.Region();
		BoundingBox box = object.getBoundingBox(Rotation.NORTH);
		// Make the object not spawn on top of the player

		pos = pos.offset(3, 0, 3);

		int lowestElevation = pos.getY() + box.getMinY();
		int highestElevation = pos.getY() + box.getMinY() + box.getHeight();

		int yshift = 0;

		if (lowestElevation <= Constants.WORLD_DEPTH+1)
		{
			yshift = (-lowestElevation) + 2;
		}
		else if (highestElevation >= Constants.WORLD_HEIGHT)
		{
			yshift = highestElevation - Constants.WORLD_HEIGHT;
		}

		Corner center = new Corner(pos.getX() + 2 + (box.getWidth() / 2), pos.getY() + yshift, pos.getZ() + 2 + (box.getDepth() / 2));

		region.setPos(new BlockPos(
			center.x + box.getMinX(),
			lowestElevation + yshift,
			center.z + box.getMinZ()));
		region.setPos(new BlockPos(
			center.x + box.getMinX() + box.getWidth(),
			highestElevation + yshift,
			center.z + box.getMinZ() + box.getDepth()));
		region.setCenter(center);
		return region;
	}

	/** Gets an object. If preset is null, uses the default preset instead.
	 *
	 * @param objectName The name of the object to get
	 * @param presetFolderName The name of the preset in question
	 * @return the object, or null if it's not found
	 */
	public static CustomObject getObject(String objectName, String presetFolderName)
	{
		if (presetFolderName == null)
		{
			presetFolderName = OTG.getEngine().getPresetLoader().getDefaultPresetFolderName();
		}
		return OTG.getEngine().getCustomObjectManager().getGlobalObjects().getObjectByName(
			objectName,
			presetFolderName,
			OTG.getEngine().getOTGRootFolder(),
			OTG.getEngine().getLogger(),
			OTG.getEngine().getCustomObjectManager(),
			OTG.getEngine().getPresetLoader().getMaterialReader(presetFolderName),
			OTG.getEngine().getCustomObjectResourcesManager(),
			OTG.getEngine().getModLoadedChecker());
	}

	/** Gets a runnable that, when run, exports an object
	 *
	 * @param type The type of object to export
	 * @param region The region containing the object
	 * @param center The center of the object
	 * @param inputObject The template of the object
	 * @param exportPath The path to save the object to
	 * @param extraBlocks Extra blocks that will be added to the object, like RandomBlocks
	 * @param presetFolderName The preset to export the object to
	 * @param verbose Whether to print a success/fail message, as well as whether to register the object after creation
	 */
	protected static Runnable getExportRunnable(ObjectType type, RegionCommand.Region region, Corner center, StructuredCustomObject inputObject,
												Path exportPath, List<BlockFunction<?>> extraBlocks, String presetFolderName, boolean verbose, boolean leaveIllegalLeaves, CommandSourceStack source, LocalWorldGenRegion worldGenRegion)
	{
		return () -> {
			// Wait for tree to finish
			try
			{
				Thread.sleep(100);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			StructuredCustomObject fixedObject = ObjectCreator.createObject(
				type,
				region.getMin(),
				region.getMax(),
				center,
				null,
				inputObject.getName(),
				leaveIllegalLeaves,
				exportPath,
				worldGenRegion,
				new ForgeNBTHelper(),
				extraBlocks,
				inputObject.getConfig(),
				presetFolderName,
				null);

			if (verbose && fixedObject != null)
			{
				source.sendSuccess(new TextComponent("Successfully updated " + type.getType() + " " + inputObject.getName()), false);
				OTG.getEngine().getCustomObjectManager().getGlobalObjects().addObjectToPreset(presetFolderName, fixedObject.getName(), fixedObject.getConfig().getFile(), inputObject);
			} else if (verbose) {
				source.sendSuccess(new TextComponent("Failed to update "+type.getType()+" " + inputObject.getName()), false);
			}
			cleanArea(worldGenRegion, region.getMin(), region.getMax(), false);
		};
	}

	protected static ForgeWorldGenRegion getWorldGenRegion(Preset preset, ServerLevel level)
	{
		if(level.getChunkSource().getGenerator() instanceof OTGNoiseChunkGenerator)
		{
			return new ForgeWorldGenRegion(
				preset.getFolderName(),
				preset.getWorldConfig(),
				level,
				(OTGNoiseChunkGenerator)level.getChunkSource().getGenerator()
			);
		} else {
			return new MCWorldGenRegion(
				preset.getFolderName(),
				preset.getWorldConfig(),
				level
			);
		}
	}
}
