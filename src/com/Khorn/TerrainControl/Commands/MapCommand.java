package com.Khorn.TerrainControl.Commands;

import com.Khorn.TerrainControl.Configuration.WorldConfig;
import com.Khorn.TerrainControl.TCPlugin;
import com.Khorn.TerrainControl.Util.MapWriter;
import net.minecraft.server.World;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Player;

import java.util.List;

public class MapCommand extends BaseCommand
{
    public MapCommand(TCPlugin _plugin)
    {
        super(_plugin);
        name = "map";
        usage = "/tc map";
        help = "Create biome map ";
        workOnConsole = false;
    }

    @Override
    public boolean onCommand(CommandSender sender, List<String> args)
    {

        World world = ((CraftWorld) ((Player) sender).getWorld()).getHandle();

        MapWriter.GenerateMaps(this.plugin, world, 400, 400);

        sender.sendMessage(MessageColor + "Done!");
        return true;
    }
}
