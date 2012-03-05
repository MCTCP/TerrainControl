package com.Khorn.TerrainControl.Bukkit.Commands;

import com.Khorn.TerrainControl.Bukkit.TCPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class TCCommandExecutor implements CommandExecutor
{
    private final TCPlugin plugin;
    private HashMap<String, BaseCommand> commandHashMap = new HashMap<String, BaseCommand>();
    private HelpCommand helpCommand;

    public TCCommandExecutor(TCPlugin plugin)
    {
        this.plugin = plugin;
        this.helpCommand = new HelpCommand(plugin);
        this.RegisterCommands();
    }

    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings)
    {

        if (!commandSender.isOp())
        {
            commandSender.sendMessage(ChatColor.RED.toString() + "You do not have permission to use this command!");
            return true;
        }
        
        ArrayList<String> arg = new ArrayList<String>(Arrays.asList(strings));

        if (arg.size() == 0)
        {
            return helpCommand.onCommand(commandSender, arg);
        }
        
        BaseCommand baseCommand = commandHashMap.get(arg.get(0));
        if (baseCommand == null)
        {
            return helpCommand.onCommand(commandSender, arg);
        }
        
        arg.remove(0);

        return baseCommand.onCommand(commandSender, arg);
    }

    private void RegisterCommands()
    {
        this.AddCommand(new ReloadCommand(plugin));
        this.AddCommand(new ListCommand(plugin));
        this.AddCommand(new CheckCommand(plugin));
        this.AddCommand(new BiomeCommand(plugin));
        this.AddCommand(new SpawnCommand(plugin));
        this.AddCommand(new MapCommand(plugin));
        this.AddCommand(this.helpCommand);
    }

    private void AddCommand(BaseCommand command)
    {
        this.commandHashMap.put(command.name, command);
        this.helpCommand.AddHelp(command);
    }
}