package com.khorn.terraincontrol.bukkit;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.TCDefaultValues;
import com.khorn.terraincontrol.configuration.WorldConfig;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;

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

        if (plugin.worlds.containsKey(world.getUID()))
        {
            WorldConfig config = plugin.worlds.get(world.getUID()).getSettings();

            TerrainControl.log(Level.FINER, "Config sent to player for world {0}", config.name); //debug
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            DataOutputStream stream = new DataOutputStream(outputStream);

            try
            {
                stream.writeInt(TCDefaultValues.ProtocolVersion.intValue());
                config.Serialize(stream);
                stream.flush();
            } catch (IOException e)
            {
                TerrainControl.log(Level.SEVERE, e.getStackTrace().toString());
            }

            byte[] data = outputStream.toByteArray();

            player.sendPluginMessage(plugin, TCDefaultValues.ChannelName.stringValue(), data);
        }
    }

}
