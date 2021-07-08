package com.pg85.otg.forge.commands;

import java.util.HashSet;
import java.util.Set;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

public class OTGCommand
{
	private static final Set<BaseCommand> commands = new HashSet<>();
	
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		HelpCommand helpCommand = new HelpCommand();

		commands.add(helpCommand);
		commands.add(new MapCommand());
		commands.add(new DataCommand());
		commands.add(new PresetCommand());
		commands.add(new FlushCommand());
		commands.add(new StructureCommand());
		commands.add(new BiomeCommand());
		commands.add(new LocateCommand());
		commands.add(new SpawnCommand());
		commands.add(new EditCommand());
		commands.add(new ExportCommand());

		LiteralArgumentBuilder<CommandSource> commandBuilder = Commands.literal("otg").requires(
				(context) -> context.hasPermission(2)
			).executes(
					context -> helpCommand.showHelp(context.getSource())
			);
		
		for (BaseCommand command : commands) {
			command.build(commandBuilder);
		}
		
		dispatcher.register(commandBuilder);
	}
}
