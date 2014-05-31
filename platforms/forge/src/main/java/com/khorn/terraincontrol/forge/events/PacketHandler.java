package com.khorn.terraincontrol.forge.events;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.ConfigFile;
import com.khorn.terraincontrol.configuration.WorldSettings;
import com.khorn.terraincontrol.configuration.standard.PluginStandardValues;
import com.khorn.terraincontrol.forge.ForgeWorld;
import com.khorn.terraincontrol.logging.LogMarker;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ClientCustomPacketEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ServerCustomPacketEvent;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import java.io.DataInputStream;
import java.util.Arrays;

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
            int clientProtocolVersion = PluginStandardValues.ProtocolVersion;
            if (serverProtocolVersion == clientProtocolVersion)
            {
                // Server sent config

                // Restore old biomes
                ForgeWorld.restoreBiomes();

                if (stream.readableBytes() > 4)
                {
                    // If the packet wasn't empty, add the new biomes
                    WorldClient worldMC = FMLClientHandler.instance().getClient().theWorld;

                    DataInputStream wrappedStream = new DataInputStream(new ByteBufInputStream(stream));
                    String worldName = ConfigFile.readStringFromStream(wrappedStream);
                    ForgeWorld worldTC = new ForgeWorld(worldName);
                    WorldSettings config = new WorldSettings(wrappedStream, worldTC);
                    wrappedStream.close();

                    worldTC.InitM(worldMC, config);
                    TerrainControl.log(LogMarker.INFO, Arrays.toString(stream.array()));
                }

                TerrainControl.log(LogMarker.INFO, "Config received from server");
            } else
            {
                // Server or client is outdated
                if (serverProtocolVersion > PluginStandardValues.ProtocolVersion)
                {
                    sendMessage(EnumChatFormatting.GREEN, "The server is running a newer version of " + PluginStandardValues.PLUGIN_NAME
                            + ". Please update!");
                } else
                {
                    sendMessage(EnumChatFormatting.YELLOW, "The server is running an outdated version of "
                            + PluginStandardValues.PLUGIN_NAME + ". Cannot load custom biome colors and weather.");
                }
                TerrainControl.log(LogMarker.WARN, "Server has different protocol version. Client: {} Server: {}",
                        PluginStandardValues.ProtocolVersion, serverProtocolVersion);
            }
        } catch (Exception e)
        {
            sendMessage(EnumChatFormatting.RED, "Error receiving packet.");
            TerrainControl.log(LogMarker.FATAL, "Failed to receive packet");
            TerrainControl.printStackTrace(LogMarker.FATAL, e);
            TerrainControl.log(LogMarker.FATAL, "Packet contents: {}", Arrays.toString(stream.array()));
        }
    }

    /**
     * Sends a message that will be displayed ingame.
     * @param color The color of the message.
     * @param message The message to send.
     */
    private void sendMessage(EnumChatFormatting color, String message)
    {
        IChatComponent chat = new ChatComponentText(PluginStandardValues.PLUGIN_NAME + ": " + message);

        ChatStyle chatStyle = new ChatStyle();
        chatStyle.setColor(color);
        chat.setChatStyle(chatStyle);

        Minecraft.getMinecraft().thePlayer.addChatMessage(chat);
    }

}
