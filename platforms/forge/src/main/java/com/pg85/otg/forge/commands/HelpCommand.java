package com.pg85.otg.forge.commands;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class HelpCommand extends BaseCommand
{
	public HelpCommand()
	{
		this.name = "help";
		this.helpMessage = "Shows help for all OTG commands.";
		this.usage = "/otg help [command/page]";
		this.detailedHelp = new String[] { 
			"[command/page]: The name of the command you want to view detailed help for, or the page number of the help menu you want to display." 
		};
	}

	@Override
	public void build(LiteralArgumentBuilder<CommandSource> builder)
	{
		builder.then(Commands.literal("help").executes(context -> showHelp(context.getSource(), ""))
				.then(Commands.argument("command", new CommandArgument()).executes(
						(context -> showHelp(context.getSource(), context.getArgument("command", String.class))))));
	}

	protected int showHelp(CommandSource source, String cmd)
	{
		if (cmd.isEmpty())
		{
			showHelp(source, 1);
		} else
		{

			if (StringUtils.isNumeric(cmd))
			{
				showHelp(source, Integer.parseInt(cmd));
			} else
			{

				OTGCommand.getCommands().stream().filter(basecmd -> basecmd.getName().equalsIgnoreCase(cmd)).findFirst()
						.ifPresent(command ->
						{
							source.sendSuccess(new StringTextComponent("/otg " + command.getName() + ": ")
									.withStyle(TextFormatting.GOLD)
									.append(new StringTextComponent(command.getHelpMessage())
											.withStyle(TextFormatting.GREEN)),
									false);
							source.sendSuccess(new StringTextComponent("usage: " + command.getUsage())
									.withStyle(TextFormatting.GRAY), false);
							for (String help : command.getDetailedHelp())
							{
								source.sendSuccess(new StringTextComponent(help).withStyle(TextFormatting.GRAY), false);
							}
						});
			}
		}
		return 0;
	}

	private void showHelp(CommandSource source, int page)
	{
		int start = (page - 1) * 5;

		List<BaseCommand> commands = OTGCommand.getCommands();

		for (int i = start; i < commands.size() && i < start + 5; i++)
		{
			BaseCommand command = commands.get(i);

			source.sendSuccess(
					new StringTextComponent("/otg " + command.getName() + ": ").withStyle(TextFormatting.GOLD).append(
							new StringTextComponent(command.getHelpMessage()).withStyle(TextFormatting.GREEN)),
					false);
			source.sendSuccess(
					new StringTextComponent(" - usage: " + command.getUsage()).withStyle(TextFormatting.GRAY), false);
		}
		source.sendSuccess(
				new StringTextComponent("Use /otg help <page> for more commands.").withStyle(TextFormatting.GOLD),
				false);
	}

	public static class CommandArgument implements ArgumentType<String>
	{
		public static CommandArgument create()
		{
			return new CommandArgument();
		}

		@Override
		public String parse(StringReader reader) throws CommandSyntaxException
		{
			return reader.readString();
		}

		@Override
		public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder)
		{
			return ISuggestionProvider.suggest(
					OTGCommand.getCommands().stream().map(BaseCommand::getName).collect(Collectors.toList()), builder);
		}
	}
}
