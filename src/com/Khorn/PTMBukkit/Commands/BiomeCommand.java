package com.Khorn.PTMBukkit.Commands;

import com.Khorn.PTMBukkit.PTMPlugin;
import net.minecraft.server.BiomeBase;
import net.minecraft.server.WorldChunkManager;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Player;

import java.util.List;


public class BiomeCommand extends BaseCommand
{
    public BiomeCommand(PTMPlugin _plugin)
    {
        super(_plugin);
        name = "biome";
        usage = "/ptm biome [-f]";
        help = "Show current chunk biome and block stats";
        workOnConsole = false;

    }

    @Override
    public boolean onCommand(CommandSender sender, List<String> args)
    {
        Player player = (Player) sender;

        Chunk chunk = player.getWorld().getChunkAt(player.getLocation());

        player.sendMessage(ChatColor.AQUA.toString() + "You are in: ");

        player.sendMessage(ValueColor + player.getWorld().getBiome(chunk.getX() * 16 + 16, chunk.getZ() * 16 + 16).name() + MessageColor + " chunk biome!");

        if (args.size() == 1 && args.get(0).equals("-f"))
        {
            BiomeBase[] biome = new BiomeBase[1];
            WorldChunkManager biomeManager = ((CraftWorld) player.getLocation().getWorld()).getHandle().getWorldChunkManager();
            biomeManager.a(biome, (int) player.getLocation().getX(), (int) player.getLocation().getZ(), 1, 1);

            /*
            player.sendMessage(ValueColor + biome[0].n + MessageColor + " block biome!");
            player.sendMessage(ValueColor + biomeManager.rain[0] + MessageColor + " block humidity!");
            double notchTemp = biomeManager.temperature[0] - (((CraftWorld) player.getLocation().getWorld()).getHandle().e((int) player.getLocation().getX(), (int) player.getLocation().getZ()) - 64) / 64.0D * 0.3D;
            player.sendMessage(ValueColor + biomeManager.temperature[0] + MessageColor + " block temperature!");
            player.sendMessage(ValueColor + notchTemp + MessageColor + " block temperature with height constant!");
            */
        }


        return true;
    }
}
