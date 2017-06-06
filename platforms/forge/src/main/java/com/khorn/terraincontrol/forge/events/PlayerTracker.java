package com.khorn.terraincontrol.forge.events;

import java.io.DataOutput;
import java.io.IOException;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.ConfigToNetworkSender;
import com.khorn.terraincontrol.configuration.standard.PluginStandardValues;
import com.khorn.terraincontrol.forge.DimensionData;
import com.khorn.terraincontrol.forge.ForgeEngine;
import com.khorn.terraincontrol.forge.OTGDimensionInfo;
import com.khorn.terraincontrol.forge.TXDimensionManager;
import com.khorn.terraincontrol.forge.TXPlugin;
import com.khorn.terraincontrol.forge.client.events.DimensionSyncPacket;
import com.khorn.terraincontrol.logging.LogMarker;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetworkManager;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.network.FMLOutboundHandler;
import net.minecraftforge.fml.common.network.handshake.NetworkDispatcher;
import net.minecraftforge.fml.relauncher.Side;

public class PlayerTracker
{
    @SubscribeEvent
    public void onConnectionCreated(FMLNetworkEvent.ServerConnectionFromClientEvent event)
    {
    	SendWorldAndBiomeConfigs(event.getManager(), true);
    }

    public static void SendAllWorldAndBiomeConfigsToAllPlayers(MinecraftServer server)
    {
    	for(EntityPlayerMP player : server.getPlayerList().getPlayers())
    	{
    		SendWorldAndBiomeConfigs(player.connection.netManager, server.isSinglePlayer());
    	}
    }
    
    private static void SendWorldAndBiomeConfigs(NetworkManager networkManager, boolean isSinglePlayer)
    {
        // Make sure worlds are sent in the correct order.
        
		OTGDimensionInfo otgDimData = TXDimensionManager.GetOrderedDimensionData();
		
        // Serialize it
        ByteBuf nettyBuffer = Unpooled.buffer();
        //PacketBuffer mojangBuffer = new PacketBuffer(nettyBuffer);
        DataOutput stream = new ByteBufOutputStream(nettyBuffer);
        
        try
        {
        	stream.writeInt(PluginStandardValues.ProtocolVersion);
        	stream.writeInt(otgDimData.orderedDimensions.size() + 1); // Number of worlds in this packet
        	   		
    		// Send worldconfig and biomeconfigs for each world.
        	
			LocalWorld localWorld = ((ForgeEngine)TerrainControl.getEngine()).getOverWorld();

			// Overworld (dim 0)
	        try
	        {
	        	stream.writeInt(0);
	            ConfigToNetworkSender.writeConfigsToStream(localWorld.getConfigs(), stream, false);
	        }
	        catch (IOException e)
	        {
	            TerrainControl.printStackTrace(LogMarker.FATAL, e);
	        }
        	
    		for(int i = 0; i <= otgDimData.highestOrder; i++)
    		{
    			if(otgDimData.orderedDimensions.containsKey(i))
    			{
    				DimensionData dimData = otgDimData.orderedDimensions.get(i);
    				localWorld = TerrainControl.getWorld(dimData.dimensionName);
    				if(localWorld == null)
    				{
    					localWorld = TerrainControl.getUnloadedWorld(dimData.dimensionName);
    				}
    				
    		        try
    		        {
    		        	stream.writeInt(dimData.dimensionId);
    		            ConfigToNetworkSender.writeConfigsToStream(localWorld.getConfigs(), stream, false); // TODO: localWorld is null after /otg dim -c
    		        }
    		        catch (IOException e)
    		        {
    		            TerrainControl.printStackTrace(LogMarker.FATAL, e);
    		        }
    			}
    		}
		}
        catch (IOException e1)
        {
			e1.printStackTrace();
		}
        
        // Make the packet        
    	DimensionSyncPacket packet = new DimensionSyncPacket();
    	// Send dimensions to client
    	packet.setData(nettyBuffer);
        
        TXPlugin.channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.DISPATCHER);
        TXPlugin.channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(networkManager.channel().attr(NetworkDispatcher.FML_DISPATCHER).get());
        TXPlugin.channels.get(Side.SERVER).writeOutbound(packet);
    }
}
