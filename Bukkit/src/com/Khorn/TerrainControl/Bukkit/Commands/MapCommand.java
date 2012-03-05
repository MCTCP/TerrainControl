package com.Khorn.TerrainControl.Bukkit.Commands;

import com.Khorn.TerrainControl.Bukkit.MapWriter;
import com.Khorn.TerrainControl.Bukkit.TCPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Player;

import java.util.List;

public class MapCommand extends BaseCommand
{
    public MapCommand(TCPlugin _plugin)
    {
        super(_plugin);
        name = "map";
        usage = "/tc map [World] [-s size] [-r rotate_angle]";
        help = "Create biome map with width and height size in chunks";
        workOnConsole = true;
    }

    @Override
    public boolean onCommand(CommandSender sender, List<String> args)
    {
        CraftWorld world = null;
        int size = 200;
        MapWriter.Angle angle = MapWriter.Angle.d0;

        if (args.size() != 0 && !args.get(0).equals("-s"))
        {
            world = (CraftWorld) Bukkit.getWorld(args.get(0));
            args.remove(0);
            if (world == null)
            {
                sender.sendMessage(ErrorColor + "You need to select world");
                return true;
            }
        }
        
        if (world == null)
        {
            if (sender instanceof ConsoleCommandSender)
            {
                sender.sendMessage(ErrorColor + "You need to select world");
                return true;
            }
            world = (CraftWorld) ((Player) sender).getWorld();
        }
            

        if (args.size() == 2 && args.get(0).equals("-s"))
        {
            try
            {
                size = Integer.parseInt(args.get(1));
                args.remove(0);
                args.remove(0);
            }
            catch (Exception e)
            {
                sender.sendMessage(ErrorColor + "Wrong size " + args.get(1));
            }
        }
            
        if (args.size() == 2 && args.get(0).equals("-r"))
        {
            try
            {
                int degrees = Integer.parseInt(args.get(1));
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
                }
                else
                {
                    sender.sendMessage(ErrorColor + "Angles must be divisible by 90 degrees");
                }
            }
            catch (Exception e)
            {
                sender.sendMessage(ErrorColor + "Wrong angle " + args.get(1));
            }
        }

        MapWriter map = new MapWriter(this.plugin, world.getHandle(), size, angle, sender);

        this.plugin.getServer().getScheduler().scheduleAsyncDelayedTask(this.plugin, map);

        return true;
    }
}