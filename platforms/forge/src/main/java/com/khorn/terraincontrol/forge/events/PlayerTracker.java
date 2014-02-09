package com.khorn.terraincontrol.forge.events;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.WorldSettings;
import com.khorn.terraincontrol.configuration.standard.PluginStandardValues;
import com.khorn.terraincontrol.forge.TCPlugin;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import net.minecraft.network.play.server.S3FPacketCustomPayload;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;

public class PlayerTracker
{

    TCPlugin plugin;

    public PlayerTracker(TCPlugin plugin)
    {
        this.plugin = plugin;
    }

    @SubscribeEvent
    public void onPlayerLogin(ClientConnectedToServerEvent event)
    {
        // Server-side - called whenever a player logs in
        // I couldn't find a way to detect if the client has TerrainControl,
        // so for now the configs are sent anyway.

        // Get the config
        // TODO only send the configs when the player is in the main world
        LocalWorld worldTC = plugin.getWorld();

        if (worldTC == null)
        {
            // World not loaded
            return;
        }
        WorldSettings configs = worldTC.getSettings();

        // Serialize it
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(outputStream);
        try
        {
            stream.writeInt(PluginStandardValues.ProtocolVersion.intValue());
            configs.writeToStream(stream);
        } catch (IOException e)
        {
            TerrainControl.printStackTrace(Level.SEVERE, e);
        }

        // Make the packet
        S3FPacketCustomPayload packet = new S3FPacketCustomPayload(PluginStandardValues.ChannelName.stringValue(), outputStream.toByteArray());

        // Send the packet
        event.handler.handleCustomPayload(packet);
        System.out.println("TerrainControl: sent config");
    }

}
