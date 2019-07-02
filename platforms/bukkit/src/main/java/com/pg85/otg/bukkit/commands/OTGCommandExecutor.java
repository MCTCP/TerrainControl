package com.pg85.otg.bukkit.commands;

import com.pg85.otg.bukkit.OTGPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class OTGCommandExecutor implements CommandExecutor
{
    private final OTGPlugin plugin;
    protected HashMap<String, BaseCommand> commandHashMap = new HashMap<String, BaseCommand>();
    private HelpCommand helpCommand;

    public OTGCommandExecutor(OTGPlugin plugin)
    {
        this.plugin = plugin;
        this.helpCommand = new HelpCommand(plugin);
        this.registerCommands();
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings)
    {
        ArrayList<String> arg = new ArrayList<String>(Arrays.asList(strings));

        BaseCommand cmd = helpCommand;
        if (!arg.isEmpty() && commandHashMap.containsKey(arg.get(0)))
        {
            cmd = commandHashMap.get(arg.get(0));
            arg.remove(0);
        }

        if (!commandSender.hasPermission(cmd.perm))
        {
            commandSender.sendMessage(ChatColor.RED.toString() + "You don't have permission to " + cmd.getHelp() + "!");
            return true;
        }

        return cmd.onCommand(commandSender, arg);
    }

    private void registerCommands()
    {
        this.addCommand(new ReloadCommand(plugin));
        this.addCommand(new CheckCommand(plugin));
        this.addCommand(new TPCommand(plugin));
        this.addCommand(new BiomeCommand(plugin));
        this.addCommand(new SpawnCommand(plugin));
        this.addCommand(new MapCommand(plugin));
        this.addCommand(this.helpCommand);
    }

    private void addCommand(BaseCommand command)
    {
        this.commandHashMap.put(command.name, command);
    }
}