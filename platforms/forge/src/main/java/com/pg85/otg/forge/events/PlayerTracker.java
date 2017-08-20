package com.pg85.otg.forge.events;

import java.io.DataOutput;
import java.io.IOException;

import com.pg85.otg.LocalWorld;
import com.pg85.otg.OTG;
import com.pg85.otg.configuration.ConfigToNetworkSender;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.forge.ForgeEngine;
import com.pg85.otg.forge.dimensions.DimensionData;
import com.pg85.otg.forge.dimensions.OTGDimensionInfo;
import com.pg85.otg.forge.dimensions.OTGDimensionManager;
import com.pg85.otg.forge.network.DimensionSyncPacket;
import com.pg85.otg.forge.network.PacketDispatcher;
import com.pg85.otg.forge.network.ParticlesPacket;
import com.pg85.otg.logging.LogMarker;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

public class PlayerTracker
{
    @SubscribeEvent
    public void onConnectionCreated(FMLNetworkEvent.ServerConnectionFromClientEvent event)
    {   	
		ByteBuf nettyBuffer = createWorldAndBiomeConfigsPacket();
		if(nettyBuffer != null)
		{
			PacketDispatcher.sendTo(new DimensionSyncPacket(nettyBuffer), event.getManager());
	    	// Reset particles in case the player just switched worlds.
	    	PacketDispatcher.sendTo(new ParticlesPacket(), event.getManager());
		} else {
			OTG.log(LogMarker.WARN, "Could not find an OTG overworld, OTG is disabled. It is currently not possible to use OTG with a non-OTG overworld. To enable OTG make sure that level-type=OTG is configured in the server.properties file.");
		}
    }

    // Used when creating / deleting dimensions
    public static void SendAllWorldAndBiomeConfigsToAllPlayers(MinecraftServer server)
    {
		ByteBuf nettyBuffer = createWorldAndBiomeConfigsPacket();
		if(nettyBuffer != null)
		{
	    	for(EntityPlayerMP player : server.getPlayerList().getPlayers())
	    	{
	        	PacketDispatcher.sendTo(new DimensionSyncPacket(nettyBuffer), (EntityPlayerMP) player);
	    	}
		}
    }
    
    private static ByteBuf createWorldAndBiomeConfigsPacket()
    {
        // Make sure worlds are sent in the correct order.
        
		OTGDimensionInfo otgDimData = OTGDimensionManager.GetOrderedDimensionData();
		
        // Serialize it
        ByteBuf nettyBuffer = Unpooled.buffer();
        //PacketBuffer mojangBuffer = new PacketBuffer(nettyBuffer);
        DataOutput stream = new ByteBufOutputStream(nettyBuffer);
        
        try
        {
        	stream.writeInt(PluginStandardValues.ProtocolVersion);
        	stream.writeInt(0); // 0 == Normal packet
        	stream.writeInt(otgDimData.orderedDimensions.size() + 1); // Number of worlds in this packet
        	   		
    		// Send worldconfig and biomeconfigs for each world.
        	
			LocalWorld localWorld = ((ForgeEngine)OTG.getEngine()).getOverWorld();

			if(localWorld == null)
			{
				// This is not an OTG world.
				return null;
			}
			
			// Overworld (dim 0)
	        try
	        {
	        	stream.writeInt(0);
	            ConfigToNetworkSender.writeConfigsToStream(localWorld.getConfigs(), stream, false);
	        }
	        catch (IOException e)
	        {
	            OTG.printStackTrace(LogMarker.FATAL, e);
	        }
        	
    		for(int i = 0; i <= otgDimData.highestOrder; i++)
    		{
    			if(otgDimData.orderedDimensions.containsKey(i))
    			{
    				DimensionData dimData = otgDimData.orderedDimensions.get(i);
    				localWorld = OTG.getWorld(dimData.dimensionName);
    				if(localWorld == null)
    				{
    					localWorld = OTG.getUnloadedWorld(dimData.dimensionName);
    				}
    				
    		        try
    		        {
    		        	stream.writeInt(dimData.dimensionId);
    		            ConfigToNetworkSender.writeConfigsToStream(localWorld.getConfigs(), stream, false); // TODO: localWorld is null after /otg dim -c
    		        }
    		        catch (IOException e)
    		        {
    		            OTG.printStackTrace(LogMarker.FATAL, e);
    		        }
    			}
    		}
		}
        catch (IOException e1)
        {
			e1.printStackTrace();
		}
        
        return nettyBuffer;
    }
}
