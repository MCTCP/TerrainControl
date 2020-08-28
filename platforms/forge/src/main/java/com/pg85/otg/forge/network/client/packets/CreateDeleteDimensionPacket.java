package com.pg85.otg.forge.network.client.packets;

import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.util.text.TextComponentString;
import org.apache.commons.lang3.StringUtils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import com.pg85.otg.OTG;
import com.pg85.otg.configuration.dimensions.DimensionConfig;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.forge.blocks.PortalColors;
import com.pg85.otg.forge.dimensions.OTGDimensionManager;
import com.pg85.otg.forge.network.AbstractServerMessageHandler;
import com.pg85.otg.forge.network.OTGPacket;
import com.pg85.otg.forge.network.server.ServerPacketManager;
import com.pg85.otg.forge.world.ForgeWorld;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.helpers.StreamHelper;

import io.netty.buffer.ByteBuf;

/**
 * Sent from the client to the server when players use the ingame UI to create/delete dimensions
 */
public class CreateDeleteDimensionPacket extends OTGPacket
{
	public CreateDeleteDimensionPacket()
	{
		super();
	}
	
	public CreateDeleteDimensionPacket(ByteBuf nettyBuffer)
	{
		super(nettyBuffer);
	}

	public static void writeCreatePacketToStream(DimensionConfig dimensionConfig, DataOutput stream) throws IOException
	{
    	stream.writeInt(PluginStandardValues.ProtocolVersion);
    	stream.writeInt(0); // 0 == Create dimension packet
    	
    	StreamHelper.writeStringToStream(stream, dimensionConfig.toYamlString());
	}
	
	public static void writeDeletePacketToStream(String dimensionName, DataOutput stream) throws IOException
	{
    	stream.writeInt(PluginStandardValues.ProtocolVersion);
    	stream.writeInt(1); // 1 == Delete dimension packet
    	
    	StreamHelper.writeStringToStream(stream, dimensionName);
	}
		
	public static class Handler extends AbstractServerMessageHandler<CreateDeleteDimensionPacket>
	{
		@Override
		public IMessage handleServerMessage(EntityPlayer player, CreateDeleteDimensionPacket message, MessageContext ctx)
		{
			try
			{
				if (!player.canUseCommand(2, "openterraingenerator.ui.create")) {
					player.sendMessage(new TextComponentString("Could not process: Missing permission '"+"openterraingenerator.ui.create"+"'"));
					return null;
				}
				int packetType = message.getStream().readInt();
				if(packetType == 0) // Create dimension
				{
					String dimConfigYaml = StreamHelper.readStringFromStream(message.getStream());
					DimensionConfig dimConfig = DimensionConfig.fromYamlString(dimConfigYaml);
					
		            IThreadListener mainThread = (WorldServer) ctx.getServerHandler().player.world;
		            mainThread.addScheduledTask(new Runnable()
		            {
		                @Override
		                public void run()
	                	{
		                	// Check if the world doesn't already exist
		                	if(OTG.getDimensionsConfig().Dimensions.size() + 1 > PortalColors.portalColors.size())
		                	{
		                		OTG.log(LogMarker.INFO, "Warning: Client tried to create a dimension, but all portal colors are in use.");
        						// Update the UI on the client
        						ServerPacketManager.sendDimensionSynchPacketToAllPlayers(player.getServer());
		                		return;
		                	}
	                		if(OTG.getDimensionsConfig().Overworld.PresetName != null && OTG.getDimensionsConfig().Overworld.PresetName.equals(dimConfig.PresetName))
	                		{
	                			// Preset is in use
	                			return;
	                		}
		                	for(DimensionConfig existingDimConfig : OTG.getDimensionsConfig().Dimensions)
		                	{
		                		if(existingDimConfig.PresetName.equals(dimConfig.PresetName))
		                		{
		                			// Preset is in use
		                			return;
		                		}
		                	}

	                		// Check if the portal color is available
		                	if(!PortalColors.isPortalColorFree(dimConfig.Settings.PortalColor, OTG.getDimensionsConfig().getAllDimensions()))
		                	{
		                		// Change the portal material
		                		dimConfig.Settings.PortalColor = PortalColors.getNextFreePortalColor(dimConfig.Settings.PortalColor, OTG.getDimensionsConfig().getAllDimensions(), false);
		                		OTG.log(LogMarker.INFO, "Warning: Client tried to create a dimension, but portal color is already in use, changed portal color.");
		                	}
		                	
		                	ArrayList<String> presetNames = new ArrayList<String>();
		                	presetNames.add(dimConfig.PresetName);
        					if(!OTG.getEngine().areEnoughBiomeIdsAvailableForPresets(presetNames))
        					{
        						// Update the UI on the client
        						ServerPacketManager.sendDimensionSynchPacketToAllPlayers(player.getServer());
        						OTG.log(LogMarker.INFO, "Warning: Client tried to create a dimension, but not enough biome id's are available.");
        						return;
        					}
		                								
            				long seed = (new Random()).nextLong();		            				
            	            String sSeed = dimConfig.Seed;
            	            if (sSeed != null && !StringUtils.isEmpty(sSeed))
            	            {
            	                try
            	                {
            	                    long j = Long.parseLong(sSeed);

            	                    if (j != 0L)
            	                    {
            	                    	seed = j;
            	                    }
            	                }
            	                catch (NumberFormatException var7)
            	                {
            	                	seed = (long)sSeed.hashCode();
            	                }
            	            }
			                
			                OTG.IsNewWorldBeingCreated = true;
							if(!OTGDimensionManager.createDimension(dimConfig, seed, true))
							{
        						OTG.IsNewWorldBeingCreated = false;
        						// Update the UI on the client
        						ServerPacketManager.sendDimensionSynchPacketToAllPlayers(player.getServer());
        						OTG.log(LogMarker.INFO, "Warning: Client tried to create a dimension, but the dimension id " + dimConfig.DimensionId + " + is not available.");
        						return;
							}
							OTG.IsNewWorldBeingCreated = false;
							
							ForgeWorld createdWorld = (ForgeWorld) OTG.getWorld(dimConfig.PresetName);
							
							if(dimConfig.Settings.CanDropChunk)
							{
								DimensionManager.unloadWorld(createdWorld.getWorld().provider.getDimension());
							}
							
			    			ServerPacketManager.sendDimensionSynchPacketToAllPlayers(player.getServer());
		                }
		            });
		            return null;
				}
				else if(packetType == 1) // Delete dimension
				{
					String worldName = StreamHelper.readStringFromStream(message.getStream());
					IThreadListener mainThread = (WorldServer) ctx.getServerHandler().player.world;
		            mainThread.addScheduledTask(new Runnable()
		            {
		                @Override
		                public void run()
	                	{
		                	OTGDimensionManager.DeleteDimensionServer(worldName, player.getServer());
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