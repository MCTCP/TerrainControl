package com.pg85.otg.bukkit.events;

import com.pg85.otg.LocalWorld;
import com.pg85.otg.OTG;
import com.pg85.otg.bukkit.OTGPlugin;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.network.ConfigProvider;
import com.pg85.otg.network.ConfigToNetworkSender;

import org.bukkit.World;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class OTGSender
{   
    private OTGPlugin plugin;

    public OTGSender(OTGPlugin plugin)
    {
        this.plugin = plugin;
    }

    public void send(Player player)
    {
        // Send the configs
        World world = player.getWorld();

        if (plugin.worlds.containsKey(world.getName()))
        {
        	LocalWorld localWorld = plugin.worlds.get(world.getName());
        	ConfigProvider configs = localWorld.getConfigs();
                         	
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            DataOutputStream stream = new DataOutputStream(outputStream);

            try
            {
                stream.writeInt(PluginStandardValues.ProtocolVersion);
                ConfigToNetworkSender.writeConfigsToStream(configs, stream, false);
                stream.flush();
            } catch (IOException e)
            {
                OTG.printStackTrace(LogMarker.FATAL, e);
            }

            byte[] data = outputStream.toByteArray();

            player.sendPluginMessage(plugin, PluginStandardValues.ChannelName, data);
        }
    }
}
