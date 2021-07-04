package com.pg85.otg.forge.network;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import org.apache.commons.lang3.SerializationUtils;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketSyncSettings implements OTGLoginMessage
{
	private Map<String, BiomeSettingWrapper> syncMap = new HashMap<>();
	private int loginIndex;

	public PacketSyncSettings()
	{
		this(OTGPacketHandler.getSyncedmap());
	}

	public PacketSyncSettings(Map<String, BiomeSettingWrapper> syncMap)
	{
		this.syncMap = syncMap;
	}

	public static void encode(PacketSyncSettings packet, PacketBuffer buffer)
	{
		for (Entry<String, BiomeSettingWrapper> entry : packet.syncMap.entrySet())
		{
			buffer.writeUtf(entry.getKey());
			entry.getValue().encode(buffer);
		}
	}

	public static PacketSyncSettings decode(PacketBuffer buffer)
	{
		PacketSyncSettings packet = new PacketSyncSettings();
		while (buffer.writerIndex() < buffer.capacity())
		{
			String key = buffer.readUtf();
			BiomeSettingWrapper wrapper = new BiomeSettingWrapper(buffer);

			packet.syncMap.putIfAbsent(key, wrapper);
		}
		return packet;
	}

	public static void handleLogin(PacketSyncSettings packet, Supplier<NetworkEvent.Context> context)
	{
		context.get().enqueueWork(() -> OTGPacketHandler.getSyncedmap().putAll(packet.syncMap));
		OTGPacketHandler.LOGIN.reply(new AcknowledgeOTGMessage(), context.get());
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
