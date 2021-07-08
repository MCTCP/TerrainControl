package com.pg85.otg.forge.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;

public class HelpCommand implements BaseCommand
{
	@Override
	public void build(LiteralArgumentBuilder<CommandSource> builder)
	{
		builder.then(Commands.literal("help")
			.executes(context -> showHelp(context.getSource()))
		);
	}

	// TODO add an argument string to each command class to print here
	protected int showHelp(CommandSource source)
	{
		source.sendSuccess(new StringTextComponent("OTG Help"), false);
		source.sendSuccess(new StringTextComponent("/otg map -> Creates a 2048x2048 biome map of the world."), false);
		source.sendSuccess(new StringTextComponent("/otg data <dataType>"), false);
		source.sendSuccess(new StringTextComponent("/otg spawn <preset name> <object name> <location>"), false);
		return 0;
	}
}
