package com.khorn.terraincontrol.bukkit.events;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.bukkit.TCPlugin;
import com.khorn.terraincontrol.configuration.WorldSettings;
import com.khorn.terraincontrol.configuration.standard.PluginStandardValues;
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
            WorldSettings configs = plugin.worlds.get(world.getUID()).getSettings();

            TerrainControl.log(Level.FINER, "Config sent to player for world {0}", configs.worldConfig.name); //debug
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            DataOutputStream stream = new DataOutputStream(outputStream);

            try
            {
                stream.writeInt(PluginStandardValues.ProtocolVersion.intValue());
                configs.writeToStream(stream);
                stream.flush();
            } catch (IOException e)
            {
                TerrainControl.printStackTrace(Level.SEVERE, e);
            }

            byte[] data = outputStream.toByteArray();

            player.sendPluginMessage(plugin, PluginStandardValues.ChannelName.stringValue(), data);
        }
    }

}
