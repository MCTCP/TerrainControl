package com.khorn.terraincontrol.bukkit.commands;

import com.khorn.terraincontrol.bukkit.TCPerm;
import com.khorn.terraincontrol.bukkit.TCPlugin;
import net.minecraft.server.v1_5_R2.BiomeBase;
import net.minecraft.server.v1_5_R2.WorldChunkManager;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_5_R2.CraftWorld;
import org.bukkit.entity.Player;

import java.util.List;


public class BiomeCommand extends BaseCommand
{
    public BiomeCommand(TCPlugin _plugin)
    {
        super(_plugin);
        name = "biome";
        perm = TCPerm.CMD_BIOME.node;
        usage = "biome [-f]";
        workOnConsole = false;
    }

    @Override
    public boolean onCommand(CommandSender sender, List<String> args)
    {
        Player player = (Player) sender;

        Chunk chunk = player.getWorld().getChunkAt(player.getLocation());

        player.sendMessage(ChatColor.AQUA.toString() + "You are in: ");
        WorldChunkManager biomeManager = ((CraftWorld) player.getLocation().getWorld()).getHandle().getWorldChunkManager();

        player.sendMessage(VALUE_COLOR + biomeManager.getBiome(chunk.getX() * 16 + 16, chunk.getZ() * 16 + 16).y + MESSAGE_COLOR + " chunk biome!");

        if (args.size() == 1 && args.get(0).equals("-f"))
        {
            BiomeBase[] biome = new BiomeBase[1];
            float[] temp = new float[1];
            float[] humidity = new float[1];

            biomeManager.getBiomeBlock(biome, (int) player.getLocation().getX(), (int) player.getLocation().getZ(), 1, 1);
            biomeManager.getTemperatures(temp, (int) player.getLocation().getX(), (int) player.getLocation().getZ(), 1, 1);
            biomeManager.getWetness(humidity, (int) player.getLocation().getX(), (int) player.getLocation().getZ(), 1, 1);


            player.sendMessage(VALUE_COLOR + biome[0].y + MESSAGE_COLOR + " block biome!");
            player.sendMessage(VALUE_COLOR + humidity[0] + MESSAGE_COLOR + " block humidity!");
            //double notchTemp = biomeManager.temperature[0] - (((CraftWorld) player.getLocation().getWorld()).getHandle().e((int) player.getLocation().getX(), (int) player.getLocation().getZ()) - 64) / 64.0D * 0.3D;
            player.sendMessage(VALUE_COLOR + temp[0] + MESSAGE_COLOR + " block temperature!");
            //player.sendMessage(ValueColor + notchTemp + MessageColor + " block temperature with height constant!");
        }

        return true;
    }
}