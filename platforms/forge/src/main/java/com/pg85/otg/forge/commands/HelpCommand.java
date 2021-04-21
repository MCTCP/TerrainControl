package com.pg85.otg.forge.commands;

import net.minecraft.command.CommandSource;
import net.minecraft.util.text.StringTextComponent;

public class HelpCommand
{
	protected static int showHelp(CommandSource source)
	{
		source.sendSuccess(new StringTextComponent("OTG Help"), false);
		source.sendSuccess(new StringTextComponent("/otg map -> Creates a 2048x2048 biome map of the world."), false);
		source.sendSuccess(new StringTextComponent("/otg data <dataType>"), false);
		source.sendSuccess(new StringTextComponent("/otg spawn <preset name> <object name> <location>"), false);
		return 0;
	}
}
