package com.pg85.otg.forge.commands;

import com.pg85.otg.OTG;
import com.pg85.otg.customobject.CustomObject;
import com.pg85.otg.forge.gen.ForgeWorldGenRegion;
import com.pg85.otg.forge.gen.OTGNoiseChunkGenerator;
import com.pg85.otg.presets.Preset;
import com.pg85.otg.util.bo3.Rotation;
import net.minecraft.command.CommandSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;

import java.util.Random;

public class SpawnCommand
{
	public static int execute(CommandSource source, String presetName, String objectName, BlockPos blockPos)
	{
		// /otg spawn BOTest fence
		// String presetName = "BOTest";
		// String objectName = "fence";
		if (!(source.getWorld().getChunkProvider().getChunkGenerator() instanceof OTGNoiseChunkGenerator))
		{
			source.sendFeedback(new StringTextComponent("Can only run this command in an OTG world"), false);
			return 0;
		}
		CustomObject objectToSpawn = OTG.getEngine().getCustomObjectManager().getGlobalObjects().getObjectByName(
			objectName,
			presetName,
			OTG.getEngine().getOTGRootFolder(),
			false,
			OTG.getEngine().getLogger(),
			OTG.getEngine().getCustomObjectManager(),
			OTG.getEngine().getMaterialReader(),
			OTG.getEngine().getCustomObjectResourcesManager(),
			OTG.getEngine().getModLoadedChecker());

		if (objectToSpawn == null)
		{
			source.sendFeedback(new StringTextComponent("Could not find an object by the name " + objectName + " in either " + presetName + " or Global objects"), false);
			return 0;
		}
		Preset p = OTG.getEngine().getPresetLoader().getPresetByName(presetName);
		if (objectToSpawn.spawnForced(
			((OTGNoiseChunkGenerator) source.getWorld().getChunkProvider().getChunkGenerator()).getStructureCache(),
			new ForgeWorldGenRegion(p.getName(), p.getWorldConfig(), source.getWorld(),
				(OTGNoiseChunkGenerator) source.getWorld().getChunkProvider().getChunkGenerator()),
			new Random(),
			Rotation.NORTH,
			blockPos.getX(),
			blockPos.getY(),
			blockPos.getZ()
		))
		{
			source.sendFeedback(new StringTextComponent("Spawned object " + objectName + " at " + blockPos.toString()), false);
		}
		else
		{
			source.sendFeedback(new StringTextComponent("Failed to spawn object " + objectName+". Is it a BO4?"), false);
		}

		return 0;
	}
}
