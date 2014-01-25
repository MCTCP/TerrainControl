package com.khorn.terraincontrol.forge;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.TCDefaultValues;
import com.khorn.terraincontrol.configuration.WorldConfig;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ClientCustomPacketEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ServerCustomPacketEvent;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import net.minecraft.client.multiplayer.WorldClient;

import java.io.DataInputStream;
import java.util.logging.Level;

public class PacketHandler
{

    @SubscribeEvent
    public void onServerPacket(ServerCustomPacketEvent event)
    {

    }

    @SubscribeEvent
    public void onClientPacket(ClientCustomPacketEvent event)
    {
        // This method receives the TerrainControl packet with the custom
        // biome colors and weather.
        
        FMLProxyPacket receivedPacket = event.packet;

        // We're on the client, receive the packet
        ByteBuf stream = receivedPacket.payload();
        try
        {
            int serverProtocolVersion = stream.readInt();
            int clientProtocolVersion = TCDefaultValues.ProtocolVersion.intValue();
            if (serverProtocolVersion == clientProtocolVersion)
            {
                // Server sent config

                // Restore old biomes
                ForgeWorld.restoreBiomes();

                if (stream.readableBytes() > 4)
                {
                    // If the packet wasn't empty, add the new biomes
                    WorldClient worldMC = FMLClientHandler.instance().getClient().theWorld;

                    ForgeWorld worldTC = new ForgeWorld("external");
                    DataInputStream wrappedStream = new DataInputStream(new ByteBufInputStream(stream));
                    WorldConfig config = new WorldConfig(wrappedStream, worldTC);
                    wrappedStream.close();

                    worldTC.InitM(worldMC, config);
                }

                System.out.println("TerrainControl: config received from server");
            } else
            {
                // Server or client is outdated
                System.out.println("TerrainControl: server has different protocol version! " + "Client: " + TCDefaultValues.ProtocolVersion.intValue() + " Server: " + serverProtocolVersion);
            }
        } catch (Exception e)
        {
            TerrainControl.log(Level.SEVERE, "Failed to receive packet");
            TerrainControl.printStackTrace(Level.SEVERE, e);
        }
    }

}
