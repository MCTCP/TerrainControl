package com.pg85.otg.forge.network.client.packets;

import java.io.DataOutput;
import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import com.pg85.otg.OTG;
import com.pg85.otg.configuration.ConfigFile;
import com.pg85.otg.configuration.dimensions.DimensionConfig;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.forge.ForgeWorld;
import com.pg85.otg.forge.dimensions.OTGDimensionManager;
import com.pg85.otg.forge.network.AbstractServerMessageHandler;
import com.pg85.otg.forge.network.OTGPacket;
import com.pg85.otg.forge.network.server.ServerPacketManager;
import com.pg85.otg.logging.LogMarker;

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

	public static void WriteCreatePacketToStream(DimensionConfig dimensionConfig, DataOutput stream) throws IOException
	{
    	stream.writeInt(PluginStandardValues.ProtocolVersion);
    	stream.writeInt(0); // 0 == Create dimension packet
    	
    	ConfigFile.writeStringToStream(stream, dimensionConfig.ToYamlString());
	}
	
	public static void WriteDeletePacketToStream(String dimensionName, DataOutput stream) throws IOException
	{
    	stream.writeInt(PluginStandardValues.ProtocolVersion);
    	stream.writeInt(1); // 1 == Delete dimension packet
    	
    	ConfigFile.writeStringToStream(stream, dimensionName);
	}
	
	public static class Handler extends AbstractServerMessageHandler<CreateDeleteDimensionPacket>
	{
		@Override
		public IMessage handleServerMessage(EntityPlayer player, CreateDeleteDimensionPacket message, MessageContext ctx)
		{
			try
			{
				int packetType = message.getStream().readInt();
				if(packetType == 0) // Create dimension
				{
					String dimConfigYaml = ConfigFile.readStringFromStream(message.getStream());
					DimensionConfig dimConfig = DimensionConfig.FromYamlString(dimConfigYaml);       			
					
		            IThreadListener mainThread = (WorldServer) ctx.getServerHandler().player.world;
		            mainThread.addScheduledTask(new Runnable()
		            {
		                @Override
		                public void run()
	                	{
		                	// Check if the world doesn't already exist
		                	for(DimensionConfig existingDimConfig : OTG.GetDimensionsConfig().Dimensions)
		                	{
		                		if(existingDimConfig.PresetName.equals(dimConfig.PresetName))
		                		{
		                			return;
		                		}
		                	}
							OTG.GetDimensionsConfig().Dimensions.add(dimConfig);
							
							long seed = (long) Math.floor((Math.random() * Long.MAX_VALUE));
			                try
			                {
			                	seed = dimConfig.Seed == null || dimConfig.Seed.trim().length() == 0 ? (long) Math.floor((Math.random() * Long.MAX_VALUE)) : Long.parseLong(dimConfig.Seed);
			                }
			            	catch(NumberFormatException ex)
			                {
			            		OTG.log(LogMarker.ERROR, "Dimension config for world \"" + dimConfig.PresetName + "\" has value \"" + dimConfig.Seed + "\" for worldSeed which cannot be parsed as a number. Using a random seed instead.");
			                }
			                
			                OTG.isNewWorldBeingCreated = true;
							OTGDimensionManager.createDimension(seed, dimConfig.PresetName, false, true, true);
							OTG.isNewWorldBeingCreated = false;
							ForgeWorld createdWorld = (ForgeWorld) OTG.getWorld(dimConfig.PresetName);
							
							if(dimConfig.Settings.CanDropChunk)
							{
								DimensionManager.unloadWorld(createdWorld.getWorld().provider.getDimension());
							}
							
			    			ServerPacketManager.SendDimensionSynchPacketToAllPlayers(player.getServer());
		                }
		            });
		            return null;
				}
				else if(packetType == 1) // Delete dimension
				{
					String worldName = ConfigFile.readStringFromStream(message.getStream());
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