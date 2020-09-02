package com.pg85.otg.bukkit.commands;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.pg85.otg.bukkit.OTGPerm;
import com.pg85.otg.bukkit.OTGPlugin;
import com.pg85.otg.bukkit.world.WorldHelper;
import com.pg85.otg.common.LocalWorld;

public class StructureCommand extends BaseCommand
{
    public StructureCommand(OTGPlugin _plugin)
    {
        super(_plugin);
        name = "structure";
        perm = OTGPerm.CMD_STRUCTURE.node;
        usage = "structure";
    }

    @Override
    public boolean onCommand(CommandSender sender, List<String> args)
    {
        Player me = (Player) sender;
        LocalWorld bukkitWorld = WorldHelper.toLocalWorld(me.getWorld());

        String structureInfo = bukkitWorld.getWorldSession().getStructureInfoAt(me.getLocation().getX(), me.getLocation().getZ());

        if (structureInfo.length() > 0)
        {
            for (String messagePart : structureInfo.split("\r\n"))
            {
            	me.sendMessage(messagePart);
            }
        } else {
        	me.sendMessage("There is no structure at this location.");
        }
        return true;
    }
}