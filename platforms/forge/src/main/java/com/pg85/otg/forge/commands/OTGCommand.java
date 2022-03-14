package com.pg85.otg.forge.commands;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class OTGCommand
{
	private static final List<BaseCommand> commands = new ArrayList<>();
	
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
		commands.add(new UpdateCommand());
		commands.add(new ExportBO4DataCommand());
		commands.add(new ConfigExportCommand());
		
		commands.sort(Comparator.comparing(BaseCommand::getName));

		LiteralArgumentBuilder<CommandSourceStack> commandBuilder = Commands.literal("otg").requires(
				(context) -> context.hasPermission(2)
			).executes(
					context -> helpCommand.showHelp(context.getSource(), "")
			);
		
		for (BaseCommand command : commands) {
			command.build(commandBuilder);
		}
		
		dispatcher.register(commandBuilder);
	}
	
	public static List<BaseCommand> getCommands() {
		return commands;
	}
}
