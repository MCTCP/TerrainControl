package com.pg85.otg.paper.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.pg85.otg.core.presets.Preset;
import com.pg85.otg.paper.gen.OTGNoiseChunkGenerator;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;

public class PresetCommand extends BaseCommand
{
	public PresetCommand() 
	{
		super("preset");
		this.helpMessage = "Displays information about the current world's preset.";
		this.usage = "/otg preset";
	}
	
	@Override
	public void build(LiteralArgumentBuilder<CommandSourceStack> builder)
	{
		builder.then(Commands.literal("preset")
			.executes(context -> showPreset(context.getSource()))
		);
	}
	
	private int showPreset(CommandSourceStack source)
	{
		if (!source.hasPermission(2, getPermission())) {
			source.sendSuccess(new TextComponent("\u00a7cPermission denied!"), false);
			return 0;
		}
		if (!(source.getLevel().getChunkSource().generator instanceof OTGNoiseChunkGenerator))
		{
			source.sendSuccess(new TextComponent("OTG is not enabled in this world"), false);
			return 0;
		}
		Preset preset = ((OTGNoiseChunkGenerator) source.getLevel().getChunkSource().generator).getPreset();
		source.sendSuccess(new TextComponent
			("Preset: " + preset.getFolderName()
			 + "\nDescription: " + preset.getDescription()
			 + "\nMajor version: " + preset.getMajorVersion()
			),
				false);
		return 0;
	}

	@Override
	public String getPermission() {
		return "otg.cmd.preset";
	}
}
