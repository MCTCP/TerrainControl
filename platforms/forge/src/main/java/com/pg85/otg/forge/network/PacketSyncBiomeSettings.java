package com.pg85.otg.forge.network;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketSyncBiomeSettings implements OTGLoginMessage
{
	private Map<String, BiomeSettingSyncWrapper> syncMap = new HashMap<>();
	private int loginIndex;

	public PacketSyncBiomeSettings()
	{
		this(OTGClientSyncManager.getSyncedData());
	}

	public PacketSyncBiomeSettings(Map<String, BiomeSettingSyncWrapper> syncMap)
	{
		this.syncMap = syncMap;
	}

	public static void encode(PacketSyncBiomeSettings packet, PacketBuffer buffer)
	{
		for (Entry<String, BiomeSettingSyncWrapper> entry : packet.syncMap.entrySet())
		{
			buffer.writeUtf(entry.getKey());
			entry.getValue().encode(buffer);
		}
	}

	public static PacketSyncBiomeSettings decode(PacketBuffer buffer)
	{
		PacketSyncBiomeSettings packet = new PacketSyncBiomeSettings();
		while (buffer.writerIndex() < buffer.capacity())
		{
			String key = buffer.readUtf();
			BiomeSettingSyncWrapper wrapper = new BiomeSettingSyncWrapper(buffer);

			packet.syncMap.putIfAbsent(key, wrapper);
		}
		return packet;
	}

	public static void handleLogin(PacketSyncBiomeSettings packet, Supplier<NetworkEvent.Context> context)
	{
		context.get().enqueueWork(() -> OTGClientSyncManager.getSyncedData().putAll(packet.syncMap));
		
		// We need to send something as a reply, or the client will hang on login forever.
		OTGClientSyncManager.LOGIN.reply(new AcknowledgeOTGMessage(), context.get());
		context.get().setPacketHandled(true);
	}

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
