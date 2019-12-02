package com.pg85.otg.forge.commands;

import java.util.List;

import com.pg85.otg.OTG;
import com.pg85.otg.forge.ForgeWorld;
import com.pg85.otg.logging.LogMarker;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.TextComponentString;

public class FlushCommand extends BaseCommand
{
    FlushCommand()
    {
        name = "flushcache";
        usage = "flushcache";
        description = "Unloads all loaded object files, use this to refresh objects after editing them. Also flushes chunk generator cache to free up memory.";
    }

    @Override
    public boolean onCommand(ICommandSender sender, List<String> args)
    {
        ForgeWorld world = (ForgeWorld) this.getWorld(sender, "");

        OTG.log(LogMarker.INFO, "Clearing caches");
        OTG.log(LogMarker.INFO, "Unloading BO3's");
        OTG.getEngine().getCustomObjectManager().reloadCustomObjectFiles();
        OTG.log(LogMarker.INFO, "BO3's unloaded");
        sender.sendMessage(new TextComponentString("Objects unloaded."));
        OTG.log(LogMarker.INFO, "Clearing chunkgenerator cache");
        world.getChunkGenerator().clearChunkCache(false);
        OTG.log(LogMarker.INFO, "Caches cleared");
        return true;
    }
}