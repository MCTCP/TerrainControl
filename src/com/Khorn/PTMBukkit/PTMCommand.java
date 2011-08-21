package com.Khorn.PTMBukkit;

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

public class PTMCommand implements CommandExecutor
{
    private final PTMPlugin plugin;

    public PTMCommand(PTMPlugin plugin)
    {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings)
    {
        boolean isConsole = false;

        if (commandSender instanceof ConsoleCommandSender)
            isConsole = true;
        if ((commandSender instanceof Player) || isConsole)
        {
            if (!commandSender.isOp())
            {
                commandSender.sendMessage(ChatColor.RED.toString() + "You do not have permission to use this command!");
                return true;
            }

            if (strings.length == 0)
                return this.SendUsage(commandSender);

            if (strings[0].equals("reload"))
                return this.ReloadCommand(commandSender, strings, isConsole);
            if (strings[0].equals("biome"))
                return this.BiomeCommand(commandSender, strings, isConsole);
            if(strings[0].equals("check"))
                return this.CheckCommand(commandSender,strings,isConsole);

            return this.SendUsage(commandSender);
        }

        return true;
    }

    private boolean ReloadCommand(CommandSender commandSender, String[] strings, boolean isConsole)
    {
        String worldName;

        if (strings.length == 1)
        {
            if (isConsole)
            {
                commandSender.sendMessage(ChatColor.RED.toString() + "You need to select world");
                return true;
            }

            worldName = ((Player) commandSender).getWorld().getName();
        } else
            worldName = strings[1];

        if (this.plugin.worldsSettings.containsKey(worldName))
        {
            Settings set = this.plugin.worldsSettings.get(worldName);
            this.plugin.worldsSettings.remove(worldName);

            set.newSettings = this.plugin.GetSettings(worldName);
            set.isDeprecated = true;

            commandSender.sendMessage(ChatColor.GREEN.toString() + "Settings for world " + worldName + " reloaded");
            return true;

        }
        commandSender.sendMessage(ChatColor.RED.toString() + " World " + worldName + " not found");
        return true;
    }

    private boolean BiomeCommand(CommandSender commandSender, String[] strings, boolean isConsole)
    {
        if (isConsole)
        {
            commandSender.sendMessage(ChatColor.RED.toString() + "You can't do it with console");
            return true;
        }
        Player player = (Player) commandSender;

        Chunk chunk = player.getWorld().getChunkAt(player.getLocation());

        player.sendMessage(ChatColor.AQUA.toString() + "You are in: ");

        player.sendMessage(ChatColor.DARK_GREEN.toString() + player.getWorld().getBiome(chunk.getX() * 16 + 16, chunk.getZ() * 16 + 16).name() + ChatColor.GREEN.toString() + " chunk biome!");

        if (strings.length == 2 && strings[1].equals("-f"))
        {
            BiomeBase[] biome = new BiomeBase[1];
            WorldChunkManager biomeManager = ((CraftWorld) player.getLocation().getWorld()).getHandle().getWorldChunkManager();
            biomeManager.a(biome, (int) player.getLocation().getX(), (int) player.getLocation().getZ(), 1, 1);


            player.sendMessage(ChatColor.DARK_GREEN.toString() + biome[0].n + ChatColor.GREEN.toString() + " block biome!");
            player.sendMessage(ChatColor.DARK_GREEN.toString() + biomeManager.rain[0] + ChatColor.GREEN.toString() + " block humidity!");
            double notchTemp = biomeManager.temperature[0] - (((CraftWorld) player.getLocation().getWorld()).getHandle().e((int) player.getLocation().getX(), (int) player.getLocation().getZ()) - 64) / 64.0D * 0.3D;
            player.sendMessage(ChatColor.DARK_GREEN.toString() + biomeManager.temperature[0] + ChatColor.GREEN.toString() + " block temperature!");
            player.sendMessage(ChatColor.DARK_GREEN.toString() + notchTemp + ChatColor.GREEN.toString() + " block temperature with height constant!");
        }


        return true;

    }

    private boolean CheckCommand(CommandSender commandSender, String[] strings, boolean isConsole)
    {
        String worldName;

        if (strings.length == 1)
        {
            if (isConsole)
            {
                commandSender.sendMessage(ChatColor.RED.toString() + "You need to select world");
                return true;
            }

            worldName = ((Player) commandSender).getWorld().getName();
        } else
            worldName = strings[1];

        if (this.plugin.worldsSettings.containsKey(worldName))
            commandSender.sendMessage(ChatColor.GREEN.toString() + "Ptm is enabled for " + worldName);
        else
            commandSender.sendMessage(ChatColor.GREEN.toString() + "Ptm is disabled for " + worldName);
        return true;
    }

    private boolean SendUsage(CommandSender commandSender)
    {
        commandSender.sendMessage(ChatColor.AQUA.toString() + "Available command:");
        commandSender.sendMessage(ChatColor.GREEN.toString() + "/ptm check [World] - Checks PTM is enable for this world");
        commandSender.sendMessage(ChatColor.GREEN.toString() + "/ptm biome [-f] - Show current chunk biome and block stats");
        commandSender.sendMessage(ChatColor.GREEN.toString() + "/ptm reload [World] - Reload world settings");
        return true;
    }
}
