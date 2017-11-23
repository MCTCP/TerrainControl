package com.pg85.otg.forge.network;

import java.io.DataInputStream;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;

import com.pg85.otg.OTG;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.forge.ForgeEngine;
import com.pg85.otg.logging.LogMarker;

public class DimensionSyncPacket implements IMessage
{
	ByteBuf data = Unpooled.buffer();
	DataInputStream wrappedStream;

	public DimensionSyncPacket() { }

	public DimensionSyncPacket(ByteBuf data)
	{
		this.data = data;
	}

	@Override
	public void fromBytes(ByteBuf data)
	{
        int serverProtocolVersion = data.readInt();
        int clientProtocolVersion = PluginStandardValues.ProtocolVersion;
        if (serverProtocolVersion == clientProtocolVersion)
        {
        	wrappedStream = new DataInputStream(new ByteBufInputStream(data));
        } else {
        	// Wrong version!
        	throw new RuntimeException("Client is using a different version of OTG than server!");
        }
	}

	@Override
	public void toBytes(ByteBuf data)
	{
		data.writeBytes(this.data);
	}

	public static class Handler extends AbstractClientMessageHandler<DimensionSyncPacket>
	{
		@Override
		public IMessage handleClientMessage(EntityPlayer player, DimensionSyncPacket message, MessageContext ctx)
		{
			try
			{
				int packetType = message.wrappedStream.readInt();
				if(packetType == 0)
				{
					((ForgeEngine)OTG.getEngine()).getWorldLoader().registerClientWorld(message.wrappedStream);
				} else {
					throw new RuntimeException();
				}
	        }
	        catch (Exception e)
	        {
	            OTG.log(LogMarker.FATAL, "Failed to receive packet");
	            OTG.printStackTrace(LogMarker.FATAL, e);
	        }
			return null;
		}
	}
}