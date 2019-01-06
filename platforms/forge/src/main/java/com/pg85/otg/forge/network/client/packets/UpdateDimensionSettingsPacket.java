package com.pg85.otg.forge.network.client.packets;

import java.io.DataOutput;
import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import com.pg85.otg.OTG;
import com.pg85.otg.configuration.ConfigFile;
import com.pg85.otg.configuration.dimensions.DimensionConfig;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.forge.dimensions.OTGTeleporter;
import com.pg85.otg.forge.network.AbstractServerMessageHandler;
import com.pg85.otg.forge.network.OTGPacket;
import com.pg85.otg.forge.network.server.ServerPacketHandler;
import com.pg85.otg.logging.LogMarker;

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
	
	public static void WriteToStream(DataOutput stream, DimensionConfig dimConfig, boolean isOverWorld) throws IOException
	{
    	stream.writeInt(PluginStandardValues.ProtocolVersion);
    	stream.writeInt(0); // 0 == Normal packet
    	stream.writeBoolean(isOverWorld);
    	
    	ConfigFile.writeStringToStream(stream, dimConfig.ToYamlString());
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
				
					boolean isOverWorld = message.getStream().readBoolean();
					
					String dimensionConfigYaml = ConfigFile.readStringFromStream(message.getStream());
					DimensionConfig dimConfig = DimensionConfig.FromYamlString(dimensionConfigYaml);
					
					IThreadListener mainThread = (WorldServer) ctx.getServerHandler().player.world;
		            mainThread.addScheduledTask(new Runnable()
		            {
		                @Override
		                public void run()
	                	{
		                	if(isOverWorld)
		                	{
		                		OTG.GetDimensionsConfig().Overworld = dimConfig;
		                	} else {
		                		// TODO: Assuming atm that only a single thread is ever 
		                		// accessing dimensionsconfig, is that true? 
		                		DimensionConfig dimConfigToRemove = null;
		                		for(DimensionConfig dimConfig2 : OTG.GetDimensionsConfig().Dimensions)
		                		{
		                			if(dimConfig.PresetName.equals(dimConfig2.PresetName))
		                			{
		                				dimConfigToRemove = dimConfig2;
		                			}
		                		}
		                		OTG.GetDimensionsConfig().Dimensions.remove(dimConfigToRemove);
		                		OTG.GetDimensionsConfig().Dimensions.add(dimConfig);
		                		ServerPacketHandler.SendDimensionSynchPacketToAllPlayers(player.getServer());
		                	}
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