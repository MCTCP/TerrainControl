package com.pg85.otg.forge.network.server.packets;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.dimensions.DimensionConfigGui;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.forge.ForgeEngine;
import com.pg85.otg.forge.dimensions.DimensionData;
import com.pg85.otg.forge.dimensions.OTGDimensionInfo;
import com.pg85.otg.forge.dimensions.OTGDimensionManager;
import com.pg85.otg.forge.network.OTGPacket;
import com.pg85.otg.forge.network.client.AbstractClientMessageHandler;
import com.pg85.otg.forge.network.client.ClientPacketManager;
import com.pg85.otg.forge.world.ForgeWorld;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.network.ClientConfigProvider;
import com.pg85.otg.network.ConfigToNetworkSender;
import com.pg85.otg.util.helpers.StreamHelper;

import io.netty.buffer.ByteBuf;

public class DimensionSyncPacket extends OTGPacket
{
	public DimensionSyncPacket()
	{
		super();
	}
	
	public DimensionSyncPacket(ByteBuf nettyBuffer)
	{
		super(nettyBuffer);
	}

	public static void writeToStream(DataOutput stream) throws IOException
	{
	    // Make sure worlds are sent in the correct order.
		OTGDimensionInfo otgDimData = OTGDimensionManager.LoadOrderedDimensionData();
	    
		stream.writeInt(PluginStandardValues.ProtocolVersion);
		stream.writeInt(0); // 0 == Normal packet
		
	    // Send ForgeWorldConfig
		StreamHelper.writeStringToStream(stream, OTG.getDimensionsConfig().toYamlString());
		
		LocalWorld localWorld = ((ForgeEngine)OTG.getEngine()).getOverWorld();
		
		// Send presets for client GUI
		stream.writeInt(ForgeEngine.Presets.size());
		for(DimensionConfigGui dimConfig : ForgeEngine.Presets.values())
		{
			StreamHelper.writeStringToStream(stream, dimConfig.toYamlString());
		}
		
		stream.writeInt(otgDimData.orderedDimensions.size());
		
		for(int i = 0; i <= otgDimData.highestOrder; i++)
		{
			if(otgDimData.orderedDimensions.containsKey(i))
			{
				DimensionData dimData = otgDimData.orderedDimensions.get(i);
				localWorld = OTG.getWorld(dimData.dimensionName);
				if(localWorld == null)
				{
					localWorld = OTG.getUnloadedWorld(dimData.dimensionName);
					stream.writeBoolean(false); // World is unloaded, used for GUI on the client
				} else {
					stream.writeBoolean(true); // World is loaded, used for GUI on the client
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
	
	public static ForgeWorld registerClientWorldBukkit(WorldClient mcWorld, DataInputStream wrappedStream, HashMap<String, ForgeWorld> worlds, HashMap<String, ForgeWorld> unloadedWorlds) throws IOException
	{
		// TODO: Test this and fix this if necessary.
		//((ForgeEngine)OTG.getEngine()).UnloadAndUnregisterAllWorlds(); // TODO: Is this necessary for Bukkit?
	    ForgeWorld world = new ForgeWorld(StreamHelper.readStringFromStream(wrappedStream));
	    ClientConfigProvider configs = new ClientConfigProvider(wrappedStream, world, Minecraft.getMinecraft().isSingleplayer());
	    world.provideClientConfigsBukkit(mcWorld, configs);
	    return world;
	}

	public static class Handler extends AbstractClientMessageHandler<DimensionSyncPacket>
	{
		@Override
		public IMessage handleClientMessage(EntityPlayer player, DimensionSyncPacket message, MessageContext ctx)
		{
			// For SP clients data is shared between client and server
			if(Minecraft.getMinecraft().isSingleplayer())
			{
				return null;
			}
			
			try
			{
				int packetType = message.getStream().readInt();
				if(packetType == 0)
				{
					ClientPacketManager.registerClientWorlds(message.getStream(), ((ForgeEngine)OTG.getEngine()).getWorldLoader());
				} else {
					throw new RuntimeException();
				}
	        }
	        catch (Exception e)
	        {
	            OTG.log(LogMarker.FATAL, "Failed to receive packet");
	            OTG.printStackTrace(LogMarker.FATAL, e);
	        } finally {
	        	// Finally is executed even if we return inside try
				message.getData().release();
			}
			return null;
		}
	}
}