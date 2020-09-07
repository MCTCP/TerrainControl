package com.pg85.otg.bukkit.commands;

import java.io.File;
import java.util.List;

import com.pg85.otg.bukkit.util.BO4Creator;
import com.pg85.otg.bukkit.util.BOCreator;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.pg85.otg.OTG;
import com.pg85.otg.bukkit.OTGPerm;
import com.pg85.otg.bukkit.OTGPlugin;
import com.pg85.otg.bukkit.util.BO3Creator;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;

public class ExportCommand extends BaseCommand
{
    private final boolean hasWorldedit;

    public ExportCommand(OTGPlugin _plugin)
    {
        super(_plugin);
        name = "export";
        perm = OTGPerm.CMD_EXPORT.node;
        usage = "export <name> [center_block] [-a include_air] [-t include_tile_entities] [-o override] [-b use branches] [-bo4]";
        hasWorldedit = Bukkit.getServer().getPluginManager().getPlugin("WorldEdit") != null;
    }

    @Override
    public boolean onCommand(CommandSender sender, List<String> args)
    {
        if (!hasWorldedit)
        {
            sender.sendMessage(ERROR_COLOR + "You must have WorldEdit installed to use this command.");
            return true;
        }

        if (args.isEmpty())
        {
            sender.sendMessage(ERROR_COLOR + "You must enter a name for the object.");
            sender.sendMessage(MESSAGE_COLOR + usage);
            return true;
        }

        Player player = (Player) sender;

        WorldEditPlugin worldEdit = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
        Selection selection = worldEdit.getSelection(player);

        if (selection == null)
        {
            sender.sendMessage(ERROR_COLOR + "No WorldEdit selection found.");
            return true;
        }

        String bo3Name = args.get(0);
        File target = new File(OTG.getEngine().getGlobalObjectsDirectory(), bo3Name + (args.contains("-bo4") ? ".BO4" : ".bo3"));

        if (target.exists() && !args.contains("-o"))
        {
            sender.sendMessage(ERROR_COLOR + "A "+ (args.contains("-bo4") ? "BO4" : "BO3") +" with that name already exists, use -o to override.");
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

        String block = args.size() > 1 && (args.get(1).charAt(0) != '-') ? args.get(1) : "";

        boolean branch;
        if (args.contains("-bo4"))
            branch = args.contains("-b") || selection.getWidth() > 16 || selection.getLength() > 16;
        else
            branch = args.contains("-b") || selection.getWidth() > 32 || selection.getLength() > 32;

        creator.author(sender.getName());

        creator.create(selection, block, branch);

        sender.sendMessage(String.format(
                "%sBO%s %s%s (%dx%dx%d) %1$shas been saved to GlobalObjects.", MESSAGE_COLOR, args.contains("-bo4") ? "4" : "3", VALUE_COLOR, bo3Name,
                selection.getWidth(), selection.getHeight(), selection.getLength()));

        if (branch)
        {
            if (args.contains("-bo4")) {
                sender.sendMessage(MESSAGE_COLOR+"BO4 is larger than 16x16 so it has been split into branches.");
            } else {
                sender.sendMessage(MESSAGE_COLOR+"BO3 is larger than 32x32 so it has been split into branches.");
            }
        }

        return true;
    }
}
