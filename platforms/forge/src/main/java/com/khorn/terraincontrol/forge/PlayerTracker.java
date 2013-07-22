package com.khorn.terraincontrol.forge;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.configuration.TCDefaultValues;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.forge.util.WorldHelper;
import cpw.mods.fml.common.IPlayerTracker;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.packet.Packet250CustomPayload;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PlayerTracker implements IPlayerTracker
{

    TCPlugin plugin;

    public PlayerTracker(TCPlugin plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public void onPlayerLogin(EntityPlayer player)
    {
        // Server-side - called whenever a player logs in
        // I couldn't find a way to detect if the client has TerrainControl,
        // so for now the configs are sent anyway.

        // Get the config
        LocalWorld worldTC = WorldHelper.toLocalWorld(player.worldObj);

        if (worldTC == null)
        {
            // World not loaded
            return;
        }
        WorldConfig config = worldTC.getSettings();

        // Serialize it
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(outputStream);
        try
        {
            stream.writeInt(TCDefaultValues.ProtocolVersion.intValue());
            config.Serialize(stream);
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        // Make the packet
        Packet250CustomPayload packet = new Packet250CustomPayload();
        packet.channel = TCDefaultValues.ChannelName.stringValue();
        packet.data = outputStream.toByteArray();
        packet.length = outputStream.size();

        // Send the packet
        ((EntityPlayerMP) player).playerNetServerHandler.sendPacketToPlayer(packet);
        System.out.println("TerrainControl: sent config");
    }

    @Override
    public void onPlayerLogout(EntityPlayer player)
    {
        // Stub method
    }

    @Override
    public void onPlayerChangedDimension(EntityPlayer player)
    {
        // Stub method
    }

    @Override
    public void onPlayerRespawn(EntityPlayer player)
    {
        // Stub method
    }

}
