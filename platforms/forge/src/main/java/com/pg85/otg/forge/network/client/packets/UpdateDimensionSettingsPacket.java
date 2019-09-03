package com.pg85.otg.forge.network.client.packets;

import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import com.pg85.otg.OTG;
import com.pg85.otg.configuration.dimensions.DimensionConfig;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.forge.ForgeEngine;
import com.pg85.otg.forge.ForgeWorld;
import com.pg85.otg.forge.network.AbstractServerMessageHandler;
import com.pg85.otg.forge.network.OTGPacket;
import com.pg85.otg.forge.network.server.ServerPacketManager;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.helpers.StreamHelper;

import io.netty.buffer.ByteBuf;

public class UpdateDimensionSettingsPacket extends OTGPacket
{
	public UpdateDimensionSettingsPacket()
	{
		super();
	}
	
	public UpdateDimensionSettingsPacket(ByteBuf nettyBuffer)
	{
		super(nettyBuffer);
	}
	
	public static void writeToStream(DataOutput stream, ArrayList<DimensionConfig> dimConfigs, boolean isOverWorldIncluded) throws IOException
	{
    	stream.writeInt(PluginStandardValues.ProtocolVersion);
    	stream.writeInt(0); // 0 == Normal packet
    	
    	stream.writeInt(dimConfigs.size());
		stream.writeBoolean(isOverWorldIncluded);
    	
    	for(DimensionConfig dimConfig : dimConfigs)
    	{
    		StreamHelper.writeStringToStream(stream, dimConfig.toYamlString());
    	}    	
	}
	
	public static class Handler extends AbstractServerMessageHandler<UpdateDimensionSettingsPacket>
	{
		@Override
		public IMessage handleServerMessage(EntityPlayer player, UpdateDimensionSettingsPacket message, MessageContext ctx)
		{			
			try
			{
				int packetType = message.getStream().readInt();
				if(packetType == 0) // Normal packet
				{
					// Update dimension settings
				
					int listSize = message.getStream().readInt();
            		boolean isOverWorld = message.getStream().readBoolean();
					ArrayList<DimensionConfig> dimConfigs = new ArrayList<DimensionConfig>();
					for(int i = 0; i < listSize; i++)
					{
						dimConfigs.add(DimensionConfig.fromYamlString(StreamHelper.readStringFromStream(message.getStream())));
					}					
				
					IThreadListener mainThread = (WorldServer) ctx.getServerHandler().player.world;
		            mainThread.addScheduledTask(new Runnable()
		            {
		                @Override
		                public void run()
	                	{
		                	for(DimensionConfig dimConfig : dimConfigs)
		                	{ 
			                	if(isOverWorld)
			                	{
	            					ForgeWorld forgeWorld = null;
	        						forgeWorld = (ForgeWorld)((ForgeEngine)OTG.getEngine()).getOverWorld();
	            					if(forgeWorld.getWorldSession().getPregenerationRadius() != dimConfig.PregeneratorRadiusInChunks)
	            					{
		            					forgeWorld.getWorldSession().setPregenerationRadius(dimConfig.PregeneratorRadiusInChunks);
		            					dimConfig.PregeneratorRadiusInChunks = forgeWorld.getWorldSession().getPregenerationRadius();
	            					}
			                		OTG.getDimensionsConfig().Overworld = dimConfig;
			                	} else {
			                		// TODO: Assuming atm that only a single thread is ever 
			                		// accessing dimensionsconfig, is that true? 
			                		DimensionConfig dimConfigToRemove = null;
			                		for(DimensionConfig dimConfig2 : OTG.getDimensionsConfig().Dimensions)
			                		{
			                			if(dimConfig.PresetName.equals(dimConfig2.PresetName))
			                			{
			                				dimConfigToRemove = dimConfig2;
			                			}
			                		}
			                		
	            					ForgeWorld forgeWorld = null;
	        						forgeWorld = (ForgeWorld)((ForgeEngine)OTG.getEngine()).getUnloadedWorld(dimConfig.PresetName);
	            					if(forgeWorld == null)
	            					{
	            						forgeWorld = (ForgeWorld)((ForgeEngine)OTG.getEngine()).getWorld(dimConfig.PresetName);	
	            					}
	            					// ForgeWorld might have been deleted, client may have sent outdated worlds list
	            					if(forgeWorld != null)
	            					{
		            					if(forgeWorld.getWorldSession().getPregenerationRadius() != dimConfig.PregeneratorRadiusInChunks)
		            					{
			            					forgeWorld.getWorldSession().setPregenerationRadius(dimConfig.PregeneratorRadiusInChunks);
			            					dimConfig.PregeneratorRadiusInChunks = forgeWorld.getWorldSession().getPregenerationRadius();
		            					}
	
				                		OTG.getDimensionsConfig().Dimensions.remove(dimConfigToRemove);
				                		OTG.getDimensionsConfig().Dimensions.add(dimConfig);
	            					}
			                	}
		                	}
	                		ServerPacketManager.sendDimensionSynchPacketToAllPlayers(player.getServer());
	                	}
		            });
		            return null;					
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