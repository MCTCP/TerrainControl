package com.pg85.otg.forge.commands;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.storage.WorldInfo;

import java.util.List;

public class WeatherCommand extends BaseCommand {

    WeatherCommand()
    {
        name = "weather";
        usage = "weather <rain/clear/thunder> [time in seconds]";
        description = "Sets weather in the current dimension.";
        needsOp = true;
    }

    @Override
    public boolean onCommand(ICommandSender sender, List<String> args) {
        if (args.size() == 0 || args.size() > 2) {
            return false;
        }
        int time;
        if (args.size() == 1) {
            time = 0;
        }
        else {
            try {
                // multiply by 50 to get seconds
                time = Integer.parseInt(args.get(1))*50;
            } catch (NumberFormatException e) {
                sender.sendMessage(new TextComponentString(args.get(1) + " is not a number"));
                return true;
            }
        }
        WorldInfo info = sender.getEntityWorld().getWorldInfo();
        switch (args.get(0).toLowerCase())
        {
            case "clear":
                info.setRaining(false);
                info.setThundering(false);
                if (time > 0) info.setCleanWeatherTime(time);
                break;
            case "rain":
                info.setRaining(true);
                info.setThundering(false);
                if (time > 0) info.setRainTime(time);
                break;
            case "thunder":
                info.setRaining(true);
                info.setThundering(true);
                if (time > 0) info.setThunderTime(time);
                break;
            default:
                return false;
        }
        String msg = "Weather set to "+args.get(0);
        if (time > 0) msg += " for "+time/50+" seconds";
        sender.sendMessage(new TextComponentString(msg));
        return true;
    }
}
