package com.pg85.otg.forge.commands;

import java.util.ArrayList;
import java.util.List;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.customobjects.bo4.BO4;
import com.pg85.otg.logging.LogMarker;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.TextComponentString;

public class ExportDataCommand extends BaseCommand
{
    ExportDataCommand()
    {
        name = "exportBO4Data";
        usage = "exportBO4Data";
        description = "Exports all BO4 files and BO3 files that have isOTGPlus:true as BO4Data files (if none exist already). BO4Data files can significantly reduce filesize and loading times, and should be used by OTG content creators when packaging presets for players.";
    }

    @Override
    public boolean onCommand(ICommandSender sender, List<String> args)
    {
        LocalWorld world = this.getWorld(sender, "");

        sender.sendMessage(
                new TextComponentString(MESSAGE_COLOR + "Exporting .BO4Data files for world, this may take a while."));
        ArrayList<BO4> bo4s = OTG.getEngine().getCustomObjectManager().getGlobalObjects().getAllBO4sForWorld(
                world.getName());

        int i = 0;
        for (BO4 bo4 : bo4s)
        {
            i++;
            OTG.log(LogMarker.INFO, "Exporting .BO4Data " + i + "/" + bo4s.size() + " \"" + bo4.getName() + "\"");
            bo4.generateBO4Data();
        }
        sender.sendMessage(new TextComponentString(MESSAGE_COLOR + ".BO4Data export complete."));
        return true;
    }
}