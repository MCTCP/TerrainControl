package com.pg85.otg.forge.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;

public class OTGCommand
{
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(
			Commands.literal("otg").requires(
				(p_198521_0_) -> {
					return p_198521_0_.hasPermissionLevel(2);
				}
			).executes(
				(p_198520_0_) -> {
					return showHelp(p_198520_0_.getSource());
				}
			).then(
				Commands.argument("help", StringArgumentType.word()).executes(
					(p_229810_0_) -> {
						return showHelp(p_229810_0_.getSource());
					}
				)
			)
		);
	}

	private static int showHelp(CommandSource source)
	{
		source.sendFeedback(new StringTextComponent("OTG Help"), true);	
		return 0;
	}
}
