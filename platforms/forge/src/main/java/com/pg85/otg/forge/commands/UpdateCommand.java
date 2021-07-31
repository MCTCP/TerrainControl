package com.pg85.otg.forge.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.pg85.otg.OTG;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.customobject.bofunctions.BlockFunction;
import com.pg85.otg.customobject.creator.ObjectType;
import com.pg85.otg.customobject.structures.StructuredCustomObject;
import com.pg85.otg.customobject.util.Corner;
import com.pg85.otg.forge.commands.arguments.PresetArgument;
import com.pg85.otg.forge.gen.ForgeWorldGenRegion;
import com.pg85.otg.presets.Preset;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class UpdateCommand extends BaseCommand
{
	public UpdateCommand()
	{
		this.name = "update";
		this.helpMessage = "Updates all the objects in a preset and places them in a folder";
		this.usage = "/otg update <preset>.";
	}

	@Override
	public void build(LiteralArgumentBuilder<CommandSource> builder)
	{
		builder.then(Commands.literal("update").then(
			Commands.argument("preset", StringArgumentType.word())
				.suggests((context, suggestionBuilder) -> PresetArgument.suggest(context, suggestionBuilder, false))
				.executes(this::update)));
	}

	private int update(CommandContext<CommandSource> context)
	{
		// Get preset
		String presetFolderName = context.getArgument("preset", String.class);
		Preset preset = OTG.getEngine().getPresetLoader().getPresetByFolderName(presetFolderName);
		CommandSource source = context.getSource();

		if (preset == null)
		{
			context.getSource().sendSuccess(new StringTextComponent("Could not find preset '" + presetFolderName + "'"), false);
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

		ForgeWorldGenRegion worldGenRegion = EditCommand.getWorldGenRegion(preset, source.getLevel());

		BlockPos pos = source.getEntity().blockPosition();

		// This is the runnable that does the updating per object

		Runnable updateIteration = ()-> {
			String objectName = objectNameList.remove(0);

			// get object
			StructuredCustomObject inputObject = EditCommand.getStructuredObject(objectName, presetFolderName);
			if (inputObject == null)
			{
				source.sendSuccess(new StringTextComponent("Could not find " + objectName), false);
				return;
			}

			ObjectType type = inputObject.getType();

			RegionCommand.Region region = ObjectUtils.getRegionFromObject(pos, inputObject);
			Corner center = region.getCenter();

			// cleanArea
			ObjectUtils.cleanArea(worldGenRegion, region.getMin(), region.getMax(), true);

			// Spawn and fix object

			ArrayList<BlockFunction<?>> extraBlocks = EditCommand.spawnAndFixObject(center.x, center.y, center.z, inputObject, worldGenRegion, true,
				presetFolderName, OTG.getEngine().getOTGRootFolder(), OTG.getEngine().getLogger(), OTG.getEngine().getCustomObjectManager(),
				OTG.getEngine().getPresetLoader().getMaterialReader(presetFolderName), OTG.getEngine().getCustomObjectResourcesManager(), OTG.getEngine().getModLoadedChecker());

			Thread t = new Thread(ObjectUtils.getExportRunnable(type, region, center, inputObject, fixedObjectFolderPath.resolve(ObjectUtils.getFoldersFromObject(inputObject)), extraBlocks, presetFolderName, false, source, worldGenRegion
			));
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
}
