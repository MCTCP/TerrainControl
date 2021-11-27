package com.pg85.otg.forge.commands;

import java.util.List;
import java.util.stream.Collectors;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.pg85.otg.forge.gen.OTGNoiseChunkGenerator;
import com.pg85.otg.presets.Preset;
import com.pg85.otg.util.materials.LocalMaterialData;

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
			List<String> portalBlocks = ((OTGNoiseChunkGenerator) source.getLevel().getChunkSource().generator)
					.getPortalBlocks().stream().map(LocalMaterialData::getName).collect(Collectors.toList());

			if (!portalBlocks.isEmpty())
			{
				source.sendSuccess(new TextComponent("Portal Blocks: " + String.join(", ", portalBlocks)), false);
			}
		return 0;
	}
}
