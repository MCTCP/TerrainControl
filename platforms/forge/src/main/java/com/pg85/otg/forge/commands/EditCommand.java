package com.pg85.otg.forge.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.pg85.otg.core.OTG;
import com.pg85.otg.core.presets.Preset;
import com.pg85.otg.customobject.CustomObject;
import com.pg85.otg.customobject.bo2.BO2;
import com.pg85.otg.customobject.bo3.BO3;
import com.pg85.otg.customobject.bo3.bo3function.BO3RandomBlockFunction;
import com.pg85.otg.customobject.bo4.BO4;
import com.pg85.otg.customobject.bo4.BO4Config;
import com.pg85.otg.customobject.bo4.bo4function.BO4RandomBlockFunction;
import com.pg85.otg.customobject.bofunctions.BlockFunction;
import com.pg85.otg.customobject.config.CustomObjectResourcesManager;
import com.pg85.otg.customobject.config.io.FileSettingsReaderBO4;
import com.pg85.otg.core.objectcreator.ObjectCreator;
import com.pg85.otg.customobject.util.ObjectType;
import com.pg85.otg.customobject.structures.StructuredCustomObject;
import com.pg85.otg.customobject.util.Corner;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.forge.commands.arguments.BiomeObjectArgument;
import com.pg85.otg.forge.commands.arguments.PresetArgument;
import com.pg85.otg.forge.gen.ForgeWorldGenRegion;
import com.pg85.otg.forge.materials.ForgeMaterialData;
import com.pg85.otg.forge.util.ForgeNBTHelper;
import com.pg85.otg.interfaces.ICustomObjectManager;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.interfaces.IModLoadedChecker;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.LocalMaterials;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.tags.BlockTags;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.ChatFormatting;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EditCommand extends BaseCommand
{
	private static final HashMap<Entity, EditSession> sessionsMap = new HashMap<>();
	
	private static final String[] FLAGS = new String[]
	{ "-nofix", "-update", "-wrongleaves" };
	
	public EditCommand() 
	{
		super("edit");
		this.helpMessage = "Allows you to edit existing BO3 and BO4 files.";
		this.usage = "Please see /otg help edit.";
	}

	@Override
	public void build(LiteralArgumentBuilder<CommandSourceStack> builder)
	{
		builder.then(Commands.literal("edit")
			.executes(this::help).then(
				Commands.argument("preset", StringArgumentType.string()).executes(this::execute)
				.suggests((context, suggestionBuilder) -> PresetArgument.suggest(context, suggestionBuilder, true)).then(
					Commands.argument("object", StringArgumentType.word()).executes(this::execute)
					.suggests(BiomeObjectArgument::suggest
						).then(
						Commands.argument("flags", StringArgumentType.greedyString()).executes(this::execute).suggests(this::suggestFlags)
					)
				)
			)
		);
		builder.then(Commands.literal("canceledit")
			.executes(this::cancelSession));
		builder.then(Commands.literal("finishedit")
			.executes(this::finish));
	}

	public int execute(CommandContext<CommandSourceStack> context)
	{
		CommandSourceStack source = context.getSource();
		try {
			String presetFolderName = context.getArgument("preset", String.class);
			String objectName = "";
			boolean immediate = false, doFixing = true, leaveIllegalLeaves = false;

			try
			{
				objectName = context.getArgument("object", String.class);
				String flags = context.getArgument("flags", String.class);
				immediate = flags.contains("-update");
				doFixing = !flags.contains("-nofix");
				leaveIllegalLeaves = flags.contains("-wrongleaves");
			}
			catch (IllegalArgumentException ignored) {}
			presetFolderName = presetFolderName != null && presetFolderName.equalsIgnoreCase("global") ? null : presetFolderName;
			boolean isGlobal = presetFolderName == null;
				
			if (objectName.equals(""))
			{
				source.sendSuccess(new TextComponent("Please supply an object name"), false);
				return 0;
			}

			if (source.getEntity() == null)
			{
				source.sendSuccess(new TextComponent("Only players can run this command"), false);
				return 0;
			}

			StructuredCustomObject inputObject;
			try {
				inputObject = getStructuredObject(objectName, presetFolderName);
			}
			catch (InvalidConfigException e)
			{
				source.sendSuccess(new TextComponent("Error loading object " + objectName), false);
				return 0;
			}
			if (inputObject == null)
			{
				source.sendSuccess(new TextComponent("Could not find " + objectName), false);
				return 0;
			}

			ObjectType type = inputObject.getType();

			Preset preset = ObjectUtils.getPresetOrDefault(presetFolderName);
			if (preset == null)
			{
				source.sendSuccess(new TextComponent("Could not find preset " + (presetFolderName == null ? "" : presetFolderName)), false);
				return 0;
			}

			// Use ForgeWorldGenRegion as a wrapper for the world that ObjectCreator can interact with
			ForgeWorldGenRegion worldGenRegion = ObjectUtils.getWorldGenRegion(preset, source.getLevel());

			RegionCommand.Region region = ObjectUtils.getRegionFromObject(source.getEntity().blockPosition(), inputObject);
			Corner center = region.getCenter();

			// Prepare area for spawning

			ObjectUtils.cleanArea(worldGenRegion, region.getMin(), region.getMax(), true);

			// -- Spawn and update --

			// Spawn code, taken and modified from BO3.java :: spawnForced()
			ArrayList<BlockFunction<?>> extraBlocks = spawnAndFixObject(center.x, center.y, center.z, inputObject, worldGenRegion, doFixing,
				presetFolderName, OTG.getEngine().getOTGRootFolder(), OTG.getEngine().getLogger(), OTG.getEngine().getCustomObjectManager(),
				OTG.getEngine().getPresetLoader().getMaterialReader(presetFolderName), OTG.getEngine().getCustomObjectResourcesManager(), OTG.getEngine().getModLoadedChecker());

			// Save the object and clean the area
			Path path = ObjectUtils.getObjectFolderPath(isGlobal ? null : preset.getPresetFolder())
				.resolve(ObjectUtils.getFoldersFromObject(inputObject));
			if (immediate)
			{
				new Thread(ObjectUtils.getExportRunnable(type, region, center, inputObject, path, extraBlocks, presetFolderName, true, leaveIllegalLeaves, source, worldGenRegion
				)).start();
				return 0;
			}
			// Store the info, wait for /otg finishedit
			sessionsMap.put(source.getEntity(), new EditSession(type, worldGenRegion, inputObject, extraBlocks,
				path, preset.getFolderName(), center, leaveIllegalLeaves));
			source.sendSuccess(new TextComponent("You can now edit the object"), false);
			source.sendSuccess(new TextComponent("To change the area of the object, use /otg region"), false);
			source.sendSuccess(new TextComponent("When you are done editing, do /otg finishedit"), false);
			source.sendSuccess(new TextComponent("To cancel, do /otg canceledit"), false);

			if (!extraBlocks.isEmpty())
				source.sendSuccess(new TextComponent("This object's center cannot be moved"), false);

			RegionCommand.playerSelectionMap.put(source.getEntity(), region);
		}
		catch (Exception e)
		{
			source.sendSuccess(new TextComponent("Edit command encountered an error, please check the logs."), false);
			OTG.getEngine().getLogger().log(LogLevel.ERROR, LogCategory.MAIN, "Edit command encountered an error: "+e.getClass().getName() + " - " +e.getMessage());
			OTG.getEngine().getLogger().printStackTrace(LogLevel.ERROR, LogCategory.MAIN, e);
		}
		return 0;
	}

	protected static StructuredCustomObject getStructuredObject(String objectName, String presetFolderName) throws InvalidConfigException
	{
		CustomObject objectToSpawn = ObjectUtils.getObject(objectName, presetFolderName);

		if (objectToSpawn instanceof BO3)
		{
			return (StructuredCustomObject) objectToSpawn;
		}
		if (presetFolderName == null)
		{
			presetFolderName = OTG.getEngine().getPresetLoader().getDefaultPresetFolderName();
		}
		if (objectToSpawn instanceof BO4)
		{
			File file = ((BO4) objectToSpawn).getConfig().getFile();
			return new BO4(
				objectName,
				file,
				new BO4Config(
					new FileSettingsReaderBO4(objectName, file, OTG.getEngine().getLogger()),
					true,
					presetFolderName,
					OTG.getEngine().getOTGRootFolder(),
					OTG.getEngine().getLogger(),
					OTG.getEngine().getCustomObjectManager(),
					OTG.getEngine().getPresetLoader().getMaterialReader(presetFolderName),
					OTG.getEngine().getCustomObjectResourcesManager(),
					OTG.getEngine().getModLoadedChecker()));
		}
		else if (objectToSpawn instanceof BO2)
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

	public int finish(CommandContext<CommandSourceStack> context)
	{
		CommandSourceStack source = context.getSource();
		try
		{
			EditSession session = sessionsMap.get(source.getEntity());
			RegionCommand.Region region = RegionCommand.playerSelectionMap.get(source.getEntity());

			if (session == null)
			{
				source.sendSuccess(new TextComponent("No active session, do '/otg edit' to start one"), false);
				return 0;
			}
			else if (ObjectUtils.isOutsideBounds(region, session.type))
			{
				source.sendSuccess(new TextComponent("Selection is too big! Maximum size is 16x16 for BO4 and 32x32 for BO3"), false);
				return 0;
			} else {
				source.sendSuccess(new TextComponent("Cleaning up..."), false);
			}

			StructuredCustomObject object = exportFromSession(session, region);

			if (object != null)
			{
				source.sendSuccess(new TextComponent("Successfully edited "+session.type.getType()+" " + object.getName()), false);
				OTG.getEngine().getCustomObjectManager().getGlobalObjects().addObjectToPreset(session.presetFolderName,  object.getName(), object.getConfig().getFile(), object);
			} else {
				source.sendSuccess(new TextComponent("Failed to edit "+session.type.getType()+" " + session.object.getName()), false);
			}
			ObjectUtils.cleanArea(session.genRegion, region.getMin(), region.getMax(), false);
			sessionsMap.put(source.getEntity(), null);
		}
		catch (Exception e)
		{
			source.sendSuccess(new TextComponent("Edit command encountered an error, please check logs."), false);
			OTG.getEngine().getLogger().log(LogLevel.ERROR, LogCategory.MAIN, "Edit command encountered an error: ");
			OTG.getEngine().getLogger().printStackTrace(LogLevel.ERROR, LogCategory.MAIN, e);
		}
		return 0;
	}

	public static StructuredCustomObject exportFromSession(EditSession session, RegionCommand.Region region)
	{
		return ObjectCreator.createObject(
			session.type,
			region.getMin(),
			region.getMax(),
			// Don't let someone change the center if there are non-spawned blocks
			session.extraBlocks.isEmpty() ? region.getCenter() : session.originalCenterPoint,
			null,
			session.object.getName(),
			session.leaveIllegalLeaves,
			session.objectPath,
			session.genRegion,
			new ForgeNBTHelper(),
			session.extraBlocks,
			session.object.getConfig(),
			session.presetFolderName,
			null
		);
	}

	public int cancelSession(CommandContext<CommandSourceStack> context)
	{
		CommandSourceStack source = context.getSource();
		EditSession session = sessionsMap.get(source.getEntity());
		RegionCommand.Region region = RegionCommand.playerSelectionMap.get(source.getEntity());

		if (session != null && region != null)
		{
			ObjectUtils.cleanArea(session.genRegion, region.getMin(), region.getMax(), false);
			sessionsMap.put(source.getEntity(), null);
			source.sendSuccess(new TextComponent("Edit session cancelled"), false);
		} else {
			source.sendSuccess(new TextComponent("No active edit session to cancel"), false);
		}
		return 0;
	}

	private record EditSession(ObjectType type,
							   ForgeWorldGenRegion genRegion,
							   StructuredCustomObject object,
							   ArrayList<BlockFunction<?>> extraBlocks,
							   Path objectPath, String presetFolderName,
							   Corner originalCenterPoint, boolean leaveIllegalLeaves) {}

	protected static ArrayList<BlockFunction<?>> spawnAndFixObject(int x, int y, int z, StructuredCustomObject object, ForgeWorldGenRegion worldGenRegion, boolean fixObject, String presetFolderName, Path otgRootFolder, ILogger logger, ICustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		BlockFunction<?>[] blocks = object.getConfig().getBlockFunctions(presetFolderName, otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker);
		HashSet<BlockPos> updates = new HashSet<>();
		HashSet<BlockPos> gravityBlocksToCheck = new HashSet<>();
		Random random = new Random();
		ArrayList<BlockFunction<?>> unspawnedBlocks = new ArrayList<>();

		for (BlockFunction<?> block : blocks)
		{
			if (
				block.material == null
				|| (block.nbt != null
					|| block instanceof BO3RandomBlockFunction
					|| block instanceof BO4RandomBlockFunction
					|| block.material.isBlank())
			)
			{
				unspawnedBlocks.add(block);
				continue;
			}

			// Queue the block coordinate for processing
			if (fixObject && updateMap.contains(ResourceLocation.tryParse(block.material.getRegistryName())))
			{
				updates.add(new BlockPos(x + block.x, y + block.y, z + block.z));
			}

			// Make sure gravel and sand blocks don't fall down
			if (gravityBlocksSet.contains(block.material))
			{
				gravityBlocksToCheck.add(new BlockPos(x + block.x, y + block.y, z + block.z));
			}

			// We set all leaves to be persistent so they don't disappear in the process
			// They lose persistence on export if they are legal
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
					//T type, BlockPos pos, long triggerTick, TickPriority priority, long subTickOrder
					worldGenRegion.getInternal().scheduleTick(blockpos, blockstate.getBlock(), 1);
				} else {
					BlockState blockstate1 = Block.updateFromNeighbourShapes(blockstate, worldGenRegion.getInternal(), blockpos);
					worldGenRegion.setBlockState(blockpos, blockstate1, 20);
				}
			}
		}
		return unspawnedBlocks;
	}

	public int help(CommandContext<CommandSourceStack> context)
	{
		context.getSource().sendSuccess(new TextComponent("To use the edit command:").withStyle(ChatFormatting.LIGHT_PURPLE), false);
		context.getSource().sendSuccess(new TextComponent("/otg edit <preset> <object> [-nofix, -update]"), false);
		context.getSource().sendSuccess(new TextComponent(" - Preset is which preset to fetch the object from, and save it back to"), false);
		context.getSource().sendSuccess(new TextComponent(" - Object is the object you want to edit"), false);
		context.getSource().sendSuccess(new TextComponent(" - The -nofix flag disables block state fixing"), false);
		context.getSource().sendSuccess(new TextComponent(" - The -update flag immediately exports and cleans after fixing"), false);
		context.getSource().sendSuccess(new TextComponent(" - Complex objects cannot have their center moved"), false);
		context.getSource().sendSuccess(new TextComponent(" - An object is \"complex\" if it contains NBT or RandomBlock"), false);
		return 0;
	}

	private CompletableFuture<Suggestions> suggestFlags(CommandContext<CommandSourceStack> context,
			SuggestionsBuilder builder)
	{
		return SharedSuggestionProvider.suggest(FLAGS, builder);
	}

	// These maps are used to figure out what blocks to update

	private static final HashSet<LocalMaterialData> gravityBlocksSet = Stream.of(
		LocalMaterials.SAND, LocalMaterials.RED_SAND, LocalMaterials.GRAVEL
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
		"dark_oak_leaves",
		"vine")
		.map(ResourceLocation::new)
		.collect(Collectors.toCollection(HashSet::new));
}
