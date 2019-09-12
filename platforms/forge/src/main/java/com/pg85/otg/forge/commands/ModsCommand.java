package com.pg85.otg.forge.commands;

import java.util.List;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

public class ModsCommand extends BaseCommand
{
    ModsCommand()
    {

        name = "mods";
        usage = "mods";
        description = "View a list of loaded mods.";
    }

    @Override
    public boolean onCommand(ICommandSender sender, List<String> args)
    {

        sender.sendMessage(new TextComponentString("-- Mod List --"));
        sender.sendMessage(new TextComponentString(""));
        sender.sendMessage(new TextComponentString("The modid shown is what's required for ModChecks."));
        sender.sendMessage(new TextComponentString(""));

        for (ModContainer mod : Loader.instance().getActiveModList())
        {
            sender.sendMessage(
                    new TextComponentString(MESSAGE_COLOR + mod.getName() + "(" + mod.getDisplayVersion() + ")"));
            sender.sendMessage(new TextComponentString(MESSAGE_COLOR + "   modid: " + VALUE_COLOR + mod.getModId()));
        }
        return true;
    }

}