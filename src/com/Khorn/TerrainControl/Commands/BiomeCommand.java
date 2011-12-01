package com.Khorn.TerrainControl.Commands;

import com.Khorn.TerrainControl.TCPlugin;
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
    public BiomeCommand(TCPlugin _plugin)
    {
        super(_plugin);
        name = "biome";
        usage = "/tc biome [-f]";
        help = "Show current chunk biome and block stats";
        workOnConsole = false;

    }

    @Override
    public boolean onCommand(CommandSender sender, List<String> args)
    {
        Player player = (Player) sender;

        Chunk chunk = player.getWorld().getChunkAt(player.getLocation());

        player.sendMessage(ChatColor.AQUA.toString() + "You are in: ");
        WorldChunkManager biomeManager = ((CraftWorld) player.getLocation().getWorld()).getHandle().getWorldChunkManager();

        player.sendMessage(ValueColor + biomeManager.getBiome(chunk.getX() * 16 + 16, chunk.getZ() * 16 + 16).r + MessageColor + " chunk biome!");

        if (args.size() == 1 && args.get(0).equals("-f"))
        {
            BiomeBase[] biome = new BiomeBase[1];
            float[] temp = new float[1];
            float[] humidity = new float[1];

            biomeManager.a(biome, (int) player.getLocation().getX(), (int) player.getLocation().getZ(), 1, 1);
            biomeManager.getTemperatures(temp, (int) player.getLocation().getX(), (int) player.getLocation().getZ(), 1, 1);
            biomeManager.getWetness(humidity, (int) player.getLocation().getX(), (int) player.getLocation().getZ(), 1, 1);


            player.sendMessage(ValueColor + biome[0].r + MessageColor + " block biome!");
            player.sendMessage(ValueColor + humidity[0] + MessageColor + " block humidity!");
            //double notchTemp = biomeManager.temperature[0] - (((CraftWorld) player.getLocation().getWorld()).getHandle().e((int) player.getLocation().getX(), (int) player.getLocation().getZ()) - 64) / 64.0D * 0.3D;
            player.sendMessage(ValueColor + temp[0] + MessageColor + " block temperature!");
            //player.sendMessage(ValueColor + notchTemp + MessageColor + " block temperature with height constant!");

        }


        return true;
    }
}
