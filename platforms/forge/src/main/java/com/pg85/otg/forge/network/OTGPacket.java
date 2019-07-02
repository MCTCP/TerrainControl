package com.pg85.otg.forge.network;

import java.io.DataInputStream;
import com.pg85.otg.configuration.standard.PluginStandardValues;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public abstract class OTGPacket implements IMessage
{
	private ByteBuf data = Unpooled.buffer();
	private DataInputStream wrappedStream;

	public OTGPacket() { }

	public OTGPacket(ByteBuf data)
	{
		this.data = data;
	}
	
	public ByteBuf getData()
	{
		return data;
	}
	
	public DataInputStream getStream()
	{
		return wrappedStream;
	}	
	
	@Override
	public void fromBytes(ByteBuf data)
	{
        int serverProtocolVersion = data.readInt();
        int clientProtocolVersion = PluginStandardValues.ProtocolVersion;
        if (serverProtocolVersion == clientProtocolVersion)
        {
        	data.retain();
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
}
