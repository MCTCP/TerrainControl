package com.pg85.otg.forge.network.client.packets;

import java.io.DataOutput;
import java.io.IOException;

import com.pg85.otg.OTG;
import com.pg85.otg.configuration.ConfigFile;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.forge.dimensions.OTGTeleporter;
import com.pg85.otg.forge.network.AbstractServerMessageHandler;
import com.pg85.otg.forge.network.OTGPacket;
import com.pg85.otg.logging.LogMarker;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class TeleportPlayerPacket extends OTGPacket
{
	public TeleportPlayerPacket()
	{
		super();
	}
	
	public TeleportPlayerPacket(ByteBuf nettyBuffer)
	{
		super(nettyBuffer);
	}
	
	public static void WriteToStream(String dimensionName, DataOutput stream) throws IOException
	{
    	stream.writeInt(PluginStandardValues.ProtocolVersion);
    	stream.writeInt(0); // 0 == Teleport player packet
    	
    	ConfigFile.writeStringToStream(stream, dimensionName);
	}
	
	public static class Handler extends AbstractServerMessageHandler<TeleportPlayerPacket>
	{
		@Override
		public IMessage handleServerMessage(EntityPlayer player, TeleportPlayerPacket message, MessageContext ctx)
		{			
			try
			{
				int packetType = message.getStream().readInt();
				if(packetType == 0) // Normal packet
				{
					// Teleport player to dimension
					
					String dimensionName = ConfigFile.readStringFromStream(message.getStream());

					IThreadListener mainThread = (WorldServer) ctx.getServerHandler().player.world;
		            mainThread.addScheduledTask(new Runnable()
		            {
		                @Override
		                public void run()
	                	{
							// Check dimension names
							for(int i = -1; i < Long.SIZE << 4; i++)
							{
								if(DimensionManager.isDimensionRegistered(i))
								{
									DimensionType dimensionType = DimensionManager.getProviderType(i);
									if(dimensionType.getName().toLowerCase().trim().equals(dimensionName.toLowerCase()))
									{
										OTGTeleporter.changeDimension(i, (EntityPlayerMP) player, false, true);								
										return;
									}
								}
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
