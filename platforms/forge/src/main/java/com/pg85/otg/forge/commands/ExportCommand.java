package com.pg85.otg.forge.commands;

import java.io.File;
import java.util.List;

import com.pg85.otg.OTG;
import com.pg85.otg.forge.util.BO3Creator;
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
        usage = "export <name> [center_block] [-a include_air] [-t include_tile_entities] [-o override]";
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
            return true;
        }
        EntityPlayerMP player = (EntityPlayerMP) sender;
        LocalSession session = ForgeWorldEdit.inst.getSession(player);
        Region selection;
        try
        {
            selection = session.getSelection(session.getSelectionWorld());
        } catch (Exception e)
        {
            selection = null;
        }

        if (selection == null)
        {
            sender.sendMessage(new TextComponentString(ERROR_COLOR + "No WorldEdit selection found."));
            return true;
        }

        String bo3Name = args.get(0);
        File target = new File(OTG.getEngine().getGlobalObjectsDirectory(), bo3Name + ".bo3");

        if (target.exists() && !args.contains("-o"))
        {
            sender.sendMessage(
                    new TextComponentString(ERROR_COLOR + "A BO3 with that name already exists, use -o to override."));
            return true;
        }

        BO3Creator creator = new BO3Creator(bo3Name);

        creator.includeAir(args.contains("-a"));
        creator.includeTiles(args.contains("-t"));

        String block = args.size() > 1 ? args.get(1) : "";
        boolean branch = selection.getWidth() > 32 || selection.getLength() > 32;

        creator.create(selection, sender.getEntityWorld(), block, branch);

        sender.sendMessage(new TextComponentString(String.format(
                "%sBO3 %s%s %2$s(%dx%dx%d) %1$shas been saved to GlobalObjects.", MESSAGE_COLOR, VALUE_COLOR, bo3Name,
                selection.getWidth(), selection.getHeight(), selection.getLength())));

        if (branch)
            sender.sendMessage(
                    new TextComponentString(MESSAGE_COLOR + "BO3 was larger than 32x32 so it has been split into branches."));

        return true;
    }

}
