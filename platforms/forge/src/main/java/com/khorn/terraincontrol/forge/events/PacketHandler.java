package com.khorn.terraincontrol.forge.events;

import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.configuration.standard.PluginStandardValues;
import com.khorn.terraincontrol.forge.ForgeWorld;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.Arrays;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.util.ReportedException;

public class PacketHandler implements IPacketHandler
{

    @Override
    public void onPacketData(INetworkManager manager, Packet250CustomPayload receivedPacket, Player player)
    {
        // This method receives the TerrainControl packet with the custom biome
        // colors and weather.

        if (!receivedPacket.channel.equals(PluginStandardValues.ChannelName.stringValue()))
        {
            // Make sure that the right channel is being received
            return;
        }

        // We're on the client, receive the packet
        ByteArrayInputStream inputStream = new ByteArrayInputStream(receivedPacket.data);
        DataInputStream stream = new DataInputStream(inputStream);
        try
        {
            int serverProtocolVersion = stream.readInt();
            int clientProtocolVersion = PluginStandardValues.ProtocolVersion.intValue();
            if (serverProtocolVersion == clientProtocolVersion)
            {
                // Server sent config

                // Restore old biomes
                ForgeWorld.restoreBiomes();

                if (receivedPacket.length > 4)
                {
                    // If the packet wasn't empty, add the new biomes
                    WorldClient worldMC = FMLClientHandler.instance().getClient().theWorld;

                    ForgeWorld worldTC = new ForgeWorld("external");
                    WorldConfig config = new WorldConfig(stream, worldTC);

                    worldTC.InitM(worldMC, config);
                }

                System.out.println("TerrainControl: config received from server");
            } else
            {
                // Server or client is outdated
                System.out.println("TerrainControl: server has different protocol version! " + "Client: " + PluginStandardValues.ProtocolVersion.intValue() + " Server: " + serverProtocolVersion);
            }
        } catch (Exception e)
        {
            CrashReport crashreport = CrashReport.makeCrashReport(e, "Exception reading biome packet");
            CrashReportCategory details = crashreport.makeCategory("TerrainControl");
            details.addCrashSection("Packet length", receivedPacket.data.length);
            details.addCrashSection("Packet data", Arrays.toString(receivedPacket.data));

            throw new ReportedException(crashreport);
        }

    }

}
