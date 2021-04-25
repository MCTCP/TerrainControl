package com.pg85.otg.forge.commands;

import com.pg85.otg.forge.gen.OTGNoiseChunkGenerator;
import com.pg85.otg.presets.Preset;
import net.minecraft.command.CommandSource;
import net.minecraft.util.text.StringTextComponent;

public class PresetCommand
{
	protected static int showPreset(CommandSource source)
	{
		if (!(source.getLevel().getChunkSource().generator instanceof OTGNoiseChunkGenerator))
		{
			source.sendSuccess(new StringTextComponent("OTG is not enabled in this world"), false);
			return 0;
		}
		Preset preset = ((OTGNoiseChunkGenerator) source.getLevel().getChunkSource().generator).getPreset();
		source.sendSuccess(new StringTextComponent
			("Preset: " + preset.getName()
			 + "\nDescription: " + preset.getDescription()
			 + "\nVersion: " + preset.getVersion()
			), false);
		return 0;
	}
}
