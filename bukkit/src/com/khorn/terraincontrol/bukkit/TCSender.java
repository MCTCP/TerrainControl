package com.khorn.terraincontrol.bukkit;

import com.khorn.terraincontrol.configuration.TCDefaultValues;
import com.khorn.terraincontrol.configuration.WorldConfig;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TCSender implements Runnable
{
    private Player player;
    private TCPlugin plugin;

    public TCSender(TCPlugin plugin, Player player)
    {
        this.plugin = plugin;
        this.player = player;
    }

    @Override
    public void run()
    {
        if (!player.getListeningPluginChannels().contains(TCDefaultValues.ChannelName.stringValue()))
        {
            // Player doesn't have TC, abort.
            // System.out.println("TerrainControl: player joined without TerrainControl"); // debug
            return;
        }

        // Send the configs
        World world = player.getWorld();

        if (plugin.worlds.containsKey(world.getUID()))
        {
            WorldConfig config = plugin.worlds.get(world.getUID()).getSettings();

            // System.out.println("TerrainControl: config sent to player for world " + config.WorldName); //debug
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            DataOutputStream stream = new DataOutputStream(outputStream);

            try
            {
                stream.writeInt(TCDefaultValues.ProtocolVersion.intValue());
                config.Serialize(stream);
                stream.flush();

            } catch (IOException e)
            {
                e.printStackTrace();
            }

            byte[] data = outputStream.toByteArray();

            player.sendPluginMessage(plugin, TCDefaultValues.ChannelName.stringValue(), data);
        }
    }

}
