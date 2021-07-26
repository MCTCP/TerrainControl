package com.pg85.otg.forge.commands;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.pg85.otg.OTG;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.customobject.CustomObject;
import com.pg85.otg.customobject.bo2.BO2;
import com.pg85.otg.customobject.bo3.BO3;
import com.pg85.otg.customobject.bo3.bo3function.BO3RandomBlockFunction;
import com.pg85.otg.customobject.bo4.bo4function.BO4RandomBlockFunction;
import com.pg85.otg.customobject.bofunctions.BlockFunction;
import com.pg85.otg.customobject.config.CustomObjectResourcesManager;
import com.pg85.otg.customobject.creator.ObjectCreator;
import com.pg85.otg.customobject.creator.ObjectType;
import com.pg85.otg.customobject.structures.StructuredCustomObject;
import com.pg85.otg.customobject.util.BoundingBox;
import com.pg85.otg.customobject.util.Corner;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.forge.commands.arguments.BiomeObjectArgument;
import com.pg85.otg.forge.commands.arguments.FlagsArgument;
import com.pg85.otg.forge.commands.arguments.PresetArgument;
import com.pg85.otg.forge.gen.ForgeWorldGenRegion;
import com.pg85.otg.forge.gen.MCWorldGenRegion;
import com.pg85.otg.forge.gen.OTGNoiseChunkGenerator;
import com.pg85.otg.forge.materials.ForgeMaterialData;
import com.pg85.otg.forge.util.ForgeNBTHelper;
import com.pg85.otg.interfaces.ICustomObjectManager;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.interfaces.IModLoadedChecker;
import com.pg85.otg.presets.Preset;
import com.pg85.otg.util.bo3.Rotation;
import com.pg85.otg.util.gen.LocalWorldGenRegion;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.LocalMaterials;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.entity.Entity;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.server.ServerWorld;

public class EditCommand extends BaseCommand
{
	private static final HashMap<Entity, EditSession> sessionsMap = new HashMap<>();
	
	private static final String[] FLAGS = new String[]
	{ "-nofix", "-update" };
	
	public EditCommand() {
		this.name = "edit";
		this.helpMessage = "Allows you to edit existing BO3 and BO4 files.";
		this.usage = "Please see /otg help edit.";
	}

	@Override
	public void build(LiteralArgumentBuilder<CommandSource> builder)
	{
		builder.then(Commands.literal("edit")
			.executes(this::help).then(
				Commands.argument("preset", StringArgumentType.word()).executes(this::execute)
				.suggests((context, suggestionBuilder) -> PresetArgument.suggest(context, suggestionBuilder, true)).then(
					Commands.argument("object", StringArgumentType.word()).executes(this::execute)
					.suggests(BiomeObjectArgument::suggest
						).then(
						Commands.argument("flags", FlagsArgument.create()).executes(this::execute).suggests(this::suggestFlags)
					)
				)
			)
		);
		builder.then(Commands.literal("canceledit")
			.executes(this::cancel));
		builder.then(Commands.literal("finishedit")
			.executes(this::finish));
		builder.then(Commands.literal("update").then(
			Commands.argument("preset", StringArgumentType.word())
			.suggests((context, suggestionBuilder) -> PresetArgument.suggest(context, suggestionBuilder, false))
			.executes(this::update)));
	}

	public int execute(CommandContext<CommandSource> context)
	{
		CommandSource source = context.getSource();
		try {
			String presetFolderName = context.getArgument("preset", String.class);
			String objectName = "";
			boolean immediate = false, doFixing = true;

			try
			{
				objectName = context.getArgument("object", String.class);
				String flags = context.getArgument("flags", String.class);
				immediate = flags.contains("-update");
				doFixing = !flags.contains("-nofix");
			}
			catch (IllegalArgumentException ignored) {}
			presetFolderName = presetFolderName != null && presetFolderName.equalsIgnoreCase("global") ? null : presetFolderName;
			boolean isGlobal = presetFolderName == null;
				
			if (objectName.equals(""))
			{
				source.sendSuccess(new StringTextComponent("Please supply an object name"), false);
				return 0;
			}

			if (source.getEntity() == null)
			{
				source.sendSuccess(new StringTextComponent("Only players can run this command"), false);
				return 0;
			}

			StructuredCustomObject object = getStructuredObject(objectName, presetFolderName);
			if (object == null)
			{
				source.sendSuccess(new StringTextComponent("Could not find " + objectName), false);
				return 0;
			}

			ObjectType type = object.getType();

			Preset preset = ExportCommand.getPresetOrDefault(presetFolderName);
			if (preset == null)
			{
				source.sendSuccess(new StringTextComponent("Could not find preset " + (presetFolderName == null ? "" : presetFolderName)), false);
				return 0;
			}

			// Use ForgeWorldGenRegion as a wrapper for the world that ObjectCreator can interact with
			ForgeWorldGenRegion worldGenRegion = getWorldGenRegion(preset, source.getLevel());

			ExportCommand.Region region = getRegionFromObject(source.getEntity().blockPosition(), object);
			Corner center = region.getCenter();

			// Prepare area for spawning

			cleanArea(worldGenRegion, region.getMin(), region.getMax(), true);

			// -- Spawn and update --

			// Spawn code, taken and modified from BO3.java :: spawnForced()
			ArrayList<BlockFunction<?>> extraBlocks = spawnAndFixObject(center.x, center.y, center.z, object, worldGenRegion, doFixing,
				presetFolderName, OTG.getEngine().getOTGRootFolder(), OTG.getEngine().getLogger(), OTG.getEngine().getCustomObjectManager(),
				OTG.getEngine().getPresetLoader().getMaterialReader(presetFolderName), OTG.getEngine().getCustomObjectResourcesManager(), OTG.getEngine().getModLoadedChecker());

			// Save the object and clean the area

			if (immediate)
			{
				Path path = ExportCommand.getObjectPath(isGlobal ? null : preset.getPresetFolder());
				// TODO: Make Edit put the object back where it found it
				// TODO: When editing an object, rename the old file to <name>.bo3.backup
				// path = path.resolve(getFoldersFromObject(object, path));
				new Thread(getExportRunnable(type, region, center, object, worldGenRegion, path,
					extraBlocks, presetFolderName, true, source)).start();
			} else {
				// Store the info, wait for /otg finishedit
				sessionsMap.put(source.getEntity(), new EditSession(type, worldGenRegion, object, extraBlocks,
					ExportCommand.getObjectPath(isGlobal ? null : preset.getPresetFolder()), preset.getFolderName(), center));
				source.sendSuccess(new StringTextComponent("You can now edit the object"), false);
				source.sendSuccess(new StringTextComponent("To change the area of the object, use /otg region"), false);
				source.sendSuccess(new StringTextComponent("When you are done editing, do /otg finishedit"), false);
				source.sendSuccess(new StringTextComponent("To cancel, do /otg canceledit"), false);

				if (!extraBlocks.isEmpty())
					source.sendSuccess(new StringTextComponent("This object's center cannot be moved"), false);

				ExportCommand.playerSelectionMap.put(source.getEntity(), region);
			}
		}
		catch (Exception e)
		{
			source.sendSuccess(new StringTextComponent("Edit command encountered an error, please check the logs."), false);
			OTG.getEngine().getLogger().log(LogLevel.ERROR, LogCategory.MAIN, "Edit command encountered an error: "+e.getClass().getName() + " - " +e.getMessage());
			for (StackTraceElement s : e.getStackTrace())
			{
				OTG.getEngine().getLogger().log(LogLevel.ERROR, LogCategory.MAIN, s.toString());
			}
		}
		return 0;
	}

	private String getFoldersFromObject(StructuredCustomObject object, Path objectPath)
	{
		String name = object.getName();
		Path filePath = object.getConfig().getFile().toPath();

		String folders = filePath.toString();
		folders = folders.replace(name, "");
		folders = folders.replace(objectPath.toString(), "");
		if (folders.startsWith("/")) folders = folders.substring(1);
		if (folders.endsWith(File.separator)) folders = folders.substring(0, folders.length()-1);
		return folders;
	}

	private Runnable getExportRunnable(ObjectType type, ExportCommand.Region region, Corner center, StructuredCustomObject object,
									   LocalWorldGenRegion worldGenRegion, Path exportPath, List<BlockFunction<?>> extraBlocks,
									   String presetFolderName, boolean verbose, CommandSource source)
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
				object.getName(),
				false,
				exportPath,
				worldGenRegion,
				new ForgeNBTHelper(),
				extraBlocks,
				object.getConfig(),
				presetFolderName,
				OTG.getEngine().getOTGRootFolder(),
				OTG.getEngine().getLogger(), OTG.getEngine().getCustomObjectManager(),
				OTG.getEngine().getPresetLoader().getMaterialReader(presetFolderName),
				OTG.getEngine().getCustomObjectResourcesManager(), OTG.getEngine().getModLoadedChecker());

			if (verbose && fixedObject != null)
			{
				source.sendSuccess(new StringTextComponent("Successfully updated "+type.getType()+" " + object.getName()), false);
				OTG.getEngine().getCustomObjectManager().getGlobalObjects().addObjectToPreset(presetFolderName, fixedObject.getName().toLowerCase(Locale.ROOT), fixedObject.getConfig().getFile(), object);
			} else if (verbose) {
				source.sendSuccess(new StringTextComponent("Failed to update "+type.getType()+" " + object.getName()), false);
			}
			cleanArea(worldGenRegion, region.getMin(), region.getMax(), false);
		};
	}

	private ForgeWorldGenRegion getWorldGenRegion(Preset preset, ServerWorld level)
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

	private StructuredCustomObject getStructuredObject(String objectName, String presetFolderName)
	{
		CustomObject objectToSpawn = SpawnCommand.getObject(objectName, presetFolderName);

		if (objectToSpawn instanceof StructuredCustomObject)
		{
			return (StructuredCustomObject) objectToSpawn;
		}
		if (objectToSpawn instanceof BO2)
		{
			// Convert the BO2 to a BO3 before editing
			try
			{
				return new BO3(
					objectToSpawn.getName(),
					ObjectType.BO3.getObjectFilePathFromName(
						objectToSpawn.getName(),
						((BO2) objectToSpawn).getFile().getParentFile().toPath()).toFile(),
					((BO2) objectToSpawn).getConvertedConfig(presetFolderName, OTG.getEngine().getOTGRootFolder(), OTG.getEngine().getLogger(), OTG.getEngine().getCustomObjectManager(),
						OTG.getEngine().getPresetLoader().getMaterialReader(presetFolderName), OTG.getEngine().getCustomObjectResourcesManager(), OTG.getEngine().getModLoadedChecker())
				);
			}
			catch (InvalidConfigException e)
			{
				OTG.getEngine().getLogger().log(LogLevel.ERROR, LogCategory.MAIN, "Failed to convert BO2 "+objectName);
				OTG.getEngine().getLogger().printStackTrace(LogLevel.ERROR, LogCategory.MAIN, e);
			}
		}
		return null;

	}

	public int finish(CommandContext<CommandSource> context)
	{
		CommandSource source = context.getSource();
		try
		{
			EditSession session = sessionsMap.get(source.getEntity());
			ExportCommand.Region region = ExportCommand.playerSelectionMap.get(source.getEntity());


			if (session == null)
			{
				source.sendSuccess(new StringTextComponent("No active session, do '/otg edit' to start one"), false);
				return 0;
			}
			else if (isOutsideBounds(region, session.type))
			{
				source.sendSuccess(new StringTextComponent("Selection is too big! Maximum size is 16x16 for BO4 and 32x32 for BO3"), false);
				return 0;
			} else {
				source.sendSuccess(new StringTextComponent("Cleaning up..."), false);
			}

			StructuredCustomObject object = exportFromSession(session, region);

			if (object != null)
			{
				source.sendSuccess(new StringTextComponent("Successfully edited "+session.type.getType()+" " + object.getName()), false);
				OTG.getEngine().getCustomObjectManager().getGlobalObjects().addObjectToPreset(session.presetFolderName,  object.getName().toLowerCase(Locale.ROOT), object.getConfig().getFile(), object);
			} else {
				source.sendSuccess(new StringTextComponent("Failed to edit "+session.type.getType()+" " + session.object.getName()), false);
			}
			cleanArea(session.genRegion, region.getMin(), region.getMax(), false);
			sessionsMap.put(source.getEntity(), null);
		}
		catch (Exception e)
		{
			source.sendSuccess(new StringTextComponent("Edit command encountered an error, please check logs."), false);
			OTG.getEngine().getLogger().log(LogLevel.ERROR, LogCategory.MAIN, String.format("Edit command encountered an error: ", (Object[])e.getStackTrace()));
		}
		return 0;
	}

	public static StructuredCustomObject exportFromSession(EditSession session, ExportCommand.Region region)
	{
		return ObjectCreator.createObject(
			session.type,
			region.getMin(),
			region.getMax(),
			// Don't let someone change the center if there are non-spawned blocks
			session.extraBlocks.isEmpty() ? region.getCenter() : session.originalCenterPoint,
			null,
			session.object.getName(),
			false,
			session.objectPath,
			session.genRegion,
			new ForgeNBTHelper(),
			session.extraBlocks,
			session.object.getConfig(),
			session.presetFolderName,
			OTG.getEngine().getOTGRootFolder(),
			OTG.getEngine().getLogger(),
			OTG.getEngine().getCustomObjectManager(),
			OTG.getEngine().getPresetLoader().getMaterialReader(session.presetFolderName),
			OTG.getEngine().getCustomObjectResourcesManager(),
			OTG.getEngine().getModLoadedChecker()
		);
	}

	public static boolean isOutsideBounds(ExportCommand.Region region, ObjectType type)
	{
		Corner min = region.getMin();
		Corner max = region.getMax();
		int xlen = Math.abs(max.x - min.x);
		int zlen = Math.abs(max.z - min.z);
		switch (type)
		{
			case BO3:
				return xlen > 31 || zlen > 31;
			case BO4:
				return xlen > 15 || zlen > 15;
			case BO2:
			default:
				return false;
		}
	}

	public int cancel(CommandContext<CommandSource> context)
	{
		CommandSource source = context.getSource();
		EditSession session = sessionsMap.get(source.getEntity());
		ExportCommand.Region region = ExportCommand.playerSelectionMap.get(source.getEntity());

		if (session != null && region != null)
		{
			cleanArea(session.genRegion, region.getMin(), region.getMax(), false);
			sessionsMap.put(source.getEntity(), null);
			source.sendSuccess(new StringTextComponent("Edit session cancelled"), false);
		} else {
			source.sendSuccess(new StringTextComponent("No active edit session to cancel"), false);
		}
		return 0;
	}

	private static class EditSession {
		private final ObjectType type;
		private final ForgeWorldGenRegion genRegion;
		private final StructuredCustomObject object;
		private final ArrayList<BlockFunction<?>> extraBlocks;
		private final Path objectPath;
		private final String presetFolderName;
		private final Corner originalCenterPoint;

		public EditSession(ObjectType type, ForgeWorldGenRegion genRegion, StructuredCustomObject object, ArrayList<BlockFunction<?>> extraBlocks, Path objectPath, String presetFolderName, Corner originalCenterPoint)
		{
			this.type = type;
			this.genRegion = genRegion;
			this.object = object;
			this.extraBlocks = extraBlocks;
			this.objectPath = objectPath;
			this.presetFolderName = presetFolderName;
			this.originalCenterPoint = originalCenterPoint;
		}
	}

	protected static ArrayList<BlockFunction<?>> spawnAndFixObject(int x, int y, int z, StructuredCustomObject object, ForgeWorldGenRegion worldGenRegion, boolean fixObject,
											String presetFolderName, Path otgRootFolder, ILogger logger, ICustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		BlockFunction<?>[] blocks = object.getConfig().getBlockFunctions(presetFolderName, otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker);
		HashSet<BlockPos> updates = new HashSet<>();
		HashSet<BlockPos> gravityBlocksToCheck = new HashSet<>();
		Random random = new Random();
		ArrayList<BlockFunction<?>> unspawnedBlocks = new ArrayList<>();

		for (BlockFunction<?> block : blocks)
		{
			if (fixObject && block.material != null && updateMap.contains(((ForgeMaterialData) block.material).internalBlock().getBlock().getRegistryName()))
			{
				updates.add(new BlockPos(x + block.x, y + block.y, z + block.z));
			}

			if (block.material == null ||
				(
					block.nbt != null
					|| block instanceof BO3RandomBlockFunction
					|| block instanceof BO4RandomBlockFunction
				))
			{
				unspawnedBlocks.add(block);
				continue;
			}

			if (gravityBlocksSet.contains(block.material))
			{
				gravityBlocksToCheck.add(new BlockPos(x + block.x, y + block.y, z + block.z));
			}

			if (block.material.isLeaves())
			{
				block.material = ForgeMaterialData.ofBlockState(((ForgeMaterialData)block.material).internalBlock()
					.setValue(LeavesBlock.PERSISTENT, true).setValue(LeavesBlock.DISTANCE, 7));
			}
			block.spawn(worldGenRegion, random, x + block.x, y + block.y, z + block.z);
		}

		for (BlockPos pos : gravityBlocksToCheck)
		{
			if (worldGenRegion.getMaterial(pos.getX(), pos.getY()-1, pos.getZ()).isMaterial(LocalMaterials.STRUCTURE_VOID))
			{
				// prop up any gravity blocks so they don't fall
				worldGenRegion.setBlock(pos.getX(), pos.getY()-1, pos.getZ(), LocalMaterials.STRUCTURE_BLOCK);
			}
		}

		if (fixObject)
		{
			for (BlockPos blockpos : updates)
			{
				BlockState blockstate = worldGenRegion.getBlockState(blockpos);
				if (blockstate.is(BlockTags.LEAVES))
				{
					// Schedule a tick on this block in 1 unit of time
					worldGenRegion.getInternal().getBlockTicks().scheduleTick(blockpos, blockstate.getBlock(), 1);
				} else {
					BlockState blockstate1 = Block.updateFromNeighbourShapes(blockstate, worldGenRegion.getInternal(), blockpos);
					worldGenRegion.setBlockState(blockpos, blockstate1, 20);
				}
			}
		}
		return unspawnedBlocks;
	}

	protected static void cleanArea(LocalWorldGenRegion region, Corner min, Corner max, boolean preparing)
	{
		for (int x1 = min.x-1; x1 <= max.x+1; x1++)
		{
			for (int z1 = min.z-1; z1 <= max.z+1; z1++)
			{
				for (int y1 = min.y-1; y1 <= max.y+1; y1++)
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

	private int update(CommandContext<CommandSource> context)
	{
		// Get preset
		String presetFolderName = context.getArgument("preset", String.class);
		Preset preset = OTG.getEngine().getPresetLoader().getPresetByFolderName(presetFolderName);
		CommandSource source = context.getSource();

		if (preset == null)
		{
			context.getSource().sendSuccess(new StringTextComponent("Could not find preset '"+presetFolderName+"'"), false);
			return 0;
		}

		// Get list of BO's
		List<String> objectNameList = OTG.getEngine().getCustomObjectManager().getGlobalObjects().getAllBONamesForPreset(presetFolderName, OTG.getEngine().getLogger(), OTG.getEngine().getOTGRootFolder());

		// Create folder for the fixed objects to be exported to
		Path fixedObjectFolderPath = preset.getPresetFolder()
			.resolve(
				preset.getPresetFolder().resolve("WorldObjects").toFile().exists() ?
				"WorldObjects" :
				Constants.WORLD_OBJECTS_FOLDER
			).resolve("Updated Objects");
		fixedObjectFolderPath.toFile().mkdirs();

		ForgeWorldGenRegion worldGenRegion = getWorldGenRegion(preset, source.getLevel());

		BlockPos pos = source.getEntity().blockPosition();

		Runnable updateIteration = ()-> {
			String objectName = objectNameList.remove(0);

			// get object
			StructuredCustomObject object = getStructuredObject(objectName, presetFolderName);
			if (object == null)
			{
				source.sendSuccess(new StringTextComponent("Could not find " + objectName), false);
				return;
			}

			ObjectType type = object.getType();

			ExportCommand.Region region = getRegionFromObject(pos, object);
			Corner center = region.getCenter();

			// cleanArea
			cleanArea(worldGenRegion, region.getMin(), region.getMax(), true);

			// Spawn and fix object

			ArrayList<BlockFunction<?>> extraBlocks = spawnAndFixObject(center.x, center.y, center.z, object, worldGenRegion, true,
				presetFolderName, OTG.getEngine().getOTGRootFolder(), OTG.getEngine().getLogger(), OTG.getEngine().getCustomObjectManager(),
				OTG.getEngine().getPresetLoader().getMaterialReader(presetFolderName), OTG.getEngine().getCustomObjectResourcesManager(), OTG.getEngine().getModLoadedChecker());

			Thread t = new Thread(getExportRunnable(type, region, center, object, worldGenRegion, fixedObjectFolderPath,
				extraBlocks, presetFolderName, false, source));
			t.start();

			try
			{
				t.join();
			}
			catch (InterruptedException e)
			{
				OTG.getEngine().getLogger().printStackTrace(LogLevel.ERROR, LogCategory.MAIN, e);
			}
		};

		// -- Thread --
		new Thread(() -> {
			int totalLength = objectNameList.size();
			int interval;
			int count = 0;
			if (totalLength < 100)
			{
				interval = 20;
			} else {
				interval = totalLength / 7;
			}
			source.sendSuccess(new StringTextComponent("Starting object updating! Please don't move away from the immediate area").withStyle(TextFormatting.GREEN), false);
			while (!objectNameList.isEmpty())
			{
				count++;
				updateIteration.run();
				if (count % interval == 0)
				{
					int percent = (int) ((double) count / (double) totalLength) *100;
					//TODO: The progress never updates past 0, find out why
					source.sendSuccess(new StringTextComponent("Progress: "+percent+"% complete").withStyle(TextFormatting.BLUE), false);
				}
			}
			source.sendSuccess(new StringTextComponent("Finished updating!").withStyle(TextFormatting.GREEN), false);
		}).start();
		return 0;
	}

	public int help(CommandContext<CommandSource> context)
	{
		context.getSource().sendSuccess(new StringTextComponent("To use the edit command:").withStyle(TextFormatting.LIGHT_PURPLE), false);
		context.getSource().sendSuccess(new StringTextComponent("/otg edit <preset> <object> [-nofix, -update]"), false);
		context.getSource().sendSuccess(new StringTextComponent(" - Preset is which preset to fetch the object from, and save it back to"), false);
		context.getSource().sendSuccess(new StringTextComponent(" - Object is the object you want to edit"), false);
		context.getSource().sendSuccess(new StringTextComponent(" - The -nofix flag disables block state fixing"), false);
		context.getSource().sendSuccess(new StringTextComponent(" - The -update flag immediately exports and cleans after fixing"), false);
		context.getSource().sendSuccess(new StringTextComponent(" - Complex objects cannot have their center moved"), false);
		context.getSource().sendSuccess(new StringTextComponent(" - An object is \"complex\" if it contains NBT or RandomBlock"), false);
		return 0;
	}

	protected static ExportCommand.Region getRegionFromObject(BlockPos pos, StructuredCustomObject object)
	{
		ExportCommand.Region region = new ExportCommand.Region();
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
	
	private CompletableFuture<Suggestions> suggestFlags(CommandContext<CommandSource> context,
			SuggestionsBuilder builder)
	{
		return ISuggestionProvider.suggest(FLAGS, builder);
	}

	private static final HashSet<LocalMaterialData> gravityBlocksSet = Stream.of(
		LocalMaterials.SAND, LocalMaterials.RED_SAND, LocalMaterials.GRAVEL
	).collect(Collectors.toCollection(HashSet::new));

	private static final HashSet<LocalMaterialData> liquidsSet = Stream.of(
		LocalMaterials.WATER, LocalMaterials.LAVA
	).collect(Collectors.toCollection(HashSet::new));

	private static final HashSet<ResourceLocation> updateMap = Stream.of(
		"oak_fence",
		"birch_fence",
		"nether_brick_fence",
		"spruce_fence",
		"jungle_fence",
		"acacia_fence",
		"dark_oak_fence",
		"iron_door",
		"oak_door",
		"spruce_door",
		"birch_door",
		"jungle_door",
		"acacia_door",
		"dark_oak_door",
		"glass_pane",
		"white_stained_glass_pane",
		"orange_stained_glass_pane",
		"magenta_stained_glass_pane",
		"light_blue_stained_glass_pane",
		"yellow_stained_glass_pane",
		"lime_stained_glass_pane",
		"pink_stained_glass_pane",
		"gray_stained_glass_pane",
		"light_gray_stained_glass_pane",
		"cyan_stained_glass_pane",
		"purple_stained_glass_pane",
		"blue_stained_glass_pane",
		"brown_stained_glass_pane",
		"green_stained_glass_pane",
		"red_stained_glass_pane",
		"black_stained_glass_pane",
		"purpur_stairs",
		"oak_stairs",
		"cobblestone_stairs",
		"brick_stairs",
		"stone_brick_stairs",
		"nether_brick_stairs",
		"spruce_stairs",
		"sandstone_stairs",
		"birch_stairs",
		"jungle_stairs",
		"quartz_stairs",
		"acacia_stairs",
		"dark_oak_stairs",
		"prismarine_stairs",
		"prismarine_brick_stairs",
		"dark_prismarine_stairs",
		"red_sandstone_stairs",
		"polished_granite_stairs",
		"smooth_red_sandstone_stairs",
		"mossy_stone_brick_stairs",
		"polished_diorite_stairs",
		"mossy_cobblestone_stairs",
		"end_stone_brick_stairs",
		"stone_stairs",
		"smooth_sandstone_stairs",
		"smooth_quartz_stairs",
		"granite_stairs",
		"andesite_stairs",
		"red_nether_brick_stairs",
		"polished_andesite_stairs",
		"diorite_stairs",
		"cobblestone_wall",
		"mossy_cobblestone_wall",
		"brick_wall",
		"prismarine_wall",
		"red_sandstone_wall",
		"mossy_stone_brick_wall",
		"granite_wall",
		"stone_brick_wall",
		"nether_brick_wall",
		"andesite_wall",
		"red_nether_brick_wall",
		"sandstone_wall",
		"end_stone_brick_wall",
		"diorite_wall",
		"iron_bars",
		"trapped_chest",
		"chest",
		"redstone_wire",
		"oak_leaves",
		"spruce_leaves",
		"birch_leaves",
		"jungle_leaves",
		"acacia_leaves",
		"dark_oak_leaves")
		.map(ResourceLocation::new)
		.collect(Collectors.toCollection(HashSet::new));
}
