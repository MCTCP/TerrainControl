package com.khorn.terraincontrol.bukkit.events;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.bukkit.TXPlugin;
import com.khorn.terraincontrol.configuration.ConfigProvider;
import com.khorn.terraincontrol.configuration.ConfigToNetworkSender;
import com.khorn.terraincontrol.configuration.standard.PluginStandardValues;
import com.khorn.terraincontrol.logging.LogMarker;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TCSender
{
    
    private TXPlugin plugin;

    public TCSender(TXPlugin plugin)
    {
        this.plugin = plugin;
    }

    public void send(Player player)
    {
        // Send the configs
        World world = player.getWorld();

        if (plugin.worlds.containsKey(world.getName()))
        {
            ConfigProvider configs = plugin.worlds.get(world.getName()).getConfigs();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            DataOutputStream stream = new DataOutputStream(outputStream);

            try
            {
                stream.writeInt(PluginStandardValues.ProtocolVersion);
                ConfigToNetworkSender.send(configs, stream);
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
