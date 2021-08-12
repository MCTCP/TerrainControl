package com.pg85.otg.spigot.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;

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
	public boolean execute(CommandSender sender, String[] args)
	{
		if (args.length >= 1)
		{
			String cmd = args[0];

			if (StringUtils.isNumeric(cmd))
			{
				showHelp(sender, Integer.parseInt(cmd));
			} else
			{

				OTGCommandExecutor.getAllCommands().stream().filter(basecmd -> basecmd.getName().equalsIgnoreCase(cmd))
						.findFirst().ifPresent(command ->
						{
							TextComponent commandName = new TextComponent("/otg " + command.getName() + ": ");
							commandName.setColor(ChatColor.GOLD);

							TextComponent helpMsg = new TextComponent(command.getHelpMessage());
							helpMsg.setColor(ChatColor.GREEN);

							sender.spigot().sendMessage(new ComponentBuilder(commandName).append(helpMsg).create());

							for (String help : command.getDetailedHelp())
							{
								helpMsg = new TextComponent(help);
								helpMsg.setColor(ChatColor.GRAY);

								sender.spigot().sendMessage(helpMsg);
							}
						});
			}
		} else
		{
			showHelp(sender, 1);
		}
		return true;
	}
	
	private void showHelp(CommandSender sender, int page)
	{
		int start = (page - 1) * 5;

		List<BaseCommand> commands = new ArrayList<>(OTGCommandExecutor.getAllCommands());

		TextComponent component;
		for (int i = start; i < commands.size() && i < start + 5; i++)
		{
			BaseCommand command = commands.get(i);

			component = new TextComponent("/otg " + command.getName() + ": ");
			component.setColor(ChatColor.GOLD);

			TextComponent helpMsg = new TextComponent(command.getHelpMessage());
			helpMsg.setColor(ChatColor.GREEN);

			sender.spigot().sendMessage(new ComponentBuilder(component).append(helpMsg).create());
			
			component = new TextComponent(" - usage: " + command.getUsage());
			component.setColor(ChatColor.GRAY);
			
			sender.spigot().sendMessage(component);
		}
		component = new TextComponent("Use /otg help <page> for more commands.");
		component.setColor(ChatColor.GOLD);
		
		sender.spigot().sendMessage(component);
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args)
	{
		List<String> commands = new ArrayList<>();
		if (args.length == 2)
		{
			StringUtil.copyPartialMatches(args[1],
					OTGCommandExecutor.getAllCommands().stream().map(BaseCommand::getName).collect(Collectors.toList()),
					commands);
		}

		return commands;
	}

}
