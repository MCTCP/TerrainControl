package com.pg85.otg.paper.commands;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.TextComponent;

public class HelpCommand extends BaseCommand
{
	public HelpCommand()
	{
		super("help");
		this.helpMessage = "Shows help for all OTG commands.";
		this.usage = "/otg help [command/page]";
		this.detailedHelp = new String[]
		{ "[command/page]: The name of the command you want to view detailed help for, or the page number of the help menu you want to display." };
	}

	@Override
	public void build(LiteralArgumentBuilder<CommandSourceStack> builder)
	{
		builder.then(Commands.literal("help").executes(context -> showHelp(context.getSource(), ""))
				.then(Commands.argument("command", StringArgumentType.word()).suggests(this::suggestHelp).executes(
						(context -> showHelp(context.getSource(), context.getArgument("command", String.class))))));
	}

	protected int showHelp(CommandSourceStack source, String cmd)
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

				OTGCommandExecutor.getCommands().stream().filter(basecmd -> basecmd.getName().equalsIgnoreCase(cmd)).findFirst()
						.ifPresent(command ->
						{
							source.sendSuccess(new TextComponent("/otg " + command.getName() + ": ")
									.withStyle(ChatFormatting.GOLD)
									.append(new TextComponent(command.getHelpMessage())
											.withStyle(ChatFormatting.GREEN)),
									false);
							source.sendSuccess(new TextComponent("usage: " + command.getUsage())
									.withStyle(ChatFormatting.GRAY), false);
							for (String help : command.getDetailedHelp())
							{
								source.sendSuccess(new TextComponent(help).withStyle(ChatFormatting.GRAY), false);
							}
						});
			}
		}
		return 0;
	}

	private void showHelp(CommandSourceStack source, int page)
	{
		int start = (page - 1) * 5;

		List<BaseCommand> commands = OTGCommandExecutor.getCommands();

		for (int i = start; i < commands.size() && i < start + 5; i++)
		{
			BaseCommand command = commands.get(i);

			source.sendSuccess(
					new TextComponent("/otg " + command.getName() + ": ").withStyle(ChatFormatting.GOLD).append(
							new TextComponent(command.getHelpMessage()).withStyle(ChatFormatting.GREEN)),
					false);
			source.sendSuccess(
					new TextComponent(" - usage: " + command.getUsage()).withStyle(ChatFormatting.GRAY), false);
		}
		source.sendSuccess(
				new TextComponent("Use /otg help <page> for more commands.").withStyle(ChatFormatting.GOLD),
				false);
	}

	private CompletableFuture<Suggestions> suggestHelp(CommandContext<CommandSourceStack> context,
			SuggestionsBuilder builder)
	{
		return SharedSuggestionProvider.suggest(
				OTGCommandExecutor.getCommands().stream().map(BaseCommand::getName).collect(Collectors.toList()), builder);
	}
}

