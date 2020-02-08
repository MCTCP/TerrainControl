package com.pg85.otg.forge.commands;

import java.util.List;

import com.pg85.otg.OTG;
import com.pg85.otg.forge.world.ForgeWorld;
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

        OTG.log(LogMarker.INFO, "Unloading BO2/BO3/BO4 files");
        OTG.getEngine().getCustomObjectManager().reloadCustomObjectFiles();
        sender.sendMessage(new TextComponentString("Objects unloaded."));
        OTG.log(LogMarker.INFO, "Clearing chunkgenerator cache");
        world.getChunkGenerator().clearChunkCache();
        OTG.log(LogMarker.INFO, "Caches cleared.");
        return true;
    }
}