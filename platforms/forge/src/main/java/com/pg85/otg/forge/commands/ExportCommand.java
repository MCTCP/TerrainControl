package com.pg85.otg.forge.commands;

import java.io.File;
import java.util.List;

import com.pg85.otg.OTG;
import com.pg85.otg.forge.util.BO3Creator;
import com.pg85.otg.forge.util.BO4Creator;
import com.pg85.otg.forge.util.BOCreator;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.forge.ForgeWorldEdit;
import com.sk89q.worldedit.regions.Region;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.Loader;

public class ExportCommand extends BaseCommand
{
    private final boolean hasWorldedit;

    public ExportCommand()
    {
        super();
        name = "export";
        usage = "export <name> [center_block] [-a include_air] [-t include_tile_entities] [-o override] [-bo4] [-b use branches]";
        hasWorldedit = Loader.isModLoaded("worldedit");
    }

    @Override
    public boolean onCommand(ICommandSender sender, List<String> args)
    {
        if (!hasWorldedit)
        {
            sender.sendMessage(
                    new TextComponentString(ERROR_COLOR + "You must have WorldEdit installed to use this command."));
            return true;
        }

        if (args.isEmpty())
        {
            sender.sendMessage(new TextComponentString(ERROR_COLOR + "You must enter a name for the object."));
            sender.sendMessage(new TextComponentString(MESSAGE_COLOR + usage));
            return true;
        }
        
        EntityPlayerMP player = (EntityPlayerMP) sender;
        LocalSession session = ForgeWorldEdit.inst.getSession(player);
        
        Region selection;
        try
        {
            selection = session.getSelection(session.getSelectionWorld());
        }
        catch (Exception e)
        {
            selection = null;
        }

        if (selection == null)
        {
            sender.sendMessage(new TextComponentString(ERROR_COLOR + "No WorldEdit selection found."));
            return true;
        }

        String bo3Name = args.get(0);
        File target = new File(OTG.getEngine().getGlobalObjectsDirectory(), bo3Name + (args.contains("-bo4") ? ".BO4" : ".bo3"));

        if (target.exists() && !args.contains("-o"))
        {
            sender.sendMessage(
                    new TextComponentString(ERROR_COLOR + "A "+ (args.contains("-bo4") ? "BO4" : "BO3") +" with that name already exists, use -o to override."));
            return true;
        }

        BOCreator creator;
        if(args.contains("-bo4"))
        {        
        	creator = new BO4Creator(bo3Name);
        } else {
        	creator = new BO3Creator(bo3Name);
        }
        creator.includeAir(args.contains("-a"));
        creator.includeTiles(args.contains("-t"));

        String block = args.size() > 1 ? args.get(1) : "";
        boolean branch;
        if (args.contains("-bo4"))
            branch = args.contains("-b") || selection.getWidth() > 16 || selection.getLength() > 16;
        else
            branch = args.contains("-b") || selection.getWidth() > 32 || selection.getLength() > 32;

        creator.create(selection, sender.getEntityWorld(), block, branch);

        sender.sendMessage(new TextComponentString(String.format(
                "%sBO%s %s%s (%dx%dx%d) %1$shas been saved to GlobalObjects.", MESSAGE_COLOR, args.contains("-bo4") ? "4" : "3", VALUE_COLOR, bo3Name,
                selection.getWidth(), selection.getHeight(), selection.getLength())));

        if (branch)
        {
            if (args.contains("-bo4")) {
                sender.sendMessage(new TextComponentString
                        (MESSAGE_COLOR+"BO4 is larger than 16x16 so it has been split into branches."));
            } else {
                sender.sendMessage(new TextComponentString
                        (MESSAGE_COLOR+"BO3 is larger than 32x32 so it has been split into branches."));
            }
        }

        return true;
    }
}
