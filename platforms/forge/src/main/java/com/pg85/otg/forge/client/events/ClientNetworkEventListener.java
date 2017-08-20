package com.pg85.otg.forge.client.events;

import com.pg85.otg.OTG;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.forge.WorldLoader;
import com.pg85.otg.logging.LogMarker;

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
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

import java.io.DataInputStream;
import java.util.Arrays;

public class ClientNetworkEventListener
{
    private final WorldLoader worldLoader;

    public ClientNetworkEventListener(WorldLoader worldLoader)
    {
        this.worldLoader = worldLoader;
    }
    
    public ClientNetworkEventListener()
    {
    	this.worldLoader = null;
	}

	// Only used when receiving packets from Spigot/Bukkit servers
    // Forge servers use a synchronous message channel for packet sending to ensure the packets with dimension and world data arrive before the world is loaded.
    // This is necessary for the multi-dimension features. See: DimensionSyncChannelHandler and PlayerTracker.onConnectionCreated()
    @SubscribeEvent
    public void onPacketReceive(ClientCustomPacketEvent event)
    {    	    	  
        // Ignore if packet was local
        if (event.getManager().isLocalChannel())
        {
            return;
        }

        // This method receives the OpenTerrainGenerator packet with the custom
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
                // Spigot server sent config
                WorldClient worldMC = FMLClientHandler.instance().getClient().world;

                if (stream.readableBytes() > 4 && worldMC != null) // TODO: If worldMC == null, there's a problem, don't just ignore the packet?
                {
                    // If the packet wasn't empty, and the client world exists:
                    // add the new biomes.
                    // (If no client world exists yet, then we're on a local
                    // server, and we can discard the packet.)

                    DataInputStream wrappedStream = new DataInputStream(new ByteBufInputStream(stream));

                    this.worldLoader.registerClientWorldBukkit(worldMC, wrappedStream);
                }
           	
                OTG.log(LogMarker.TRACE, "Config received from server");
            } else {
                // Server or client is outdated
                if (serverProtocolVersion > PluginStandardValues.ProtocolVersion)
                {
                    sendMessage(TextFormatting.GREEN,
                            "The server is running a newer version of " + PluginStandardValues.PLUGIN_NAME + ". Please update!");
                } else
                {
                    sendMessage(TextFormatting.YELLOW,
                            "The server is running an outdated version of " + PluginStandardValues.PLUGIN_NAME + ". Cannot load custom biome colors and weather.");
                }
                OTG.log(LogMarker.WARN, "Server has different protocol version. Client: {} Server: {}",
                        PluginStandardValues.ProtocolVersion, serverProtocolVersion);
            }
        } catch (Exception e)
        {
            OTG.log(LogMarker.FATAL, "Failed to receive packet");
            OTG.printStackTrace(LogMarker.FATAL, e);
            OTG.log(LogMarker.FATAL, "Packet contents: {}", Arrays.toString(stream.array()));
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

        Minecraft.getMinecraft().player.sendMessage(chat);
    }
}