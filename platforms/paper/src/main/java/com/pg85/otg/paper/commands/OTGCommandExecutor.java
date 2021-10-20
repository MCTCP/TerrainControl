package com.pg85.otg.paper.commands;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class OTGCommandExecutor
{
	private static final List<BaseCommand> commands = new ArrayList<>();
	
	//
	// our command is registered as minecraft:otg, which is not ideal -josh
	// -- Partially fixed this by removing the command from our plugin.yml, now works using only /otg -auth
	//
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		HelpCommand helpCommand = new HelpCommand();

		commands.clear();
		commands.add(helpCommand);
		commands.add(new MapCommand());
		commands.add(new DataCommand());
		commands.add(new PresetCommand());
		commands.add(new FlushCommand());
		commands.add(new StructureCommand());
		commands.add(new BiomeCommand());
		commands.add(new TpCommand());
		commands.add(new SpawnCommand());
		commands.add(new EditCommand());
		commands.add(new ExportCommand());
		commands.add(new RegionCommand());
		
		commands.sort(Comparator.comparing(BaseCommand::getName));

		LiteralArgumentBuilder<CommandSourceStack> commandBuilder = Commands.literal("otg")
				.executes(
					context -> helpCommand.showHelp(context.getSource(), "")
			);

		for (BaseCommand command : commands) {
			command.build(commandBuilder);
		}
		
		dispatcher.register(commandBuilder);
	}
	
	// 
	// TODO I had to remove our custom flag argument, so we need another way to handle trailing flags in command arguments
	// 
	public static void registerArguments() {
		//ArgumentTypes.register("flags", FlagsArgument.class, new EmptyArgumentSerializer<>(FlagsArgument::create));
	}
	
	public static List<BaseCommand> getCommands() {
		return commands;
	}
}
