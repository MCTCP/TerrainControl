package com.pg85.otg.forge.commands;

import java.util.List;

import com.pg85.otg.forge.commands.runnables.MapWriter;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class MapCommand extends BaseCommand
{
    MapCommand()
    {
        super();
        name = "map";
        usage = "map [World] [-s size] [-r rotate_angle] [-o offsetX offsetZ] [-l (add coordinate label to filename)]";
        description = "Create biome and temperature map files";
    }

    @Override
    public boolean onCommand(ICommandSender sender, List<String> args)
    {

        World world = sender.getEntityWorld();
        int size = 200;
        int offsetX = 0;
        int offsetZ = 0;
        MapWriter.Angle angle = MapWriter.Angle.d0;
        String label = "";

        for (int i = 0; i < args.size(); i++)
        {
            if (args.get(i).equals("-s"))
            {
                try  {
                    size = Integer.parseInt(args.get(i + 1));
                } catch (Exception e) {
                    sender.sendMessage(new TextComponentString(ERROR_COLOR + "Wrong size " + args.get(i + 1)));
                }
            }
            if (args.get(i).equals("-o"))
            {
                try {
                    offsetX = Integer.parseInt(args.get(i + 1));
                    offsetZ = Integer.parseInt(args.get(i + 2));
                } catch (Exception e) {
                    sender.sendMessage(new TextComponentString(ERROR_COLOR + "Wrong size " + args.get(i + 1)));
                }
            }
            if (args.get(i).equals("-r"))
            {
                try {
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
                    } else {
                        sender.sendMessage(
                                new TextComponentString(ERROR_COLOR + "Angles must be divisible by 90 degrees"));
                    }
                } catch (Exception e) {
                    sender.sendMessage(new TextComponentString(ERROR_COLOR + "Wrong angle " + args.get(i + 1)));
                }
            }
            if (args.get(i).equals("-l"))
            {
                label = "[" + offsetX + "_" + offsetZ + "]";
            }
        }

        MapWriter map = new MapWriter(world, size, angle, sender, offsetX, offsetZ, label);

        // We don't have the runnable system from bukkit, so we run it all at
        // once for
        // now. Will cause a noticeable lag spike
        map.run();

        return true;
    }
}