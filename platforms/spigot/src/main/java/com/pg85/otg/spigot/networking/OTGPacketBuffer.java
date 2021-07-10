package com.pg85.otg.spigot.networking;

import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.EncoderException;
import net.minecraft.server.v1_16_R3.PacketDataSerializer;

public class OTGPacketBuffer extends PacketDataSerializer
{

	public OTGPacketBuffer(ByteBuf bytebuf)
	{
		super(bytebuf);
	}

	public PacketDataSerializer writeUtf(String p_180714_1_)
	{
		return this.writeUtf(p_180714_1_, 32767);
	}

	public PacketDataSerializer writeUtf(String p_211400_1_, int p_211400_2_)
	{
		byte[] abyte = p_211400_1_.getBytes(StandardCharsets.UTF_8);
		if (abyte.length > p_211400_2_)
		{
			throw new EncoderException(
					"String too big (was " + abyte.length + " bytes encoded, max " + p_211400_2_ + ")");
		} else
		{
			this.writeVarInt(abyte.length);
			this.writeBytes(abyte);
			return this;
		}
	}

	public PacketDataSerializer writeVarInt(int p_150787_1_)
	{
		while ((p_150787_1_ & -128) != 0)
		{
			this.writeByte(p_150787_1_ & 127 | 128);
			p_150787_1_ >>>= 7;
		}

		this.writeByte(p_150787_1_);
		return this;
	}
}