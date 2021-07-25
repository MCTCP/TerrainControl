package com.pg85.otg.forge.commands;

import java.util.HashSet;
import java.util.Set;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.pg85.otg.forge.commands.arguments.BiomeNameArgument;
import com.pg85.otg.forge.commands.arguments.BiomeObjectArgument;
import com.pg85.otg.forge.commands.arguments.PresetArgument;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.ArgumentSerializer;
import net.minecraft.command.arguments.ArgumentTypes;

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
		commands.add(new TpCommand());
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
	
	public static void registerArguments() {
		ArgumentTypes.register("biome_name", BiomeNameArgument.class, new ArgumentSerializer<>(BiomeNameArgument::create));
		ArgumentTypes.register("preset", PresetArgument.class, new ArgumentSerializer<>(PresetArgument::create));
		ArgumentTypes.register("biome_object", BiomeObjectArgument.class, new ArgumentSerializer<>(BiomeObjectArgument::create));
	}
}
