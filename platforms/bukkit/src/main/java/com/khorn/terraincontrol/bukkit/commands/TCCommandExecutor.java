package com.khorn.terraincontrol.bukkit.commands;

import com.khorn.terraincontrol.bukkit.TCPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class TCCommandExecutor implements CommandExecutor
{
    protected final TCPlugin plugin;
    protected HashMap<String, BaseCommand> commandHashMap = new HashMap<String, BaseCommand>();
    protected HelpCommand helpCommand;

    public TCCommandExecutor(TCPlugin plugin)
    {
        this.plugin = plugin;
        this.helpCommand = new HelpCommand(plugin);
        this.RegisterCommands();
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

    private void RegisterCommands()
    {
        this.AddCommand(new ReloadCommand(plugin));
        this.AddCommand(new ListCommand(plugin));
        this.AddCommand(new CheckCommand(plugin));
        this.AddCommand(new BiomeCommand(plugin));
        this.AddCommand(new SpawnCommand(plugin));
        this.AddCommand(new MapCommand(plugin));
        this.AddCommand(new ReplaceBiomeCommand(plugin));
        this.AddCommand(this.helpCommand);
    }

    private void AddCommand(BaseCommand command)
    {
        this.commandHashMap.put(command.name, command);
    }
}