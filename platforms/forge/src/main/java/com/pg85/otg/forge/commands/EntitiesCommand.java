package com.pg85.otg.forge.commands;

import java.util.List;

import com.pg85.otg.OTG;
import com.pg85.otg.logging.LogMarker;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;

public class EntitiesCommand extends BaseCommand
{
    EntitiesCommand()
    {

        name = "entities";
        usage = "entities";
        description = "View a list of entities that can be spawned inside objects using the Entity() tag.";
    }

    @Override
    public boolean onCommand(ICommandSender sender, List<String> args)
    {

        sender.sendMessage(new TextComponentString(""));
        OTG.log(LogMarker.INFO, "-- Entities List --");
        sender.sendMessage(new TextComponentString("-- Entities List --"));
        sender.sendMessage(new TextComponentString(""));
        sender.sendMessage(
                new TextComponentString(MESSAGE_COLOR + "Some of these, like ThrownPotion, FallingSand, Mob and Painting may crash the game so be sure to test your BO3 in single player."));
        sender.sendMessage(new TextComponentString(""));
        EnumCreatureType[] aenumcreaturetype = EnumCreatureType.values();
        for (ResourceLocation entry : EntityList.getEntityNameList())
        {
            if (EntityList.getClass(entry) != null)
            {
                String msg = entry.getNamespace() + ":" + entry.getPath();
                for (int k3 = 0; k3 < aenumcreaturetype.length; ++k3)
                {
                    EnumCreatureType enumcreaturetype = aenumcreaturetype[k3];
                    if (enumcreaturetype.getCreatureClass().isAssignableFrom(EntityList.getClass(entry)))
                    {
                        msg += VALUE_COLOR + " (" + enumcreaturetype.name() + ")";
                    }
                }
                OTG.log(LogMarker.INFO, msg.replace("§2", "").replace("§", "").replace("§a", ""));
                sender.sendMessage(new TextComponentString(MESSAGE_COLOR + "- " + msg));
            } else {
                // This can happen for LIGHTNING_BOLT since it appears to be
                // added to the
                // getEntityNameList list but doesn't actually have an entity
                // registered
                // TODO: Find out how lightning bolt is supposed to work and
                // make sure
                // all other entities are registered properly (including ones
                // added by other
                // mods).
            }
        }

        OTG.log(LogMarker.INFO, "----");
        return true;
    }

}