package com.pg85.otg.forge.commands;

import java.util.List;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.forge.OTGPlugin;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.ChunkCoordinate;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.event.FMLInterModComms;

public class ModDataCommand extends BaseCommand
{
    ModDataCommand()
    {
        name = "getmoddata";
        usage = "getmoddata <ModName> <Radius>";
        description = "Sends any ModData() tags in BO3's within the specified <Radius> in chunks to the specified <ModName>. Some OTG mob spawning commands can be used this way. Be sure to set up ModData() tags in your BO3 to make this work.";
    }

    @Override
    public boolean onCommand(ICommandSender sender, List<String> args)
    {
        LocalWorld world = this.getWorld(sender, "");

        BlockPos pos = sender.getPosition();
        int playerX = pos.getX();
        int playerZ = pos.getZ();

        if (world == null)
        {
            sender.sendMessage(
                    new TextComponentTranslation(ERROR_COLOR + "This command is only available for OpenTerrainGenerator worlds."));
            return true;
        }

        if (args.size() == 2)
        {
            ChunkCoordinate chunkCoord = ChunkCoordinate.fromBlockCoords(playerX, playerZ);
            FMLInterModComms.sendRuntimeMessage(OTGPlugin.Instance, args.get(1), "GetModData",
                    sender.getEntityWorld().getWorldInfo().getWorldName() + "," + chunkCoord.getChunkX() + "," + chunkCoord.getChunkZ());
        }
		else if (args.size() == 3)
        {
            try {
                ChunkCoordinate chunkCoord = ChunkCoordinate.fromBlockCoords(playerX, playerZ);
                int radius = Integer.parseInt(args.get(2));
                if (radius > 50)
                {
                    radius = 50;
                    OTG.log(LogMarker.WARN,
                            "Error in GetModData call: Parameter radius can be no higher than 50. Radius was set to 50.");
                }
                for (int x = -radius; x <= radius; x++)
                {
                    for (int z = -radius; z <= radius; z++)
                    {
                        ChunkCoordinate chunkCoord2 = ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX() + x,
                                chunkCoord.getChunkZ() + z);
                        FMLInterModComms.sendRuntimeMessage(OTGPlugin.Instance, args.get(1), "GetModData",
                                sender.getEntityWorld().getWorldInfo().getWorldName() + "," + chunkCoord2.getChunkX() + "," + chunkCoord2.getChunkZ());
                    }
                }
            } catch (NumberFormatException ex) {
                OTG.log(LogMarker.WARN,
                        "Error in GetModData call: value \"" + args.get(2) + "\" was expected to be a number");
            }

        } else {
            return false;
        }
        return true;
    }
}