package com.khorn.terraincontrol.bukkit.events;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.bukkit.TCPlugin;
import com.khorn.terraincontrol.configuration.WorldSettings;
import com.khorn.terraincontrol.configuration.standard.PluginStandardValues;
import com.khorn.terraincontrol.logging.LogMarker;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TCSender
{
    
    private TCPlugin plugin;

    public TCSender(TCPlugin plugin)
    {
        this.plugin = plugin;
    }

    public void send(Player player)
    {
        // Send the configs
        World world = player.getWorld();

        if (plugin.worlds.containsKey(world.getName()))
        {
            WorldSettings configs = plugin.worlds.get(world.getName()).getSettings();

            TerrainControl.log(LogMarker.TRACE, "Config sent to player for world ", configs.worldConfig.getName()); //debug
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            DataOutputStream stream = new DataOutputStream(outputStream);

            try
            {
                stream.writeInt(PluginStandardValues.ProtocolVersion);
                configs.writeToStream(stream);
                stream.flush();
            } catch (IOException e)
            {
                TerrainControl.printStackTrace(LogMarker.FATAL, e);
            }

            byte[] data = outputStream.toByteArray();

            player.sendPluginMessage(plugin, PluginStandardValues.ChannelName, data);
        }
    }

}
