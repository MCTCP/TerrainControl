package com.khorn.terraincontrol.forge.events;

import java.io.DataOutput;
import java.io.IOException;

import com.google.common.base.Preconditions;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.ConfigProvider;
import com.khorn.terraincontrol.configuration.ConfigToNetworkSender;
import com.khorn.terraincontrol.configuration.standard.PluginStandardValues;
import com.khorn.terraincontrol.forge.WorldLoader;
import com.khorn.terraincontrol.logging.LogMarker;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketCustomPayload;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

public class PlayerTracker
{

    private final WorldLoader worldLoader;

    public PlayerTracker(WorldLoader worldLoader)
    {
        this.worldLoader = Preconditions.checkNotNull(worldLoader);
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event)
    {
        // Server-side - called whenever a player logs in
        // I couldn't find a way to detect if the client has TerrainControl,
        // so for now the configs are sent anyway.

        // Get the config
        if (!(event.player instanceof EntityPlayerMP))
        {
            return;
        }

        EntityPlayerMP player = (EntityPlayerMP) event.player;

        LocalWorld worldTC = this.worldLoader.getWorld(player.getEntityWorld());
        if (worldTC == null)
        {
            // World not loaded
            return;
        }
        ConfigProvider configs = worldTC.getConfigs();

        // Serialize it
        ByteBuf nettyBuffer = Unpooled.buffer();
        PacketBuffer mojangBuffer = new PacketBuffer(nettyBuffer);

        DataOutput stream = new ByteBufOutputStream(nettyBuffer);
        try
        {
            stream.writeInt(PluginStandardValues.ProtocolVersion);
            ConfigToNetworkSender.send(configs, stream);
        } catch (IOException e)
        {
            TerrainControl.printStackTrace(LogMarker.FATAL, e);
        }

        // Make the packet
        SPacketCustomPayload packet = new SPacketCustomPayload(PluginStandardValues.ChannelName, mojangBuffer);

        // Send the packet
        player.connection.sendPacket(packet);
    }

}
