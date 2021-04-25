package com.pg85.otg.forge.commands;

import com.pg85.otg.OTG;
import com.pg85.otg.customobject.CustomObject;
import com.pg85.otg.customobject.structures.CustomStructureCache;
import com.pg85.otg.forge.gen.ForgeWorldGenRegion;
import com.pg85.otg.forge.gen.OTGNoiseChunkGenerator;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.presets.Preset;
import com.pg85.otg.util.bo3.Rotation;
import com.pg85.otg.util.gen.LocalWorldGenRegion;
import net.minecraft.command.CommandSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;

import java.util.Random;

public class SpawnCommand
{
	public static int execute(CommandSource source, String presetName, String objectName, BlockPos blockPos)
	{
		try
		{
			CustomObject objectToSpawn = getObject(objectName, presetName);

			if (objectToSpawn == null)
			{
				source.sendSuccess(new StringTextComponent("Could not find an object by the name " + objectName + " in either " + presetName + " or Global objects"), false);
				return 0;
			}

			Preset preset = ExportCommand.getPreset(presetName);

			LocalWorldGenRegion region = new ForgeWorldGenRegion(preset.getName(), preset.getWorldConfig(), source.getLevel(),
				source.getLevel().getChunkSource().getGenerator());

			CustomStructureCache cache =  source.getLevel().getChunkSource().getGenerator() instanceof OTGNoiseChunkGenerator ?
										  ((OTGNoiseChunkGenerator) source.getLevel().getChunkSource().getGenerator()).getStructureCache() :
										  null;

			// Cache is only null in non-OTG worlds
			if (cache == null && objectToSpawn.doReplaceBlocks())
			{
				source.sendSuccess(new StringTextComponent("Cannot spawn objects with DoReplaceBlocks in on-OTG worlds"), false);
				return 0;
			}

			if (objectToSpawn.spawnForced(
				cache,
				region,
				new Random(),
				Rotation.NORTH,
				blockPos.getX(),
				blockPos.getY(),
				blockPos.getZ()
			))
			{
				source.sendSuccess(new StringTextComponent("Spawned object " + objectName + " at " + blockPos.toString()), false);
			}
			else
			{
				source.sendSuccess(new StringTextComponent("Failed to spawn object " + objectName + ". Is it a BO4?"), false);
			}
		}
		catch (Exception e)
		{
			source.sendSuccess(new StringTextComponent("Something went wrong, please check logs"), false);
			OTG.log(LogMarker.INFO, e.toString());
			for (StackTraceElement s :
				e.getStackTrace())
			{
				OTG.log(LogMarker.INFO, s.toString());
			}
		}
		return 0;
	}

	public static CustomObject getObject(String objectName, String presetName)
	{
		if (presetName.equalsIgnoreCase("global"))
		{
			presetName = OTG.getEngine().getPresetLoader().getDefaultPresetName();
		}
		return OTG.getEngine().getCustomObjectManager().getGlobalObjects().getObjectByName(
			objectName,
			presetName,
			OTG.getEngine().getOTGRootFolder(),
			false,
			OTG.getEngine().getLogger(),
			OTG.getEngine().getCustomObjectManager(),
			OTG.getEngine().getMaterialReader(),
			OTG.getEngine().getCustomObjectResourcesManager(),
			OTG.getEngine().getModLoadedChecker());
	}
}
