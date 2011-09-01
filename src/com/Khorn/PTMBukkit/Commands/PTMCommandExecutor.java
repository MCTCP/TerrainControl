package com.Khorn.PTMBukkit.Commands;

import com.Khorn.PTMBukkit.CustomObjects.CustomObject;
import com.Khorn.PTMBukkit.PTMPlugin;
import com.Khorn.PTMBukkit.Settings;
import net.minecraft.server.BiomeBase;
import net.minecraft.server.WorldChunkManager;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class PTMCommandExecutor implements CommandExecutor
{
    private final PTMPlugin plugin;
    private HashMap<String, BaseCommand> commandHashMap = new HashMap<String, BaseCommand>();
    private HelpCommand helpCommand;

    public PTMCommandExecutor(PTMPlugin plugin)
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

        if(arg.size() == 0)
        {
            return helpCommand.onCommand(commandSender,arg);
        }
        BaseCommand baseCommand = commandHashMap.get(arg.get(0));
        if(baseCommand == null)
             return helpCommand.onCommand(commandSender,arg);
        arg.remove(0);

        return baseCommand.onCommand(commandSender,arg);
    }

    private void RegisterCommands()
    {
       this.AddCommand(new ReloadCommand(plugin));
        this.AddCommand(new ListCommand(plugin));
        this.AddCommand(new CheckCommand(plugin));
        this.AddCommand(new BiomeCommand(plugin));
        this.AddCommand(this.helpCommand);
    }

    private void AddCommand(BaseCommand command)
    {
        this.commandHashMap.put(command.name, command);
        this.helpCommand.AddHelp(command);
    }
}
