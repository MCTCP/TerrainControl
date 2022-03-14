package com.pg85.otg.forge.network;

import net.minecraft.network.FriendlyByteBuf;

public class AcknowledgeOTGMessage implements OTGLoginMessage
{
	private int loginIndex;

	public AcknowledgeOTGMessage() { }

	public static void serialize(AcknowledgeOTGMessage msg, FriendlyByteBuf buf) { }

	public static AcknowledgeOTGMessage deserialize(FriendlyByteBuf buf)
	{
		return new AcknowledgeOTGMessage();
	}

	/*
	public static void handle(FMLHandshakeHandler __, AcknowledgeOTGMessage msg, Supplier<NetworkEvent.Context> ctx)
	{
		ctx.get().enqueueWork(() -> ctx.get().setPacketHandled(true));
		ctx.get().setPacketHandled(true);
	}
	*/

	@Override
	public int getLoginIndex()
	{
		return loginIndex;
	}

	@Override
	public void setLoginIndex(int loginIndex)
	{
		this.loginIndex = loginIndex;
	}
}
