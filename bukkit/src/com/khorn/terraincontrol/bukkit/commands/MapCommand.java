package com.khorn.terraincontrol.bukkit.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.craftbukkit.v1_4_6.CraftWorld;
import org.bukkit.entity.Player;

import com.khorn.terraincontrol.bukkit.MapWriter;
import com.khorn.terraincontrol.bukkit.TCPerm;
import com.khorn.terraincontrol.bukkit.TCPlugin;

public class MapCommand extends BaseCommand
{
    public MapCommand(TCPlugin _plugin)
    {
        super(_plugin);
        name = "map";
        perm = TCPerm.CMD_MAP.node;
        usage = "map [World] [-s size] [-r rotate_angle] [-o offsetX offsetZ] [-l (add coordinate label to filename)]";
        workOnConsole = true;
    }

    @Override
    public boolean onCommand(CommandSender sender, List<String> args)
    {
        CraftWorld world = null;
        int size = 200;
        int offsetX = 0;
        int offsetZ = 0;
        MapWriter.Angle angle = MapWriter.Angle.d0;
        String label = "";

        if (args.size() != 0 && !args.get(0).startsWith("-"))
        {
            world = (CraftWorld) Bukkit.getWorld(args.get(0));
            args.remove(0);
        }

        if (world == null)
        {
            if (sender instanceof ConsoleCommandSender)
            {
                sender.sendMessage(ErrorColor + "You need to select world");
                return true;
            }
            world = (CraftWorld) ((Player) sender).getWorld();
            Player player = (Player) sender;
            offsetX = (int) player.getLocation().getX();
            offsetZ = (int) player.getLocation().getZ();
        }

        for (int i = 0; i < args.size(); i++)
        {
            if (args.get(i).equals("-s"))
            {
                try
                {
                    size = Integer.parseInt(args.get(i + 1));
                } catch (Exception e)
                {
                    sender.sendMessage(ErrorColor + "Wrong size " + args.get(i + 1));
                }
            }
            if (args.get(i).equals("-o"))
            {
                try
                {
                    offsetX = Integer.parseInt(args.get(i + 1));
                    offsetZ = Integer.parseInt(args.get(i + 2));
                } catch (Exception e)
                {
                    sender.sendMessage(ErrorColor + "Wrong size " + args.get(i + 1));
                }
            }
            if (args.get(i).equals("-r"))
            {
                try
                {
                    int degrees = Integer.parseInt(args.get(i + 1));
                    if (degrees % 90 == 0)
                    {
                        switch (degrees)
                        {
                            case 90:
                                angle = MapWriter.Angle.d90;
                                break;
                            case 180:
                                angle = MapWriter.Angle.d180;
                                break;
                            case 270:
                                angle = MapWriter.Angle.d270;
                                break;
                        }
                    } else
                    {
                        sender.sendMessage(ErrorColor + "Angles must be divisible by 90 degrees");
                    }
                } catch (Exception e)
                {
                    sender.sendMessage(ErrorColor + "Wrong angle " + args.get(i + 1));
                }
            }
            if (args.get(i).equals("-l"))
            {
                label = "[" + offsetX + "_" + offsetZ + "]";
            }
        }


        MapWriter map = new MapWriter(this.plugin, world.getHandle(), size, angle, sender, offsetX, offsetZ, label);

        this.plugin.getServer().getScheduler().scheduleAsyncDelayedTask(this.plugin, map);

        return true;
    }
}