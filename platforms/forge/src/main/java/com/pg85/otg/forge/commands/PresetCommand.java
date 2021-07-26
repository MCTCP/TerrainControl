package com.pg85.otg.forge.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.pg85.otg.forge.gen.OTGNoiseChunkGenerator;
import com.pg85.otg.presets.Preset;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;

public class PresetCommand extends BaseCommand
{
	public PresetCommand() {
		this.name = "preset";
		this.helpMessage = "Displays information about the current world's preset.";
		this.usage = "/otg preset";
	}
	
	@Override
	public void build(LiteralArgumentBuilder<CommandSource> builder)
	{
		builder.then(Commands.literal("preset")
			.executes(context -> showPreset(context.getSource()))
		);
	}
	
	private int showPreset(CommandSource source)
	{
		if (!(source.getLevel().getChunkSource().generator instanceof OTGNoiseChunkGenerator))
		{
			source.sendSuccess(new StringTextComponent("OTG is not enabled in this world"), false);
			return 0;
		}
		Preset preset = ((OTGNoiseChunkGenerator) source.getLevel().getChunkSource().generator).getPreset();
		source.sendSuccess(new StringTextComponent
			("Preset: " + preset.getFolderName()
			 + "\nDescription: " + preset.getDescription()
			 + "\nMajor version: " + preset.getMajorVersion()
			), false);
		return 0;
	}
}
