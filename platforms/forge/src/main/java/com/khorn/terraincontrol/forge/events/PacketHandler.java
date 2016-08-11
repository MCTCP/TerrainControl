package com.khorn.terraincontrol.forge.events;

import java.io.DataInputStream;
import java.util.Arrays;

import com.google.common.base.Preconditions;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.standard.PluginStandardValues;
import com.khorn.terraincontrol.forge.WorldLoader;
import com.khorn.terraincontrol.logging.LogMarker;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientCustomPacketEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ServerCustomPacketEvent;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

public class PacketHandler
{
    private final WorldLoader worldLoader;

    public PacketHandler(WorldLoader worldLoader)
    {
        this.worldLoader = Preconditions.checkNotNull(worldLoader);
    }

    @SubscribeEvent
    public void onServerPacket(ServerCustomPacketEvent event)
    {

    }


    @SubscribeEvent
    public void onClientPacket(ClientCustomPacketEvent event)
    {
        // This method receives the TerrainControl packet with the custom
        // biome colors and weather.

        FMLProxyPacket receivedPacket = event.getPacket();

        // We're on the client, receive the packet
        ByteBuf stream = receivedPacket.payload();
        try
        {
            int serverProtocolVersion = stream.readInt();
            int clientProtocolVersion = PluginStandardValues.ProtocolVersion;
            if (serverProtocolVersion == clientProtocolVersion)
            {
                // Server sent config
                WorldClient worldMC = FMLClientHandler.instance().getClient().theWorld;

                if (stream.readableBytes() > 4 && worldMC != null)
                {
                    // If the packet wasn't empty, and the client world exists:
                    // add the new biomes.
                    // (If no client world exists yet, then we're on a local
                    // server, and we can discard the packet.)

                    DataInputStream wrappedStream = new DataInputStream(new ByteBufInputStream(stream));

                    worldLoader.demandClientWorld(worldMC, wrappedStream);
                }

                TerrainControl.log(LogMarker.INFO, "Config received from server");
            } else
            {
                // Server or client is outdated
                if (serverProtocolVersion > PluginStandardValues.ProtocolVersion)
                {
                    sendMessage(TextFormatting.GREEN, "The server is running a newer version of " + PluginStandardValues.PLUGIN_NAME
                            + ". Please update!");
                } else
                {
                    sendMessage(TextFormatting.YELLOW, "The server is running an outdated version of "
                            + PluginStandardValues.PLUGIN_NAME + ". Cannot load custom biome colors and weather.");
                }
                TerrainControl.log(LogMarker.WARN, "Server has different protocol version. Client: {} Server: {}",
                        PluginStandardValues.ProtocolVersion, serverProtocolVersion);
            }
        } catch (Exception e)
        {
            TerrainControl.log(LogMarker.FATAL, "Failed to receive packet");
            TerrainControl.printStackTrace(LogMarker.FATAL, e);
            TerrainControl.log(LogMarker.FATAL, "Packet contents: {}", Arrays.toString(stream.array()));
            sendMessage(TextFormatting.RED, "Error receiving packet.");
        }
    }

    /**
     * Sends a message that will be displayed ingame.
     * @param color The color of the message.
     * @param message The message to send.
     */
    private void sendMessage(TextFormatting color, String message)
    {
        ITextComponent chat = new TextComponentString(PluginStandardValues.PLUGIN_NAME + ": " + message);

        Style chatStyle = new Style();
        chatStyle.setColor(color);
        chat.setStyle(chatStyle);

        Minecraft.getMinecraft().thePlayer.addChatMessage(chat);
    }

}
