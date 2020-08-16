package com.pg85.otg.forge.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

public final class OTGCommandHandler implements ICommand
{
    private final List<String> aliases = Arrays.asList("otg");

    private static HashMap<String, BaseCommand> commandHashMap = new HashMap<String, BaseCommand>();
    private BaseCommand helpCommand;

    public OTGCommandHandler()
    {
        this.helpCommand = new HelpCommand();
        this.registerCommands();
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] argString)
    {
        if (!sender.getEntityWorld().isRemote)
        {
            ArrayList<String> arg = new ArrayList<String>(Arrays.asList(argString));

            BaseCommand cmd = helpCommand;
            if (!arg.isEmpty() && commandHashMap.containsKey(arg.get(0).toLowerCase()))
            {
                cmd = commandHashMap.get(arg.get(0).toLowerCase());
                arg.remove(0);
            }

            if (!sender.canUseCommand(2, "openterraingenerator.command."+cmd.name) && cmd.needsOp)
            {
                sender.sendMessage(
                        new TextComponentString(BaseCommand.ERROR_COLOR + "You do not have permission to use that command."));
            } else {
                if (!cmd.onCommand(sender, arg))
                {
                    sender.sendMessage(new TextComponentString("Usage: /otg " + cmd.usage));
                }
            }
        }
    }

    private void registerCommands()
    {
        this.addCommand(this.helpCommand);
        this.addCommand(new BiomeCommand());
        this.addCommand(new TPCommand());
        this.addCommand(new LookupCommand());
        this.addCommand(new SpawnCommand());
        this.addCommand(new ExportCommand());
        this.addCommand(new MapCommand());
        this.addCommand(new StructureCommand());
        this.addCommand(new FlushCommand());
        this.addCommand(new ExportDataCommand());
        this.addCommand(new ModsCommand());
        this.addCommand(new BiomesCommand());
        this.addCommand(new BlocksCommand());
        this.addCommand(new ParticlesCommand());
        this.addCommand(new EntitiesCommand());
        this.addCommand(new ModDataCommand());
        this.addCommand(new TimeCommand());
        this.addCommand(new WeatherCommand());
    }

    private void addCommand(BaseCommand command)
    {
        OTGCommandHandler.commandHashMap.put(command.name.toLowerCase(), command);
    }

    public static Collection<BaseCommand> getAllCommands()
    {
        return commandHashMap.values();
    }

    @Override
    public boolean isUsernameIndex(String[] var1, int var2)
    {
        return false;
    }

    @Override
    public String getName()
    {
        return "otg";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "otg";
    }

    @Override
    public List<String> getAliases()
    {
        return aliases;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos)
    {
        if (args.length == 1)
        {
            List<String> commands = new ArrayList<String>();

            for (BaseCommand command : commandHashMap.values())
            {
                commands.add(command.name);
            }

            return commands;
        }
        return new ArrayList<String>();
    }

    @Override
    public int compareTo(ICommand o)
    {
        return 0;
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender)
    {
        return true;
    }
}
